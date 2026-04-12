const request = require('../../utils/request')
const storage = require('../../utils/storage')

Page({
  data: {
    platformName: '管伊佳ERP',
    user: {},
    statistics: {
      todaySale: '0',
      todayBuy: '0',
      todayRetailSale: '0',
      monthSale: '0',
      monthBuy: '0',
      monthRetailSale: '0'
    },
    statLoading: false,
    approvalCount: 0
  },

  onShow() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    this.setData({ user: storage.getUser() || {} })
    this.loadPlatformName()
    this.loadStatistics()
    this.loadApprovalCount()
  },

  loadPlatformName() {
    request.get('/platformConfig/getPlatform/name').then((res) => {
      if (typeof res === 'string' && res) {
        this.setData({ platformName: res })
      }
    }).catch(() => {})
  },

  loadStatistics() {
    this.setData({ statLoading: true })
    request.get('/depotHead/getBuyAndSaleStatistics').then((res) => {
      if (res && res.code === 200 && res.data) {
        this.setData({ statistics: res.data })
      }
    }).catch(() => {
      wx.showToast({ title: '经营概览加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ statLoading: false })
    })
  },

  loadApprovalCount() {
    request.get('/approval/task/count').then((res) => {
      if (res && res.code === 200 && res.data) {
        this.setData({ approvalCount: res.data.count || 0 })
      }
    }).catch(() => {})
  },

  goMaterial() {
    wx.switchTab({ url: '/pages/material/material' })
  },

  goBill(event) {
    const type = event.currentTarget.dataset.type
    if (type) {
      wx.setStorageSync('Bill-Filter', type)
    }
    wx.switchTab({ url: '/pages/bill/bill' })
  },

  refreshStatistics() {
    this.loadStatistics()
  },

  goStockReport() {
    wx.navigateTo({ url: '/pages/stock-report/stock-report' })
  },

  goBuySaleReport() {
    wx.navigateTo({ url: '/pages/buy-sale-report/buy-sale-report' })
  },

  goAccountReport() {
    wx.navigateTo({ url: '/pages/account-report/account-report' })
  },

  goAccountList() {
    wx.navigateTo({ url: '/pages/account-list/account-list' })
  },

  goFinanceBill() {
    wx.navigateTo({ url: '/pages/finance-bill/finance-bill' })
  },

  goApproval() {
    wx.navigateTo({ url: '/pages/approval/approval' })
  },

  goDepot() {
    wx.navigateTo({ url: '/pages/depot/depot' })
  },

  goStockWarning() {
    wx.navigateTo({ url: '/pages/stock-warning/stock-warning' })
  },

  scanCode() {
    wx.scanCode({
      success: (res) => {
        wx.setStorageSync('Material-Scan-Keyword', res.result)
        wx.switchTab({
          url: '/pages/material/material'
        })
      }
    })
  }
})
