const request = require('../../utils/request')
const storage = require('../../utils/storage')

function normalizeMaterial(info) {
  return Object.assign({}, info, {
    displayName: info.name || '未命名商品',
    statusText: info.enabled === false ? '停用' : '启用',
    barCodeText: info.mBarCode || info.barCode || '-',
    unitText: info.unit || info.unitName || info.commodityUnit || '-',
    stockText: info.stock || info.currentStock || 0,
    purchaseText: info.purchaseDecimal || '-',
    saleText: info.wholesaleDecimal || '-',
    retailText: info.commodityDecimal || '-',
    lowText: info.lowDecimal || '-',
    specText: [info.standard, info.model, info.color].filter(Boolean).join(' / '),
    skuText: info.sku || '',
    categoryText: info.categoryName || '-'
  })
}

function normalizeDepotStock(row) {
  return Object.assign({}, row, {
    displayName: row.name || '未命名仓库',
    currentText: row.currentStock || 0,
    initText: row.initStock || 0,
    lowText: row.lowSafeStock === undefined || row.lowSafeStock === null ? '-' : row.lowSafeStock,
    highText: row.highSafeStock === undefined || row.highSafeStock === null ? '-' : row.highSafeStock
  })
}

Page({
  data: {
    id: '',
    material: {},
    depotStocks: [],
    loading: false
  },

  onLoad(options) {
    const id = options.id || ''
    const cached = wx.getStorageSync('Material-Detail')
    this.setData({
      id,
      material: cached && String(cached.id) === String(id) ? normalizeMaterial(cached) : {}
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
      wx.showToast({ title: '商品信息不完整', icon: 'none' })
      return Promise.resolve()
    }

    this.setData({ loading: true })
    const detailRequest = request.get('/material/findById', { id: this.data.id })
    const stockRequest = request.get('/depot/getAllListWithStock', { mId: this.data.id })

    return Promise.all([detailRequest, stockRequest]).then(([res, stockRes]) => {
      if (res && res.code === 200 && res.data && res.data.length) {
        const current = this.data.material || {}
        const detail = Object.assign({}, current, res.data[0])
        this.setData({ material: normalizeMaterial(detail) })
      }

      if (stockRes && stockRes.code === 200 && stockRes.data) {
        this.setData({ depotStocks: stockRes.data.map(normalizeDepotStock) })
      }
    }).catch((err) => {
      if (!this.data.material.id) {
        wx.showToast({ title: err.message || '商品详情加载失败', icon: 'none' })
      }
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  copyBarCode() {
    const barCode = this.data.material.barCodeText
    if (!barCode || barCode === '-') {
      wx.showToast({ title: '暂无条码', icon: 'none' })
      return
    }
    wx.setClipboardData({ data: barCode })
  },

  editMaterial() {
    if (!this.data.id) {
      return
    }
    wx.setStorageSync('Material-Edit', this.data.material)
    wx.navigateTo({ url: `/pages/material-edit/material-edit?id=${this.data.id}` })
  }
})
