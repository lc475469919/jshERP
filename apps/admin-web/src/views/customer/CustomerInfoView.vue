<template>
  <div class="page customer-page">
    <aside class="side-panel">
      <section class="side-section">
        <div class="side-title">
          <span>客户类别</span>
          <a-space>
            <a-button size="small" @click="openCategoryCreate">新增</a-button>
            <a-button size="small" :disabled="!selectedCategoryId" @click="openCategoryEdit">修改</a-button>
            <a-popconfirm title="确认删除客户类别？" @confirm="removeCategory">
              <a-button size="small" danger :disabled="!selectedCategoryId">删除</a-button>
            </a-popconfirm>
          </a-space>
        </div>
        <a-tree
          block-node
          :tree-data="categoryTree"
          :selected-keys="selectedCategoryId ? [selectedCategoryId] : []"
          @select="selectCategory"
        />
      </section>

      <section class="side-section">
        <div class="side-title">
          <span>客户标签</span>
          <a-button size="small" @click="tagOpen = true">新增</a-button>
        </div>
        <div class="tag-list">
          <button v-for="tag in customerTags" :key="tag" type="button" @click="filters.tags = tag">{{ tag }}</button>
        </div>
      </section>
    </aside>

    <section class="list-panel">
      <div class="page-toolbar">
        <div>
          <h1 class="page-title">客户信息</h1>
          <p class="page-desc">参考系统路径：客户 / 客户管理 / 客户信息。</p>
        </div>
      </div>

      <div class="filter-panel">
        <a-space wrap>
          <a-input v-model:value="filters.customerCode" placeholder="客户编号" allow-clear @press-enter="load" />
          <a-input v-model:value="filters.customerName" placeholder="客户名称" allow-clear @press-enter="load" />
          <a-input v-model:value="filters.contactName" placeholder="联系人名称" allow-clear @press-enter="load" />
          <a-input v-model:value="filters.contactPhone" placeholder="联系人座机/手机号" allow-clear @press-enter="load" />
          <a-input v-model:value="filters.country" placeholder="国家/地区" allow-clear @press-enter="load" />
          <a-input v-model:value="filters.address" placeholder="客户地址" allow-clear @press-enter="load" />
          <a-input v-model:value="filters.salespersonName" placeholder="选择业务员" allow-clear @press-enter="load" />
          <a-select v-model:value="filters.status" class="status-select" allow-clear placeholder="选择客户状态" @change="load">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
          <a-button type="primary" @click="load">查询</a-button>
          <a-button @click="resetSearch">重置</a-button>
        </a-space>
      </div>

      <div class="action-toolbar">
        <a-space wrap>
          <a-button type="primary" @click="openCreate">新增</a-button>
          <a-button :disabled="!selectedRow" @click="openEdit">修改</a-button>
          <a-popconfirm title="确认删除选中客户？" @confirm="remove">
            <a-button danger :disabled="!selectedRow">删除</a-button>
          </a-popconfirm>
          <a-button @click="importOpen = true">导入</a-button>
          <a-button :disabled="!selectedRow">转为会员</a-button>
          <a-button :disabled="!selectedRow">重置登录密码</a-button>
          <a-dropdown>
            <a-button :disabled="!selectedRows.length">批量操作</a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="enable" @click="setStatus(1)">启用</a-menu-item>
                <a-menu-item key="disable" @click="setStatus(0)">停用</a-menu-item>
                <a-menu-item key="return" disabled>退回公海</a-menu-item>
                <a-menu-item key="formal" disabled>转为正式客户</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-button>导出</a-button>
        </a-space>
      </div>

      <a-table
        row-key="id"
        size="middle"
        :loading="loading"
        :columns="columns"
        :data-source="records"
        :pagination="pagination"
        :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
        :scroll="{ x: 2280 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record, text }">
          <a-button v-if="column.key === 'operation'" type="link" size="small" @click="openEdit(record)">修改</a-button>
          <a-tag v-else-if="column.key === 'status'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '启用' : '停用' }}</a-tag>
          <a-tag v-else-if="column.key === 'selfOrderEnabled'" :color="text === 1 ? 'blue' : 'default'">{{ text === 1 ? '是' : '否' }}</a-tag>
          <a-tag v-else-if="column.key === 'invoiceEnabled'" :color="text === 1 ? 'blue' : 'default'">{{ text === 1 ? '启用' : '未启用' }}</a-tag>
          <span v-else>{{ text }}</span>
        </template>
      </a-table>
    </section>

    <a-drawer v-model:open="drawerOpen" width="720" :title="editingId ? '修改客户' : '新增客户'">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="编号"><a-input v-model:value="form.customerCode" placeholder="由系统生成/手工录入" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="名称"><a-input v-model:value="form.customerName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="类别"><a-select v-model:value="form.categoryId" allow-clear :options="categoryOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="业务员"><a-input v-model:value="form.salespersonName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="联系人"><a-input v-model:value="form.contactName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="手机号码"><a-input v-model:value="form.mobile" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="座机"><a-input v-model:value="form.telephone" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="电子邮箱"><a-input v-model:value="form.email" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="国家/地区"><a-input v-model:value="form.country" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="关联供应商"><a-input v-model:value="form.linkedSupplier" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="地址"><a-input v-model:value="form.address" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="物流公司"><a-input v-model:value="form.logisticsCompany" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="会员等级"><a-input v-model:value="form.memberLevel" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="自助下单"><a-select v-model:value="form.selfOrderEnabled"><a-select-option :value="1">是</a-select-option><a-select-option :value="0">否</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="登录账号"><a-input v-model:value="form.loginAccount" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="信用额度(￥)"><a-input-number v-model:value="form.creditLimit" class="full-input" :min="0" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="预收款(￥)"><a-input-number v-model:value="form.prepaidAmount" class="full-input" :min="0" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="发票管理"><a-select v-model:value="form.invoiceEnabled"><a-select-option :value="1">启用</a-select-option><a-select-option :value="0">未启用</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="状态"><a-select v-model:value="form.status"><a-select-option :value="1">启用</a-select-option><a-select-option :value="0">停用</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="标签"><a-input v-model:value="form.tags" placeholder="多个标签用逗号分隔" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remark" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>
      <template #extra>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="save">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="categoryOpen" :title="categoryEditingId ? '修改客户类别' : '新增客户类别'" @ok="saveCategory">
      <a-form layout="vertical" :model="categoryForm">
        <a-form-item label="类别编号"><a-input v-model:value="categoryForm.categoryCode" /></a-form-item>
        <a-form-item label="类别名称"><a-input v-model:value="categoryForm.categoryName" /></a-form-item>
        <a-form-item label="排序"><a-input-number v-model:value="categoryForm.sortOrder" class="full-input" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="tagOpen" title="新增客户标签" @ok="addTag">
      <a-form layout="vertical">
        <a-form-item label="标签名称"><a-input v-model:value="tagName" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="importOpen" title="客户导入" @ok="importOpen = false">
      <div class="import-guide">
        <p>1、下载固定模板制作您的导入信息表格。</p>
        <p>2、点击“请选择 xlsx 格式的文件”，选择文件后确认导入。</p>
        <a-button>下载模板</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteData, getData, postData, putData, type PageResult } from '@/api/http'

type Row = Record<string, any>

const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const categoryOpen = ref(false)
const tagOpen = ref(false)
const importOpen = ref(false)
const editingId = ref<number | null>(null)
const categoryEditingId = ref<number | null>(null)
const selectedCategoryId = ref<number | null>(null)
const selectedRow = ref<Row | null>(null)
const selectedRows = ref<Row[]>([])
const selectedRowKeys = ref<number[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const records = ref<Row[]>([])
const categories = ref<Row[]>([])
const customerTags = ref(['A类', 'B类', 'C类', 'D类', '重点客户'])
const tagName = ref('')
const filters = reactive<Row>({ status: 1 })
const form = reactive<Row>({})
const categoryForm = reactive<Row>({})

const columns = [
  { key: 'operation', title: '操作', width: 70, fixed: 'left' as const },
  { dataIndex: 'customerCode', key: 'customerCode', title: '编号', width: 100, fixed: 'left' as const },
  { dataIndex: 'customerName', key: 'customerName', title: '名称', width: 150, fixed: 'left' as const },
  { dataIndex: 'categoryId', key: 'categoryId', title: '类别', width: 90 },
  { dataIndex: 'contactName', key: 'contactName', title: '联系人', width: 100 },
  { dataIndex: 'mobile', key: 'mobile', title: '手机号码', width: 120 },
  { dataIndex: 'address', key: 'address', title: '地址', width: 160 },
  { dataIndex: 'salespersonName', key: 'salespersonName', title: '业务员', width: 100 },
  { dataIndex: 'prepaidAmount', key: 'prepaidAmount', title: '预收款(￥)', width: 110 },
  { dataIndex: 'memberLevel', key: 'memberLevel', title: '会员等级', width: 100 },
  { dataIndex: 'selfOrderEnabled', key: 'selfOrderEnabled', title: '自助下单', width: 90 },
  { dataIndex: 'loginAccount', key: 'loginAccount', title: '登录账号', width: 110 },
  { dataIndex: 'creditLimit', key: 'creditLimit', title: '信用额度(￥)', width: 120 },
  { dataIndex: 'invoiceEnabled', key: 'invoiceEnabled', title: '发票管理', width: 90 },
  { dataIndex: 'status', key: 'status', title: '状态', width: 80 },
  { dataIndex: 'remark', key: 'remark', title: '备注', width: 160 },
  { dataIndex: 'country', key: 'country', title: '国家/地区', width: 100 },
  { dataIndex: 'tags', key: 'tags', title: '标签', width: 130 },
  { dataIndex: 'followCount', key: 'followCount', title: '跟进次数', width: 90 },
  { dataIndex: 'totalDealAmount', key: 'totalDealAmount', title: '累计成交额', width: 120 },
  { dataIndex: 'lastDealDate', key: 'lastDealDate', title: '最近交易', width: 130 },
  { dataIndex: 'linkedSupplier', key: 'linkedSupplier', title: '关联供应商', width: 130 }
]

const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: total.value, showSizeChanger: true }))
const categoryTree = computed(() => categories.value.map((item) => ({ title: item.categoryName, key: item.id })))
const categoryOptions = computed(() => categories.value.map((item) => ({ label: item.categoryName, value: item.id })))

async function load() {
  loading.value = true
  try {
    const params = { ...filters, page: page.value, pageSize: pageSize.value, categoryId: selectedCategoryId.value || undefined }
    const result = await getData<PageResult<Row>>('/master-data/customers', params)
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  categories.value = await getData<Row[]>('/master-data/categories', { categoryType: 'CUS' })
}

function resetForm(row?: Row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, {
    customerCode: row?.customerCode,
    customerName: row?.customerName,
    categoryId: row?.categoryId ?? selectedCategoryId.value,
    contactName: row?.contactName,
    mobile: row?.mobile,
    telephone: row?.telephone,
    email: row?.email,
    country: row?.country,
    address: row?.address,
    salespersonName: row?.salespersonName,
    logisticsCompany: row?.logisticsCompany,
    linkedSupplier: row?.linkedSupplier,
    prepaidAmount: row?.prepaidAmount ?? 0,
    memberLevel: row?.memberLevel,
    selfOrderEnabled: row?.selfOrderEnabled ?? 0,
    loginAccount: row?.loginAccount,
    creditLimit: row?.creditLimit ?? 0,
    invoiceEnabled: row?.invoiceEnabled ?? 0,
    status: row?.status ?? 1,
    tags: row?.tags,
    remark: row?.remark
  })
}

function openCreate() { editingId.value = null; resetForm(); drawerOpen.value = true }
function openEdit(row?: Row) { const target = row || selectedRow.value; if (!target) return; editingId.value = target.id; resetForm(target); drawerOpen.value = true }

async function save() {
  saving.value = true
  try {
    if (editingId.value) await putData<void>(`/master-data/customers/${editingId.value}`, form)
    else await postData<void>('/master-data/customers', form)
    message.success('保存成功')
    drawerOpen.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function remove() {
  if (!selectedRow.value) return
  await deleteData<void>(`/master-data/customers/${selectedRow.value.id}`)
  message.success('删除成功')
  selectedRow.value = null
  selectedRows.value = []
  selectedRowKeys.value = []
  await load()
}

async function setStatus(value: number) {
  const targets = selectedRows.value.length ? selectedRows.value : selectedRow.value ? [selectedRow.value] : []
  await Promise.all(targets.map((row) => putData<void>(`/master-data/customers/${row.id}`, { ...row, status: value })))
  await load()
}

function resetSearch() {
  Object.keys(filters).forEach((key) => delete filters[key])
  filters.status = 1
  selectedCategoryId.value = null
  page.value = 1
  load()
}

function onSelectChange(keys: any[], rows: Row[]) {
  selectedRowKeys.value = keys as number[]
  selectedRows.value = rows
  selectedRow.value = rows[0] || null
}

function handleTableChange(next: TablePaginationConfig) {
  page.value = next.current || 1
  pageSize.value = next.pageSize || 20
  load()
}

function selectCategory(keys: any[]) {
  selectedCategoryId.value = keys[0] || null
  load()
}

function resetCategoryForm(row?: Row) {
  Object.keys(categoryForm).forEach((key) => delete categoryForm[key])
  Object.assign(categoryForm, { categoryType: 'CUS', parentId: 0, categoryCode: row?.categoryCode, categoryName: row?.categoryName, sortOrder: row?.sortOrder ?? 0, saleEnabled: 1, purchaseEnabled: 1 })
}

function openCategoryCreate() { categoryEditingId.value = null; resetCategoryForm(); categoryOpen.value = true }
function openCategoryEdit() { const row = categories.value.find((item) => item.id === selectedCategoryId.value); if (!row) return; categoryEditingId.value = row.id; resetCategoryForm(row); categoryOpen.value = true }
async function saveCategory() { if (categoryEditingId.value) await putData<void>(`/master-data/categories/${categoryEditingId.value}`, categoryForm); else await postData<void>('/master-data/categories', categoryForm); categoryOpen.value = false; await loadCategories() }
async function removeCategory() { if (!selectedCategoryId.value) return; await deleteData<void>(`/master-data/categories/${selectedCategoryId.value}`); selectedCategoryId.value = null; await loadCategories(); await load() }

function addTag() {
  const value = tagName.value.trim()
  if (value && !customerTags.value.includes(value)) customerTags.value.push(value)
  tagName.value = ''
  tagOpen.value = false
}

onMounted(async () => { await loadCategories(); await load() })
</script>

<style scoped>
.customer-page { display: flex; gap: 12px; padding: 14px; }
.side-panel { width: 280px; display: grid; gap: 12px; align-content: start; }
.side-section { padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.side-title { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; font-weight: 600; }
.tag-list { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-list button { height: 26px; padding: 0 10px; color: #475467; background: #f3f6fa; border: 1px solid #dfe6ee; cursor: pointer; }
.list-panel { flex: 1; min-width: 0; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.filter-panel :deep(.ant-input) { width: 170px; }
.status-select { width: 150px; }
.import-guide p { margin: 0 0 8px; color: #475467; }
</style>
