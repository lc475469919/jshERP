const request = require('../../utils/request')
const storage = require('../../utils/storage')

function normalizePartner(info) {
  return Object.assign({}, info, {
    displayName: info.supplier || '未命名单位',
    statusText: info.enabled === false ? '停用' : '启用',
    phoneText: info.telephone || info.phoneNum || '-',
    needGetText: info.allNeedGet || info.beginNeedGet || 0,
    needPayText: info.allNeedPay || info.beginNeedPay || 0,
    advanceInText: info.advanceIn || 0,
    taxRateText: info.taxRate || 0
  })
}

Page({
  data: {
    id: '',
    partner: {},
    loading: false
  },

  onLoad(options) {
    const id = options.id || ''
    const cached = wx.getStorageSync('Partner-Detail')
    this.setData({
      id,
      partner: cached && String(cached.id) === String(id) ? normalizePartner(cached) : {}
    })
    this.loadDetail()
  },

  onPullDownRefresh() {
    this.loadDetail().finally(() => wx.stopPullDownRefresh())
  },

  loadDetail() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }

    if (!this.data.id) {
      wx.showToast({ title: '往来单位信息不完整', icon: 'none' })
      return Promise.resolve()
    }

    this.setData({ loading: true })
    return request.get('/supplier/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (info) {
        this.setData({ partner: normalizePartner(Object.assign({}, this.data.partner, info)) })
      }
    }).catch((err) => {
      if (!this.data.partner.id) {
        wx.showToast({ title: err.message || '往来详情加载失败', icon: 'none' })
      }
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  callPhone() {
    const phone = this.data.partner.phoneText
    if (!phone || phone === '-') {
      wx.showToast({ title: '暂无联系电话', icon: 'none' })
      return
    }
    wx.makePhoneCall({ phoneNumber: phone })
  },

  copyAddress() {
    const address = this.data.partner.address
    if (!address) {
      wx.showToast({ title: '暂无地址', icon: 'none' })
      return
    }
    wx.setClipboardData({ data: address })
  },

  editPartner() {
    if (!this.data.id) {
      return
    }
    wx.setStorageSync('Partner-Edit', this.data.partner)
    wx.navigateTo({ url: `/pages/partner-edit/partner-edit?id=${this.data.id}` })
  }
})
