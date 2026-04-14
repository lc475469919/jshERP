<template>
  <a-row :gutter="24">
    <a-col :md="24">
      <a-card :style="cardStyle" :bordered="false">
        <div class="table-page-search-wrapper">
          <a-form layout="inline" @keyup.enter.native="loadData(1)">
            <a-row :gutter="24">
              <a-col :md="6" :sm="24">
                <a-form-item label="关键字" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input v-model="queryParam.keyword" placeholder="工序编号、工序名称"></a-input>
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
          <a-button type="primary" icon="plus" @click="handleAdd">新增工序</a-button>
        </div>
        <a-table
          size="middle"
          bordered
          rowKey="id"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="ipagination"
          :loading="loading"
          :scroll="{x: 1000}"
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
          :width="680"
          :visible="modalVisible"
          :confirmLoading="saving"
          :maskClosable="false"
          @ok="handleSave"
          @cancel="handleCancel">
          <a-form>
            <a-row :gutter="16">
              <a-col :md="12" :sm="24">
                <a-form-item label="工序编号">
                  <a-input v-model="model.processNo" placeholder="留空自动生成"></a-input>
                </a-form-item>
              </a-col>
              <a-col :md="12" :sm="24">
                <a-form-item label="工序名称">
                  <a-input v-model="model.name" placeholder="请输入工序名称"></a-input>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="16">
              <a-col :md="8" :sm="24">
                <a-form-item label="计价方式">
                  <a-select v-model="model.wageType" placeholder="请选择">
                    <a-select-option value="计件">计件</a-select-option>
                    <a-select-option value="计时">计时</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="工序单价">
                  <a-input-number v-model="model.unitPrice" :min="0" :precision="6" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="排序">
                  <a-input-number v-model="model.sort" :min="0" style="width:100%"></a-input-number>
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="状态">
              <a-switch v-model="model.enabled" checkedChildren="启用" unCheckedChildren="禁用" />
            </a-form-item>
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
    name: 'ProductionProcessList',
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
        modalTitle: '新增工序',
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
          { title: '工序编号', dataIndex: 'processNo', width: 150 },
          { title: '工序名称', dataIndex: 'name', width: 160 },
          { title: '计价方式', dataIndex: 'wageType', width: 100 },
          { title: '工序单价', dataIndex: 'unitPrice', width: 110 },
          { title: '排序', dataIndex: 'sort', width: 80 },
          { title: '状态', dataIndex: 'enabled', width: 80, scopedSlots: { customRender: 'enabledRender' } },
          { title: '备注', dataIndex: 'remark', width: 220 }
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
        getAction('/production/process/list', {
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
        this.modalTitle = '新增工序'
        this.model = { enabled: true, wageType: '计件', unitPrice: 0, sort: 0 }
        this.modalVisible = true
      },
      handleEdit(record) {
        this.modalTitle = '编辑工序'
        this.model = Object.assign({ enabled: true, wageType: '计件', unitPrice: 0, sort: 0 }, record)
        this.modalVisible = true
      },
      handleDelete(id) {
        deleteAction('/production/process/delete', { id }).then((res) => {
          if (res.code === 200) {
            this.$message.success('删除成功')
            this.loadData()
          } else {
            this.$message.warning(res.data && res.data.message ? res.data.message : res.data)
          }
        })
      },
      handleSave() {
        if (!this.model.name) {
          this.$message.warning('请输入工序名称')
          return
        }
        this.saving = true
        postAction('/production/process/save', this.model).then((res) => {
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
