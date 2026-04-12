const request = require('../../utils/request')
const storage = require('../../utils/storage')

function normalizeRow(row) {
  const lowCritical = Number(row.lowCritical || 0)
  const highCritical = Number(row.highCritical || 0)
  return Object.assign({}, row, {
    id: row.mid || row.MId || row.mId || row.id,
    displayName: row.mname || row.materialName || '未命名商品',
    specText: [row.mstandard, row.mmodel, row.mcolor].filter(Boolean).join(' / '),
    warningText: lowCritical > 0 ? `建议入库 ${row.lowCritical}` : (highCritical > 0 ? `建议出库 ${row.highCritical}` : '库存正常'),
    warningType: lowCritical > 0 ? 'low' : (highCritical > 0 ? 'high' : 'normal'),
    currentText: row.currentNumber || 0,
    lowText: row.lowSafeStock || '-',
    highText: row.highSafeStock || '-',
    unitText: row.materialUnit || '-'
  })
}

Page({
  data: {
    keyword: '',
    depotList: [{ id: '', depotName: '全部仓库' }],
    depotIndex: 0,
    depotId: '',
    currentDepotName: '全部仓库',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    this.loadDepots()
    this.loadList(true)
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadMore()
  },

  onKeywordInput(event) {
    this.setData({ keyword: event.detail.value })
  },

  loadDepots() {
    request.get('/depot/findDepotByCurrentUser').then((res) => {
      if (res && res.code === 200 && res.data) {
        this.setData({
          depotList: [{ id: '', depotName: '全部仓库' }].concat(res.data)
        })
      }
    }).catch(() => {})
  },

  onDepotChange(event) {
    const depotIndex = Number(event.detail.value)
    const depot = this.data.depotList[depotIndex] || {}
    this.setData({
      depotIndex,
      depotId: depot.id || '',
      currentDepotName: depot.depotName || '全部仓库'
    })
    this.loadList(true)
  },

  search() {
    this.loadList(true)
  },

  openMaterial(event) {
    const id = event.currentTarget.dataset.id
    const material = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !material) {
      wx.showToast({ title: '商品信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Material-Detail', {
      id,
      name: material.mname || material.displayName,
      mBarCode: material.barCode,
      standard: material.mstandard,
      model: material.mmodel,
      color: material.mcolor,
      unit: material.materialUnit,
      stock: material.currentNumber
    })
    wx.navigateTo({ url: `/pages/material-detail/material-detail?id=${id}` })
  },

  editMaterial(event) {
    const id = event.currentTarget.dataset.id
    const material = this.data.list.find((item) => String(item.id) === String(id))
    if (!id || !material) {
      wx.showToast({ title: '商品信息不完整', icon: 'none' })
      return
    }
    wx.setStorageSync('Material-Edit', {
      id,
      name: material.mname || material.displayName,
      mBarCode: material.barCode,
      standard: material.mstandard,
      model: material.mmodel,
      color: material.mcolor,
      unit: material.materialUnit
    })
    wx.navigateTo({ url: `/pages/material-edit/material-edit?id=${id}` })
  },

  loadMore() {
    if (!this.data.hasMore || this.data.loading) {
      return
    }
    this.setData({ pageNo: this.data.pageNo + 1 })
    this.loadList(false)
  },

  loadList(reset) {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return Promise.resolve()
    }

    if (reset) {
      this.setData({ pageNo: 1, list: [] })
    }

    this.setData({ loading: true })
    return request.get('/depotItem/findStockWarningCount', {
      currentPage: this.data.pageNo,
      pageSize: this.data.pageSize,
      materialParam: this.data.keyword || '',
      depotId: this.data.depotId || '',
      mpList: ''
    }).then((res) => {
      const data = res && res.code === 200 && res.data ? res.data : {}
      const rows = data.rows || []
      const total = data.total || rows.length
      const normalizedRows = rows.map(normalizeRow)
      const list = reset ? normalizedRows : this.data.list.concat(normalizedRows)
      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '库存预警加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
