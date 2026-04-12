const storage = require('./storage')
const { DEFAULT_API_BASE_URL } = require('./config')

function getBaseUrl() {
  const app = getApp()
  return (app && app.globalData && app.globalData.apiBaseUrl) || storage.getBaseUrl() || DEFAULT_API_BASE_URL
}

function request(options) {
  const token = storage.getToken()
  const header = Object.assign({
    'content-type': 'application/json'
  }, options.header || {})

  if (token) {
    header['X-Access-Token'] = token
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${getBaseUrl()}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      timeout: options.timeout || 30000,
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          if (res.data === 'loginOut') {
            storage.clearSession()
            wx.reLaunch({ url: '/pages/login/login' })
            reject(new Error('登录已过期'))
            return
          }
          if (res.data && typeof res.data === 'object' && res.data.code && res.data.code !== 200) {
            const message = typeof res.data.data === 'string' ? res.data.data : res.data.message
            reject(new Error(message || `请求失败：${res.data.code}`))
            return
          }
          resolve(res.data)
          return
        }

        if (res.statusCode === 500 && res.data === 'loginOut') {
          storage.clearSession()
          wx.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }

        reject(new Error((res.data && res.data.message) || `请求失败：${res.statusCode}`))
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络请求失败'))
      }
    })
  })
}

function get(url, data) {
  return request({ url, method: 'GET', data })
}

function post(url, data) {
  return request({ url, method: 'POST', data })
}

function put(url, data) {
  return request({ url, method: 'PUT', data })
}

function del(url, data) {
  const query = data ? Object.keys(data).filter((key) => data[key] !== undefined && data[key] !== null).map((key) => {
    return `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`
  }).join('&') : ''
  return request({ url: query ? `${url}?${query}` : url, method: 'DELETE' })
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  getBaseUrl
}
