const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.apiBaseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'content-type': 'application/json',
        Authorization: app.globalData.token || '',
        ...(options.header || {})
      },
      success(res) {
        const payload = res.data || {}
        if (payload.code && payload.code !== 200) {
          wx.showToast({
            title: payload.message || '请求失败',
            icon: 'none'
          })
          reject(payload)
          return
        }
        resolve(payload.data === undefined ? payload : payload.data)
      },
      fail(err) {
        wx.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

function setLoginState(payload) {
  app.globalData.token = payload.tokenValue
  app.globalData.user = payload.user
  wx.setStorageSync('yz_token', payload.tokenValue)
  wx.setStorageSync('yz_user', payload.user)
}

function clearLoginState() {
  app.globalData.token = ''
  app.globalData.user = null
  wx.removeStorageSync('yz_token')
  wx.removeStorageSync('yz_user')
}

module.exports = {
  request,
  setLoginState,
  clearLoginState
}
