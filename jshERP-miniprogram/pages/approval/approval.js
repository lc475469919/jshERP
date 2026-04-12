const request = require('../../utils/request')
const storage = require('../../utils/storage')

const STATUS_TEXT = {
  pending: '待审批',
  approved: '已通过',
  rejected: '已驳回'
}

function normalizeTask(row) {
  return Object.assign({}, row, {
    statusText: STATUS_TEXT[row.status] || row.status || '未知',
    amountText: row.billAmount || 0,
    timeText: row.createTime || row.updateTime || '',
    roleText: row.approverRoleName || '-',
    submitterText: row.submitterName || '-'
  })
}

Page({
  data: {
    activeTab: 'my',
    submittedStatus: '',
    statusOptions: [
      { label: '全部', value: '' },
      { label: '待审批', value: 'pending' },
      { label: '已通过', value: 'approved' },
      { label: '已驳回', value: 'rejected' }
    ],
    myTasks: [],
    submittedTasks: [],
    loading: false,
    processingId: ''
  },

  onShow() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData().finally(() => wx.stopPullDownRefresh())
  },

  switchTab(event) {
    const tab = event.currentTarget.dataset.tab
    if (tab && tab !== this.data.activeTab) {
      this.setData({ activeTab: tab })
    }
  },

  switchSubmittedStatus(event) {
    const status = event.currentTarget.dataset.status
    if (status === this.data.submittedStatus) {
      return
    }
    this.setData({ submittedStatus: status })
    this.loadData()
  },

  loadData() {
    this.setData({ loading: true })
    const myRequest = request.get('/approval/task/my', { status: 'pending' })
    const submittedParams = this.data.submittedStatus ? { status: this.data.submittedStatus } : {}
    const submittedRequest = request.get('/approval/task/submitted', submittedParams)

    return Promise.all([myRequest, submittedRequest]).then(([myRes, submittedRes]) => {
      this.setData({
        myTasks: this.readRows(myRes),
        submittedTasks: this.readRows(submittedRes)
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '审批列表加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  readRows(res) {
    const rows = res && res.code === 200 && Array.isArray(res.data) ? res.data : []
    return rows.map(normalizeTask)
  },

  openBill(event) {
    const task = event.currentTarget.dataset.task
    if (!task) {
      return
    }
    if (task.billTable === 'depot_head') {
      wx.navigateTo({
        url: `/pages/bill-detail/bill-detail?number=${encodeURIComponent(task.billNo || '')}&headerId=${task.billId}`
      })
      return
    }
    if (task.billTable === 'account_head') {
      wx.navigateTo({
        url: `/pages/finance-bill-detail/finance-bill-detail?id=${task.billId}&billNo=${encodeURIComponent(task.billNo || '')}`
      })
    }
  },

  approveTask(event) {
    this.finishTask(event.currentTarget.dataset.id, 'approve')
  },

  rejectTask(event) {
    this.finishTask(event.currentTarget.dataset.id, 'reject')
  },

  finishTask(id, action) {
    if (!id || this.data.processingId) {
      return
    }
    const isApprove = action === 'approve'
    wx.showModal({
      title: isApprove ? '审批通过' : '驳回审批',
      content: isApprove ? '通过后会按原系统逻辑审核单据。' : '驳回后单据回到未审核状态。',
      confirmText: isApprove ? '通过' : '驳回',
      editable: true,
      placeholderText: '填写审批意见',
      success: (modalRes) => {
        if (!modalRes.confirm) {
          return
        }
        this.setData({ processingId: id })
        request.post(isApprove ? '/approval/task/approve' : '/approval/task/reject', {
          id,
          approveRemark: modalRes.content || ''
        }).then(() => {
          wx.showToast({ title: isApprove ? '已通过' : '已驳回', icon: 'success' })
          return this.loadData()
        }).catch((err) => {
          wx.showToast({ title: err.message || '处理失败', icon: 'none' })
        }).finally(() => {
          this.setData({ processingId: '' })
        })
      }
    })
  }
})
