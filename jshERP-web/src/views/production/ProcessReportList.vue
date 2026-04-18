<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <div class="table-page-search-wrapper">
          <a-form layout="inline" @keyup.enter.native="loadData(1)">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="关键字" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input v-model="queryParam.keyword" placeholder="任务、工序、汇报人"></a-input>
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
              <a-col :md="6" :sm="24">
                <a-form-item label="工序" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-select v-model="queryParam.processId" allow-clear showSearch optionFilterProp="children" placeholder="请选择工序">
                    <a-select-option v-for="process in processList" :key="process.id" :value="process.id">
                      {{ process.name }}
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
          <a-button type="primary" icon="plus" @click="handleAdd">新增工序汇报</a-button>
        </div>
        <a-table
          size="middle"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="ipagination"
          :loading="loading"
          :scroll="{x: 1200}"
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
              <a-select v-model="model.orderId" showSearch optionFilterProp="children" placeholder="请选择生产任务">
                <a-select-option v-for="order in orderList" :key="order.id" :value="order.id">
                  {{ order.orderNo }} {{ order.materialName }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-row :gutter="16">
              <a-col :md="12" :sm="24">
                <a-form-item label="工序">
                  <a-select v-model="model.processId" showSearch optionFilterProp="children" placeholder="请选择工序">
                    <a-select-option v-for="process in processList" :key="process.id" :value="process.id">
                      {{ process.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="12" :sm="24">
                <a-form-item label="汇报人">
                  <a-input v-model="model.workerName" placeholder="请输入汇报人"></a-input>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="合格数">
                  <a-input-number v-model="model.goodQuantity" :min="0" :precision="6" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="不良数">
                  <a-input-number v-model="model.defectQuantity" :min="0" :precision="6" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="报废数">
                  <a-input-number v-model="model.scrapQuantity" :min="0" :precision="6" style="width:100%"></a-input-number>
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
    name: 'ProductionProcessReportList',
    data() {
      return {
        cardStyle: '',
        labelCol: { span: 5 },
        wrapperCol: { span: 18, offset: 1 },
        queryParam: { keyword: '', orderId: undefined, processId: undefined },
        dataSource: [],
        orderList: [],
        processList: [],
        loading: false,
        saving: false,
        modalVisible: false,
        modalTitle: '新增工序汇报',
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
          { title: '成品名称', dataIndex: 'materialName', width: 160 },
          { title: '工序', dataIndex: 'processName', width: 140 },
          { title: '汇报人', dataIndex: 'workerName', width: 120 },
          { title: '合格数', dataIndex: 'goodQuantity', width: 110 },
          { title: '不良数', dataIndex: 'defectQuantity', width: 110 },
          { title: '报废数', dataIndex: 'scrapQuantity', width: 110 },
          { title: '汇报时间', dataIndex: 'reportTime', width: 180 },
          { title: '备注', dataIndex: 'remark', width: 220 }
        ]
      }
    },
    created() {
      this.loadOrderList()
      this.loadProcessList()
      this.loadData(1)
    },
    methods: {
      loadData(arg) {
        if (arg === 1) {
          this.ipagination.current = 1
        }
        this.loading = true
        getAction('/production/processReport/list', {
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
      loadProcessList() {
        getAction('/production/process/enabledList').then((res) => {
          if (res.code === 200) {
            this.processList = res.data || []
          }
        })
      },
      searchReset() {
        this.queryParam = { keyword: '', orderId: undefined, processId: undefined }
        this.loadData(1)
      },
      handleTableChange(pagination) {
        this.ipagination = Object.assign(this.ipagination, pagination)
        this.loadData()
      },
      handleAdd() {
        this.modalTitle = '新增工序汇报'
        this.model = { goodQuantity: 0, defectQuantity: 0, scrapQuantity: 0 }
        this.modalVisible = true
      },
      handleEdit(record) {
        this.modalTitle = '编辑工序汇报'
        this.model = Object.assign({ goodQuantity: 0, defectQuantity: 0, scrapQuantity: 0 }, record)
        this.modalVisible = true
      },
      handleDelete(id) {
        deleteAction('/production/processReport/delete', { id }).then((res) => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      handleSave() {
        if (!this.model.orderId) {
          this.$message.warning('请选择生产任务')
          return
        }
        if (!this.model.processId) {
          this.$message.warning('请选择工序')
          return
        }
        this.saving = true
        postAction('/production/processReport/save', this.model).then((res) => {
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
