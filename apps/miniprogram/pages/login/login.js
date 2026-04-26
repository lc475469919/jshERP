const { request, setLoginState } = require('../../utils/request')

Page({
  data: {
    username: 'admin',
    password: 'admin123',
    loading: false
  },

  onUsernameInput(event) {
    this.setData({ username: event.detail.value })
  },

  onPasswordInput(event) {
    this.setData({ password: event.detail.value })
  },

  async login() {
    if (!this.data.username || !this.data.password) {
      wx.showToast({
        title: '请输入账号和密码',
        icon: 'none'
      })
      return
    }
    this.setData({ loading: true })
    try {
      const payload = await request({
        url: '/auth/login',
        method: 'POST',
        data: {
          account: this.data.username,
          password: this.data.password
        }
      })
      setLoginState(payload)
      wx.switchTab({
        url: '/pages/home/home'
      })
    } finally {
      this.setData({ loading: false })
    }
  }
})
