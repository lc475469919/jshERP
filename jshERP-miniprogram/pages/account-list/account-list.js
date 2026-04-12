const request = require('../../utils/request')
const storage = require('../../utils/storage')

function normalizeAccount(row) {
  return Object.assign({}, row, {
    displayName: row.name || '未命名账户',
    statusText: row.enabled === false ? '停用' : '启用',
    currentText: row.currentAmount || 0,
    initialText: row.initialAmount || 0,
    monthText: row.thisMonthAmount || 0
  })
}

Page({
  data: {
    keyword: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    totalAmount: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    this.loadList(true)
  },

  onShow() {
    const refresh = wx.getStorageSync('Account-Refresh')
    if (refresh) {
      wx.removeStorageSync('Account-Refresh')
      this.loadList(true)
    }
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadMore()
  },

  onKeywordInput(event) {
    this.setData({ keyword: event.detail.value })
  },

  search() {
    this.loadList(true)
  },

  createAccount() {
    wx.navigateTo({ url: '/pages/account-edit/account-edit' })
  },

  editAccount(event) {
    const id = event.currentTarget.dataset.id
    const account = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !account) {
      wx.showToast({ title: '账户信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Account-Edit', account)
    wx.navigateTo({ url: `/pages/account-edit/account-edit?id=${id}` })
  },

  setDefaultAccount(event) {
    const id = event.currentTarget.dataset.id
    if (!id) {
      return
    }
    request.post('/account/updateIsDefault', { id }).then(() => {
      wx.setStorageSync('Account-Refresh', '1')
      wx.showToast({ title: '已设为默认', icon: 'success' })
      this.loadList(true)
    }).catch((err) => {
      wx.showToast({ title: err.message || '设置失败', icon: 'none' })
    })
  },

  openFlow(event) {
    const id = event.currentTarget.dataset.id
    const account = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !account) {
      wx.showToast({ title: '账户信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Account-Flow-Filter', account)
    wx.navigateTo({ url: '/pages/account-flow/account-flow' })
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
    return request.get('/account/listWithBalance', {
      name: this.data.keyword || '',
      serialNo: '',
      pageNo: this.data.pageNo,
      pageSize: this.data.pageSize
    }).then((res) => {
      const rows = res.rows || (res.data && res.data.rows) || []
      const total = res.total || (res.data && res.data.total) || rows.length
      const normalizedRows = rows.map(normalizeAccount)
      const list = reset ? normalizedRows : this.data.list.concat(normalizedRows)
      const totalAmount = list.reduce((sum, item) => sum + Number(item.currentAmount || 0), 0)
      this.setData({
        list,
        total,
        totalAmount,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '资金账户加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
