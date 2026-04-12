const request = require('../../utils/request')
const storage = require('../../utils/storage')

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function normalizeRow(row) {
  return Object.assign({}, row, {
    id: row.mid || row.MId || row.mId || row.id,
    displayName: row.materialName || '未命名商品',
    specText: [row.materialStandard, row.materialModel, row.materialColor].filter(Boolean).join(' / '),
    stockText: row.thisSum || 0,
    inText: row.inSum || 0,
    outText: row.outSum || 0,
    amountText: row.thisAllPrice || 0,
    unitText: row.unitName || '-'
  })
}

Page({
  data: {
    keyword: '',
    depotList: [{ id: '', depotName: '全部仓库' }],
    depotIndex: 0,
    depotId: '',
    currentDepotName: '全部仓库',
    beginTime: '',
    endTime: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    const now = new Date()
    const begin = new Date(now.getFullYear(), 0, 1)
    this.setData({
      beginTime: formatDate(begin),
      endTime: formatDate(now)
    })
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

  onBeginDateChange(event) {
    this.setData({ beginTime: event.detail.value })
    this.loadList(true)
  },

  onEndDateChange(event) {
    this.setData({ endTime: event.detail.value })
    this.loadList(true)
  },

  resetDateRange() {
    this.setQuickRange({ currentTarget: { dataset: { range: 'year' } } })
  },

  setQuickRange(event) {
    const range = event.currentTarget.dataset.range
    const now = new Date()
    let begin = new Date(now.getFullYear(), 0, 1)
    if (range === 'month') {
      begin = new Date(now.getFullYear(), now.getMonth(), 1)
    }
    if (range === 'quarter') {
      begin = new Date(now.getFullYear(), now.getMonth() - 3, now.getDate())
    }
    this.setData({
      beginTime: formatDate(begin),
      endTime: formatDate(now)
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
      name: material.materialName || material.displayName,
      mBarCode: material.barCode,
      standard: material.materialStandard,
      model: material.materialModel,
      color: material.materialColor,
      unit: material.unitName || material.materialUnit,
      stock: material.stockText
    })
    wx.navigateTo({ url: `/pages/material-detail/material-detail?id=${id}` })
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
    return request.get('/depotItem/getInOutStock', {
      currentPage: this.data.pageNo,
      pageSize: this.data.pageSize,
      depotIds: this.data.depotId || '',
      beginTime: this.data.beginTime,
      endTime: this.data.endTime,
      materialParam: this.data.keyword || '',
      mpList: ''
    }).then((res) => {
      const rows = res && res.code === 200 && res.data ? res.data.rows || [] : []
      const total = res && res.code === 200 && res.data ? res.data.total || rows.length : rows.length
      const list = reset ? rows.map(normalizeRow) : this.data.list.concat(rows.map(normalizeRow))
      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '库存报表加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
