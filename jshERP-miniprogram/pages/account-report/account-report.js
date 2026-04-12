const request = require('../../utils/request')
const storage = require('../../utils/storage')

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function normalizeRow(row) {
  return Object.assign({}, row, {
    displayName: row.supplier || '未命名单位',
    phoneText: row.telephone || row.phoneNum || '-',
    preNeedText: row.preNeed || 0,
    debtText: row.debtMoney || 0,
    backText: row.backMoney || 0,
    allNeedText: row.allNeed || 0
  })
}

Page({
  data: {
    type: '客户',
    hasDebt: '1',
    beginTime: '',
    endTime: '',
    firstMoney: 0,
    lastMoney: 0,
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
    this.setData({
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

  changeType(event) {
    const type = event.currentTarget.dataset.type
    if (type === this.data.type) {
      return
    }
    this.setData({ type })
    this.loadList(true)
  },

  changeDebt(event) {
    const hasDebt = event.currentTarget.dataset.debt
    if (hasDebt === this.data.hasDebt) {
      return
    }
    this.setData({ hasDebt })
    this.loadList(true)
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

  openPartner(event) {
    const id = event.currentTarget.dataset.id
    const partner = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !partner) {
      wx.showToast({ title: '往来单位信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Partner-Detail', partner)
    wx.navigateTo({ url: `/pages/partner-detail/partner-detail?id=${id}` })
  },

  openBills(event) {
    const id = event.currentTarget.dataset.id
    const partner = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !partner) {
      wx.showToast({ title: '往来单位信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Account-Bills-Filter', {
      organId: id,
      organName: partner.displayName,
      supplierType: this.data.type,
      beginTime: this.data.beginTime,
      endTime: this.data.endTime
    })
    wx.navigateTo({ url: '/pages/account-bills/account-bills' })
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

    this.setData({ loading: true })
    return request.get('/depotHead/getStatementAccount', {
      currentPage: this.data.pageNo,
      pageSize: this.data.pageSize,
      beginTime: this.data.beginTime,
      endTime: this.data.endTime,
      hasDebt: this.data.hasDebt,
      supplierType: this.data.type
    }).then((res) => {
      const data = res && res.code === 200 && res.data ? res.data : {}
      const rows = data.rows || []
      const total = data.total || rows.length
      const list = reset ? rows.map(normalizeRow) : this.data.list.concat(rows.map(normalizeRow))
      this.setData({
        list,
        total,
        firstMoney: data.firstMoney || 0,
        lastMoney: data.lastMoney || 0,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '往来账款加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
