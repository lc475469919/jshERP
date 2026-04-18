<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <div class="table-page-search-wrapper">
          <a-form layout="inline" @keyup.enter.native="loadData(1)">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="关键字" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input v-model="queryParam.keyword" placeholder="任务、成品、检验员、不良项"></a-input>
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
          <a-button type="primary" icon="plus" @click="handleAdd">新增生产质检</a-button>
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
                <a-form-item label="检验员">
                  <a-input v-model="model.inspectorName" placeholder="请输入检验员"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="12" :sm="24">
                <a-form-item label="不良品项">
                  <a-input v-model="model.defectItem" placeholder="如：外观、尺寸、功能"></a-input>
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
    name: 'ProductionQualityInspectionList',
    data() {
      return {
        cardStyle: '',
        labelCol: { span: 5 },
        wrapperCol: { span: 18, offset: 1 },
        queryParam: { keyword: '', orderId: undefined },
        dataSource: [],
        orderList: [],
        loading: false,
        saving: false,
        modalVisible: false,
        modalTitle: '新增生产质检',
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
          { title: '检验员', dataIndex: 'inspectorName', width: 120 },
          { title: '合格数', dataIndex: 'goodQuantity', width: 110 },
          { title: '不良数', dataIndex: 'defectQuantity', width: 110 },
          { title: '报废数', dataIndex: 'scrapQuantity', width: 110 },
          { title: '不良品项', dataIndex: 'defectItem', width: 160 },
          { title: '检验时间', dataIndex: 'inspectTime', width: 180 },
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
        getAction('/production/qualityInspection/list', {
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
      searchReset() {
        this.queryParam = { keyword: '', orderId: undefined }
        this.loadData(1)
      },
      handleTableChange(pagination) {
        this.ipagination = Object.assign(this.ipagination, pagination)
        this.loadData()
      },
      handleAdd() {
        this.modalTitle = '新增生产质检'
        this.model = { goodQuantity: 0, defectQuantity: 0, scrapQuantity: 0 }
        this.modalVisible = true
      },
      handleEdit(record) {
        this.modalTitle = '编辑生产质检'
        this.model = Object.assign({ goodQuantity: 0, defectQuantity: 0, scrapQuantity: 0 }, record)
        this.modalVisible = true
      },
      handleDelete(id) {
        deleteAction('/production/qualityInspection/delete', { id }).then((res) => {
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
        this.saving = true
        postAction('/production/qualityInspection/save', this.model).then((res) => {
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
