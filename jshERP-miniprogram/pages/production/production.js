const request = require('../../utils/request')
const storage = require('../../utils/storage')

const TABS = [
  { label: 'BOM', value: 'bom', path: '/production/bom/list' },
  { label: '生产单', value: 'order', path: '/production/order/list' }
]

Page({
  data: {
    tabs: TABS,
    activeTab: 'bom',
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

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadMore()
  },

  changeTab(event) {
    const activeTab = event.currentTarget.dataset.type
    if (activeTab === this.data.activeTab) {
      return
    }
    this.setData({ activeTab, keyword: '' })
    this.loadList(true)
  },

  onKeywordInput(event) {
    this.setData({ keyword: event.detail.value })
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
    if (reset) {
      this.setData({ pageNo: 1, list: [] })
    }

    const tab = TABS.find((item) => item.value === this.data.activeTab) || TABS[0]
    const search = JSON.stringify({
      keyword: this.data.keyword || '',
      status: ''
    })

    this.setData({ loading: true })
    return request.get(tab.path, {
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
      wx.showToast({ title: err.message || '生产数据查询失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
