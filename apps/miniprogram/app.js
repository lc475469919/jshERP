App({
  onLaunch() {
    const token = wx.getStorageSync('yz_token')
    const user = wx.getStorageSync('yz_user')
    this.globalData.token = token || ''
    this.globalData.user = user || null
  },

  globalData: {
    apiBaseUrl: 'http://127.0.0.1:8088/api',
    token: '',
    user: null
  }
})
