<template>
  <div class="page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">{{ config.title }}</h1>
        <p class="page-desc">{{ config.description }}</p>
      </div>
      <a-space>
        <a-input-search v-model:value="keyword" placeholder="搜索关键字" allow-clear @search="load" />
        <a-button v-if="config.editable" v-permission="config.permissions.add" type="primary" @click="openCreate">新增</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :loading="loading"
      :columns="tableColumns"
      :data-source="records"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'operation'">
          <a-space>
            <a-button v-permission="config.permissions.edit" type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-popconfirm title="确认删除这条数据？" @confirm="remove(record)">
              <a-button v-permission="config.permissions.delete" type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-drawer
      v-model:open="drawerOpen"
      width="520"
      :title="editingId ? `编辑${config.shortTitle}` : `新增${config.shortTitle}`"
      destroy-on-close
    >
      <a-form layout="vertical" :model="form">
        <a-form-item v-for="field in config.fields" :key="field.key" :label="field.label">
          <a-input
            v-if="field.type === 'text'"
            v-model:value="form[field.key]"
            :disabled="Boolean(editingId && field.createOnly)"
          />
          <a-input-password v-else-if="field.type === 'password'" v-model:value="form[field.key]" />
          <a-input-number v-else-if="field.type === 'number'" v-model:value="form[field.key]" class="full-input" />
          <a-select v-else-if="field.type === 'status'" v-model:value="form[field.key]">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
          <a-tree
            v-else-if="field.type === 'menuTree'"
            v-model:checkedKeys="form[field.key]"
            checkable
            default-expand-all
            :tree-data="menuTreeOptions"
          />
          <a-table
            v-else-if="field.type === 'dictItems'"
            row-key="id"
            size="small"
            :columns="dictItemColumns"
            :data-source="dictItems"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '停用' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'operation'">
                <a-space>
                  <a-button type="link" size="small" @click="openDictItemEdit(record)">编辑</a-button>
                  <a-popconfirm title="确认删除这个字典项？" @confirm="removeDictItem(record)">
                    <a-button type="link" size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
          <a-textarea v-else v-model:value="form[field.key]" :rows="3" />
        </a-form-item>
        <a-button v-if="config.kind === 'dicts' && editingId" block @click="openDictItemCreate">新增字典项</a-button>
      </a-form>
      <template #extra>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="save">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal
      v-model:open="dictItemOpen"
      :title="dictItemEditingId ? '编辑字典项' : '新增字典项'"
      :confirm-loading="dictItemSaving"
      @ok="saveDictItem"
    >
      <a-form layout="vertical" :model="dictItemForm">
        <a-form-item label="字典项名称">
          <a-input v-model:value="dictItemForm.itemLabel" />
        </a-form-item>
        <a-form-item label="字典项值">
          <a-input v-model:value="dictItemForm.itemValue" />
        </a-form-item>
        <a-form-item label="颜色">
          <a-input v-model:value="dictItemForm.color" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="dictItemForm.sortOrder" class="full-input" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="dictItemForm.status">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="dictItemForm.remark" :rows="3" />
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

type RecordRow = Record<string, any>
type FieldType = 'text' | 'password' | 'number' | 'status' | 'textarea' | 'menuTree' | 'dictItems'

interface FieldConfig {
  key: string
  label: string
  type: FieldType
  createOnly?: boolean
  defaultValue?: unknown
}

interface ListConfig {
  kind: string
  title: string
  shortTitle: string
  description: string
  endpoint: string
  editable: boolean
  permissions: { add: string; edit: string; delete: string }
  columns: Array<{ title: string; dataIndex: string; key: string; width?: number }>
  fields: FieldConfig[]
}

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const drawerOpen = ref(false)
const dictItemOpen = ref(false)
const keyword = ref('')
const records = ref<RecordRow[]>([])
const dictItems = ref<RecordRow[]>([])
const menuTree = ref<RecordRow[]>([])
const form = reactive<RecordRow>({})
const dictItemForm = reactive<RecordRow>({})
const editingId = ref<number | null>(null)
const dictItemEditingId = ref<number | null>(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dictItemSaving = ref(false)

const configs: Record<string, ListConfig> = {
  users: {
    kind: 'users',
    title: '用户管理',
    shortTitle: '用户',
    description: '维护登录账号、员工身份、启停状态和角色分配。',
    endpoint: '/system/users',
    editable: true,
    permissions: { add: 'system:user:add', edit: 'system:user:edit', delete: 'system:user:delete' },
    columns: [
      { title: '账号', dataIndex: 'account', key: 'account', width: 140 },
      { title: '姓名', dataIndex: 'name', key: 'name', width: 140 },
      { title: '手机号', dataIndex: 'mobile', key: 'mobile', width: 160 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
      { title: '最后登录', dataIndex: 'lastLoginAt', key: 'lastLoginAt', width: 190 },
      { title: '备注', dataIndex: 'remark', key: 'remark' }
    ],
    fields: [
      { key: 'account', label: '账号', type: 'text', createOnly: true },
      { key: 'name', label: '姓名', type: 'text' },
      { key: 'mobile', label: '手机号', type: 'text' },
      { key: 'password', label: '密码', type: 'password' },
      { key: 'status', label: '状态', type: 'status', defaultValue: 1 },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  roles: {
    kind: 'roles',
    title: '角色管理',
    shortTitle: '角色',
    description: '维护角色编码、名称和后续菜单权限分配。',
    endpoint: '/system/roles',
    editable: true,
    permissions: { add: 'system:role:add', edit: 'system:role:edit', delete: 'system:role:delete' },
    columns: [
      { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 180 },
      { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 180 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
      { title: '备注', dataIndex: 'remark', key: 'remark' }
    ],
    fields: [
      { key: 'roleCode', label: '角色编码', type: 'text', createOnly: true },
      { key: 'roleName', label: '角色名称', type: 'text' },
      { key: 'status', label: '状态', type: 'status', defaultValue: 1 },
      { key: 'remark', label: '备注', type: 'textarea' },
      { key: 'menuIds', label: '菜单和按钮权限', type: 'menuTree', defaultValue: [] }
    ]
  },
  dicts: {
    kind: 'dicts',
    title: '字典管理',
    shortTitle: '字典',
    description: '维护系统状态、菜单类型、单据状态等可配置选项。',
    endpoint: '/system/dicts',
    editable: true,
    permissions: { add: 'system:dict:add', edit: 'system:dict:edit', delete: 'system:dict:delete' },
    columns: [
      { title: '字典编码', dataIndex: 'dictCode', key: 'dictCode', width: 200 },
      { title: '字典名称', dataIndex: 'dictName', key: 'dictName', width: 180 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
      { title: '备注', dataIndex: 'remark', key: 'remark' }
    ],
    fields: [
      { key: 'dictCode', label: '字典编码', type: 'text', createOnly: true },
      { key: 'dictName', label: '字典名称', type: 'text' },
      { key: 'status', label: '状态', type: 'status', defaultValue: 1 },
      { key: 'remark', label: '备注', type: 'textarea' },
      { key: 'dictItems', label: '字典项', type: 'dictItems' }
    ]
  },
  'number-rules': {
    kind: 'number-rules',
    title: '编号规则',
    shortTitle: '编号规则',
    description: '维护采购、销售、生产等业务单据的自动编号规则。',
    endpoint: '/system/number-rules',
    editable: true,
    permissions: { add: 'system:number-rule:add', edit: 'system:number-rule:edit', delete: 'system:number-rule:delete' },
    columns: [
      { title: '单据类型', dataIndex: 'documentType', key: 'documentType', width: 190 },
      { title: '单据名称', dataIndex: 'documentName', key: 'documentName', width: 160 },
      { title: '前缀', dataIndex: 'prefix', key: 'prefix', width: 100 },
      { title: '日期格式', dataIndex: 'datePattern', key: 'datePattern', width: 140 },
      { title: '流水位数', dataIndex: 'serialLength', key: 'serialLength', width: 110 },
      { title: '当前流水', dataIndex: 'currentSerial', key: 'currentSerial', width: 110 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
    ],
    fields: [
      { key: 'documentType', label: '单据类型', type: 'text', createOnly: true },
      { key: 'documentName', label: '单据名称', type: 'text' },
      { key: 'prefix', label: '前缀', type: 'text' },
      { key: 'datePattern', label: '日期格式', type: 'text', defaultValue: 'yyyyMMdd' },
      { key: 'serialLength', label: '流水位数', type: 'number', defaultValue: 4 },
      { key: 'currentSerial', label: '当前流水', type: 'number', defaultValue: 0 },
      { key: 'resetDaily', label: '按日重置', type: 'status', defaultValue: 1 },
      { key: 'status', label: '状态', type: 'status', defaultValue: 1 },
      { key: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  logs: {
    kind: 'logs',
    title: '操作日志',
    shortTitle: '操作日志',
    description: '查看系统关键操作记录，为后续审计和追溯做准备。',
    endpoint: '/system/logs',
    editable: false,
    permissions: { add: '', edit: '', delete: '' },
    columns: [
      { title: '用户', dataIndex: 'userName', key: 'userName', width: 140 },
      { title: '模块', dataIndex: 'moduleName', key: 'moduleName', width: 140 },
      { title: '操作', dataIndex: 'operationType', key: 'operationType', width: 140 },
      { title: '业务编号', dataIndex: 'businessNo', key: 'businessNo', width: 160 },
      { title: '结果', dataIndex: 'requestResult', key: 'requestResult', width: 100 },
      { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 190 }
    ],
    fields: []
  }
}

const config = computed(() => configs[String(route.params.kind)] || configs.users)
const tableColumns = computed(() => {
  if (!config.value.editable) return config.value.columns
  return [...config.value.columns, { title: '操作', dataIndex: 'operation', key: 'operation', width: 150 }]
})
const menuTreeOptions = computed(() => toTreeOptions(menuTree.value))
const pagination = computed<TablePaginationConfig>(() => ({
  current: page.value,
  pageSize: pageSize.value,
  total: total.value,
  showSizeChanger: true
}))
const dictItemColumns = [
  { title: '名称', dataIndex: 'itemLabel', key: 'itemLabel', width: 120 },
  { title: '值', dataIndex: 'itemValue', key: 'itemValue', width: 100 },
  { title: '颜色', dataIndex: 'color', key: 'color', width: 90 },
  { title: '排序', dataIndex: 'sortOrder', key: 'sortOrder', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', dataIndex: 'operation', key: 'operation', width: 130 }
]

function toTreeOptions(nodes: RecordRow[]): Array<{ title: string; key: number; children?: any[] }> {
  return nodes.map((node) => ({
    title: node.menuName,
    key: node.id,
    children: node.children?.length ? toTreeOptions(node.children) : undefined
  }))
}

async function load() {
  loading.value = true
  try {
    const result = await getData<PageResult<RecordRow>>(config.value.endpoint, {
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function resetForm(row?: RecordRow) {
  Object.keys(form).forEach((key) => delete form[key])
  config.value.fields.forEach((field) => {
    form[field.key] = row?.[field.key] ?? field.defaultValue ?? undefined
  })
}

async function openCreate() {
  editingId.value = null
  dictItems.value = []
  resetForm()
  if (config.value.kind === 'roles') {
    menuTree.value = await getData<RecordRow[]>('/system/menus/tree')
  }
  drawerOpen.value = true
}

async function openEdit(row: RecordRow) {
  editingId.value = Number(row.id)
  resetForm(row)
  if (config.value.kind === 'roles') {
    menuTree.value = await getData<RecordRow[]>('/system/menus/tree')
    form.menuIds = await getData<number[]>(`/system/roles/${editingId.value}/menu-ids`)
  }
  if (config.value.kind === 'dicts') {
    await loadDictItems()
  }
  drawerOpen.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await putData<void>(`${config.value.endpoint}/${editingId.value}`, form)
    } else {
      await postData<void>(config.value.endpoint, form)
    }
    message.success('保存成功')
    drawerOpen.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function loadDictItems() {
  if (!editingId.value) {
    dictItems.value = []
    return
  }
  dictItems.value = await getData<RecordRow[]>(`/system/dicts/${editingId.value}/items`)
}

function openDictItemCreate() {
  dictItemEditingId.value = null
  Object.keys(dictItemForm).forEach((key) => delete dictItemForm[key])
  Object.assign(dictItemForm, { itemLabel: '', itemValue: '', color: '', sortOrder: 0, status: 1, remark: '' })
  dictItemOpen.value = true
}

function openDictItemEdit(row: RecordRow) {
  dictItemEditingId.value = Number(row.id)
  Object.keys(dictItemForm).forEach((key) => delete dictItemForm[key])
  Object.assign(dictItemForm, row)
  dictItemOpen.value = true
}

async function saveDictItem() {
  if (!editingId.value) return
  dictItemSaving.value = true
  try {
    if (dictItemEditingId.value) {
      await putData<void>(`/system/dicts/items/${dictItemEditingId.value}`, {
        ...dictItemForm,
        dictTypeId: editingId.value
      })
    } else {
      await postData<void>(`/system/dicts/${editingId.value}/items`, dictItemForm)
    }
    message.success('保存成功')
    dictItemOpen.value = false
    await loadDictItems()
  } finally {
    dictItemSaving.value = false
  }
}

async function removeDictItem(row: RecordRow) {
  await deleteData<void>(`/system/dicts/items/${row.id}`)
  message.success('删除成功')
  await loadDictItems()
}

async function remove(row: RecordRow) {
  await deleteData<void>(`${config.value.endpoint}/${row.id}`)
  message.success('删除成功')
  await load()
}

function handleTableChange(next: TablePaginationConfig) {
  page.value = next.current || 1
  pageSize.value = next.pageSize || 20
  load()
}

watch(() => route.params.kind, () => {
  page.value = 1
  records.value = []
  drawerOpen.value = false
  load()
})

onMounted(load)
</script>
