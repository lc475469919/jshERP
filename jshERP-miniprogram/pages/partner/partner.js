const request = require('../../utils/request')
const storage = require('../../utils/storage')

Page({
  data: {
    type: '客户',
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

    const refresh = wx.getStorageSync('Partner-Refresh')
    if (refresh) {
      wx.removeStorageSync('Partner-Refresh')
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

  changeType(event) {
    const type = event.currentTarget.dataset.type
    if (type === this.data.type) {
      return
    }
    this.setData({ type, keyword: '' })
    this.loadList(true)
  },

  onKeywordInput(event) {
    this.setData({ keyword: event.detail.value })
  },

  search() {
    this.loadList(true)
  },

  createPartner() {
    wx.navigateTo({
      url: `/pages/partner-edit/partner-edit?type=${encodeURIComponent(this.data.type)}`
    })
  },

  openDetail(event) {
    const id = event.currentTarget.dataset.id
    const partner = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !partner) {
      wx.showToast({ title: '往来单位信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Partner-Detail', partner)
    wx.navigateTo({
      url: `/pages/partner-detail/partner-detail?id=${id}`
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
    const keyword = this.data.keyword || ''
    const search = JSON.stringify({
      supplier: keyword,
      contacts: '',
      telephone: '',
      phonenum: '',
      type: this.data.type
    })

    return request.get('/supplier/list', {
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
      wx.showToast({ title: err.message || '查询失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
