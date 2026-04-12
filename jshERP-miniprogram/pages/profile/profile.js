const request = require('../../utils/request')
const storage = require('../../utils/storage')

Page({
  data: {
    user: {},
    avatarText: 'J',
    baseUrl: '',
    checking: false,
    serverStatus: '未检测',
    unreadCount: 0,
    approvalCount: 0,
    miniConfig: {},
    configLoading: false
  },

  onShow() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.loadLocalUser()
    this.loadUnreadCount()
    this.loadApprovalCount()
    this.loadMiniProgramConfig()
  },

  loadLocalUser() {
    const user = storage.getUser() || {}
    const name = user.username || user.loginName || 'J'
    this.setData({
      user,
      avatarText: name.slice(0, 1).toUpperCase(),
      baseUrl: storage.getBaseUrl() || getApp().globalData.apiBaseUrl
    })
  },

  refreshSession() {
    request.get('/user/getUserSession').then((res) => {
      if (res.code === 200 && res.data && res.data.user) {
        storage.setUser(res.data.user)
        this.loadLocalUser()
        wx.showToast({ title: '已刷新', icon: 'success' })
      } else {
        throw new Error('刷新失败')
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '刷新失败', icon: 'none' })
    })
  },

  loadUnreadCount() {
    request.get('/msg/getMsgCountByStatus', { status: '1' }).then((res) => {
      if (res && res.code === 200 && res.data) {
        this.setData({ unreadCount: res.data.count || 0 })
      }
    }).catch(() => {})
  },

  loadApprovalCount() {
    request.get('/approval/task/count').then((res) => {
      if (res && res.code === 200 && res.data) {
        this.setData({ approvalCount: res.data.count || 0 })
      }
    }).catch(() => {})
  },

  loadMiniProgramConfig() {
    const keys = [
      'miniprogram_appid',
      'miniprogram_api_base_url',
      'miniprogram_request_domain',
      'miniprogram_preview_qrcode',
      'miniprogram_remark'
    ]
    this.setData({ configLoading: true })
    Promise.all(keys.map((key) => {
      return request.get('/platformConfig/getInfoByKey', { platformKey: key }).then((res) => {
        return {
          key,
          value: res && res.code === 200 && res.data ? res.data.platformValue || '' : ''
        }
      }).catch(() => ({ key, value: '' }))
    })).then((items) => {
      const miniConfig = {}
      items.forEach((item) => {
        miniConfig[item.key] = item.value
      })
      this.setData({ miniConfig })
    }).finally(() => {
      this.setData({ configLoading: false })
    })
  },

  goMessage() {
    wx.navigateTo({ url: '/pages/message/message' })
  },

  goApproval() {
    wx.navigateTo({ url: '/pages/approval/approval' })
  },

  goBasicData() {
    wx.navigateTo({ url: '/pages/basic-data/basic-data' })
  },

  checkServer() {
    this.setData({ checking: true, serverStatus: '检测中' })
    request.get('/platformConfig/getPlatform/name').then((res) => {
      const name = typeof res === 'string' ? res : '服务正常'
      this.setData({ serverStatus: name })
      wx.showToast({ title: '连接正常', icon: 'success' })
    }).catch((err) => {
      this.setData({ serverStatus: '连接失败' })
      wx.showToast({ title: err.message || '连接失败', icon: 'none' })
    }).finally(() => {
      this.setData({ checking: false })
    })
  },

  copyBaseUrl() {
    if (!this.data.baseUrl) {
      wx.showToast({ title: '暂无服务地址', icon: 'none' })
      return
    }
    wx.setClipboardData({ data: this.data.baseUrl })
  },

  copyConfigValue(event) {
    const value = event.currentTarget.dataset.value
    if (!value) {
      wx.showToast({ title: '暂无内容', icon: 'none' })
      return
    }
    wx.setClipboardData({ data: value })
  },

  applyDesktopApiUrl() {
    const apiBaseUrl = this.data.miniConfig.miniprogram_api_base_url
    if (!apiBaseUrl) {
      wx.showToast({ title: '电脑端未配置 API 地址', icon: 'none' })
      return
    }
    wx.showModal({
      title: '应用 API 地址',
      content: `将当前小程序服务地址切换为：${apiBaseUrl}`,
      confirmText: '应用',
      success: (res) => {
        if (!res.confirm) {
          return
        }
        storage.setBaseUrl(apiBaseUrl)
        getApp().globalData.apiBaseUrl = apiBaseUrl
        this.setData({ baseUrl: apiBaseUrl })
        wx.showToast({ title: '已应用', icon: 'success' })
      }
    })
  },

  logout() {
    request.get('/user/logout').finally(() => {
      storage.clearSession()
      wx.reLaunch({ url: '/pages/login/login' })
    })
  }
})
