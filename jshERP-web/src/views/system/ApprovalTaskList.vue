<template>
  <a-card :bordered="false">
    <div class="table-operator">
      <a-button type="primary" icon="reload" @click="loadData">刷新</a-button>
    </div>
    <a-tabs :active-key="activeTab" @change="handleTabChange">
      <a-tab-pane key="todo" tab="待我审批" />
      <a-tab-pane key="submitted" tab="我提交的审批" />
    </a-tabs>
    <div class="filter-row">
      <span>状态：</span>
      <a-select style="width: 160px" v-model="status" @change="loadData">
        <a-select-option value="pending">审批中</a-select-option>
        <a-select-option value="approved">已通过</a-select-option>
        <a-select-option value="rejected">已驳回</a-select-option>
        <a-select-option value="">全部</a-select-option>
      </a-select>
    </div>
    <a-table
      rowKey="id"
      size="middle"
      bordered
      :columns="columns"
      :dataSource="dataSource"
      :pagination="false"
      :loading="loading">
      <template slot="billAmount" slot-scope="text">
        {{ formatMoney(text) }}
      </template>
      <template slot="status" slot-scope="text">
        <a-tag :color="statusColor(text)">{{ statusText(text) }}</a-tag>
      </template>
      <template slot="step" slot-scope="text, record">
        {{ formatStep(record) }}
      </template>
      <template slot="createTime" slot-scope="text">
        {{ formatTime(text) }}
      </template>
      <template slot="approveTime" slot-scope="text">
        {{ formatTime(text) }}
      </template>
      <template slot="action" slot-scope="text, record">
        <a-button
          v-if="activeTab === 'todo' && record.status === 'pending'"
          type="primary"
          size="small"
          @click="openFinish(record, true)">
          通过
        </a-button>
        <a-button
          v-if="activeTab === 'todo' && record.status === 'pending'"
          size="small"
          style="margin-left: 8px"
          @click="openFinish(record, false)">
          驳回
        </a-button>
        <span v-if="activeTab !== 'todo' || record.status !== 'pending'">-</span>
      </template>
    </a-table>
    <a-modal
      v-model="finishVisible"
      :title="finishTitle"
      :confirmLoading="finishing"
      @ok="finishTask">
      <a-form :form="finishForm">
        <a-form-item label="审批意见" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
          <a-textarea
            :rows="4"
            placeholder="可填写审批意见"
            v-decorator="['approveRemark']" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-card>
</template>

<script>
import { getAction, postAction } from '@/api/manage'

export default {
  name: 'ApprovalTaskList',
  data() {
    return {
      activeTab: 'todo',
      status: 'pending',
      loading: false,
      dataSource: [],
      finishVisible: false,
      finishing: false,
      finishRecord: null,
      finishIsApprove: true,
      finishForm: this.$form.createForm(this),
      columns: [
        { title: '单据编号', dataIndex: 'billNo', width: 150 },
        { title: '模块', dataIndex: 'moduleName', width: 110 },
        { title: '单据类型', dataIndex: 'billSubType', width: 110 },
        { title: '金额', dataIndex: 'billAmount', scopedSlots: { customRender: 'billAmount' }, width: 120 },
        { title: '提交人', dataIndex: 'submitterName', width: 100 },
        { title: '审批角色', dataIndex: 'approverRoleName', width: 120 },
        { title: '当前步骤', dataIndex: 'currentStepNo', scopedSlots: { customRender: 'step' }, width: 100 },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' }, width: 100 },
        { title: '提交说明', dataIndex: 'remark', width: 160 },
        { title: '审批意见', dataIndex: 'approveRemark', width: 160 },
        { title: '提交时间', dataIndex: 'createTime', scopedSlots: { customRender: 'createTime' }, width: 170 },
        { title: '审批时间', dataIndex: 'approveTime', scopedSlots: { customRender: 'approveTime' }, width: 170 },
        { title: '操作', dataIndex: 'action', scopedSlots: { customRender: 'action' }, width: 150 }
      ]
    }
  },
  computed: {
    finishTitle() {
      return this.finishIsApprove ? '审批通过' : '审批驳回'
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    handleTabChange(key) {
      this.activeTab = key
      this.status = key === 'todo' ? 'pending' : ''
      this.loadData()
    },
    loadData() {
      this.loading = true
      const url = this.activeTab === 'todo' ? '/approval/task/my' : '/approval/task/submitted'
      getAction(url, { status: this.status }).then(res => {
        this.dataSource = (res && res.data) || []
      }).catch(() => {
        this.$message.warning('审批列表加载失败')
      }).finally(() => {
        this.loading = false
      })
    },
    openFinish(record, isApprove) {
      this.finishRecord = record
      this.finishIsApprove = isApprove
      this.finishVisible = true
      this.$nextTick(() => {
        this.finishForm.setFieldsValue({ approveRemark: '' })
      })
    },
    finishTask() {
      if (!this.finishRecord) {
        return
      }
      this.finishForm.validateFields((err, values) => {
        if (err) {
          return
        }
        this.finishing = true
        const url = this.finishIsApprove ? '/approval/task/approve' : '/approval/task/reject'
        postAction(url, {
          id: this.finishRecord.id,
          approveRemark: values.approveRemark || ''
        }).then(() => {
          this.$message.success(this.finishIsApprove ? '审批已通过' : '审批已驳回')
          this.finishVisible = false
          this.loadData()
        }).catch(() => {
          this.$message.warning('审批处理失败')
        }).finally(() => {
          this.finishing = false
        })
      })
    },
    statusText(status) {
      const map = {
        pending: '审批中',
        approved: '已通过',
        rejected: '已驳回'
      }
      return map[status] || status || '-'
    },
    statusColor(status) {
      const map = {
        pending: 'orange',
        approved: 'green',
        rejected: 'red'
      }
      return map[status] || 'default'
    },
    formatMoney(value) {
      if (value === null || value === undefined || value === '') {
        return '-'
      }
      const number = Number(value)
      return Number.isNaN(number) ? value : number.toFixed(2)
    },
    formatTime(value) {
      if (!value) {
        return '-'
      }
      if (typeof value === 'number') {
        return new Date(value).toLocaleString()
      }
      return String(value).replace('T', ' ')
    },
    formatStep(record) {
      if (!record.currentStepNo || !record.totalStep) {
        return '-'
      }
      return `${record.currentStepNo}/${record.totalStep}`
    }
  }
}
</script>

<style scoped>
.filter-row {
  margin: 0 0 16px 0;
}
</style>
