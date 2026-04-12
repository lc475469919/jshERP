const request = require('../../utils/request')
const storage = require('../../utils/storage')

function flattenCategoryTree(nodes, level) {
  return (nodes || []).reduce((list, node) => {
    const item = Object.assign({}, node, { level })
    const children = flattenCategoryTree(node.children || [], level + 1)
    return list.concat(item, children)
  }, [])
}

Page({
  data: {
    id: '',
    isEdit: false,
    name: '',
    serialNo: '',
    sort: '',
    remark: '',
    parents: [{ id: 0, title: '顶级分类', level: -1 }],
    parentIndex: 0,
    saving: false,
    loading: false
  },

  onLoad(options) {
    const id = options.id || ''
    const cached = wx.getStorageSync('Category-Edit')
    const data = { id, isEdit: !!id }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readCategory(cached))
    }
    this.setData(data)
    this.loadParents().then(() => {
      if (id) {
        this.loadCategory()
      }
    })
  },

  readCategory(info) {
    return {
      name: info.name || info.title || '',
      serialNo: info.serialNo || '',
      sort: info.sort || '',
      remark: info.remark || ''
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  onParentChange(event) {
    this.setData({ parentIndex: Number(event.detail.value) })
  },

  loadParents() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }
    this.setData({ loading: true })
    return request.get('/materialCategory/getMaterialCategoryTree', { id: 0 }).then((res) => {
      const rows = flattenCategoryTree(Array.isArray(res) ? res : [], 0)
      const parents = [{ id: 0, title: '顶级分类', level: -1 }].concat(rows.map((row) => {
        const prefix = new Array(row.level + 1).join('　')
        return Object.assign({}, row, {
          title: `${prefix}${row.title || row.name || '未命名分类'}`
        })
      }))
      this.setData({ parents })
    }).catch((err) => {
      wx.showToast({ title: err.message || '分类加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  loadCategory() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/materialCategory/info', { id: this.data.id }).then((res) => {
      const info = res && res.data && res.data.info ? res.data.info : null
      if (!info) {
        return
      }
      const parentIndex = this.data.parents.findIndex((parent) => String(parent.id || 0) === String(info.parentId || 0))
      this.setData(Object.assign(this.readCategory(info), {
        parentIndex: parentIndex >= 0 ? parentIndex : 0
      }))
    }).catch((err) => {
      wx.showToast({ title: err.message || '商品分类加载失败', icon: 'none' })
    })
  },

  validate() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return '登录已过期'
    }
    if (!this.data.name.trim()) {
      return '请输入分类名称'
    }
    if (!this.data.serialNo.trim()) {
      return '请输入分类编号'
    }
    return ''
  },

  saveCategory() {
    if (this.data.saving) {
      return
    }
    const error = this.validate()
    if (error) {
      wx.showToast({ title: error, icon: 'none' })
      return
    }

    const parent = this.data.parents[this.data.parentIndex] || this.data.parents[0]
    const payload = {
      id: this.data.id || undefined,
      name: this.data.name.trim(),
      serialNo: this.data.serialNo.trim(),
      parentId: parent.id || 0,
      categoryLevel: Number(parent.level || 0) + 2,
      sort: this.data.sort.trim(),
      remark: this.data.remark.trim()
    }

    this.setData({ saving: true })
    const action = this.data.isEdit ? request.put('/materialCategory/update', payload) : request.post('/materialCategory/add', payload)
    action.then(() => {
      wx.setStorageSync('Basic-Data-Refresh', '1')
      wx.setStorageSync('Category-Refresh', '1')
      wx.removeStorageSync('Category-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
