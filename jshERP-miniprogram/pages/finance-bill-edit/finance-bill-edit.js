const request = require('../../utils/request')
const storage = require('../../utils/storage')

const TYPE_CONFIG = {
  '收款': { prefix: 'SK', partnerType: '客户', negative: false, usePartner: true, useItem: false },
  '付款': { prefix: 'FK', partnerType: '供应商', negative: true, usePartner: true, useItem: false },
  '收入': { prefix: 'SR', partnerType: '', negative: false, usePartner: false, useItem: true, itemType: '收入' },
  '支出': { prefix: 'ZC', partnerType: '', negative: true, usePartner: false, useItem: true, itemType: '支出' },
  '转账': { prefix: 'ZZ', partnerType: '', negative: true, usePartner: false, useItem: false, useTransfer: true }
}

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
    type: '收款',
    config: TYPE_CONFIG['收款'],
    billNo: '',
    billTime: '',
    partners: [],
    partnerIndex: -1,
    accounts: [],
    accountIndex: -1,
    targetAccountIndex: -1,
    inOutItems: [],
    inOutItemIndex: -1,
    persons: [],
    personIndex: -1,
    amount: '',
    remark: '',
    loading: false,
    saving: false,
    submitAfterSave: false
  },

  onLoad(options) {
    const type = TYPE_CONFIG[decodeURIComponent(options.type || '')] ? decodeURIComponent(options.type || '') : '收款'
    this.setData({
      type,
      config: TYPE_CONFIG[type],
      billTime: nowText()
    })
    this.loadInitialData()
  },

  onShow() {
    if (wx.getStorageSync('Partner-Refresh') && this.data.config.usePartner) {
      wx.removeStorageSync('Partner-Refresh')
      this.loadPartners()
    }
    if (wx.getStorageSync('Account-Refresh')) {
      wx.removeStorageSync('Account-Refresh')
      this.loadAccounts()
    }
    if (wx.getStorageSync('Basic-Data-Refresh') && this.data.config.useItem) {
      wx.removeStorageSync('Basic-Data-Refresh')
      this.loadInOutItems()
    }
    if (wx.getStorageSync('Person-Refresh')) {
      wx.removeStorageSync('Person-Refresh')
      this.loadPersons()
    }
  },

  switchType(event) {
    const type = event.currentTarget.dataset.type
    if (!TYPE_CONFIG[type] || type === this.data.type || this.data.saving) {
      return
    }
    this.setData({
      type,
      config: TYPE_CONFIG[type],
      billNo: '',
      partners: [],
      partnerIndex: -1,
      inOutItems: [],
      inOutItemIndex: -1,
      personIndex: -1,
      targetAccountIndex: -1,
      amount: '',
      remark: ''
    })
    this.loadInitialData()
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field
    if (field) {
      this.setData({ [field]: event.detail.value })
    }
  },

  onPartnerChange(event) {
    this.setData({ partnerIndex: Number(event.detail.value) })
  },

  onAccountChange(event) {
    this.setData({ accountIndex: Number(event.detail.value) })
  },

  onTargetAccountChange(event) {
    this.setData({ targetAccountIndex: Number(event.detail.value) })
  },

  onInOutItemChange(event) {
    this.setData({ inOutItemIndex: Number(event.detail.value) })
  },

  onPersonChange(event) {
    this.setData({ personIndex: Number(event.detail.value) })
  },

  createPartner() {
    wx.navigateTo({
      url: `/pages/partner-edit/partner-edit?type=${encodeURIComponent(this.data.config.partnerType)}`
    })
  },

  createAccount() {
    wx.navigateTo({ url: '/pages/account-edit/account-edit' })
  },

  createInOutItem() {
    const type = this.data.config.itemType || '收入'
    wx.navigateTo({ url: `/pages/inout-item-edit/inout-item-edit?type=${encodeURIComponent(type)}` })
  },

  createPerson() {
    wx.navigateTo({ url: `/pages/person-edit/person-edit?type=${encodeURIComponent('财务员')}` })
  },

  loadInitialData() {
    if (!storage.getToken()) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    this.setData({ loading: true })
    const tasks = [this.loadNumber(), this.loadAccounts(), this.loadPersons()]
    if (this.data.config.usePartner) {
      tasks.push(this.loadPartners())
    }
    if (this.data.config.useItem) {
      tasks.push(this.loadInOutItems())
    }
    Promise.all(tasks).finally(() => {
      this.setData({ loading: false })
    })
  },

  loadNumber() {
    return request.get('/sequence/buildNumber').then((res) => {
      const defaultNumber = res.data && res.data.defaultNumber ? res.data.defaultNumber : String(Date.now())
      this.setData({ billNo: `${this.data.config.prefix}${defaultNumber}` })
    }).catch(() => {
      this.setData({ billNo: `${this.data.config.prefix}${Date.now()}` })
    })
  },

  loadPartners() {
    const search = JSON.stringify({
      supplier: '',
      contacts: '',
      telephone: '',
      phonenum: '',
      type: this.data.config.partnerType
    })
    return request.get('/supplier/list', {
      pageNo: 1,
      pageSize: 200,
      search
    }).then((res) => {
      const partners = getRows(res)
      this.setData({
        partners,
        partnerIndex: partners.length ? 0 : -1
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '往来单位加载失败', icon: 'none' })
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
      const targetAccountIndex = accounts.length > 1 ? (Math.max(defaultIndex, 0) === 0 ? 1 : 0) : -1
      this.setData({
        accounts,
        accountIndex: accounts.length ? Math.max(defaultIndex, 0) : -1,
        targetAccountIndex
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '账户加载失败', icon: 'none' })
    })
  },

  loadInOutItems() {
    const search = JSON.stringify({
      name: '',
      type: this.data.config.itemType,
      remark: ''
    })
    return request.get('/inOutItem/list', {
      pageNo: 1,
      pageSize: 200,
      search
    }).then((res) => {
      const inOutItems = getRows(res)
      this.setData({
        inOutItems,
        inOutItemIndex: inOutItems.length ? 0 : -1
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '收支项目加载失败', icon: 'none' })
    })
  },

  loadPersons() {
    const search = JSON.stringify({
      name: '',
      type: '财务员',
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
      wx.showToast({ title: err.message || '财务员加载失败', icon: 'none' })
    })
  },

  validate() {
    if (!this.data.billNo) {
      return '单据编号未生成'
    }
    if (this.data.accountIndex < 0) {
      return '请选择账户'
    }
    if (this.data.config.useTransfer && this.data.targetAccountIndex < 0) {
      return '请选择收款账户'
    }
    if (this.data.config.useTransfer && this.data.accountIndex === this.data.targetAccountIndex) {
      return '付款账户和收款账户不能相同'
    }
    if (this.data.config.usePartner && this.data.partnerIndex < 0) {
      return `请选择${this.data.config.partnerType}`
    }
    if (this.data.config.useItem && this.data.inOutItemIndex < 0) {
      return '请选择收支项目'
    }
    if (toNumber(this.data.amount) <= 0) {
      return '请输入金额'
    }
    return ''
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

    const amount = money(this.data.amount)
    const signedAmount = this.data.config.negative ? money(0 - amount) : amount
    const account = this.data.accounts[this.data.accountIndex]
    const targetAccount = this.data.config.useTransfer ? this.data.accounts[this.data.targetAccountIndex] : account
    const partner = this.data.config.usePartner ? this.data.partners[this.data.partnerIndex] : null
    const inOutItem = this.data.config.useItem ? this.data.inOutItems[this.data.inOutItemIndex] : null
    const person = this.data.personIndex >= 0 ? this.data.persons[this.data.personIndex] : null
    const info = {
      type: this.data.type,
      billNo: this.data.billNo,
      billTime: this.data.billTime,
      organId: partner ? partner.id : '',
      accountId: account.id,
      changeAmount: signedAmount,
      totalPrice: signedAmount,
      fileName: '',
      handsPersonId: person ? person.id : '',
      remark: this.data.remark.trim(),
      status: '0'
    }
    const rows = [{
      accountId: targetAccount.id,
      inOutItemId: inOutItem ? inOutItem.id : '',
      billNumber: '',
      needDebt: '',
      finishDebt: '',
      eachAmount: amount,
      remark: this.data.remark.trim()
    }]

    this.setData({ saving: true, submitAfterSave })
    request.post('/accountHead/addAccountHeadAndDetail', {
      info: JSON.stringify(info),
      rows: JSON.stringify(rows)
    }).then(() => {
      wx.setStorageSync('Finance-Bill-Refresh', '1')
      return this.findSavedBillId().then((id) => {
        if (submitAfterSave && id) {
          return request.post('/approval/task/submit', {
            billTable: 'account_head',
            billId: id
          }).then(() => id)
        }
        return id
      }).then((id) => {
        wx.showToast({ title: submitAfterSave ? '已提交审批' : '保存成功', icon: 'success' })
        setTimeout(() => {
          if (id) {
            wx.redirectTo({
              url: `/pages/finance-bill-detail/finance-bill-detail?id=${id}&billNo=${encodeURIComponent(this.data.billNo)}`
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
      type: this.data.type,
      billNo: this.data.billNo,
      beginTime: '',
      endTime: '',
      organId: '',
      creator: '',
      handsPersonId: '',
      accountId: '',
      status: '',
      remark: '',
      number: '',
      inOutItemId: ''
    })
    return request.get('/accountHead/list', {
      pageNo: 1,
      pageSize: 1,
      search
    }).then((res) => {
      const rows = getRows(res)
      return rows.length ? rows[0].id : ''
    }).catch(() => '')
  }
})
