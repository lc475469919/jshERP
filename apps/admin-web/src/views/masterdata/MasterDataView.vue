<template>
  <div class="page master-page">
    <aside v-if="config.categoryType" class="tree-panel">
      <div class="panel-title">{{ config.categoryTitle }}</div>
      <a-space class="tree-actions">
        <a-button size="small" @click="openCategoryCreate">新增</a-button>
        <a-button size="small" :disabled="!selectedCategoryId" @click="openCategoryEdit">修改</a-button>
        <a-popconfirm title="确认删除类别？" @confirm="removeCategory">
          <a-button size="small" danger :disabled="!selectedCategoryId">删除</a-button>
        </a-popconfirm>
      </a-space>
      <a-tree
        block-node
        :tree-data="categoryTree"
        :selected-keys="selectedCategoryId ? [selectedCategoryId] : []"
        @select="selectCategory"
      />
    </aside>

    <section class="list-panel">
      <div class="page-toolbar">
        <div>
          <h1 class="page-title">{{ config.title }}</h1>
          <p class="page-desc">{{ config.description }}</p>
        </div>
        <a-space>
          <a-input v-model:value="keyword" :placeholder="config.keywordPlaceholder" allow-clear @press-enter="load" />
          <a-select v-if="config.statusTabs" v-model:value="status" class="status-select" @change="load">
            <a-select-option :value="1">使用中</a-select-option>
            <a-select-option :value="0">已停用</a-select-option>
          </a-select>
          <a-button @click="load">查询</a-button>
          <a-button type="primary" @click="openCreate">新增</a-button>
          <a-button :disabled="!selectedRow" @click="openEdit">修改</a-button>
          <a-popconfirm title="确认删除选中数据？" @confirm="remove">
            <a-button danger :disabled="!selectedRow">删除</a-button>
          </a-popconfirm>
          <a-button v-if="config.canDisable" :disabled="!selectedRow" @click="setStatus(0)">停用</a-button>
          <a-button v-if="config.canDisable" :disabled="!selectedRow" @click="setStatus(1)">启用</a-button>
          <a-button disabled>导入</a-button>
          <a-button disabled>导出</a-button>
        </a-space>
      </div>

      <a-tabs v-if="config.kind === 'product-attrs'" v-model:active-key="attrType" @change="load">
        <a-tab-pane key="UNIT" tab="计量单位" />
        <a-tab-pane key="COLOR" tab="颜色" />
        <a-tab-pane key="BRAND" tab="品牌" />
      </a-tabs>

      <a-table
        row-key="id"
        size="middle"
        :loading="loading"
        :columns="columns"
        :data-source="records"
        :pagination="pagination"
        :row-selection="{ type: 'radio', selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
      />
    </section>

    <a-drawer v-model:open="drawerOpen" width="560" :title="editingId ? `修改${config.shortTitle}` : `新增${config.shortTitle}`">
      <a-form layout="vertical" :model="form">
        <a-form-item v-for="field in activeFields" :key="field.key" :label="field.label">
          <a-input v-if="field.type === 'text'" v-model:value="form[field.key]" />
          <a-input-number v-else-if="field.type === 'number'" v-model:value="form[field.key]" class="full-input" />
          <a-select v-else-if="field.type === 'status'" v-model:value="form[field.key]">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
          <a-textarea v-else v-model:value="form[field.key]" :rows="3" />
        </a-form-item>
      </a-form>
      <template #extra>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="save">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="categoryOpen" :title="categoryEditingId ? '修改类别' : '新增类别'" @ok="saveCategory">
      <a-form layout="vertical" :model="categoryForm">
        <a-form-item label="类别编号">
          <a-input v-model:value="categoryForm.categoryCode" />
        </a-form-item>
        <a-form-item label="类别名称">
          <a-input v-model:value="categoryForm.categoryName" />
        </a-form-item>
        <a-form-item label="显示顺序">
          <a-input-number v-model:value="categoryForm.sortOrder" class="full-input" />
        </a-form-item>
        <a-form-item label="可销售">
          <a-select v-model:value="categoryForm.saleEnabled">
            <a-select-option :value="1">是</a-select-option>
            <a-select-option :value="0">否</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="可采购">
          <a-select v-model:value="categoryForm.purchaseEnabled">
            <a-select-option :value="1">是</a-select-option>
            <a-select-option :value="0">否</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { deleteData, getData, postData, putData, type PageResult } from '@/api/http'

type Row = Record<string, any>
type Field = { key: string; label: string; type: 'text' | 'number' | 'status' | 'textarea'; defaultValue?: unknown }

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const categoryOpen = ref(false)
const keyword = ref('')
const status = ref(1)
const attrType = ref('UNIT')
const records = ref<Row[]>([])
const categories = ref<Row[]>([])
const selectedRow = ref<Row | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedCategoryId = ref<number | null>(null)
const editingId = ref<number | null>(null)
const categoryEditingId = ref<number | null>(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const form = reactive<Row>({})
const categoryForm = reactive<Row>({})

const configs: Record<string, any> = {
  products: {
    kind: 'products',
    title: '商品信息',
    shortTitle: '商品',
    description: '参考系统左侧商品类别树、右侧商品列表、筛选区和批量工具栏。',
    endpoint: '/master-data/products',
    categoryType: 'COM',
    categoryTitle: '商品类别',
    keywordPlaceholder: '商品编号/名称/助记码/规格',
    canDisable: true,
    columns: [
      ['productCode', '编号', 100], ['productName', '名称', 150], ['categoryId', '类别', 90],
      ['barcode', '条形码', 120], ['brandId', '品牌', 90], ['specification', '规格型号', 110],
      ['supplierId', '供应商名称', 120], ['shelfNo', '仓位/货架号', 110], ['defaultUnitId', '默认单位', 90],
      ['purchasePrice', '采购价', 90], ['costPrice', '成本价', 90], ['wholesalePrice', '批发价', 90],
      ['retailPrice', '零售价', 90], ['status', '状态', 80], ['remark', '备注', 160]
    ],
    fields: [
      ['productCode', '编号'], ['productName', '名称'], ['mnemonicCode', '助记码'], ['barcode', '条形码'],
      ['specification', '规格型号'], ['shelfNo', '仓位/货架号'], ['purchasePrice', '采购价', 'number'],
      ['costPrice', '成本价', 'number'], ['wholesalePrice', '批发价', 'number'], ['retailPrice', '零售价', 'number'],
      ['status', '状态', 'status', 1], ['remark', '备注', 'textarea']
    ]
  },
  'product-attrs': {
    kind: 'product-attrs',
    title: '商品属性',
    shortTitle: '商品属性',
    description: '参考系统使用计量单位、颜色、品牌三个 Tab。',
    endpoint: '/master-data/product-attrs',
    keywordPlaceholder: '名称',
    columns: [['attrName', '名称', 160], ['allowDecimal', '允许小数', 100], ['sortOrder', '排列顺序', 100], ['remark', '备注', 180]],
    fields: [['attrName', '名称'], ['allowDecimal', '允许小数', 'status', 0], ['sortOrder', '排列顺序', 'number', 0], ['remark', '备注', 'textarea']]
  },
  projects: {
    kind: 'projects',
    title: '项目信息',
    shortTitle: '项目',
    description: '参考系统使用“使用中/已停用”状态切换。',
    endpoint: '/master-data/projects',
    keywordPlaceholder: '项目名称',
    statusTabs: true,
    canDisable: true,
    columns: [['projectCode', '项目编号', 130], ['projectName', '项目名称', 180], ['status', '状态', 80], ['remark', '备注', 260]],
    fields: [['projectCode', '项目编号'], ['projectName', '项目名称'], ['status', '状态', 'status', 1], ['remark', '备注', 'textarea']]
  },
  suppliers: {
    kind: 'suppliers',
    title: '供应商管理',
    shortTitle: '供应商',
    description: '参考系统左侧供应商类别树，右侧供应商列表。',
    endpoint: '/master-data/suppliers',
    categoryType: 'SUP',
    categoryTitle: '供应商类别',
    keywordPlaceholder: '供应商编号/名称/联系人/手机号码',
    canDisable: true,
    columns: [['supplierCode', '编号', 100], ['supplierName', '供应商名称', 150], ['contactName', '联系人', 100], ['mobile', '手机号码', 120], ['address', '地址', 180], ['prepaidAmount', '预付款', 100], ['invoiceEnabled', '开票标志', 90], ['status', '状态', 80], ['remark', '备注', 160]],
    fields: [['supplierCode', '编号'], ['supplierName', '供应商名称'], ['contactName', '联系人'], ['mobile', '手机号码'], ['email', '电子邮箱'], ['telephone', '座机'], ['address', '地址'], ['prepaidAmount', '预付款', 'number'], ['invoiceEnabled', '开票标志', 'status', 0], ['status', '状态', 'status', 1], ['remark', '备注', 'textarea']]
  },
  logistics: {
    kind: 'logistics',
    title: '物流公司',
    shortTitle: '物流公司',
    description: '参考系统物流公司列表和寄件人管理入口。',
    endpoint: '/master-data/logistics',
    keywordPlaceholder: '物流名称',
    columns: [['logisticsCode', '物流编号', 110], ['logisticsName', '物流名称', 160], ['expressType', '快递公司类型', 130], ['expressCompanyName', '快递公司名称', 150], ['remark', '备注', 220]],
    fields: [['logisticsCode', '物流编号'], ['logisticsName', '物流名称'], ['expressType', '快递公司类型'], ['expressCompanyName', '快递公司名称'], ['remark', '备注', 'textarea']]
  }
}

const config = computed(() => configs[String(route.params.kind)] || configs.products)
const columns = computed(() => config.value.columns.map(([key, title, width]: any[]) => ({ dataIndex: key, key, title, width })))
const activeFields = computed<Field[]>(() => config.value.fields.map(([key, label, type = 'text', defaultValue]: any[]) => ({ key, label, type, defaultValue })))
const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: total.value, showSizeChanger: true }))
const categoryTree = computed(() => categories.value.map((item) => ({ title: item.categoryName, key: item.id })))

async function load() {
  loading.value = true
  try {
    const params: Row = { page: page.value, pageSize: pageSize.value }
    if (keyword.value) params.keyword = keyword.value
    if (config.value.kind === 'product-attrs') params.attrType = attrType.value
    if (config.value.kind === 'projects') params.projectName = keyword.value
    if (config.value.kind === 'projects') params.status = status.value
    if (selectedCategoryId.value && ['products', 'suppliers'].includes(config.value.kind)) params.categoryId = selectedCategoryId.value
    const result = await getData<PageResult<Row>>(config.value.endpoint, params)
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  if (!config.value.categoryType) return
  categories.value = await getData<Row[]>('/master-data/categories', { categoryType: config.value.categoryType })
}

function resetForm(row?: Row) {
  Object.keys(form).forEach((key) => delete form[key])
  activeFields.value.forEach((field) => { form[field.key] = row?.[field.key] ?? field.defaultValue })
  if (config.value.kind === 'product-attrs') form.attrType = attrType.value
  if (config.value.categoryType) form.categoryId = selectedCategoryId.value
}

function openCreate() { editingId.value = null; resetForm(); drawerOpen.value = true }
function openEdit() { if (!selectedRow.value) return; editingId.value = selectedRow.value.id; resetForm(selectedRow.value); drawerOpen.value = true }

async function save() {
  saving.value = true
  try {
    if (editingId.value) await putData<void>(`${config.value.endpoint}/${editingId.value}`, form)
    else await postData<void>(config.value.endpoint, form)
    message.success('保存成功')
    drawerOpen.value = false
    await load()
  } finally { saving.value = false }
}

async function remove() {
  if (!selectedRow.value) return
  await deleteData<void>(`${config.value.endpoint}/${selectedRow.value.id}`)
  message.success('删除成功')
  selectedRow.value = null
  selectedRowKeys.value = []
  await load()
}

async function setStatus(value: number) {
  if (!selectedRow.value) return
  const next = { ...selectedRow.value, status: value }
  await putData<void>(`${config.value.endpoint}/${selectedRow.value.id}`, next)
  await load()
}

function onSelectChange(keys: any[], rows: Row[]) { selectedRowKeys.value = keys as number[]; selectedRow.value = rows[0] || null }
function handleTableChange(next: TablePaginationConfig) { page.value = next.current || 1; pageSize.value = next.pageSize || 20; load() }
function selectCategory(keys: any[]) { selectedCategoryId.value = keys[0] || null; load() }

function resetCategoryForm(row?: Row) {
  Object.keys(categoryForm).forEach((key) => delete categoryForm[key])
  Object.assign(categoryForm, { categoryType: config.value.categoryType, parentId: 0, categoryCode: row?.categoryCode, categoryName: row?.categoryName, sortOrder: row?.sortOrder ?? 0, saleEnabled: row?.saleEnabled ?? 1, purchaseEnabled: row?.purchaseEnabled ?? 1 })
}
function openCategoryCreate() { categoryEditingId.value = null; resetCategoryForm(); categoryOpen.value = true }
function openCategoryEdit() { const row = categories.value.find((item) => item.id === selectedCategoryId.value); if (!row) return; categoryEditingId.value = row.id; resetCategoryForm(row); categoryOpen.value = true }
async function saveCategory() { if (categoryEditingId.value) await putData<void>(`/master-data/categories/${categoryEditingId.value}`, categoryForm); else await postData<void>('/master-data/categories', categoryForm); categoryOpen.value = false; await loadCategories() }
async function removeCategory() { if (!selectedCategoryId.value) return; await deleteData<void>(`/master-data/categories/${selectedCategoryId.value}`); selectedCategoryId.value = null; await loadCategories(); await load() }

watch(() => route.params.kind, async () => { page.value = 1; records.value = []; selectedRow.value = null; selectedCategoryId.value = null; await loadCategories(); await load() })
onMounted(async () => { await loadCategories(); await load() })
</script>

<style scoped>
.master-page { display: flex; gap: 12px; }
.tree-panel { width: 260px; min-height: calc(100vh - 88px); padding: 14px; background: #fff; border: 1px solid #e5e7eb; }
.panel-title { margin-bottom: 12px; font-weight: 600; }
.tree-actions { margin-bottom: 12px; }
.list-panel { flex: 1; min-width: 0; }
.status-select { width: 120px; }
</style>
