<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <div class="table-page-search-wrapper">
          <a-form layout="inline" @keyup.enter.native="loadData(1)">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="关键字" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input v-model="queryParam.keyword" placeholder="任务编号、原料名称"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="生产任务" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-select v-model="queryParam.orderId" allow-clear showSearch optionFilterProp="children" placeholder="请选择生产任务">
                    <a-select-option v-for="order in orderList" :key="order.id" :value="order.id">
                      {{ order.orderNo }} {{ order.materialName }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
                <a-col :md="6" :sm="24">
                  <a-button type="primary" @click="loadData(1)">查询</a-button>
                  <a-button style="margin-left: 8px" @click="searchReset">重置</a-button>
                </a-col>
              </span>
            </a-row>
          </a-form>
        </div>
        <div class="table-operator" style="margin-top: 5px">
          <a-button type="primary" icon="plus" @click="handleAdd">新增用料登记</a-button>
        </div>
        <a-table
          size="middle"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="ipagination"
          :loading="loading"
          :scroll="{x: 1100}"
          @change="handleTableChange">
          <span slot="action" slot-scope="text, record">
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
              <a>删除</a>
            </a-popconfirm>
          </span>
        </a-table>
        <a-modal
          :title="modalTitle"
          :width="760"
          :visible="modalVisible"
          :confirmLoading="saving"
          :maskClosable="false"
          @ok="handleSave"
          @cancel="handleCancel">
          <a-form>
            <a-form-item label="生产任务">
              <a-select v-model="model.orderId" showSearch optionFilterProp="children" placeholder="请选择生产任务" @change="handleOrderChange">
                <a-select-option v-for="order in orderList" :key="order.id" :value="order.id">
                  {{ order.orderNo }} {{ order.materialName }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="用料明细">
              <a-select v-model="model.orderItemId" showSearch optionFilterProp="children" placeholder="请选择用料明细" @change="handleOrderItemChange">
                <a-select-option v-for="item in orderItems" :key="item.id" :value="item.id">
                  {{ item.materialName }} / 计划 {{ item.plannedQuantity || 0 }} / 已领 {{ item.issuedQuantity || 0 }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="原料名称">
                  <a-input v-model="model.materialName" disabled></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="单位">
                  <a-input v-model="model.materialUnit" disabled></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="登记用量">
                  <a-input-number v-model="model.quantity" :min="0.000001" :precision="6" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="备注">
              <a-input v-model="model.remark"></a-input>
            </a-form-item>
          </a-form>
        </a-modal>
      </a-card>
    </a-col>
  </a-row>
</template>

<script>
  import { getAction, postAction, deleteAction } from '@/api/manage'

  export default {
    name: 'ProductionMaterialRecordList',
    data() {
      return {
        cardStyle: '',
        labelCol: { span: 5 },
        wrapperCol: { span: 18, offset: 1 },
        queryParam: { keyword: '', orderId: undefined },
        dataSource: [],
        orderList: [],
        orderItems: [],
        orderItemMap: {},
        loading: false,
        saving: false,
        modalVisible: false,
        modalTitle: '新增用料登记',
        model: {},
        ipagination: {
          current: 1,
          pageSize: 10,
          pageSizeOptions: ['10', '20', '30'],
          showTotal: (total) => `共 ${total} 条`,
          showQuickJumper: true,
          showSizeChanger: true,
          total: 0
        },
        columns: [
          { title: '操作', dataIndex: 'action', align: 'center', width: 120, scopedSlots: { customRender: 'action' } },
          { title: '任务编号', dataIndex: 'orderNo', width: 160 },
          { title: '原料名称', dataIndex: 'materialName', width: 160 },
          { title: '单位', dataIndex: 'materialUnit', width: 90 },
          { title: '登记用量', dataIndex: 'quantity', width: 120 },
          { title: '登记时间', dataIndex: 'recordTime', width: 180 },
          { title: '备注', dataIndex: 'remark', width: 220 }
        ]
      }
    },
    created() {
      this.loadOrderList()
      this.loadData(1)
    },
    methods: {
      loadData(arg) {
        if (arg === 1) {
          this.ipagination.current = 1
        }
        this.loading = true
        getAction('/production/materialRecord/list', {
          search: JSON.stringify(this.queryParam),
          currentPage: this.ipagination.current,
          pageSize: this.ipagination.pageSize
        }).then((res) => {
          if (res.code === 200) {
            this.dataSource = res.data.rows || []
            this.ipagination.total = res.data.total || 0
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        }).finally(() => {
          this.loading = false
        })
      },
      loadOrderList() {
        getAction('/production/order/list', {
          search: JSON.stringify({}),
          currentPage: 1,
          pageSize: 200
        }).then((res) => {
          if (res.code === 200) {
            this.orderList = res.data.rows || []
          }
        })
      },
      loadOrderItems(orderId, selectedItemId) {
        this.orderItems = []
        this.orderItemMap = {}
        if (!orderId) {
          return
        }
        getAction('/production/order/info', { id: orderId }).then((res) => {
          if (res.code === 200) {
            const detail = res.data.info || {}
            this.orderItems = detail.items || []
            this.orderItems.forEach(item => {
              this.orderItemMap[item.id] = item
            })
            if (selectedItemId) {
              this.model.orderItemId = selectedItemId
              this.handleOrderItemChange(selectedItemId, true)
            }
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      searchReset() {
        this.queryParam = { keyword: '', orderId: undefined }
        this.loadData(1)
      },
      handleTableChange(pagination) {
        this.ipagination = Object.assign(this.ipagination, pagination)
        this.loadData()
      },
      handleAdd() {
        this.modalTitle = '新增用料登记'
        this.model = { quantity: 1 }
        this.orderItems = []
        this.orderItemMap = {}
        this.modalVisible = true
      },
      handleEdit(record) {
        this.modalTitle = '编辑用料登记'
        this.model = Object.assign({}, record)
        this.modalVisible = true
        this.loadOrderItems(record.orderId, record.orderItemId)
      },
      handleDelete(id) {
        deleteAction('/production/materialRecord/delete', { id }).then((res) => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      handleOrderChange(orderId) {
        this.model.orderItemId = undefined
        this.model.materialName = ''
        this.model.materialUnit = ''
        this.loadOrderItems(orderId)
      },
      handleOrderItemChange(orderItemId, keepQuantity) {
        const item = this.orderItemMap[orderItemId]
        if (!item) {
          return
        }
        this.model.materialName = item.materialName
        this.model.materialUnit = item.materialUnit
        if (!keepQuantity) {
          const planned = Number(item.plannedQuantity || 0)
          const issued = Number(item.issuedQuantity || 0)
          const remain = planned - issued
          this.model.quantity = remain > 0 ? remain : 1
        }
      },
      handleSave() {
        if (!this.model.orderItemId) {
          this.$message.warning('请选择用料明细')
          return
        }
        if (!this.model.quantity || this.model.quantity <= 0) {
          this.$message.warning('请输入登记用量')
          return
        }
        this.saving = true
        postAction('/production/materialRecord/save', this.model).then((res) => {
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.modalVisible = false
            this.loadData()
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        }).finally(() => {
          this.saving = false
        })
      },
      handleCancel() {
        this.modalVisible = false
      }
    }
  }
</script>
