const { clearLoginState, request } = require('../../utils/request')

Page({
  data: {
    user: null
  },

  onShow() {
    const app = getApp()
    this.setData({ user: app.globalData.user })
  },

  async logout() {
    try {
      await request({
        url: '/auth/logout',
        method: 'POST'
      })
    } catch (error) {
      // 本地退出优先，服务端失败不阻断用户重新登录。
    }
    clearLoginState()
    wx.redirectTo({ url: '/pages/login/login' })
  }
})
