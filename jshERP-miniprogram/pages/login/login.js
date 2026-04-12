const md5 = require('../../utils/md5')
const request = require('../../utils/request')
const storage = require('../../utils/storage')
const { DEFAULT_API_BASE_URL } = require('../../utils/config')

Page({
  data: {
    platformName: '管伊佳ERP',
    baseUrl: DEFAULT_API_BASE_URL,
    loginName: 'jsh',
    password: '',
    code: '',
    uuid: '',
    captchaImage: '',
    checkcodeFlag: '0',
    loading: false
  },

  onLoad() {
    const savedBaseUrl = storage.getBaseUrl()
    if (savedBaseUrl) {
      this.setData({ baseUrl: savedBaseUrl })
      getApp().globalData.apiBaseUrl = savedBaseUrl
    }

    if (storage.getToken()) {
      wx.switchTab({ url: '/pages/home/home' })
      return
    }

    this.loadCheckcodeFlag()
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    this.setData({ [field]: event.detail.value })
  },

  submitLogin() {
    const baseUrl = this.data.baseUrl.trim()
    const loginName = this.data.loginName.trim()
    const password = this.data.password

    if (!baseUrl || !loginName || !password) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }

    storage.setBaseUrl(baseUrl)
    getApp().globalData.apiBaseUrl = baseUrl

    if (this.data.checkcodeFlag === '1' && !this.data.code.trim()) {
      wx.showToast({ title: '请输入验证码', icon: 'none' })
      return
    }

    this.setData({ loading: true })

    request.post('/user/login', {
      loginName,
      password: md5(password),
      code: this.data.code.trim(),
      uuid: this.data.uuid
    }).then((res) => {
      if (res.code !== 200 || !res.data) {
        const message = typeof res.data === 'string' ? res.data : (res.data && res.data.message)
        throw new Error(message || '登录失败')
      }

      const data = res.data
      if (data.msgTip && data.msgTip !== 'user can login') {
        throw new Error(this.getLoginMessage(data.msgTip))
      }

      storage.setToken(data.token)
      storage.setUser(data.user || {})
      if (data.user && data.user.id) {
        storage.setUserId(data.user.id)
      }

      wx.showToast({ title: '登录成功', icon: 'success' })
      wx.switchTab({ url: '/pages/home/home' })
    }).catch((err) => {
      wx.showToast({ title: err.message || '登录失败', icon: 'none' })
      if (this.data.checkcodeFlag === '1') {
        this.loadCaptcha()
      }
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  loadCheckcodeFlag() {
    request.get('/platformConfig/getPlatform/checkcodeFlag').then((res) => {
      const flag = `${res}`
      this.setData({ checkcodeFlag: flag })
      if (flag === '1') {
        this.loadCaptcha()
      }
    }).catch(() => {
      this.setData({ checkcodeFlag: '0' })
    })
  },

  loadCaptcha() {
    request.get('/user/randomImage').then((res) => {
      if (res.code === 200 && res.data) {
        this.setData({
          uuid: res.data.uuid,
          captchaImage: res.data.base64,
          code: ''
        })
      } else {
        throw new Error('验证码加载失败')
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '验证码加载失败', icon: 'none' })
    })
  },

  getLoginMessage(msgTip) {
    const map = {
      'user already login': '用户已登录',
      'user is black': '账号已停用',
      'user password error': '账号或密码错误',
      'access service error': '服务访问异常'
    }
    return map[msgTip] || '登录失败'
  }
})
