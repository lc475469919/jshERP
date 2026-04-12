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
    type: '收入',
    name: '',
    sort: '',
    remark: '',
    saving: false
  },

  onLoad(options) {
    const type = decodeURIComponent(options.type || '')
    const id = options.id || ''
    const cached = wx.getStorageSync('InOutItem-Edit')
    const data = { id, isEdit: !!id }
    if (type === '收入' || type === '支出') {
      data.type = type
    }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readItem(cached))
    }
    this.setData(data)
    if (id) {
      this.loadItem()
    }
  },

  readItem(info) {
    return {
      type: info.type || this.data.type,
      name: info.name || '',
      sort: info.sort === undefined || info.sort === null ? '' : String(info.sort),
      remark: info.remark || ''
    }
  },

  switchType(event) {
    const type = event.currentTarget.dataset.type
    if (type === '收入' || type === '支出') {
      this.setData({ type })
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  loadItem() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/inOutItem/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (info) {
        this.setData(this.readItem(info))
      }
    }).catch((err) => {
      wx.showToast({ title: err.message || '收支项目加载失败', icon: 'none' })
    })
  },

  saveItem() {
    if (this.data.saving) {
      return
    }
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    if (!this.data.name.trim()) {
      wx.showToast({ title: '请输入项目名称', icon: 'none' })
      return
    }

    this.setData({ saving: true })
    const payload = {
      id: this.data.id || undefined,
      type: this.data.type,
      name: this.data.name.trim(),
      sort: this.data.sort === '' ? 0 : toNumber(this.data.sort),
      remark: this.data.remark.trim(),
      enabled: true
    }
    const action = this.data.isEdit ? request.put('/inOutItem/update', payload) : request.post('/inOutItem/add', payload)
    action.then(() => {
      wx.setStorageSync('Basic-Data-Refresh', '1')
      wx.removeStorageSync('InOutItem-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
