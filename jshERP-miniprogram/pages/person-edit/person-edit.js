const request = require('../../utils/request')
const storage = require('../../utils/storage')

Page({
  data: {
    id: '',
    isEdit: false,
    type: '销售员',
    name: '',
    sort: '',
    remark: '',
    saving: false
  },

  onLoad(options) {
    const type = decodeURIComponent(options.type || '')
    const id = options.id || ''
    const cached = wx.getStorageSync('Person-Edit')
    const data = { id, isEdit: !!id }
    if (['销售员', '仓管员', '财务员'].indexOf(type) > -1) {
      data.type = type
    }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readPerson(cached))
    }
    this.setData(data)
    if (id) {
      this.loadPerson()
    }
  },

  readPerson(info) {
    return {
      type: info.type || this.data.type,
      name: info.name || '',
      sort: info.sort === undefined || info.sort === null ? '' : String(info.sort),
      remark: info.remark || ''
    }
  },

  switchType(event) {
    const type = event.currentTarget.dataset.type
    if (['销售员', '仓管员', '财务员'].indexOf(type) > -1) {
      this.setData({ type })
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  loadPerson() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/person/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (info) {
        this.setData(this.readPerson(info))
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '经手人加载失败', icon: 'none' })
    })
  },

  savePerson() {
    if (this.data.saving) {
      return
    }
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (!this.data.name.trim()) {
      wx.showToast({ title: '请输入姓名', icon: 'none' })
      return
    }
    const sort = Number(this.data.sort)
    this.setData({ saving: true })
    const payload = {
      id: this.data.id || undefined,
      type: this.data.type,
      name: this.data.name.trim(),
      sort: Number.isFinite(sort) ? sort : 0,
      remark: this.data.remark.trim(),
      enabled: true
    }
    const action = this.data.isEdit ? request.put('/person/update', payload) : request.post('/person/add', payload)
    action.then(() => {
      wx.setStorageSync('Basic-Data-Refresh', '1')
      wx.setStorageSync('Person-Refresh', '1')
      wx.removeStorageSync('Person-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
