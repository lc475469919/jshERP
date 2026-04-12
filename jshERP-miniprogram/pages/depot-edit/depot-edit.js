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
    principal: '',
    address: '',
    sort: '',
    warehousing: '',
    truckage: '',
    remark: '',
    saving: false
  },

  onLoad(options) {
    const id = options.id || ''
    const cached = wx.getStorageSync('Depot-Edit')
    const data = { id, isEdit: !!id }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readDepot(cached))
    }
    this.setData(data)
    if (id) {
      this.loadDepot()
    }
  },

  readDepot(info) {
    return {
      name: info.name || '',
      principal: info.principal || '',
      address: info.address || '',
      sort: info.sort === undefined || info.sort === null ? '' : String(info.sort),
      warehousing: info.warehousing === undefined || info.warehousing === null ? '' : String(info.warehousing),
      truckage: info.truckage === undefined || info.truckage === null ? '' : String(info.truckage),
      remark: info.remark || ''
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  loadDepot() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/depot/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (info) {
        this.setData(this.readDepot(info))
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '仓库加载失败', icon: 'none' })
    })
  },

  saveDepot() {
    if (this.data.saving) {
      return
    }
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (!this.data.name.trim()) {
      wx.showToast({ title: '请输入仓库名称', icon: 'none' })
      return
    }

    this.setData({ saving: true })
    const payload = {
      id: this.data.id || undefined,
      name: this.data.name.trim(),
      principal: this.data.principal.trim(),
      address: this.data.address.trim(),
      sort: this.data.sort === '' ? 0 : toNumber(this.data.sort),
      warehousing: this.data.warehousing === '' ? 0 : toNumber(this.data.warehousing),
      truckage: this.data.truckage === '' ? 0 : toNumber(this.data.truckage),
      remark: this.data.remark.trim(),
      enabled: true
    }
    const action = this.data.isEdit ? request.put('/depot/update', payload) : request.post('/depot/add', payload)
    action.then(() => {
      wx.setStorageSync('Depot-Refresh', '1')
      wx.removeStorageSync('Depot-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
