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
    type: '客户',
    supplier: '',
    contacts: '',
    telephone: '',
    phoneNum: '',
    email: '',
    taxRate: '',
    description: '',
    saving: false
  },

  onLoad(options) {
    const type = decodeURIComponent(options.type || '')
    const id = options.id || ''
    const cached = wx.getStorageSync('Partner-Edit')
    const data = { id, isEdit: !!id }
    if (type === '客户' || type === '供应商') {
      data.type = type
    }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readPartner(cached))
    }
    this.setData(data)
    if (id) {
      this.loadPartner()
    }
  },

  readPartner(info) {
    return {
      type: info.type || this.data.type,
      supplier: info.supplier || '',
      contacts: info.contacts || '',
      telephone: info.telephone || '',
      phoneNum: info.phoneNum || '',
      email: info.email || '',
      taxRate: info.taxRate === undefined || info.taxRate === null ? '' : String(info.taxRate),
      description: info.description || info.remark || ''
    }
  },

  switchType(event) {
    const type = event.currentTarget.dataset.type
    if (type === '客户' || type === '供应商') {
      this.setData({ type })
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (!field) {
      return
    }
    this.setData({ [field]: event.detail.value })
  },

  loadPartner() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/supplier/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (info) {
        this.setData(this.readPartner(info))
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '往来单位加载失败', icon: 'none' })
    })
  },

  validate() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return '登录已过期'
    }
    if (!this.data.supplier.trim()) {
      return '请输入单位名称'
    }
    const taxRate = this.data.taxRate === '' ? 0 : toNumber(this.data.taxRate)
    if (taxRate < 0 || taxRate > 100) {
      return '税率需在0到100之间'
    }
    return ''
  },

  savePartner() {
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
      type: this.data.type,
      supplier: this.data.supplier.trim(),
      contacts: this.data.contacts.trim(),
      telephone: this.data.telephone.trim(),
      phoneNum: this.data.phoneNum.trim(),
      email: this.data.email.trim(),
      taxRate: this.data.taxRate === '' ? 0 : toNumber(this.data.taxRate),
      description: this.data.description.trim(),
      enabled: true
    }

    this.setData({ saving: true })
    const action = this.data.isEdit ? request.put('/supplier/update', payload) : request.post('/supplier/add', payload)
    action.then(() => {
      wx.setStorageSync('Partner-Refresh', '1')
      wx.removeStorageSync('Partner-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
