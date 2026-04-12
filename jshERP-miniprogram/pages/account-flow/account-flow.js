const request = require('../../utils/request')
const storage = require('../../utils/storage')

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function normalizeFlow(row) {
  return Object.assign({}, row, {
    displayType: row.type || '流水',
    partnerText: row.supplierName || '-',
    amountText: row.changeAmount || 0,
    balanceText: row.balance || 0,
    timeText: row.operTime || ''
  })
}

Page({
  data: {
    account: {},
    beginTime: '',
    endTime: '',
    number: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    const now = new Date()
    const begin = new Date(now.getFullYear(), now.getMonth() - 3, now.getDate())
    const account = wx.getStorageSync('Account-Flow-Filter') || {}
    this.setData({
      account,
      beginTime: formatDate(begin),
      endTime: formatDate(now)
    })
    this.loadList(true)
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadMore()
  },

  onNumberInput(event) {
    this.setData({ number: event.detail.value })
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
    let begin = new Date(now.getFullYear(), now.getMonth() - 3, now.getDate())
    if (range === 'month') {
      begin = new Date(now.getFullYear(), now.getMonth(), 1)
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

    if (!this.data.account.id) {
      wx.showToast({ title: '账户信息不完整', icon: 'none' })
      return Promise.resolve()
    }

    if (reset) {
      this.setData({ pageNo: 1, list: [] })
    }

    this.setData({ loading: true })
    return request.get('/account/findAccountInOutList', {
      currentPage: this.data.pageNo,
      pageSize: this.data.pageSize,
      accountId: this.data.account.id,
      initialAmount: this.data.account.initialAmount || 0,
      number: this.data.number || '',
      beginTime: this.data.beginTime,
      endTime: this.data.endTime
    }).then((res) => {
      const data = res && res.code === 200 && res.data ? res.data : {}
      const rows = data.rows || []
      const total = data.total || rows.length
      const list = reset ? rows.map(normalizeFlow) : this.data.list.concat(rows.map(normalizeFlow))
      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '账户流水加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
