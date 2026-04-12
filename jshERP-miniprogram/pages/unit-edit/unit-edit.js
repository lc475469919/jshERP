const request = require('../../utils/request')
const storage = require('../../utils/storage')

function toNumber(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

Page({
  data: {
    id: '',
    isEdit: false,
    basicUnit: '',
    otherUnit: '',
    ratio: '',
    otherUnitTwo: '',
    ratioTwo: '',
    otherUnitThree: '',
    ratioThree: '',
    saving: false
  },

  onLoad(options) {
    const id = options.id || ''
    const cached = wx.getStorageSync('Unit-Edit')
    const data = { id, isEdit: !!id }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readUnit(cached))
    }
    this.setData(data)
    if (id) {
      this.loadUnit()
    }
  },

  readUnit(info) {
    return {
      basicUnit: info.basicUnit || '',
      otherUnit: info.otherUnit || '',
      ratio: info.ratio === undefined || info.ratio === null ? '' : String(info.ratio),
      otherUnitTwo: info.otherUnitTwo || '',
      ratioTwo: info.ratioTwo === undefined || info.ratioTwo === null ? '' : String(info.ratioTwo),
      otherUnitThree: info.otherUnitThree || '',
      ratioThree: info.ratioThree === undefined || info.ratioThree === null ? '' : String(info.ratioThree)
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  loadUnit() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/unit/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (info) {
        this.setData(this.readUnit(info))
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '计量单位加载失败', icon: 'none' })
    })
  },

  validate() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return '登录已过期'
    }
    if (!this.data.basicUnit.trim()) {
      return '请输入基础单位'
    }
    if (!this.data.otherUnit.trim()) {
      return '请输入换算单位'
    }
    if (toNumber(this.data.ratio) <= 0) {
      return '请输入换算比例'
    }
    if (this.data.otherUnitTwo.trim() && toNumber(this.data.ratioTwo) <= 0) {
      return '请输入第二换算比例'
    }
    if (this.data.otherUnitThree.trim() && toNumber(this.data.ratioThree) <= 0) {
      return '请输入第三换算比例'
    }
    return ''
  },

  saveUnit() {
    if (this.data.saving) {
      return
    }
    const error = this.validate()
    if (error) {
      wx.showToast({ title: error, icon: 'none' })
      return
    }

    const payload = {
      id: this.data.id || undefined,
      basicUnit: this.data.basicUnit.trim(),
      otherUnit: this.data.otherUnit.trim(),
      ratio: toNumber(this.data.ratio),
      otherUnitTwo: this.data.otherUnitTwo.trim(),
      ratioTwo: this.data.otherUnitTwo.trim() ? toNumber(this.data.ratioTwo) : null,
      otherUnitThree: this.data.otherUnitThree.trim(),
      ratioThree: this.data.otherUnitThree.trim() ? toNumber(this.data.ratioThree) : null,
      enabled: true
    }

    this.setData({ saving: true })
    const action = this.data.isEdit ? request.put('/unit/update', payload) : request.post('/unit/add', payload)
    action.then(() => {
      wx.setStorageSync('Basic-Data-Refresh', '1')
      wx.setStorageSync('Unit-Refresh', '1')
      wx.removeStorageSync('Unit-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
