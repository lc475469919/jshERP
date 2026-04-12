const request = require('../../utils/request')
const storage = require('../../utils/storage')

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

  onShow() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    const refresh = wx.getStorageSync('Material-Refresh')
    if (refresh) {
      wx.removeStorageSync('Material-Refresh')
      this.loadList(true)
      return
    }

    const scanKeyword = wx.getStorageSync('Material-Scan-Keyword')
    if (scanKeyword) {
      wx.removeStorageSync('Material-Scan-Keyword')
      this.setData({ keyword: scanKeyword })
      this.loadList(true)
      return
    }

    if (!this.data.list.length) {
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

  createMaterial() {
    wx.navigateTo({ url: '/pages/material-edit/material-edit' })
  },

  editMaterial(event) {
    const id = event.currentTarget.dataset.id
    const material = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !material) {
      wx.showToast({ title: '商品信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Material-Edit', material)
    wx.navigateTo({ url: `/pages/material-edit/material-edit?id=${id}` })
  },

  searchByKeyword(keyword) {
    this.setData({ keyword })
    this.loadList(true)
  },

  openDetail(event) {
    const id = event.currentTarget.dataset.id
    const material = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !material) {
      wx.showToast({ title: '商品信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Material-Detail', material)
    wx.navigateTo({
      url: `/pages/material-detail/material-detail?id=${id}`
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

    this.setData({ loading: true })
    const search = JSON.stringify({
      materialParam: this.data.keyword || '',
      mpList: ''
    })

    return request.get('/material/list', {
      pageNo: this.data.pageNo,
      pageSize: this.data.pageSize,
      search
    }).then((res) => {
      const rows = res.rows || (res.data && res.data.rows) || []
      const total = res.total || (res.data && res.data.total) || rows.length
      const list = reset ? rows : this.data.list.concat(rows)

      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '商品查询失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
