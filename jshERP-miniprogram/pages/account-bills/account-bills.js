const request = require('../../utils/request')
const storage = require('../../utils/storage')

const STATUS_TEXT = {
  0: '未审核',
  1: '已审核',
  2: '已完成',
  3: '部分完成',
  9: '审核中'
}

function normalizeBill(row) {
  const statusKey = row.status === undefined || row.status === null ? '' : String(row.status)
  return Object.assign({}, row, {
    statusText: STATUS_TEXT[statusKey] || '未知',
    timeText: row.operTimeStr || row.operTime || row.createTime || '',
    amountText: row.needDebt || row.discountLastMoney || row.totalPrice || 0,
    debtText: row.debt || row.lastDebt || 0,
    paidText: row.finishDebt || 0
  })
}

Page({
  data: {
    filter: {},
    number: '',
    materialParam: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    const filter = wx.getStorageSync('Account-Bills-Filter') || {}
    this.setData({ filter })
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

  onMaterialInput(event) {
    this.setData({ materialParam: event.detail.value })
  },

  search() {
    this.loadList(true)
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
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }

    if (!this.data.filter.organId) {
      wx.showToast({ title: '缺少往来单位', icon: 'none' })
      return Promise.resolve()
    }

    if (reset) {
      this.setData({ pageNo: 1, list: [] })
    }

    const search = JSON.stringify({
      organId: this.data.filter.organId,
      materialParam: this.data.materialParam || '',
      number: this.data.number || '',
      beginTime: this.data.filter.beginTime || '',
      endTime: this.data.filter.endTime || '',
      status: ''
    })

    this.setData({ loading: true })
    return request.get('/depotHead/debtList', {
      currentPage: this.data.pageNo,
      pageSize: this.data.pageSize,
      search
    }).then((res) => {
      const data = res && res.code === 200 && res.data ? res.data : {}
      const rows = data.rows || []
      const total = data.total || rows.length
      const list = reset ? rows.map(normalizeBill) : this.data.list.concat(rows.map(normalizeBill))
      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '欠款单据加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
