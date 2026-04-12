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

function normalizeDetail(info) {
  const statusKey = info.status === undefined || info.status === null ? '' : String(info.status)
  return Object.assign({}, info, {
    title: info.depotHeadType || `${info.type || ''}${info.subType || ''}` || '单据详情',
    statusText: STATUS_TEXT[statusKey] || '未知',
    timeText: info.operTimeStr || info.operTime || info.createTime || '',
    amountText: info.discountLastMoney || info.totalPrice || info.changeAmount || 0,
    partnerText: info.organName || '未选择往来单位',
    contactText: [info.contacts, info.telephone || info.phoneNum].filter(Boolean).join(' '),
    debtText: info.lastDebt || info.needDebt || info.debt || 0
  })
}

function normalizeItem(row) {
  return Object.assign({}, row, {
    displayName: row.name || row.materialName || '未命名商品',
    specText: [row.standard, row.model, row.color].filter(Boolean).join(' / '),
    quantityText: row.operNumber || row.basicNumber || 0,
    priceText: row.allPrice || row.taxLastMoney || 0
  })
}

function normalizeFinanceBill(row) {
  return Object.assign({}, row, {
    displayType: row.type || '财务单据',
    displayNo: row.billNo || '-',
    amountText: row.totalPrice || row.changeAmount || 0,
    accountText: row.accountName || '-'
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
    number: '',
    headerId: '',
    detail: {},
    items: [],
    financeBills: [],
    approvalTask: null,
    totalRow: null,
    loading: false,
    canSubmitApproval: false,
    canDelete: false,
    deleting: false,
    submittingApproval: false
  },

  onLoad(options) {
    this.setData({
      number: decodeURIComponent(options.number || ''),
      headerId: options.headerId || ''
    })
    this.loadDetail()
  },

  onPullDownRefresh() {
    this.loadDetail().finally(() => wx.stopPullDownRefresh())
  },

  loadDetail() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }

    if (!this.data.number || !this.data.headerId) {
      wx.showToast({ title: '单据信息不完整', icon: 'none' })
      return Promise.resolve()
    }

    this.setData({ loading: true })
    const headRequest = request.get('/depotHead/getDetailByNumber', { number: this.data.number })
    const itemRequest = request.get('/depotItem/getDetailList', {
      headerId: this.data.headerId,
      mpList: '',
      isReadOnly: '1'
    })
    const financeRequest = request.get('/accountHead/getFinancialBillNoByBillId', {
      billId: this.data.headerId
    })
    const approvalRequest = request.get('/approval/task/latest', {
      billTable: 'depot_head',
      billId: this.data.headerId
    })

    return Promise.all([headRequest, itemRequest, financeRequest, approvalRequest]).then(([headRes, itemRes, financeRes, approvalRes]) => {
      const head = headRes && headRes.code === 200 ? headRes.data || {} : {}
      const rows = itemRes && itemRes.code === 200 && itemRes.data ? itemRes.data.rows || [] : []
      const totalRow = rows.length ? rows[rows.length - 1] : null
      const detailRows = rows.filter((row) => row && (row.name || row.barCode || row.depotName))
      const financeBills = financeRes && financeRes.code === 200 && financeRes.data ? financeRes.data : []
      const approvalTask = approvalRes && approvalRes.code === 200 && approvalRes.data ? normalizeApprovalTask(approvalRes.data) : null

      this.setData({
        detail: normalizeDetail(head),
        items: detailRows.map(normalizeItem),
        financeBills: financeBills.map(normalizeFinanceBill),
        approvalTask,
        totalRow: totalRow && !totalRow.name ? totalRow : null,
        canSubmitApproval: String(head.status || '') === '0',
        canDelete: String(head.status || '') === '0'
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '单据详情加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  openFinanceBill(event) {
    const id = event.currentTarget.dataset.id
    const billNo = event.currentTarget.dataset.number
    if (!id || !billNo) {
      wx.showToast({ title: '财务单据信息不完整', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: `/pages/finance-bill-detail/finance-bill-detail?id=${id}&billNo=${encodeURIComponent(billNo)}`
    })
  },

  submitApproval() {
    if (!this.data.headerId || this.data.submittingApproval || !this.data.canSubmitApproval) {
      return
    }

    wx.showModal({
      title: '提交审批',
      content: '提交后单据进入审核中，按电脑端审批配置流转。',
      confirmText: '提交',
      success: (modalRes) => {
        if (!modalRes.confirm) {
          return
        }
        this.setData({ submittingApproval: true })
        request.post('/approval/task/submit', {
          billTable: 'depot_head',
          billId: this.data.headerId
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
    if (!this.data.headerId || !this.data.canDelete || this.data.deleting) {
      return
    }
    wx.showModal({
      title: '删除单据',
      content: '删除后不能在手机端恢复。',
      confirmText: '删除',
      confirmColor: '#b42318',
      success: (modalRes) => {
        if (!modalRes.confirm) {
          return
        }
        this.setData({ deleting: true })
        request.del('/depotHead/delete', { id: this.data.headerId }).then(() => {
          wx.setStorageSync('Bill-Refresh', '1')
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
