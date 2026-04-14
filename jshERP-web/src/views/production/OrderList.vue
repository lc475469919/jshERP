<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <div class="table-page-search-wrapper">
          <a-form layout="inline" @keyup.enter.native="loadData(1)">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="关键字" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input v-model="queryParam.keyword" placeholder="任务编号、成品名称"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="6" :sm="24">
                <a-form-item label="状态" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-select v-model="queryParam.status" allow-clear placeholder="请选择状态">
                    <a-select-option value="草稿">草稿</a-select-option>
                    <a-select-option value="已下达">已下达</a-select-option>
                    <a-select-option value="生产中">生产中</a-select-option>
                    <a-select-option value="已完工">已完工</a-select-option>
                    <a-select-option value="已关闭">已关闭</a-select-option>
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
          <a-button type="primary" icon="plus" @click="handleAdd">新增生产任务</a-button>
        </div>
        <a-table
          size="middle"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="ipagination"
          :loading="loading"
          :scroll="{x: 1300}"
          @change="handleTableChange">
          <span slot="action" slot-scope="text, record">
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical" />
            <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
              <a>删除</a>
            </a-popconfirm>
          </span>
          <template slot="statusRender" slot-scope="status">
            <a-tag :color="statusColor(status)">{{ status || '草稿' }}</a-tag>
          </template>
        </a-table>
        <a-modal
          :title="modalTitle"
          :width="1060"
          :visible="modalVisible"
          :confirmLoading="saving"
          :maskClosable="false"
          @ok="handleSave"
          @cancel="handleCancel">
          <a-form>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="任务编号">
                  <a-input v-model="model.orderNo" placeholder="留空自动生成"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="选择BOM">
                  <a-select v-model="model.bomId" allow-clear showSearch optionFilterProp="children" placeholder="请选择BOM" @change="handleBomChange">
                    <a-select-option v-for="bom in bomList" :key="bom.id" :value="bom.id">
                      {{ bom.bomNo }} {{ bom.name }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="状态">
                  <a-select v-model="model.status">
                    <a-select-option value="草稿">草稿</a-select-option>
                    <a-select-option value="已下达">已下达</a-select-option>
                    <a-select-option value="生产中">生产中</a-select-option>
                    <a-select-option value="已完工">已完工</a-select-option>
                    <a-select-option value="已关闭">已关闭</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="成品商品ID">
                  <a-input-number v-model="model.materialId" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="成品扩展ID">
                  <a-input-number v-model="model.materialExtendId" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="计划生产数量">
                  <a-input-number v-model="model.planQuantity" :min="0.000001" :precision="6" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="成品名称">
                  <a-input v-model="model.materialName"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="成品单位">
                  <a-input v-model="model.materialUnit"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="已完工数量">
                  <a-input-number v-model="model.finishedQuantity" :min="0" :precision="6" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="备注">
              <a-input v-model="model.remark"></a-input>
            </a-form-item>
          </a-form>
          <div class="production-detail-head">
            <span>用料明细</span>
            <span>
              <a-button size="small" @click="calculateMaterials">按BOM计算用料</a-button>
              <a-button icon="plus" size="small" style="margin-left:8px" @click="addItem">新增用料</a-button>
            </span>
          </div>
          <a-table
            size="small"
            bordered
            rowKey="rowKey"
            :columns="itemColumns"
            :dataSource="items"
            :pagination="false"
            :scroll="{x: 1000}">
            <template slot="materialId" slot-scope="text, record">
              <a-input-number v-model="record.materialId" style="width:100%"></a-input-number>
            </template>
            <template slot="materialExtendId" slot-scope="text, record">
              <a-input-number v-model="record.materialExtendId" style="width:100%"></a-input-number>
            </template>
            <template slot="materialName" slot-scope="text, record">
              <a-input v-model="record.materialName"></a-input>
            </template>
            <template slot="materialUnit" slot-scope="text, record">
              <a-input v-model="record.materialUnit"></a-input>
            </template>
            <template slot="plannedQuantity" slot-scope="text, record">
              <a-input-number v-model="record.plannedQuantity" :min="0" :precision="6" style="width:100%"></a-input-number>
            </template>
            <template slot="issuedQuantity" slot-scope="text, record">
              <a-input-number v-model="record.issuedQuantity" :min="0" :precision="6" style="width:100%"></a-input-number>
            </template>
            <template slot="remark" slot-scope="text, record">
              <a-input v-model="record.remark"></a-input>
            </template>
            <template slot="itemAction" slot-scope="text, record, index">
              <a @click="removeItem(index)">删除</a>
            </template>
          </a-table>
        </a-modal>
      </a-card>
    </a-col>
  </a-row>
</template>

<script>
  import { getAction, postAction, deleteAction } from '@/api/manage'

  export default {
    name: 'ProductionOrderList',
    data() {
      return {
        cardStyle: '',
        labelCol: { span: 5 },
        wrapperCol: { span: 18, offset: 1 },
        queryParam: { keyword: '', status: undefined },
        dataSource: [],
        bomList: [],
        bomMap: {},
        loading: false,
        saving: false,
        modalVisible: false,
        modalTitle: '新增生产任务',
        model: {},
        items: [],
        rowSeed: 1,
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
          { title: '成品单位', dataIndex: 'materialUnit', width: 90 },
          { title: '计划数量', dataIndex: 'planQuantity', width: 100 },
          { title: '完工数量', dataIndex: 'finishedQuantity', width: 100 },
          { title: '状态', dataIndex: 'status', width: 90, scopedSlots: { customRender: 'statusRender' } },
          { title: '计划开工', dataIndex: 'planStartDate', width: 160 },
          { title: '计划完工', dataIndex: 'planFinishDate', width: 160 },
          { title: '备注', dataIndex: 'remark', width: 200 }
        ],
        itemColumns: [
          { title: '原料ID', dataIndex: 'materialId', width: 100, scopedSlots: { customRender: 'materialId' } },
          { title: '扩展ID', dataIndex: 'materialExtendId', width: 100, scopedSlots: { customRender: 'materialExtendId' } },
          { title: '原料名称', dataIndex: 'materialName', width: 150, scopedSlots: { customRender: 'materialName' } },
          { title: '单位', dataIndex: 'materialUnit', width: 90, scopedSlots: { customRender: 'materialUnit' } },
          { title: '计划用量', dataIndex: 'plannedQuantity', width: 120, scopedSlots: { customRender: 'plannedQuantity' } },
          { title: '已领料', dataIndex: 'issuedQuantity', width: 120, scopedSlots: { customRender: 'issuedQuantity' } },
          { title: '备注', dataIndex: 'remark', width: 170, scopedSlots: { customRender: 'remark' } },
          { title: '操作', dataIndex: 'itemAction', width: 70, scopedSlots: { customRender: 'itemAction' } }
        ]
      }
    },
    created() {
      this.loadBomList()
      this.loadData(1)
    },
    methods: {
      loadData(arg) {
        if (arg === 1) {
          this.ipagination.current = 1
        }
        this.loading = true
        getAction('/production/order/list', {
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
      loadBomList() {
        getAction('/production/bom/enabledList').then((res) => {
          if (res.code === 200) {
            this.bomList = res.data || []
            this.bomMap = {}
            this.bomList.forEach(item => {
              this.bomMap[item.id] = item
            })
          }
        })
      },
      searchReset() {
        this.queryParam = { keyword: '', status: undefined }
        this.loadData(1)
      },
      handleTableChange(pagination) {
        this.ipagination = Object.assign(this.ipagination, pagination)
        this.loadData()
      },
      handleAdd() {
        this.modalTitle = '新增生产任务'
        this.model = { status: '草稿', planQuantity: 1, finishedQuantity: 0 }
        this.items = []
        this.modalVisible = true
        this.loadBomList()
      },
      handleEdit(record) {
        this.modalTitle = '编辑生产任务'
        getAction('/production/order/info', { id: record.id }).then((res) => {
          if (res.code === 200) {
            const detail = res.data.info || {}
            this.model = Object.assign({ status: '草稿', planQuantity: 1, finishedQuantity: 0 }, detail.info || {})
            this.items = (detail.items || []).map(item => Object.assign({ rowKey: this.nextRowKey() }, item))
            this.modalVisible = true
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      handleDelete(id) {
        deleteAction('/production/order/delete', { id }).then((res) => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      handleBomChange(bomId) {
        const bom = this.bomMap[bomId]
        if (!bom) {
          return
        }
        this.model.materialId = bom.materialId
        this.model.materialExtendId = bom.materialExtendId
        this.model.materialName = bom.materialName
        this.model.materialUnit = bom.materialUnit
      },
      calculateMaterials() {
        if (!this.model.bomId) {
          this.$message.warning('请先选择BOM')
          return
        }
        getAction('/production/order/calculateMaterials', {
          bomId: this.model.bomId,
          planQuantity: this.model.planQuantity || 1
        }).then((res) => {
          if (res.code === 200) {
            this.items = (res.data || []).map(item => Object.assign({ rowKey: this.nextRowKey() }, item))
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      addItem() {
        this.items.push({ rowKey: this.nextRowKey(), plannedQuantity: 0, issuedQuantity: 0 })
      },
      removeItem(index) {
        this.items.splice(index, 1)
      },
      handleSave() {
        if (!this.model.materialName) {
          this.$message.warning('请输入成品名称')
          return
        }
        this.saving = true
        const payload = Object.assign({}, this.model, {
          items: this.items.map((item, index) => Object.assign({}, item, { sort: index }))
        })
        postAction('/production/order/save', payload).then((res) => {
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
      },
      statusColor(status) {
        if (status === '已完工') {
          return 'green'
        }
        if (status === '生产中') {
          return 'blue'
        }
        if (status === '已关闭') {
          return 'red'
        }
        if (status === '已下达') {
          return 'orange'
        }
        return 'default'
      },
      nextRowKey() {
        return `row_${this.rowSeed++}`
      }
    }
  }
</script>

<style scoped>
  @import '~@assets/less/common.less';

  .production-detail-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 8px 0;
    font-weight: 600;
  }
</style>
