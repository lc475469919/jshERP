const request = require('../../utils/request')
const storage = require('../../utils/storage')

const TYPE_FILTERS = [
  { label: '全部', value: '' },
  { label: '收款', value: '收款' },
  { label: '付款', value: '付款' },
  { label: '收入', value: '收入' },
  { label: '支出', value: '支出' },
  { label: '转账', value: '转账' }
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
    displayType: row.type || '财务单据',
    displayNo: row.billNo || '-',
    displayTime: row.billTimeStr || row.billTime || '',
    displayOrgan: row.organName || '无往来单位',
    displayAccount: row.accountName || '-',
    amountText: row.totalPrice || row.changeAmount || 0,
    statusText: STATUS_TEXT[statusKey] || '未知'
  })
}

Page({
  data: {
    typeFilters: TYPE_FILTERS,
    statusFilters: STATUS_FILTERS,
    type: '',
    status: '',
    billNo: '',
    beginTime: '',
    endTime: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    const now = new Date()
    const begin = new Date(now.getFullYear(), now.getMonth(), 1)
    this.setData({
      beginTime: formatDate(begin),
      endTime: formatDate(now)
    })
    this.loadList(true)
  },

  onShow() {
    const refresh = wx.getStorageSync('Finance-Bill-Refresh')
    if (refresh) {
      wx.removeStorageSync('Finance-Bill-Refresh')
      this.loadList(true)
    }
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadMore()
  },

  changeType(event) {
    const type = event.currentTarget.dataset.type
    if (type === this.data.type) {
      return
    }
    this.setData({ type })
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

  onBillNoInput(event) {
    this.setData({ billNo: event.detail.value })
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
    const type = event.currentTarget.dataset.type || this.data.type || '收款'
    wx.navigateTo({
      url: `/pages/finance-bill-edit/finance-bill-edit?type=${encodeURIComponent(type)}`
    })
  },

  openDetail(event) {
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

  loadMore() {
    if (!this.data.hasMore || this.data.loading) {
      return
    }
    this.setData({ pageNo: this.data.pageNo + 1 })
    this.loadList(false)
  },

  loadList(reset) {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }

    if (reset) {
      this.setData({ pageNo: 1, list: [] })
    }

    const search = JSON.stringify({
      type: this.data.type || '',
      billNo: this.data.billNo || '',
      beginTime: this.data.beginTime,
      endTime: this.data.endTime,
      organId: '',
      creator: '',
      handsPersonId: '',
      accountId: '',
      status: this.data.status,
      remark: '',
      number: '',
      inOutItemId: ''
    })

    this.setData({ loading: true })
    return request.get('/accountHead/list', {
      pageNo: this.data.pageNo,
      pageSize: this.data.pageSize,
      search
    }).then((res) => {
      const rows = res.rows || (res.data && res.data.rows) || []
      const total = res.total || (res.data && res.data.total) || rows.length
      const normalizedRows = rows.map(normalizeBill)
      const list = reset ? normalizedRows : this.data.list.concat(normalizedRows)
      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '财务单据加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
