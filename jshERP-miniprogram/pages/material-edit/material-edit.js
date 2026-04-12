const request = require('../../utils/request')
const storage = require('../../utils/storage')

function toNumber(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

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
    meId: '',
    isEdit: false,
    name: '',
    barCode: '',
    unit: '',
    units: [],
    unitIndex: -1,
    categories: [{ id: 0, title: '未分类' }],
    categoryIndex: 0,
    standard: '',
    model: '',
    color: '',
    purchaseDecimal: '',
    commodityDecimal: '',
    wholesaleDecimal: '',
    lowDecimal: '',
    remark: '',
    saving: false
  },

  onLoad(options) {
    const id = options.id || ''
    const cached = wx.getStorageSync('Material-Edit')
    const data = { id, isEdit: !!id }
    if (id && cached && String(cached.id) === String(id)) {
      Object.assign(data, this.readMaterial(cached))
    }
    this.setData(data)
    this.loadOptions().then(() => {
      this.alignSelectedOptions()
      if (id) {
        this.loadMaterial()
      }
    })
  },

  onShow() {
    if (wx.getStorageSync('Unit-Refresh')) {
      wx.removeStorageSync('Unit-Refresh')
      this.loadUnits().then(() => this.alignSelectedOptions())
    }
    if (wx.getStorageSync('Category-Refresh')) {
      wx.removeStorageSync('Category-Refresh')
      this.loadCategories().then(() => this.alignSelectedOptions())
    }
  },

  readMaterial(info) {
    return {
      name: info.name || '',
      barCode: info.mBarCode || info.barCode || '',
      unit: info.commodityUnit || info.unit || info.unitName || '',
      unitIndex: -1,
      categoryIndex: 0,
      standard: info.standard || '',
      model: info.model || '',
      color: info.color || '',
      purchaseDecimal: info.purchaseDecimal === undefined || info.purchaseDecimal === null ? '' : String(info.purchaseDecimal),
      commodityDecimal: info.commodityDecimal === undefined || info.commodityDecimal === null ? '' : String(info.commodityDecimal),
      wholesaleDecimal: info.wholesaleDecimal === undefined || info.wholesaleDecimal === null ? '' : String(info.wholesaleDecimal),
      lowDecimal: info.lowDecimal === undefined || info.lowDecimal === null ? '' : String(info.lowDecimal),
      remark: info.remark || '',
      meId: info.meId || '',
      unitId: info.unitId || '',
      categoryId: info.categoryId || ''
    }
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  scanBarCode() {
    wx.scanCode({
      success: (res) => {
        this.setData({ barCode: res.result || '' })
      }
    })
  },

  onUnitChange(event) {
    const unitIndex = Number(event.detail.value)
    const unit = this.data.units[unitIndex]
    this.setData({
      unitIndex,
      unit: unit ? unit.basicUnit : this.data.unit
    })
  },

  onCategoryChange(event) {
    this.setData({ categoryIndex: Number(event.detail.value) })
  },

  createUnit() {
    wx.navigateTo({ url: '/pages/unit-edit/unit-edit' })
  },

  createCategory() {
    wx.navigateTo({ url: '/pages/category-edit/category-edit' })
  },

  loadOptions() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }
    return Promise.all([
      this.loadUnits(),
      this.loadCategories()
    ])
  },

  loadUnits() {
    const search = JSON.stringify({ name: this.data.unit || '' })
    return request.get('/unit/list', {
      pageNo: 1,
      pageSize: 200,
      search
    }).then((res) => {
      const units = res.rows || (res.data && res.data.rows) || []
      this.setData({
        units,
        unitIndex: units.length ? Math.max(this.data.unitIndex, -1) : -1
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '计量单位加载失败', icon: 'none' })
    })
  },

  loadCategories() {
    return request.get('/materialCategory/getMaterialCategoryTree', { id: 0 }).then((res) => {
      const rows = flattenCategoryTree(Array.isArray(res) ? res : [], 0)
      const categories = [{ id: 0, title: '未分类', level: -1 }].concat(rows.map((row) => {
        const prefix = new Array(row.level + 1).join('　')
        return Object.assign({}, row, {
          title: `${prefix}${row.title || row.name || '未命名分类'}`
        })
      }))
      this.setData({
        categories,
        categoryIndex: Math.min(this.data.categoryIndex, categories.length - 1)
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '商品分类加载失败', icon: 'none' })
    })
  },

  alignSelectedOptions() {
    const unitIndex = this.data.units.findIndex((item) => String(item.id) === String(this.data.materialUnitId || this.data.unitId || ''))
    const categoryIndex = this.data.categories.findIndex((item) => String(item.id) === String(this.data.categoryId || ''))
    this.setData({
      unitIndex: unitIndex >= 0 ? unitIndex : this.data.unitIndex,
      categoryIndex: categoryIndex >= 0 ? categoryIndex : this.data.categoryIndex
    })
  },

  loadMaterial() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    request.get('/material/findById', { id: this.data.id }).then((res) => {
      const info = res && res.code === 200 && res.data && res.data.length ? res.data[0] : null
      if (!info) {
        return
      }
      const next = this.readMaterial(info)
      next.unitId = info.unitId || ''
      next.categoryId = info.categoryId || ''
      this.setData(next)
      this.alignSelectedOptions()
    }).catch((err) => {
      wx.showToast({ title: err.message || '商品加载失败', icon: 'none' })
    })
  },

  validate() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return '登录已过期'
    }
    if (!this.data.name.trim()) {
      return '请输入商品名称'
    }
    if (!this.data.barCode.trim()) {
      return '请输入商品条码'
    }
    if (!this.data.unit.trim()) {
      return '请输入单位'
    }
    if (this.data.isEdit && !this.data.meId) {
      return '商品条码信息未加载'
    }
    return ''
  },

  saveMaterial() {
    if (this.data.saving) {
      return
    }
    const error = this.validate()
    if (error) {
      wx.showToast({ title: error, icon: 'none' })
      return
    }

    const meInfo = {
      id: this.data.meId || String(Date.now()),
      barCode: this.data.barCode.trim(),
      commodityUnit: this.data.unit.trim(),
      sku: '',
      purchaseDecimal: this.data.purchaseDecimal === '' ? '' : toNumber(this.data.purchaseDecimal),
      commodityDecimal: this.data.commodityDecimal === '' ? '' : toNumber(this.data.commodityDecimal),
      wholesaleDecimal: this.data.wholesaleDecimal === '' ? '' : toNumber(this.data.wholesaleDecimal),
      lowDecimal: this.data.lowDecimal === '' ? '' : toNumber(this.data.lowDecimal)
    }

    const selectedUnit = this.data.unitIndex >= 0 ? this.data.units[this.data.unitIndex] : null
    const selectedCategory = this.data.categories[this.data.categoryIndex] || this.data.categories[0]
    const payload = {
      id: this.data.id || undefined,
      name: this.data.name.trim(),
      categoryId: selectedCategory && selectedCategory.id ? selectedCategory.id : null,
      standard: this.data.standard.trim(),
      model: this.data.model.trim(),
      color: this.data.color.trim(),
      unitId: selectedUnit ? selectedUnit.id : null,
      remark: this.data.remark.trim(),
      enabled: true,
      enableSerialNumber: '0',
      enableBatchNumber: '0',
      manySku: '',
      meList: [meInfo],
      meDeleteIdList: [],
      sortList: '',
      stock: []
    }
    if (this.data.isEdit) {
      payload.sortList = JSON.stringify([{ id: this.data.meId, defaultFlag: '1' }])
    }

    this.setData({ saving: true })
    const action = this.data.isEdit ? request.put('/material/update', payload) : request.post('/material/add', payload)
    action.then(() => {
      wx.setStorageSync('Material-Refresh', '1')
      wx.removeStorageSync('Material-Edit')
      wx.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 600)
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false })
    })
  }
})
