<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h1 class="page-title">生产任务</h1>
        <div class="sub-title">自制业务主单，后续领料、工序汇报、质检、入库都会关联到这里</div>
      </div>
      <a-space>
        <a-button @click="load">刷新</a-button>
        <a-button type="primary" @click="openCreate">新建生产任务</a-button>
      </a-space>
    </div>

    <a-form class="filter-bar" layout="inline">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" allow-clear placeholder="任务号 / 产品 / 客户" />
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
      size="middle"
      :loading="loading"
      :columns="columns"
      :data-source="rows"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-if="column.key === 'progress'">
          {{ formatQty(record.finishedQty) }} / {{ formatQty(record.planQty) }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button size="small" :disabled="record.status !== 'DRAFT'" @click="setStatus(record, 'CONFIRMED')">确认</a-button>
            <a-button size="small" :disabled="!['CONFIRMED', 'DRAFT'].includes(record.status)" @click="setStatus(record, 'IN_PROGRESS')">开工</a-button>
            <a-button size="small" :disabled="record.status !== 'IN_PROGRESS'" @click="setStatus(record, 'FINISHED')">完工</a-button>
            <a-button size="small" danger :disabled="record.status === 'FINISHED'" @click="setStatus(record, 'CANCELLED')">作废</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalOpen" title="新建生产任务" width="760px" @ok="submit" @cancel="modalOpen = false">
      <a-form :model="form" layout="vertical">
        <div class="form-grid">
          <a-form-item label="任务日期" required>
            <a-date-picker v-model:value="form.taskDate" value-format="YYYY-MM-DD" class="full" />
          </a-form-item>
          <a-form-item label="优先级">
            <a-select v-model:value="form.priority" :options="priorityOptions" />
          </a-form-item>
          <a-form-item label="产品ID" required>
            <a-input-number v-model:value="form.productId" :min="1" class="full" />
          </a-form-item>
          <a-form-item label="产品名称" required>
            <a-input v-model:value="form.productName" placeholder="例如：五金配件 A 款" />
          </a-form-item>
          <a-form-item label="计划数量" required>
            <a-input-number v-model:value="form.planQty" :min="0.000001" class="full" />
          </a-form-item>
          <a-form-item label="订单数量">
            <a-input-number v-model:value="form.orderedQty" :min="0" class="full" />
          </a-form-item>
          <a-form-item label="客户名称">
            <a-input v-model:value="form.customerName" />
          </a-form-item>
          <a-form-item label="客户订单号">
            <a-input v-model:value="form.customerOrderNo" />
          </a-form-item>
          <a-form-item label="计划开工">
            <a-date-picker v-model:value="form.planStartDate" value-format="YYYY-MM-DD" class="full" />
          </a-form-item>
          <a-form-item label="计划完工">
            <a-date-picker v-model:value="form.planFinishDate" value-format="YYYY-MM-DD" class="full" />
          </a-form-item>
        </div>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, type TableColumnsType } from 'ant-design-vue'
import { changeMfgTaskStatus, createMfgTask, fetchMfgTasks, type MfgTask, type MfgTaskPayload } from '@/api/manufacturing'

const loading = ref(false)
const rows = ref<MfgTask[]>([])
const total = ref(0)
const modalOpen = ref(false)

const query = reactive({
  pageNo: 1,
  pageSize: 20,
  keyword: '',
  status: undefined as string | undefined
})

const form = reactive<MfgTaskPayload>({
  taskDate: new Date().toISOString().slice(0, 10),
  priority: 'NORMAL',
  productId: 1,
  productName: '',
  orderedQty: 0,
  planQty: 1,
  remark: ''
})

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '生产中', value: 'IN_PROGRESS' },
  { label: '已完工', value: 'FINISHED' },
  { label: '已作废', value: 'CANCELLED' }
]

const priorityOptions = [
  { label: '普通', value: 'NORMAL' },
  { label: '加急', value: 'URGENT' },
  { label: '低', value: 'LOW' }
]

const columns: TableColumnsType = [
  { title: '任务号', dataIndex: 'taskNo', width: 150, fixed: 'left' },
  { title: '日期', dataIndex: 'taskDate', width: 110 },
  { title: '产品', dataIndex: 'productName', width: 180 },
  { title: '客户', dataIndex: 'customerName', width: 150 },
  { title: '计划数', dataIndex: 'planQty', width: 100 },
  { title: '完成进度', key: 'progress', width: 130 },
  { title: '状态', key: 'status', width: 100 },
  { title: '计划完工', dataIndex: 'planFinishDate', width: 120 },
  { title: '操作', key: 'action', width: 260, fixed: 'right' }
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
    const data = await fetchMfgTasks(query)
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

function openCreate() {
  modalOpen.value = true
}

async function submit() {
  if (!form.productName || !form.productId || !form.planQty) {
    message.warning('请填写产品和计划数量')
    return
  }
  await createMfgTask(form)
  message.success('生产任务已创建')
  modalOpen.value = false
  await load()
}

async function setStatus(record: MfgTask, status: string) {
  await changeMfgTaskStatus(record.id, status)
  message.success('状态已更新')
  await load()
}

function statusText(status: string) {
  return statusOptions.find((item) => item.value === status)?.label || status
}

function statusColor(status: string) {
  const colors: Record<string, string> = {
    DRAFT: 'default',
    CONFIRMED: 'blue',
    IN_PROGRESS: 'processing',
    FINISHED: 'success',
    CANCELLED: 'error'
  }
  return colors[status] || 'default'
}

function formatQty(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 6 })
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

@media (max-width: 720px) {
  .page-head {
    display: block;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
