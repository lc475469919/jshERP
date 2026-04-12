const request = require('../../utils/request')
const storage = require('../../utils/storage')

const BILL_TYPES = [
  { name: '销售出库', type: '出库', subType: '销售' },
  { name: '采购入库', type: '入库', subType: '采购' },
  { name: '销售退货', type: '入库', subType: '销售退货' },
  { name: '采购退货', type: '出库', subType: '采购退货' },
  { name: '调拨出库', type: '出库', subType: '调拨' }
]

const STATUS_TEXT = {
  0: '未审核',
  1: '已审核',
  2: '已完成',
  3: '部分完成',
  9: '审核中'
}

const STATUS_FILTERS = [
  { label: '全部', value: '' },
  { label: '未审核', value: '0' },
  { label: '审核中', value: '9' },
  { label: '已审核', value: '1' },
  { label: '已完成', value: '2' },
  { label: '部分完成', value: '3' }
]

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function normalizeBill(row) {
  const statusKey = row.status === undefined || row.status === null ? '' : String(row.status)
  return Object.assign({}, row, {
    typeText: `${row.type || ''}${row.subType || ''}`,
    statusText: STATUS_TEXT[statusKey] || '未知',
    timeText: row.operTimeStr || row.operTime || row.createTime || '',
    amountText: row.discountLastMoney || row.totalPrice || row.changeAmount || 0,
    partnerText: row.organName || row.organ || row.supplier || '未选择往来单位'
  })
}

Page({
  data: {
    billTypes: BILL_TYPES,
    statusFilters: STATUS_FILTERS,
    activeIndex: 0,
    status: '',
    number: '',
    materialParam: '',
    beginTime: '',
    endTime: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onShow() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    const filter = wx.getStorageSync('Bill-Filter')
    if (filter) {
      wx.removeStorageSync('Bill-Filter')
      const activeIndex = BILL_TYPES.findIndex((item) => item.name === filter)
      this.setData({ activeIndex: activeIndex > -1 ? activeIndex : 0 })
      this.loadList(true)
      return
    }

    const refresh = wx.getStorageSync('Bill-Refresh')
    if (refresh) {
      wx.removeStorageSync('Bill-Refresh')
      this.loadList(true)
      return
    }

    if (!this.data.list.length) {
      this.loadList(true)
    }
  },

  onLoad() {
    const now = new Date()
    const begin = new Date(now.getFullYear(), now.getMonth(), 1)
    this.setData({
      beginTime: formatDate(begin),
      endTime: formatDate(now)
    })
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadMore()
  },

  changeType(event) {
    const activeIndex = Number(event.currentTarget.dataset.index)
    if (activeIndex === this.data.activeIndex) {
      return
    }
    this.setData({ activeIndex, number: '', materialParam: '' })
    this.loadList(true)
  },

  changeStatus(event) {
    const status = event.currentTarget.dataset.status
    if (status === this.data.status) {
      return
    }
    this.setData({ status })
    this.loadList(true)
  },

  onNumberInput(event) {
    this.setData({ number: event.detail.value })
  },

  onMaterialInput(event) {
    this.setData({ materialParam: event.detail.value })
  },

  onBeginDateChange(event) {
    this.setData({ beginTime: event.detail.value })
    this.loadList(true)
  },

  onEndDateChange(event) {
    this.setData({ endTime: event.detail.value })
    this.loadList(true)
  },

  setQuickRange(event) {
    const range = event.currentTarget.dataset.range
    const now = new Date()
    let begin = new Date(now.getFullYear(), now.getMonth(), 1)
    if (range === 'quarter') {
      begin = new Date(now.getFullYear(), now.getMonth() - 3, now.getDate())
    }
    if (range === 'year') {
      begin = new Date(now.getFullYear(), 0, 1)
    }
    this.setData({
      beginTime: formatDate(begin),
      endTime: formatDate(now)
    })
    this.loadList(true)
  },

  search() {
    this.loadList(true)
  },

  createBill(event) {
    const billType = BILL_TYPES[this.data.activeIndex] || BILL_TYPES[0]
    const kindMap = {
      '销售出库': 'saleOut',
      '采购入库': 'purchaseIn',
      '销售退货': 'saleBack',
      '采购退货': 'purchaseBack',
      '调拨出库': 'allocationOut'
    }
    const kind = event.currentTarget.dataset.kind || kindMap[billType.name] || 'saleOut'
    wx.navigateTo({
      url: `/pages/bill-edit/bill-edit?kind=${kind}`
    })
  },

  scanMaterial() {
    wx.scanCode({
      success: (res) => {
        this.setData({ materialParam: res.result || '' })
        this.loadList(true)
      }
    })
  },

  openDetail(event) {
    const number = event.currentTarget.dataset.number
    const headerId = event.currentTarget.dataset.id
    if (!number || !headerId) {
      wx.showToast({ title: '单据信息不完整', icon: 'none' })
      return
    }
    wx.navigateTo({
      url: `/pages/bill-detail/bill-detail?number=${encodeURIComponent(number)}&headerId=${headerId}`
    })
  },

  loadMore() {
    if (!this.data.hasMore || this.data.loading) {
      return
    }
    this.setData({ pageNo: this.data.pageNo + 1 })
    this.loadList(false)
  },

  loadList(reset) {
    if (reset) {
      this.setData({ pageNo: 1, list: [] })
    }

    const billType = BILL_TYPES[this.data.activeIndex]
    const search = JSON.stringify({
      type: billType.type,
      subType: billType.subType,
      hasDebt: '',
      status: this.data.status,
      purchaseStatus: '',
      number: this.data.number || '',
      linkApply: '',
      linkNumber: '',
      beginTime: this.data.beginTime,
      endTime: this.data.endTime,
      materialParam: this.data.materialParam || '',
      organId: '',
      creator: '',
      depotId: '',
      accountId: '',
      salesMan: '',
      remark: ''
    })

    this.setData({ loading: true })
    return request.get('/depotHead/list', {
      pageNo: this.data.pageNo,
      pageSize: this.data.pageSize,
      search
    }).then((res) => {
      const rows = res.rows || (res.data && res.data.rows) || []
      const total = res.total || (res.data && res.data.total) || rows.length
      const list = reset ? rows.map(normalizeBill) : this.data.list.concat(rows.map(normalizeBill))

      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '单据查询失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
