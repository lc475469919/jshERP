<template>
  <div class="page sales-quote-page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">报价单</h1>
        <p class="page-desc">参考系统路径：销售 / 批发业务 / 报价单。</p>
      </div>
    </div>

    <div class="filter-panel">
      <a-space wrap>
        <span class="field-label">报价日期</span>
        <a-date-picker v-model:value="filters.djrqq" class="date-input" />
        <span class="date-separator">-</span>
        <a-date-picker v-model:value="filters.djrqz" class="date-input" />
        <a-input v-model:value="filters.djbh" class="quote-code" placeholder="报价单号" allow-clear @press-enter="query" />
        <a-select v-model:value="filters.shzt" class="status-select" placeholder="审核状态" @change="query">
          <a-select-option value="">审核状态：</a-select-option>
          <a-select-option value="0">未审核</a-select-option>
          <a-select-option value="1">已审核</a-select-option>
        </a-select>
        <a-input v-model:value="filters.spMc" class="goods-input" placeholder="商品编号/名称/助记码/规格" allow-clear @press-enter="query" />
        <a-input-group compact class="customer-picker">
          <a-input v-model:value="filters.khMc" class="customer-input" placeholder="客户名称" readonly />
          <a-button @click="customerOpen = true">...</a-button>
        </a-input-group>
        <a-button type="primary" @click="query">查询</a-button>
      </a-space>
    </div>

    <div class="action-toolbar">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增</a-button>
        <a-button :disabled="!selectedRows.length" @click="copyQuote">复制</a-button>
        <a-button :disabled="!selectedRows.length" @click="auditQuotes">审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="reverseAuditQuotes">反审核</a-button>
        <a-button danger :disabled="!selectedRows.length" @click="generateSalesBill">生成销售单</a-button>
        <a-button danger :disabled="!selectedRows.length" @click="generateSalesOrder">生成销售订单</a-button>
        <a-button @click="exportQuotes">导出</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :columns="columns"
      :data-source="records"
      :pagination="pagination"
      :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ x: 1420 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, text }">
        <a-space v-if="column.key === 'operation'" size="small">
          <a-button type="link" size="small" @click="openEdit(record)">修改</a-button>
          <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
        </a-space>
        <a-tag v-else-if="column.key === 'shzt'" :color="text === 1 ? 'green' : 'red'">{{ text === 1 ? '已审核' : '未审核' }}</a-tag>
        <a-tag v-else-if="column.key === 'scxsd' || column.key === 'scxsdd'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '已生成' : '未生成' }}</a-tag>
        <a-button v-else-if="column.key === 'id2'" type="link" size="small" @click="attachmentOpen = true">附件</a-button>
        <span v-else>{{ text }}</span>
      </template>
    </a-table>

    <a-drawer v-model:open="quoteOpen" width="860" :title="editingId ? '修改报价单' : '新增报价单'">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="报价单号"><a-input v-model:value="form.djbh" placeholder="由编号规则生成" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="报价日期"><a-date-picker v-model:value="form.djrq" class="full-input" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="币种"><a-input v-model:value="form.bz" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户名称"><a-input v-model:value="form.khMc" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="客户联系人"><a-input v-model:value="form.khLxr" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="客户电话"><a-input v-model:value="form.khPhone" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="支付方式"><a-input v-model:value="form.zffs" /></a-form-item></a-col>
          <a-col :span="16"><a-form-item label="备注"><a-input v-model:value="form.remarks" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="detail-title">商品报价明细</div>
      <a-table size="small" :columns="detailColumns" :data-source="detailRows" :pagination="false" :scroll="{ x: 980 }" />

      <template #extra>
        <a-space>
          <a-button @click="quoteOpen = false">取消</a-button>
          <a-button type="primary" @click="saveQuote">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="customerOpen" title="选择客户" @ok="chooseCustomer">
      <a-space class="modal-filter" wrap>
        <a-select class="modal-select" placeholder="选择客户类别">
          <a-select-option value="">选择客户类别</a-select-option>
          <a-select-option value="local">本省客户</a-select-option>
          <a-select-option value="normal">普通客户</a-select-option>
          <a-select-option value="important">重点客户</a-select-option>
          <a-select-option value="retail">零售客户</a-select-option>
        </a-select>
        <a-input class="modal-input" placeholder="客户名称/联系人/电话" />
        <a-button type="primary">查询</a-button>
        <a-button>添加</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="customerColumns" :data-source="customers" :pagination="false" @row="customerRow" />
    </a-modal>

    <a-modal v-model:open="attachmentOpen" title="附件" @ok="attachmentOpen = false">
      <div class="action-toolbar inline-toolbar">
        <a-button type="primary">添加附件</a-button>
      </div>
      <a-table size="small" row-key="id" :columns="attachmentColumns" :data-source="attachments" :pagination="false" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { computed, reactive, ref } from 'vue'

type Row = Record<string, any>

const filters = reactive<Row>({ shzt: '' })
const form = reactive<Row>({})
const quoteOpen = ref(false)
const customerOpen = ref(false)
const attachmentOpen = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<Row[]>([])
const selectedCustomer = ref<Row | null>(null)
const page = ref(1)
const pageSize = ref(20)

const records = ref<Row[]>([
  {
    id: 1,
    djrq: '2026-04-28',
    djbh: 'BJ-20260428-0001',
    khMc: '杭州示例客户',
    khLxr: '赵六',
    khPhone: '13800000000',
    zffs: '月结',
    bz: '人民币',
    shzt: 0,
    shrMc: '',
    shDate: '',
    zdrMc: '管理员',
    remarks: '按参考系统字段展示',
    scxsd: 0,
    scxsdd: 0
  }
])

const columns = [
  { key: 'operation', title: '操作', width: 90, fixed: 'left' as const },
  { dataIndex: 'djrq', key: 'djrq', title: '报价日期', width: 90, fixed: 'left' as const },
  { dataIndex: 'djbh', key: 'djbh', title: '报价单号', width: 110, fixed: 'left' as const },
  { dataIndex: 'khMc', key: 'khMc', title: '客户名称', width: 90 },
  { dataIndex: 'khLxr', key: 'khLxr', title: '客户联系人', width: 90 },
  { dataIndex: 'khPhone', key: 'khPhone', title: '客户电话', width: 90 },
  { dataIndex: 'zffs', key: 'zffs', title: '支付方式', width: 90 },
  { dataIndex: 'bz', key: 'bz', title: '币种', width: 90 },
  { dataIndex: 'id2', key: 'id2', title: '附件', width: 70 },
  { dataIndex: 'shzt', key: 'shzt', title: '状态', width: 90 },
  { dataIndex: 'shrMc', key: 'shrMc', title: '审核人', width: 90 },
  { dataIndex: 'shDate', key: 'shDate', title: '审核时间', width: 130 },
  { dataIndex: 'zdrMc', key: 'zdrMc', title: '制单人', width: 90 },
  { dataIndex: 'remarks', key: 'remarks', title: '备注', width: 180 },
  { dataIndex: 'scxsd', key: 'scxsd', title: '销售单', width: 80 },
  { dataIndex: 'scxsdd', key: 'scxsdd', title: '销售订单', width: 80 }
]

const detailColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 100 },
  { dataIndex: 'spMc', title: '商品名称', width: 150 },
  { dataIndex: 'ggxh', title: '规格型号', width: 110 },
  { dataIndex: 'dwMc', title: '单位', width: 60 },
  { dataIndex: 'bjsl', title: '数量', width: 80 },
  { dataIndex: 'bjdj', title: '报价单价', width: 100 },
  { dataIndex: 'bjje', title: '报价金额', width: 100 },
  { dataIndex: 'remarks', title: '备注', width: 160 }
]
const detailRows = [{ spBh: 'SP-001', spMc: '示例商品', ggxh: '标准', dwMc: '件', bjsl: 20, bjdj: '235.00', bjje: '4,700.00', remarks: '' }]

const customers = [{ id: 1, khlbMc: '重点客户', khMc: '杭州示例客户', syLxr: '赵六', syPhone: '13800000000' }]
const customerColumns = [
  { dataIndex: 'khlbMc', title: '客户类别', width: 100 },
  { dataIndex: 'khMc', title: '客户名称', width: 130 },
  { dataIndex: 'syLxr', title: '联系人', width: 100 },
  { dataIndex: 'syPhone', title: '手机号码', width: 120 }
]
const attachments = [{ id: 1, tpmc: '报价附件.pdf' }]
const attachmentColumns = [
  { dataIndex: 'id', title: '操作', width: 80 },
  { dataIndex: 'tpmc', title: '附件', width: 350 }
]

const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: records.value.length, showSizeChanger: true }))

function query() {
  page.value = 1
  message.success('查询完成')
}

function resetForm(row?: Row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, row || { djbh: '', khMc: '', khLxr: '', khPhone: '', zffs: '', bz: '人民币', remarks: '' })
}

function openCreate() { editingId.value = null; resetForm(); quoteOpen.value = true }
function openEdit(row: Row) { editingId.value = row.id; resetForm(row); quoteOpen.value = true }
function openDetail(row: Row) { openEdit(row) }
function saveQuote() { message.success('保存成功'); quoteOpen.value = false }
function copyQuote() { message.success('已复制为新报价单') }
function auditQuotes() { selectedRows.value.forEach((row) => { row.shzt = 1; row.shrMc = '管理员'; row.shDate = '2026-04-28 12:00' }); message.success('审核成功') }
function reverseAuditQuotes() { selectedRows.value.forEach((row) => { row.shzt = 0; row.shrMc = ''; row.shDate = '' }); message.success('反审核成功') }
function generateSalesBill() { selectedRows.value.forEach((row) => { row.scxsd = 1 }); message.success('销售单已生成') }
function generateSalesOrder() { selectedRows.value.forEach((row) => { row.scxsdd = 1 }); message.success('销售订单已生成') }
function exportQuotes() { message.success('导出任务已创建') }

function onSelectChange(keys: any[], rows: Row[]) {
  selectedRowKeys.value = keys as number[]
  selectedRows.value = rows
}

function handleTableChange(next: TablePaginationConfig) {
  page.value = next.current || 1
  pageSize.value = next.pageSize || 20
}

function customerRow(row: Row) {
  return { onClick: () => { selectedCustomer.value = row } }
}

function chooseCustomer() {
  if (selectedCustomer.value) filters.khMc = selectedCustomer.value.khMc
  customerOpen.value = false
}
</script>

<style scoped>
.sales-quote-page { padding: 14px; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.field-label { color: #334155; font-size: 13px; }
.date-input { width: 124px; }
.date-separator { color: #94a3b8; }
.quote-code { width: 140px; }
.status-select { width: 118px; }
.goods-input { width: 220px; }
.customer-picker { display: inline-flex; width: 178px; }
.customer-input { width: 130px; }
.full-input { width: 100%; }
.detail-title { margin: 8px 0 10px; font-weight: 600; color: #1f2d3d; }
.modal-filter { width: 100%; margin-bottom: 12px; }
.modal-select { width: 150px; }
.modal-input { width: 220px; }
.inline-toolbar { padding: 0; border: 0; }
</style>
