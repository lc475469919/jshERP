const { request } = require('../../utils/request')

Page({
  data: {
    loading: false,
    keyword: '',
    rows: [],
    statusText: {
      DRAFT: '草稿',
      CONFIRMED: '已确认',
      IN_PROGRESS: '生产中',
      FINISHED: '已完工',
      CANCELLED: '已作废'
    }
  },

  onLoad() {
    this.loadTasks()
  },

  onPullDownRefresh() {
    this.loadTasks().finally(() => wx.stopPullDownRefresh())
  },

  onKeywordInput(event) {
    this.setData({
      keyword: event.detail.value
    })
  },

  search() {
    this.loadTasks()
  },

  async loadTasks() {
    this.setData({ loading: true })
    try {
      const result = await request({
        url: '/manufacturing/tasks',
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
