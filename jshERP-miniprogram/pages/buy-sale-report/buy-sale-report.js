const request = require('../../utils/request')
const storage = require('../../utils/storage')

const reportTypes = {
  buy: {
    title: '采购',
    path: '/depotItem/buyIn',
    inLabel: '采购',
    outLabel: '退货',
    amountKey: 'inOutSumPrice'
  },
  sale: {
    title: '销售',
    path: '/depotItem/saleOut',
    inLabel: '退货',
    outLabel: '销售',
    amountKey: 'outInSumPrice'
  },
  retail: {
    title: '零售',
    path: '/depotItem/retailOut',
    inLabel: '退货',
    outLabel: '零售',
    amountKey: 'outInSumPrice'
  }
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function normalizeRow(row, config) {
  return Object.assign({}, row, {
    displayName: row.materialName || '未命名商品',
    specText: [row.materialStandard, row.materialModel, row.materialColor].filter(Boolean).join(' / '),
    unitText: row.materialUnit || row.unitName || '-',
    inText: row.inSum || 0,
    outText: row.outSum || 0,
    inPriceText: row.inSumPrice || 0,
    outPriceText: row.outSumPrice || 0,
    amountText: row[config.amountKey] || 0
  })
}

Page({
  data: {
    type: 'buy',
    reportTitle: '采购',
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
    totalAmount: 0,
    inLabel: '采购',
    outLabel: '退货',
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

  changeType(event) {
    const type = event.currentTarget.dataset.type
    if (!reportTypes[type] || type === this.data.type) {
      return
    }
    const config = reportTypes[type]
    this.setData({
      type,
      reportTitle: config.title,
      inLabel: config.inLabel,
      outLabel: config.outLabel
    })
    this.loadList(true)
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
    const barCode = event.currentTarget.dataset.barcode
    if (!barCode) {
      wx.showToast({ title: '商品条码缺失', icon: 'none' })
      return
    }
    wx.setStorageSync('Material-Scan-Keyword', barCode)
    wx.switchTab({ url: '/pages/material/material' })
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

    const config = reportTypes[this.data.type]
    this.setData({ loading: true })
    return request.get(config.path, {
      currentPage: this.data.pageNo,
      pageSize: this.data.pageSize,
      beginTime: this.data.beginTime,
      endTime: this.data.endTime,
      depotId: this.data.depotId || '',
      materialParam: this.data.keyword || '',
      mpList: ''
    }).then((res) => {
      const data = res && res.code === 200 && res.data ? res.data : {}
      const rows = data.rows || []
      const total = data.total || rows.length
      const normalizedRows = rows.map((row) => normalizeRow(row, config))
      const list = reset ? normalizedRows : this.data.list.concat(normalizedRows)
      this.setData({
        list,
        total,
        totalAmount: data.realityPriceTotal || 0,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '购销统计加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
