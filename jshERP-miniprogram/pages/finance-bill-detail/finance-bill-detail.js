const request = require('../../utils/request')
const storage = require('../../utils/storage')

const STATUS_TEXT = {
  0: '未审核',
  1: '已审核',
  2: '已完成',
  3: '部分完成',
  9: '审核中'
}

const APPROVAL_STATUS_TEXT = {
  pending: '待审批',
  approved: '已通过',
  rejected: '已驳回'
}

function normalizeDetail(row) {
  const statusKey = row.status === undefined || row.status === null ? '' : String(row.status)
  return Object.assign({}, row, {
    displayType: row.type || '财务单据',
    displayNo: row.billNo || '-',
    displayTime: row.billTimeStr || row.billTime || '',
    displayOrgan: row.organName || '无往来单位',
    displayAccount: row.accountName || '-',
    amountText: row.totalPrice || row.changeAmount || 0,
    discountText: row.discountMoney || 0,
    statusText: STATUS_TEXT[statusKey] || '未知'
  })
}

function normalizeItem(row) {
  return Object.assign({}, row, {
    accountText: row.accountName || '-',
    itemText: row.inOutItemName || '-',
    billText: row.billNumber === 'QiChu' ? '期初' : (row.billNumber || '-'),
    needDebtText: row.needDebt || 0,
    finishDebtText: row.finishDebt || 0,
    amountText: row.eachAmount || 0,
    remarkText: row.remark || ''
  })
}

function normalizeApprovalTask(row) {
  return Object.assign({}, row, {
    statusText: APPROVAL_STATUS_TEXT[row.status] || row.status || '',
    roleText: row.approverRoleName || '-',
    submitterText: row.submitterName || '-',
    approverText: row.approverName || '-',
    timeText: row.createTime || '',
    approveTimeText: row.approveTime || ''
  })
}

Page({
  data: {
    id: '',
    billNo: '',
    detail: {},
    approvalTask: null,
    rows: [],
    loading: false,
    canSubmitApproval: false,
    canDelete: false,
    deleting: false,
    submittingApproval: false
  },

  onLoad(options) {
    this.setData({
      id: options.id || '',
      billNo: options.billNo ? decodeURIComponent(options.billNo) : ''
    })
    this.loadDetail()
  },

  onPullDownRefresh() {
    this.loadDetail().finally(() => wx.stopPullDownRefresh())
  },

  copyBillNo() {
    if (!this.data.detail.billNo) {
      return
    }
    wx.setClipboardData({ data: this.data.detail.billNo })
  },

  loadDetail() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }

    if (!this.data.id || !this.data.billNo) {
      wx.showToast({ title: '财务单据信息不完整', icon: 'none' })
      return Promise.resolve()
    }

    this.setData({ loading: true })
    const detailRequest = request.get('/accountHead/getDetailByNumber', { billNo: this.data.billNo })
    const rowsRequest = request.get('/accountItem/getDetailList', { headerId: this.data.id })
    const approvalRequest = request.get('/approval/task/latest', {
      billTable: 'account_head',
      billId: this.data.id
    })

    return Promise.all([detailRequest, rowsRequest, approvalRequest]).then(([detailRes, rowsRes, approvalRes]) => {
      const detail = detailRes && detailRes.code === 200 && detailRes.data ? normalizeDetail(detailRes.data) : {}
      const data = rowsRes && rowsRes.code === 200 && rowsRes.data ? rowsRes.data : {}
      const approvalTask = approvalRes && approvalRes.code === 200 && approvalRes.data ? normalizeApprovalTask(approvalRes.data) : null
      this.setData({
        detail,
        approvalTask,
        rows: (data.rows || []).map(normalizeItem),
        canSubmitApproval: String(detail.status || '') === '0',
        canDelete: String(detail.status || '') === '0'
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '财务单据详情加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  submitApproval() {
    if (!this.data.id || this.data.submittingApproval || !this.data.canSubmitApproval) {
      return
    }

    wx.showModal({
      title: '提交审批',
      content: '提交后财务单据进入审核中，按电脑端审批配置流转。',
      confirmText: '提交',
      success: (modalRes) => {
        if (!modalRes.confirm) {
          return
        }
        this.setData({ submittingApproval: true })
        request.post('/approval/task/submit', {
          billTable: 'account_head',
          billId: this.data.id
        }).then(() => {
          wx.showToast({ title: '已提交审批', icon: 'success' })
          return this.loadDetail()
        }).catch((err) => {
          wx.showToast({ title: err.message || '提交审批失败', icon: 'none' })
        }).finally(() => {
          this.setData({ submittingApproval: false })
        })
      }
    })
  },

  deleteBill() {
    if (!this.data.id || !this.data.canDelete || this.data.deleting) {
      return
    }
    wx.showModal({
      title: '删除财务单据',
      content: '删除后不能在手机端恢复。',
      confirmText: '删除',
      confirmColor: '#b42318',
      success: (modalRes) => {
        if (!modalRes.confirm) {
          return
        }
        this.setData({ deleting: true })
        request.del('/accountHead/delete', { id: this.data.id }).then(() => {
          wx.setStorageSync('Finance-Bill-Refresh', '1')
          wx.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 600)
        }).catch((err) => {
          wx.showToast({ title: err.message || '删除失败', icon: 'none' })
        }).finally(() => {
          this.setData({ deleting: false })
        })
      }
    })
  }
})
