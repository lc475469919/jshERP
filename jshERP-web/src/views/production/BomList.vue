<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <div class="table-page-search-wrapper">
          <a-form layout="inline" @keyup.enter.native="loadData(1)">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="关键字" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input v-model="queryParam.keyword" placeholder="BOM编号、名称、成品名称"></a-input>
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
          <a-button type="primary" icon="plus" @click="handleAdd">新增</a-button>
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
          <template slot="enabledRender" slot-scope="enabled">
            <a-tag v-if="enabled" color="green">启用</a-tag>
            <a-tag v-else color="orange">禁用</a-tag>
          </template>
        </a-table>
        <a-modal
          :title="modalTitle"
          :width="980"
          :visible="modalVisible"
          :confirmLoading="saving"
          :maskClosable="false"
          @ok="handleSave"
          @cancel="handleCancel">
          <a-form>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="BOM编号">
                  <a-input v-model="model.bomNo" placeholder="留空自动生成"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="BOM名称">
                  <a-input v-model="model.name" placeholder="请输入BOM名称"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="状态">
                  <a-switch v-model="model.enabled" checkedChildren="启用" unCheckedChildren="禁用" />
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
                <a-form-item label="标准产出数量">
                  <a-input-number v-model="model.outputQuantity" :min="0.000001" :precision="6" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="成品名称">
                  <a-input v-model="model.materialName" placeholder="请输入成品名称"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="成品单位">
                  <a-input v-model="model.materialUnit" placeholder="例如：件"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="备注">
                  <a-input v-model="model.remark"></a-input>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
          <div class="production-detail-head">
            <span>原料明细</span>
            <a-button icon="plus" size="small" @click="addItem">新增原料</a-button>
          </div>
          <a-table
            size="small"
            bordered
            rowKey="rowKey"
            :columns="itemColumns"
            :dataSource="items"
            :pagination="false"
            :scroll="{x: 920}">
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
            <template slot="quantity" slot-scope="text, record">
              <a-input-number v-model="record.quantity" :min="0.000001" :precision="6" style="width:100%"></a-input-number>
            </template>
            <template slot="lossRate" slot-scope="text, record">
              <a-input-number v-model="record.lossRate" :min="0" :precision="6" style="width:100%"></a-input-number>
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
    name: 'ProductionBomList',
    data() {
      return {
        cardStyle: '',
        labelCol: { span: 5 },
        wrapperCol: { span: 18, offset: 1 },
        queryParam: { keyword: '' },
        dataSource: [],
        loading: false,
        saving: false,
        modalVisible: false,
        modalTitle: '新增BOM',
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
          { title: 'BOM编号', dataIndex: 'bomNo', width: 160 },
          { title: 'BOM名称', dataIndex: 'name', width: 160 },
          { title: '成品名称', dataIndex: 'materialName', width: 160 },
          { title: '成品单位', dataIndex: 'materialUnit', width: 90 },
          { title: '标准产出', dataIndex: 'outputQuantity', width: 100 },
          { title: '状态', dataIndex: 'enabled', width: 80, scopedSlots: { customRender: 'enabledRender' } },
          { title: '备注', dataIndex: 'remark', width: 200 }
        ],
        itemColumns: [
          { title: '原料ID', dataIndex: 'materialId', width: 100, scopedSlots: { customRender: 'materialId' } },
          { title: '扩展ID', dataIndex: 'materialExtendId', width: 100, scopedSlots: { customRender: 'materialExtendId' } },
          { title: '原料名称', dataIndex: 'materialName', width: 150, scopedSlots: { customRender: 'materialName' } },
          { title: '单位', dataIndex: 'materialUnit', width: 90, scopedSlots: { customRender: 'materialUnit' } },
          { title: '标准用量', dataIndex: 'quantity', width: 120, scopedSlots: { customRender: 'quantity' } },
          { title: '损耗率%', dataIndex: 'lossRate', width: 120, scopedSlots: { customRender: 'lossRate' } },
          { title: '备注', dataIndex: 'remark', width: 160, scopedSlots: { customRender: 'remark' } },
          { title: '操作', dataIndex: 'itemAction', width: 70, scopedSlots: { customRender: 'itemAction' } }
        ]
      }
    },
    created() {
      this.loadData(1)
    },
    methods: {
      loadData(arg) {
        if (arg === 1) {
          this.ipagination.current = 1
        }
        this.loading = true
        getAction('/production/bom/list', {
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
      searchReset() {
        this.queryParam = { keyword: '' }
        this.loadData(1)
      },
      handleTableChange(pagination) {
        this.ipagination = Object.assign(this.ipagination, pagination)
        this.loadData()
      },
      handleAdd() {
        this.modalTitle = '新增BOM'
        this.model = { enabled: true, outputQuantity: 1 }
        this.items = []
        this.addItem()
        this.modalVisible = true
      },
      handleEdit(record) {
        this.modalTitle = '编辑BOM'
        getAction('/production/bom/info', { id: record.id }).then((res) => {
          if (res.code === 200) {
            const detail = res.data.info || {}
            this.model = Object.assign({ enabled: true, outputQuantity: 1 }, detail.info || {})
            this.items = (detail.items || []).map(item => Object.assign({ rowKey: this.nextRowKey() }, item))
            this.modalVisible = true
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      handleDelete(id) {
        deleteAction('/production/bom/delete', { id }).then((res) => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      addItem() {
        this.items.push({ rowKey: this.nextRowKey(), quantity: 1, lossRate: 0 })
      },
      removeItem(index) {
        this.items.splice(index, 1)
      },
      handleSave() {
        if (!this.model.name) {
          this.$message.warning('请输入BOM名称')
          return
        }
        if (!this.model.materialName) {
          this.$message.warning('请输入成品名称')
          return
        }
        if (!this.items.length) {
          this.$message.warning('请至少录入一条原料明细')
          return
        }
        this.saving = true
        const payload = Object.assign({}, this.model, {
          items: this.items.map((item, index) => Object.assign({}, item, { sort: index }))
        })
        postAction('/production/bom/save', payload).then((res) => {
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
