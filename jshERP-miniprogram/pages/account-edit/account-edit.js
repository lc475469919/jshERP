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
    name: '',
    serialNo: '',
    initialAmount: '',
    remark: '',
    saving: false
  },

  onLoad(options) {
    const id = options.id || ''
    const cached = wx.getStorageSync('Account-Edit')
    const data = { id, isEdit: !!id }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readAccount(cached))
    }
    this.setData(data)
    if (id) {
      this.loadAccount()
    }
  },

  readAccount(info) {
    return {
      name: info.name || '',
      serialNo: info.serialNo || '',
      initialAmount: info.initialAmount === undefined || info.initialAmount === null ? '' : String(info.initialAmount),
      remark: info.remark || ''
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  loadAccount() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/account/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (info) {
        this.setData(this.readAccount(info))
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '账户加载失败', icon: 'none' })
    })
  },

  saveAccount() {
    if (this.data.saving) {
      return
    }
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (!this.data.name.trim()) {
      wx.showToast({ title: '请输入账户名称', icon: 'none' })
      return
    }

    this.setData({ saving: true })
    const amount = this.data.initialAmount === '' ? 0 : toNumber(this.data.initialAmount)
    const payload = {
      id: this.data.id || undefined,
      name: this.data.name.trim(),
      serialNo: this.data.serialNo.trim(),
      initialAmount: amount,
      remark: this.data.remark.trim(),
      enabled: true
    }
    if (!this.data.isEdit) {
      payload.currentAmount = amount
    }
    const action = this.data.isEdit ? request.put('/account/update', payload) : request.post('/account/add', payload)
    action.then(() => {
      wx.setStorageSync('Account-Refresh', '1')
      wx.removeStorageSync('Account-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
