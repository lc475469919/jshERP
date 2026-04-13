const request = require('../../utils/request')
const storage = require('../../utils/storage')

const TYPES = [
  { label: '经手人', value: 'person', path: '/person/list', deletePath: '/person/delete' },
  { label: '收支项目', value: 'inOutItem', path: '/inOutItem/list', deletePath: '/inOutItem/delete' },
  { label: '计量单位', value: 'unit', path: '/unit/list', deletePath: '/unit/delete' },
  { label: '商品分类', value: 'category', path: '/materialCategory/getMaterialCategoryTree', deletePath: '/materialCategory/delete' }
]

function flattenCategoryTree(nodes, level) {
  return (nodes || []).reduce((list, node) => {
    const item = Object.assign({}, node, { level })
    const children = flattenCategoryTree(node.children || [], level + 1)
    return list.concat(item, children)
  }, [])
}

function normalizeRow(row, type) {
  if (type === 'category') {
    return Object.assign({}, row, {
      id: row.id || row.value,
      titleText: row.title || row.name || '未命名分类',
      tagText: `层级 ${row.level + 1}`,
      lineOne: `编号：${row.id || row.value || '-'}`,
      lineTwo: `子分类：${row.children && row.children.length ? row.children.length : 0}`,
      lineThree: row.state ? `状态：${row.state}` : ''
    })
  }

  if (type === 'unit') {
    const ratioText = row.otherUnit ? `${row.otherUnit}=${row.ratio || '-'}${row.basicUnit || ''}` : ''
    return Object.assign({}, row, {
      titleText: row.name || '未命名单位',
      tagText: row.enabled === false ? '停用' : '启用',
      lineOne: `基础单位：${row.basicUnit || '-'}`,
      lineTwo: ratioText ? `换算：${ratioText}` : '换算：-',
      lineThree: row.otherUnitTwo ? `辅助单位：${row.otherUnitTwo}` : ''
    })
  }

  if (type === 'inOutItem') {
    return Object.assign({}, row, {
      titleText: row.name || '未命名项目',
      tagText: row.type || '-',
      lineOne: `状态：${row.enabled === false ? '停用' : '启用'}`,
      lineTwo: `备注：${row.remark || '-'}`,
      lineThree: `排序：${row.sort || '-'}`
    })
  }

  return Object.assign({}, row, {
    titleText: row.name || '未命名人员',
    tagText: row.type || '-',
    lineOne: `状态：${row.enabled === false ? '停用' : '启用'}`,
    lineTwo: `排序：${row.sort || '-'}`,
    lineThree: ''
  })
}

Page({
  data: {
    typeList: TYPES,
    type: 'person',
    keyword: '',
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: false
  },

  onLoad() {
    this.loadList(true)
  },

  onShow() {
    const refresh = wx.getStorageSync('Basic-Data-Refresh')
    if (refresh) {
      wx.removeStorageSync('Basic-Data-Refresh')
      this.loadList(true)
    }
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    this.loadMore()
  },

  changeType(event) {
    const type = event.currentTarget.dataset.type
    if (type === this.data.type) {
      return
    }
    this.setData({ type, keyword: '' })
    this.loadList(true)
  },

  onKeywordInput(event) {
    this.setData({ keyword: event.detail.value })
  },

  search() {
    this.loadList(true)
  },

  createCurrent() {
    if (this.data.type === 'person') {
      wx.navigateTo({ url: '/pages/person-edit/person-edit' })
      return
    }
    if (this.data.type === 'inOutItem') {
      wx.navigateTo({ url: '/pages/inout-item-edit/inout-item-edit' })
      return
    }
    if (this.data.type === 'unit') {
      wx.navigateTo({ url: '/pages/unit-edit/unit-edit' })
      return
    }
    if (this.data.type === 'category') {
      wx.navigateTo({ url: '/pages/category-edit/category-edit' })
      return
    }
  },

  editCurrent(event) {
    const id = event.currentTarget.dataset.id
    const item = this.data.list.find((row) => String(row.id) === String(id))
    if (!id || !item) {
      wx.showToast({ title: '基础资料信息不完整', icon: 'none' })
      return
    }
    if (this.data.type === 'person') {
      wx.setStorageSync('Person-Edit', item)
      wx.navigateTo({ url: `/pages/person-edit/person-edit?id=${id}` })
      return
    }
    if (this.data.type === 'inOutItem') {
      wx.setStorageSync('InOutItem-Edit', item)
      wx.navigateTo({ url: `/pages/inout-item-edit/inout-item-edit?id=${id}` })
      return
    }
    if (this.data.type === 'unit') {
      wx.setStorageSync('Unit-Edit', item)
      wx.navigateTo({ url: `/pages/unit-edit/unit-edit?id=${id}` })
      return
    }
    if (this.data.type === 'category') {
      wx.setStorageSync('Category-Edit', item)
      wx.navigateTo({ url: `/pages/category-edit/category-edit?id=${id}` })
    }
  },

  deleteCurrent(event) {
    const id = event.currentTarget.dataset.id
    const item = this.data.list.find((row) => String(row.id) === String(id))
    const currentType = TYPES.find((type) => type.value === this.data.type) || TYPES[0]
    if (!id || !item || !currentType.deletePath) {
      wx.showToast({ title: '基础资料信息不完整', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认删除',
      content: `确定删除“${item.titleText || '该资料'}”吗？`,
      confirmColor: '#b91c1c',
      success: (res) => {
        if (!res.confirm) {
          return
        }
        request.del(currentType.deletePath, { id }).then(() => {
          wx.showToast({ title: '删除成功', icon: 'success' })
          this.loadList(true)
        }).catch((err) => {
          wx.showToast({ title: err.message || '删除失败', icon: 'none' })
        })
      }
    })
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

    const currentType = TYPES.find((item) => item.value === this.data.type) || TYPES[0]
    const search = JSON.stringify({
      name: this.data.keyword || '',
      type: '',
      remark: ''
    })

    this.setData({ loading: true })
    if (this.data.type === 'category') {
      return request.get(currentType.path, { id: 0 }).then((res) => {
        const keyword = this.data.keyword || ''
        const rows = flattenCategoryTree(Array.isArray(res) ? res : [], 0)
        const filteredRows = keyword ? rows.filter((row) => String(row.title || '').indexOf(keyword) > -1) : rows
        const pageRows = filteredRows.slice((this.data.pageNo - 1) * this.data.pageSize, this.data.pageNo * this.data.pageSize)
        const normalizedRows = pageRows.map((row) => normalizeRow(row, this.data.type))
        const list = reset ? normalizedRows : this.data.list.concat(normalizedRows)
        this.setData({
          list,
          total: filteredRows.length,
          hasMore: list.length < filteredRows.length
        })
      }).catch((err) => {
        wx.showToast({ title: err.message || '基础资料加载失败', icon: 'none' })
      }).finally(() => {
        this.setData({ loading: false })
      })
    }

    return request.get(currentType.path, {
      pageNo: this.data.pageNo,
      pageSize: this.data.pageSize,
      search
    }).then((res) => {
      const rows = res.rows || (res.data && res.data.rows) || []
      const total = res.total || (res.data && res.data.total) || rows.length
      const normalizedRows = rows.map((row) => normalizeRow(row, this.data.type))
      const list = reset ? normalizedRows : this.data.list.concat(normalizedRows)
      this.setData({
        list,
        total,
        hasMore: list.length < total
      })
    }).catch((err) => {
      wx.showToast({ title: err.message || '基础资料加载失败', icon: 'none' })
    }).finally(() => {
      this.setData({ loading: false })
    })
  }
})
