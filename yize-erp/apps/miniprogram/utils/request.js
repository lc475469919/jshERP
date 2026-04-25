const app = getApp()

function request(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.apiBaseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'content-type': 'application/json',
        ...(options.header || {})
      },
      success(res) {
        resolve(res.data)
      },
      fail(err) {
        reject(err)
      }
    })
  })
}

module.exports = {
  request
}
