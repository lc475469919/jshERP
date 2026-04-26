Page({
  data: {
    username: '',
    password: ''
  },

  onUsernameInput(event) {
    this.setData({ username: event.detail.value })
  },

  onPasswordInput(event) {
    this.setData({ password: event.detail.value })
  },

  login() {
    wx.switchTab({
      url: '/pages/home/home'
    })
  }
})
