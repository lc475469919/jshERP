const request = require('../../utils/request')
const storage = require('../../utils/storage')

const BILL_KINDS = {
  saleOut: {
    name: '销售出库',
    type: '出库',
    subType: '销售',
    partnerType: '客户',
    numberPrefix: 'XSCK',
    priceField: 'wholesaleDecimal',
    paymentLabel: '本次收款',
    personType: '销售员'
  },
  saleBack: {
    name: '销售退货',
    type: '入库',
    subType: '销售退货',
    partnerType: '客户',
    numberPrefix: 'XSTH',
    priceField: 'wholesaleDecimal',
    paymentLabel: '本次退款',
    personType: '销售员'
  },
  purchaseIn: {
    name: '采购入库',
    type: '入库',
    subType: '采购',
    partnerType: '供应商',
    numberPrefix: 'CGRK',
    priceField: 'purchaseDecimal',
    paymentLabel: '本次付款'
  },
  purchaseBack: {
    name: '采购退货',
    type: '出库',
    subType: '采购退货',
    partnerType: '供应商',
    numberPrefix: 'CGTH',
    priceField: 'purchaseDecimal',
    paymentLabel: '本次收款'
  },
  allocationOut: {
    name: '调拨出库',
    type: '出库',
    subType: '调拨',
    partnerType: '',
    numberPrefix: 'DBCK',
    priceField: 'purchaseDecimal',
    paymentLabel: '',
    usePartner: false,
    useAccount: false,
    useTargetDepot: true
  }
}

const NEGATIVE_KINDS = ['purchaseIn', 'saleBack']

function toNumber(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

function money(value) {
  return Number(toNumber(value).toFixed(2))
}

function nowText() {
  const date = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function getRows(res) {
  return res.rows || (res.data && res.data.rows) || []
}

Page({
  data: {
    kind: 'saleOut',
    billKind: BILL_KINDS.saleOut,
    number: '',
    operTime: '',
    partners: [],
    partnerIndex: -1,
    depots: [],
    depotIndex: -1,
    targetDepotIndex: -1,
    accounts: [],
    accountIndex: -1,
    persons: [],
    personIndex: -1,
    remark: '',
    materialKeyword: '',
    materialResults: [],
    items: [],
    totalAmount: 0,
    changeAmount: 0,
    loading: false,
    saving: false,
    submitAfterSave: false
  },

  onLoad(options) {
    const kind = BILL_KINDS[options.kind] ? options.kind : 'saleOut'
    this.setData({
      kind,
      billKind: BILL_KINDS[kind],
      operTime: nowText()
    })
    this.loadInitialData()
  },

  onShow() {
    if (wx.getStorageSync('Partner-Refresh')) {
      wx.removeStorageSync('Partner-Refresh')
      this.loadPartners()
    }
    if (wx.getStorageSync('Depot-Refresh')) {
      wx.removeStorageSync('Depot-Refresh')
      this.loadDepots()
    }
    if (wx.getStorageSync('Account-Refresh')) {
      wx.removeStorageSync('Account-Refresh')
      this.loadAccounts()
    }
    if (wx.getStorageSync('Person-Refresh') && this.data.billKind.personType) {
      wx.removeStorageSync('Person-Refresh')
      this.loadPersons()
    }
    if (wx.getStorageSync('Material-Refresh') && this.data.materialKeyword) {
      wx.removeStorageSync('Material-Refresh')
      this.searchMaterial()
    }
  },

  switchKind(event) {
    const kind = event.currentTarget.dataset.kind
    if (!BILL_KINDS[kind] || kind === this.data.kind || this.data.saving) {
      return
    }
    this.setData({
      kind,
      billKind: BILL_KINDS[kind],
      number: '',
      partnerIndex: -1,
      personIndex: -1,
      targetDepotIndex: -1,
      materialKeyword: '',
      materialResults: [],
      items: [],
      totalAmount: 0,
      changeAmount: 0
    })
    this.loadInitialData()
  },

  onPartnerChange(event) {
    this.setData({ partnerIndex: Number(event.detail.value) })
  },

  onDepotChange(event) {
    const depotIndex = Number(event.detail.value)
    const depot = this.data.depots[depotIndex]
    const items = this.data.items.map((item) => Object.assign({}, item, {
      depotId: depot ? depot.id : ''
    }))
    this.setData({ depotIndex, items })
  },

  onTargetDepotChange(event) {
    const targetDepotIndex = Number(event.detail.value)
    const depot = this.data.depots[targetDepotIndex]
    const items = this.data.items.map((item) => Object.assign({}, item, {
      anotherDepotId: depot ? depot.id : ''
    }))
    this.setData({ targetDepotIndex, items })
  },

  onAccountChange(event) {
    this.setData({ accountIndex: Number(event.detail.value) })
  },

  onPersonChange(event) {
    this.setData({ personIndex: Number(event.detail.value) })
  },

  onRemarkInput(event) {
    this.setData({ remark: event.detail.value })
  },

  onMaterialInput(event) {
    this.setData({ materialKeyword: event.detail.value })
  },

  onQuantityInput(event) {
    this.updateItem(event.currentTarget.dataset.index, 'operNumber', event.detail.value)
  },

  onPriceInput(event) {
    this.updateItem(event.currentTarget.dataset.index, 'unitPrice', event.detail.value)
  },

  searchMaterial() {
    const keyword = this.data.materialKeyword
    const search = JSON.stringify({
      materialParam: keyword || '',
      mpList: ''
    })
    return request.get('/material/list', {
      pageNo: 1,
      pageSize: 20,
      search
    }).then((res) => {
      this.setData({ materialResults: getRows(res) })
    }).catch((err) => {
      wx.showToast({ title: err.message || '商品查询失败', icon: 'none' })
    })
  },

  scanMaterial() {
    wx.scanCode({
      success: (res) => {
        this.setData({ materialKeyword: res.result || '' })
        this.searchMaterial()
      }
    })
  },

  createPartner() {
    wx.navigateTo({
      url: `/pages/partner-edit/partner-edit?type=${encodeURIComponent(this.data.billKind.partnerType)}`
    })
  },

  createDepot() {
    wx.navigateTo({ url: '/pages/depot-edit/depot-edit' })
  },

  createAccount() {
    wx.navigateTo({ url: '/pages/account-edit/account-edit' })
  },

  createMaterial() {
    wx.navigateTo({ url: '/pages/material-edit/material-edit' })
  },

  createPerson() {
    const type = this.data.billKind.personType || '销售员'
    wx.navigateTo({ url: `/pages/person-edit/person-edit?type=${encodeURIComponent(type)}` })
  },

  addMaterial(event) {
    const index = Number(event.currentTarget.dataset.index)
    const material = this.data.materialResults[index]
    if (!material || !material.mBarCode) {
      wx.showToast({ title: '商品条码缺失，不能录入单据', icon: 'none' })
      return
    }

    const depot = this.data.depots[this.data.depotIndex]
    const targetDepot = this.data.depots[this.data.targetDepotIndex]
    const price = toNumber(material[this.data.billKind.priceField])
    const item = {
      barCode: material.mBarCode,
      name: material.name || material.materialName || '未命名商品',
      standard: material.standard || '',
      model: material.model || '',
      color: material.color || '',
      unit: material.unit || material.unitName || '',
      depotId: depot ? depot.id : '',
      anotherDepotId: this.data.billKind.useTargetDepot && targetDepot ? targetDepot.id : '',
      operNumber: 1,
      unitPrice: price,
      allPrice: money(price),
      taxRate: 0,
      taxMoney: 0,
      taxLastMoney: money(price),
      remark: ''
    }

    this.setData({
      items: this.data.items.concat(item),
      materialKeyword: '',
      materialResults: []
    })
    this.recalculate()
  },

  removeItem(event) {
    const index = Number(event.currentTarget.dataset.index)
    const items = this.data.items.filter((_, itemIndex) => itemIndex !== index)
    this.setData({ items })
    this.recalculate()
  },

  updateItem(index, field, value) {
    const itemIndex = Number(index)
    const items = this.data.items.map((item, currentIndex) => {
      if (currentIndex !== itemIndex) {
        return item
      }
      const next = Object.assign({}, item, {
        [field]: value
      })
      next.operNumber = toNumber(next.operNumber)
      next.unitPrice = toNumber(next.unitPrice)
      next.allPrice = money(next.operNumber * next.unitPrice)
      next.taxLastMoney = next.allPrice
      return next
    })
    this.setData({ items })
    this.recalculate()
  },

  recalculate() {
    const totalAmount = money(this.data.items.reduce((sum, item) => {
      return sum + toNumber(item.allPrice)
    }, 0))
    this.setData({
      totalAmount,
      changeAmount: totalAmount
    })
  },

  loadInitialData() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ loading: true })
    const tasks = [this.loadNumber(), this.loadDepots()]
    if (this.data.billKind.usePartner !== false) {
      tasks.push(this.loadPartners())
    }
    if (this.data.billKind.useAccount !== false) {
      tasks.push(this.loadAccounts())
    }
    if (this.data.billKind.personType) {
      tasks.push(this.loadPersons())
    }
    Promise.all(tasks).finally(() => {
      this.setData({ loading: false })
    })
  },

  loadNumber() {
    return request.get('/sequence/buildNumber').then((res) => {
      const defaultNumber = res.data && res.data.defaultNumber ? res.data.defaultNumber : String(Date.now())
      this.setData({ number: `${this.data.billKind.numberPrefix}${defaultNumber}` })
    }).catch(() => {
      this.setData({ number: `${this.data.billKind.numberPrefix}${Date.now()}` })
    })
  },

  loadPartners() {
    const search = JSON.stringify({
      supplier: '',
      contacts: '',
      telephone: '',
      phonenum: '',
      type: this.data.billKind.partnerType
    })
    return request.get('/supplier/list', {
      pageNo: 1,
      pageSize: 200,
      search
    }).then((res) => {
      this.setData({
        partners: getRows(res),
        partnerIndex: getRows(res).length ? 0 : -1
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '往来单位加载失败', icon: 'none' })
    })
  },

  loadDepots() {
    const search = JSON.stringify({
      name: '',
      type: '',
      remark: ''
    })
    return request.get('/depot/list', {
      pageNo: 1,
      pageSize: 200,
      search
    }).then((res) => {
      const depots = getRows(res)
      const defaultIndex = depots.findIndex((item) => item.isDefault)
      const depotIndex = depots.length ? Math.max(defaultIndex, 0) : -1
      const targetDepotIndex = depots.length > 1 ? (depotIndex === 0 ? 1 : 0) : -1
      this.setData({
        depots,
        depotIndex,
        targetDepotIndex
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '仓库加载失败', icon: 'none' })
    })
  },

  loadAccounts() {
    return request.get('/account/listWithBalance', {
      name: '',
      serialNo: '',
      pageNo: 1,
      pageSize: 200
    }).then((res) => {
      const accounts = getRows(res)
      const defaultIndex = accounts.findIndex((item) => item.isDefault)
      this.setData({
        accounts,
        accountIndex: accounts.length ? Math.max(defaultIndex, 0) : -1
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '账户加载失败', icon: 'none' })
    })
  },

  loadPersons() {
    const search = JSON.stringify({
      name: '',
      type: this.data.billKind.personType || '',
      remark: ''
    })
    return request.get('/person/list', {
      pageNo: 1,
      pageSize: 200,
      search
    }).then((res) => {
      const persons = getRows(res)
      this.setData({
        persons,
        personIndex: persons.length ? 0 : -1
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '经手人加载失败', icon: 'none' })
    })
  },

  validate() {
    if (!this.data.number) {
      return '单据编号未生成'
    }
    if (this.data.billKind.usePartner !== false && this.data.partnerIndex < 0) {
      return `请选择${this.data.billKind.partnerType}`
    }
    if (this.data.depotIndex < 0) {
      return '请选择仓库'
    }
    if (this.data.billKind.useTargetDepot && this.data.targetDepotIndex < 0) {
      return '请选择调入仓库'
    }
    if (this.data.billKind.useTargetDepot && this.data.targetDepotIndex === this.data.depotIndex) {
      return '调出仓库和调入仓库不能相同'
    }
    if (this.data.billKind.useAccount !== false && this.data.accountIndex < 0) {
      return '请选择结算账户'
    }
    if (!this.data.items.length) {
      return '请至少添加一个商品'
    }
    const invalid = this.data.items.find((item) => !item.barCode || !item.depotId || (this.data.billKind.useTargetDepot && !item.anotherDepotId) || toNumber(item.operNumber) <= 0)
    return invalid ? '商品、仓库或数量不完整' : ''
  },

  saveBill(event) {
    if (this.data.saving) {
      return
    }
    const submitAfterSave = event && event.currentTarget && event.currentTarget.dataset.submit === '1'
    const error = this.validate()
    if (error) {
      wx.showToast({ title: error, icon: 'none' })
      return
    }

    const partner = this.data.billKind.usePartner === false ? null : this.data.partners[this.data.partnerIndex]
    const account = this.data.billKind.useAccount === false ? null : this.data.accounts[this.data.accountIndex]
    const person = this.data.billKind.personType && this.data.personIndex >= 0 ? this.data.persons[this.data.personIndex] : null
    const totalAmount = money(this.data.totalAmount)
    const isNegative = NEGATIVE_KINDS.indexOf(this.data.kind) > -1
    const signedTotal = isNegative ? money(0 - totalAmount) : totalAmount
    const signedChangeAmount = isNegative ? money(0 - totalAmount) : totalAmount
    const info = {
      type: this.data.billKind.type,
      subType: this.data.billKind.subType,
      number: this.data.number,
      operTime: this.data.operTime,
      organId: partner ? partner.id : '',
      accountId: account ? account.id : '',
      accountIdList: '',
      accountMoneyList: '',
      payType: '现付',
      discount: 0,
      discountMoney: 0,
      discountLastMoney: totalAmount,
      otherMoney: 0,
      deposit: 0,
      changeAmount: signedChangeAmount,
      debt: 0,
      totalPrice: signedTotal,
      fileName: '',
      salesMan: person ? person.id : '',
      remark: this.data.remark,
      status: '0'
    }

    this.setData({ saving: true, submitAfterSave })
    request.post('/depotHead/addDepotHeadAndDetail', {
      info: JSON.stringify(info),
      rows: JSON.stringify(this.data.items)
    }).then(() => {
      wx.setStorageSync('Bill-Refresh', '1')
      return this.findSavedBillId().then((headerId) => {
        if (submitAfterSave && headerId) {
          return request.post('/approval/task/submit', {
            billTable: 'depot_head',
            billId: headerId
          }).then(() => headerId)
        }
        return headerId
      }).then((headerId) => {
        wx.showToast({ title: submitAfterSave ? '已提交审批' : '保存成功', icon: 'success' })
        setTimeout(() => {
          if (headerId) {
            wx.redirectTo({
              url: `/pages/bill-detail/bill-detail?number=${encodeURIComponent(this.data.number)}&headerId=${headerId}`
            })
            return
          }
          wx.navigateBack()
        }, 600)
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    }).finally(() => {
      this.setData({ saving: false, submitAfterSave: false })
    })
  },

  findSavedBillId() {
    const search = JSON.stringify({
      type: this.data.billKind.type,
      subType: this.data.billKind.subType,
      hasDebt: '',
      status: '',
      purchaseStatus: '',
      number: this.data.number,
      linkApply: '',
      linkNumber: '',
      beginTime: '',
      endTime: '',
      materialParam: '',
      organId: '',
      creator: '',
      depotId: '',
      accountId: '',
      salesMan: '',
      remark: ''
    })
    return request.get('/depotHead/list', {
      pageNo: 1,
      pageSize: 1,
      search
    }).then((res) => {
      const rows = getRows(res)
      return rows.length ? rows[0].id : ''
    }).catch(() => '')
  }
})
