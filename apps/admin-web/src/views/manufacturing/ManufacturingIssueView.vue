<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">生产领料</h1>
        <div class="sub-title">按生产任务生成领料单，确认后回写任务用料状态</div>
      </div>
      <a-space>
        <a-button @click="load">刷新</a-button>
        <a-button type="primary" @click="modalOpen = true">新建领料单</a-button>
      </a-space>
    </div>

    <a-form class="filter-bar" layout="inline">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" allow-clear placeholder="领料单号" />
      </a-form-item>
      <a-form-item label="任务ID">
        <a-input-number v-model:value="query.taskId" :min="1" class="task-input" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear class="status-select" :options="statusOptions" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="search">查询</a-button>
      </a-form-item>
    </a-form>

    <a-table
      row-key="id"
      :loading="loading"
      :columns="columns"
      :data-source="rows"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'CONFIRMED' ? 'success' : record.status === 'CANCELLED' ? 'error' : 'default'">
            {{ statusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" :disabled="record.status !== 'DRAFT'" @click="setStatus(record, 'CONFIRMED')">确认出库</a-button>
            <a-button size="small" danger :disabled="record.status === 'CONFIRMED'" @click="setStatus(record, 'CANCELLED')">作废</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalOpen" title="新建生产领料" width="820px" @ok="submit">
      <a-form :model="form" layout="vertical">
        <div class="form-grid">
          <a-form-item label="生产任务ID" required>
            <a-input-number v-model:value="form.taskId" :min="1" class="full" />
          </a-form-item>
          <a-form-item label="仓库ID" required>
            <a-input-number v-model:value="form.warehouseId" :min="1" class="full" />
          </a-form-item>
          <a-form-item label="领料人">
            <a-input v-model:value="form.issueUserName" />
          </a-form-item>
          <a-form-item label="备注">
            <a-input v-model:value="form.remark" />
          </a-form-item>
        </div>

        <div class="line-head">
          <span>领料明细</span>
          <a-button size="small" @click="addLine">增加物料</a-button>
        </div>
        <div v-for="(line, index) in form.items" :key="index" class="line-row">
          <a-input-number v-model:value="line.materialId" :min="1" placeholder="物料ID" />
          <a-input v-model:value="line.materialName" placeholder="物料名称" />
          <a-input-number v-model:value="line.planQty" :min="0" placeholder="计划数" />
          <a-input-number v-model:value="line.issueQty" :min="0.000001" placeholder="领料数" />
          <a-input v-model:value="line.unitName" placeholder="单位" />
          <a-button danger @click="removeLine(index)">删除</a-button>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, type TableColumnsType } from 'ant-design-vue'
import {
  changeMaterialIssueStatus,
  createMaterialIssue,
  fetchMaterialIssues,
  type MfgMaterialIssue,
  type MfgMaterialIssuePayload
} from '@/api/manufacturing'

const loading = ref(false)
const modalOpen = ref(false)
const rows = ref<MfgMaterialIssue[]>([])
const total = ref(0)

const query = reactive({
  pageNo: 1,
  pageSize: 20,
  keyword: '',
  taskId: undefined as number | undefined,
  status: undefined as string | undefined
})

const form = reactive<MfgMaterialIssuePayload>({
  taskId: 1,
  warehouseId: 1,
  issueUserName: '',
  remark: '',
  items: [{ materialId: 1, materialName: '', planQty: 0, issueQty: 1, unitName: '' }]
})

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已作废', value: 'CANCELLED' }
]

const columns: TableColumnsType = [
  { title: '领料单号', dataIndex: 'issueNo', width: 170 },
  { title: '任务ID', dataIndex: 'taskId', width: 100 },
  { title: '仓库ID', dataIndex: 'warehouseId', width: 100 },
  { title: '领料人', dataIndex: 'issueUserName', width: 120 },
  { title: '领料时间', dataIndex: 'issueTime', width: 180 },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true
}))

async function load() {
  loading.value = true
  try {
    const data = await fetchMaterialIssues(query)
    rows.value = data.rows
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNo = 1
  load()
}

function handleTableChange(page: { current?: number; pageSize?: number }) {
  query.pageNo = page.current || 1
  query.pageSize = page.pageSize || 20
  load()
}

function addLine() {
  form.items.push({ materialId: 1, materialName: '', planQty: 0, issueQty: 1, unitName: '' })
}

function removeLine(index: number) {
  if (form.items.length > 1) {
    form.items.splice(index, 1)
  }
}

async function submit() {
  if (!form.taskId || !form.warehouseId || form.items.some((item) => !item.materialId || !item.issueQty)) {
    message.warning('请填写任务、仓库和领料明细')
    return
  }
  await createMaterialIssue(form)
  message.success('领料单已创建')
  modalOpen.value = false
  await load()
}

async function setStatus(record: MfgMaterialIssue, status: string) {
  await changeMaterialIssueStatus(record.id, status)
  message.success('状态已更新')
  await load()
}

function statusText(status: string) {
  return statusOptions.find((item) => item.value === status)?.label || status
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.sub-title {
  color: #667085;
  font-size: 13px;
}

.filter-bar {
  margin-bottom: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.task-input,
.status-select {
  width: 140px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.full {
  width: 100%;
}

.line-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 6px 0 10px;
  font-weight: 600;
}

.line-row {
  display: grid;
  grid-template-columns: 100px minmax(160px, 1fr) 110px 110px 90px 70px;
  gap: 8px;
  margin-bottom: 8px;
}
</style>
