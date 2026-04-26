const { request } = require('../../utils/request')

Page({
  data: {
    loading: false,
    keyword: '',
    rows: [],
    statusText: {
      DRAFT: '草稿',
      CONFIRMED: '已确认',
      CANCELLED: '已作废'
    }
  },

  onLoad() {
    this.loadIssues()
  },

  onPullDownRefresh() {
    this.loadIssues().finally(() => wx.stopPullDownRefresh())
  },

  onKeywordInput(event) {
    this.setData({
      keyword: event.detail.value
    })
  },

  search() {
    this.loadIssues()
  },

  async loadIssues() {
    this.setData({ loading: true })
    try {
      const result = await request({
        url: '/manufacturing/material-issues',
        data: {
          pageNo: 1,
          pageSize: 30,
          keyword: this.data.keyword
        }
      })
      const rows = result.data ? result.data.rows : []
      this.setData({
        rows: rows.map((item) => ({
          ...item,
          statusName: this.data.statusText[item.status] || item.status
        }))
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})
