const { request } = require('../../utils/request')

Page({
  data: {
    user: null,
    modules: [
      '系统管理',
      '基础资料',
      '采购',
      '销售',
      '库存',
      '生产',
      '人事工资'
    ]
  },

  async onShow() {
    const app = getApp()
    if (!app.globalData.token) {
      wx.redirectTo({ url: '/pages/login/login' })
      return
    }
    try {
      const payload = await request({ url: '/auth/me' })
      this.setData({ user: payload.user })
    } catch (error) {
      wx.redirectTo({ url: '/pages/login/login' })
    }
  }
})
