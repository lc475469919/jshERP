const TOKEN_KEY = 'Access-Token'
const USER_KEY = 'User-Info'
const USER_ID_KEY = 'Login_Userid'
const BASE_URL_KEY = 'Api-Base-Url'

function get(key) {
  return wx.getStorageSync(key)
}

function set(key, value) {
  wx.setStorageSync(key, value)
}

function remove(key) {
  wx.removeStorageSync(key)
}

function clearSession() {
  remove(TOKEN_KEY)
  remove(USER_KEY)
  remove(USER_ID_KEY)
}

module.exports = {
  TOKEN_KEY,
  USER_KEY,
  USER_ID_KEY,
  BASE_URL_KEY,
  getToken: () => get(TOKEN_KEY),
  setToken: (token) => set(TOKEN_KEY, token),
  getUser: () => get(USER_KEY),
  setUser: (user) => set(USER_KEY, user),
  getUserId: () => get(USER_ID_KEY),
  setUserId: (userId) => set(USER_ID_KEY, userId),
  getBaseUrl: () => get(BASE_URL_KEY),
  setBaseUrl: (baseUrl) => set(BASE_URL_KEY, baseUrl),
  clearSession
}
