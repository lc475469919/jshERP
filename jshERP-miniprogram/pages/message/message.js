const request = require('../../utils/request')
const storage = require('../../utils/storage')

function normalizeMessage(row) {
  return Object.assign({}, row, {
    titleText: row.msgTitle || '未命名消息',
    contentText: row.msgContent || '',
    timeText: row.createTimeStr || row.createTime || '',
    typeText: row.type || '通知'
  })
}

Page({
  data: {
    status: '1',
    unreadCount: 0,
    readCount: 0,
    list: [],
    loading: false,
    processingId: '',
    processingAll: false
  },

  onShow() {
    this.loadAll()
  },

  onPullDownRefresh() {
    this.loadAll().finally(() => wx.stopPullDownRefresh())
  },

  changeStatus(event) {
    const status = event.currentTarget.dataset.status
    if (status === this.data.status) {
      return
    }
    this.setData({ status })
    this.loadList()
  },

  loadAll() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }
    return Promise.all([
      this.loadCounts(),
      this.loadList()
    ])
  },

  loadCounts() {
    return Promise.all([
      request.get('/msg/getMsgCountByStatus', { status: '1' }),
      request.get('/msg/getMsgCountByStatus', { status: '2' })
    ]).then(([unreadRes, readRes]) => {
      this.setData({
        unreadCount: unreadRes && unreadRes.code === 200 && unreadRes.data ? unreadRes.data.count || 0 : 0,
        readCount: readRes && readRes.code === 200 && readRes.data ? readRes.data.count || 0 : 0
      })
    }).catch(() => {})
  },

  loadList() {
    this.setData({ loading: true })
    return request.get('/msg/getMsgByStatus', { status: this.data.status }).then((res) => {
      const rows = res && res.code === 200 ? res.data || [] : []
      this.setData({ list: rows.map(normalizeMessage) })
    }).catch((err) => {
      wx.showToast({ title: err.message || '消息加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  markRead(event) {
    const id = event.currentTarget.dataset.id
    if (!id || this.data.processingId) {
      return
    }
    this.setData({ processingId: id })
    request.post('/msg/batchUpdateStatus', {
      ids: id,
      status: '2'
    }).then(() => {
      wx.showToast({ title: '已标记已读', icon: 'success' })
      return this.loadAll()
    }).catch((err) => {
      wx.showToast({ title: err.message || '操作失败', icon: 'none' })
    }).finally(() => {
      this.setData({ processingId: '' })
    })
  },

  readAll() {
    if (this.data.processingAll || !this.data.unreadCount) {
      return
    }
    wx.showModal({
      title: '全部已读',
      content: '将所有未读消息标记为已读。',
      confirmText: '标记',
      success: (res) => {
        if (!res.confirm) {
          return
        }
        this.setData({ processingAll: true })
        request.post('/msg/readAllMsg', {}).then(() => {
          wx.showToast({ title: '已全部已读', icon: 'success' })
          this.setData({ status: '2' })
          return this.loadAll()
        }).catch((err) => {
          wx.showToast({ title: err.message || '操作失败', icon: 'none' })
        }).finally(() => {
          this.setData({ processingAll: false })
        })
      }
    })
  }
})
