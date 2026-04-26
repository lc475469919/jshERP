<template>
  <div class="page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">{{ config.title }}</h1>
        <p class="page-desc">{{ config.description }}</p>
      </div>
      <a-space>
        <a-input-search v-model:value="keyword" placeholder="搜索关键字" allow-clear @search="load" />
        <a-button type="primary" disabled>新增</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :loading="loading"
      :columns="config.columns"
      :data-source="records"
      :pagination="pagination"
      @change="handleTableChange"
    />
  </div>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getData, type PageResult } from '@/api/http'

type RecordRow = Record<string, unknown>

interface ListConfig {
  title: string
  description: string
  endpoint: string
  columns: Array<{ title: string; dataIndex: string; key: string; width?: number }>
}

const route = useRoute()
const loading = ref(false)
const keyword = ref('')
const records = ref<RecordRow[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const configs: Record<string, ListConfig> = {
  users: {
    title: '用户管理',
    description: '维护登录账号、员工身份、启停状态和角色分配。',
    endpoint: '/system/users',
    columns: [
      { title: '账号', dataIndex: 'account', key: 'account', width: 140 },
      { title: '姓名', dataIndex: 'name', key: 'name', width: 140 },
      { title: '手机号', dataIndex: 'mobile', key: 'mobile', width: 160 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
      { title: '最后登录', dataIndex: 'lastLoginAt', key: 'lastLoginAt', width: 190 },
      { title: '备注', dataIndex: 'remark', key: 'remark' }
    ]
  },
  roles: {
    title: '角色管理',
    description: '维护角色编码、名称和后续菜单权限分配。',
    endpoint: '/system/roles',
    columns: [
      { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 180 },
      { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 180 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
      { title: '备注', dataIndex: 'remark', key: 'remark' }
    ]
  },
  dicts: {
    title: '字典管理',
    description: '维护系统状态、菜单类型、单据状态等可配置选项。',
    endpoint: '/system/dicts',
    columns: [
      { title: '字典编码', dataIndex: 'dictCode', key: 'dictCode', width: 200 },
      { title: '字典名称', dataIndex: 'dictName', key: 'dictName', width: 180 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
      { title: '备注', dataIndex: 'remark', key: 'remark' }
    ]
  },
  'number-rules': {
    title: '编号规则',
    description: '维护采购、销售、生产等业务单据的自动编号规则。',
    endpoint: '/system/number-rules',
    columns: [
      { title: '单据类型', dataIndex: 'documentType', key: 'documentType', width: 190 },
      { title: '单据名称', dataIndex: 'documentName', key: 'documentName', width: 160 },
      { title: '前缀', dataIndex: 'prefix', key: 'prefix', width: 100 },
      { title: '日期格式', dataIndex: 'datePattern', key: 'datePattern', width: 140 },
      { title: '流水位数', dataIndex: 'serialLength', key: 'serialLength', width: 110 },
      { title: '当前流水', dataIndex: 'currentSerial', key: 'currentSerial', width: 110 },
      { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
    ]
  },
  logs: {
    title: '操作日志',
    description: '查看系统关键操作记录，为后续审计和追溯做准备。',
    endpoint: '/system/logs',
    columns: [
      { title: '用户', dataIndex: 'userName', key: 'userName', width: 140 },
      { title: '模块', dataIndex: 'moduleName', key: 'moduleName', width: 140 },
      { title: '操作', dataIndex: 'operationType', key: 'operationType', width: 140 },
      { title: '业务编号', dataIndex: 'businessNo', key: 'businessNo', width: 160 },
      { title: '结果', dataIndex: 'requestResult', key: 'requestResult', width: 100 },
      { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 190 }
    ]
  }
}

const config = computed(() => configs[String(route.params.kind)] || configs.users)
const pagination = computed<TablePaginationConfig>(() => ({
  current: page.value,
  pageSize: pageSize.value,
  total: total.value,
  showSizeChanger: true
}))

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

function handleTableChange(next: TablePaginationConfig) {
  page.value = next.current || 1
  pageSize.value = next.pageSize || 20
  load()
}

watch(() => route.params.kind, () => {
  page.value = 1
  records.value = []
  load()
})

onMounted(load)
</script>
