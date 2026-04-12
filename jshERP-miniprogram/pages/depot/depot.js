const request = require('../../utils/request')
const storage = require('../../utils/storage')

function normalizeDepot(row) {
  return Object.assign({}, row, {
    displayName: row.name || '未命名仓库',
    statusText: row.enabled === false ? '停用' : '启用',
    defaultText: row.isDefault ? '默认仓库' : '',
    typeText: row.type === 1 ? '委外仓' : '普通仓',
    principalText: row.principalName || '-',
    addressText: row.address || '-'
  })
}

Page({
  data: {
    keyword: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    this.loadList(true)
  },

  onShow() {
    const refresh = wx.getStorageSync('Depot-Refresh')
    if (refresh) {
      wx.removeStorageSync('Depot-Refresh')
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

  createDepot() {
    wx.navigateTo({ url: '/pages/depot-edit/depot-edit' })
  },

  editDepot(event) {
    const id = event.currentTarget.dataset.id
    const depot = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !depot) {
      wx.showToast({ title: '仓库信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Depot-Edit', depot)
    wx.navigateTo({ url: `/pages/depot-edit/depot-edit?id=${id}` })
  },

  setDefaultDepot(event) {
    const id = event.currentTarget.dataset.id
    if (!id) {
      return
    }
    request.post('/depot/updateIsDefault', { id }).then(() => {
      wx.setStorageSync('Depot-Refresh', '1')
      wx.showToast({ title: '已设为默认', icon: 'success' })
      this.loadList(true)
    }).catch((err) => {
      wx.showToast({ title: err.message || '设置失败', icon: 'none' })
    })
  },

  copyAddress(event) {
    const address = event.currentTarget.dataset.address
    if (!address || address === '-') {
      wx.showToast({ title: '暂无地址', icon: 'none' })
      return
    }
    wx.setClipboardData({ data: address })
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
      name: this.data.keyword || '',
      type: '',
      remark: ''
    })

    this.setData({ loading: true })
    return request.get('/depot/list', {
      pageNo: this.data.pageNo,
      pageSize: this.data.pageSize,
      search
    }).then((res) => {
      const rows = res.rows || (res.data && res.data.rows) || []
      const total = res.total || (res.data && res.data.total) || rows.length
      const normalizedRows = rows.map(normalizeDepot)
      const list = reset ? normalizedRows : this.data.list.concat(normalizedRows)
      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '仓库资料加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
