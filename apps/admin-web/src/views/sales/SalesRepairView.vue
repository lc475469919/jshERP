<template>
  <div class="page sales-repair-page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">销售返修单</h1>
        <p class="page-desc">参考系统路径：销售 / 批发业务 / 销售返修单。</p>
      </div>
    </div>

    <div class="filter-panel">
      <a-space wrap>
        <span class="field-label">单据日期</span>
        <a-date-picker v-model:value="filters.djrqq" class="date-input" />
        <span class="date-separator">-</span>
        <a-date-picker v-model:value="filters.djrqz" class="date-input" />
        <a-input v-model:value="filters.khMc" class="keyword-input" placeholder="客户名称" allow-clear @press-enter="query" />
        <a-input v-model:value="filters.spBh" class="keyword-input" placeholder="商品" allow-clear @press-enter="query" />
        <a-select v-model:value="filters.zt" class="status-select" placeholder="任务状态" @change="query">
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="0">未审核</a-select-option>
          <a-select-option value="1">处理中</a-select-option>
          <a-select-option value="2">完工</a-select-option>
        </a-select>
        <a-button @click="advancedOpen = true">高级搜索</a-button>
        <a-button type="primary" @click="query">查询</a-button>
      </a-space>
    </div>

    <div class="action-toolbar">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增</a-button>
        <a-button :disabled="!selectedRows.length" @click="auditRepairs">审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="reverseAuditRepairs">反审核</a-button>
        <a-button danger :disabled="!selectedRows.length" @click="voidRepairs">作废</a-button>
        <a-button @click="exportRepairs">导出</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :columns="columns"
      :data-source="visibleRecords"
      :pagination="pagination"
      :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ x: 1460 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, text }">
        <a-space v-if="column.key === 'operation'" size="small">
          <template v-if="record.zt === 0">
            <a-button type="link" size="small" @click="openEdit(record)">修改</a-button>
            <a-button type="link" size="small" danger @click="deleteRepair(record)">删除</a-button>
          </template>
          <a-button v-if="record.zt === 1" type="link" size="small" @click="repairProcess(record)">维修处理</a-button>
          <a-button v-if="record.zt === 2" type="link" size="small" @click="repairAgain(record)">重新维修</a-button>
          <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
        </a-space>
        <a-tag v-else-if="column.key === 'zt'" :color="repairStatusColor(text)">{{ repairStatusText(text) }}</a-tag>
        <span v-else>{{ text }}</span>
      </template>
    </a-table>

    <a-drawer v-model:open="repairOpen" width="860" :title="drawerTitle">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="客户">
              <a-input-group compact>
                <a-input v-model:value="form.khMc" class="form-selector-input" readonly />
                <a-button @click="customerOpen = true">...</a-button>
              </a-input-group>
            </a-form-item>
          </a-col>
          <a-col :span="6"><a-form-item label="联系人"><a-input v-model:value="form.khLxr" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="联系电话"><a-input v-model:value="form.khPhone" /></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="返修商品">
              <a-input-group compact>
                <a-input v-model:value="form.spMc" class="form-selector-input" readonly />
                <a-button @click="goodsOpen = true">...</a-button>
              </a-input-group>
            </a-form-item>
          </a-col>
          <a-col :span="6"><a-form-item label="返修数量"><a-input-number v-model:value="form.sl" class="full-input" :min="1" /></a-form-item></a-col>
          <a-col :span="6"><a-form-item label="计量单位"><a-select v-model:value="form.dwMc"><a-select-option value="件">件</a-select-option><a-select-option value="套">套</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="颜色"><a-select v-model:value="form.ysMc"><a-select-option value="">无</a-select-option><a-select-option value="蓝">蓝</a-select-option><a-select-option value="黑">黑</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="规格型号"><a-input v-model:value="form.ggxh" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="销售单号"><a-input v-model:value="form.xsbh" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remarks" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <template #extra>
        <a-space>
          <a-button @click="repairOpen = false">取消</a-button>
          <a-button type="primary" @click="saveRepair">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="advancedOpen" title="高级搜索" @ok="query">
      <a-form layout="vertical" :model="filters">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="单据编号"><a-input v-model:value="filters.djbh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="已作废"><a-select v-model:value="filters.xszfBz"><a-select-option value="0">不显示</a-select-option><a-select-option value="1">显示</a-select-option></a-select></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="goodsOpen" title="选择商品" @ok="chooseGoods">
      <a-space class="modal-filter" wrap>
        <a-input class="modal-input-wide" placeholder="商品编号/名称/助记码/规格查询" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="goodsColumns" :data-source="goodsList" :pagination="false" @row="goodsRow" />
    </a-modal>

    <a-modal v-model:open="customerOpen" title="选择客户" @ok="chooseCustomer">
      <a-space class="modal-filter" wrap>
        <a-select class="modal-select" placeholder="选择客户类别"><a-select-option value="">全部</a-select-option></a-select>
        <a-input class="modal-input-wide" placeholder="输入客户名称/助记码/联系人/电话查询" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="customerColumns" :data-source="customers" :pagination="false" @row="customerRow" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import type { TablePaginationConfig } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { computed, reactive, ref } from 'vue'

type Row = Record<string, any>

const filters = reactive<Row>({ zt: '', xszfBz: '0' })
const form = reactive<Row>({})
const repairOpen = ref(false)
const advancedOpen = ref(false)
const goodsOpen = ref(false)
const customerOpen = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<Row[]>([])
const selectedGoods = ref<Row | null>(null)
const selectedCustomer = ref<Row | null>(null)
const page = ref(1)
const pageSize = ref(20)

const records = ref<Row[]>([
  {
    id: 1,
    djbh: 'XSFX-20260510-0001',
    xsbh: 'XS-20260428-0001',
    djrq: '2026-05-10',
    khMc: '杭州示例客户',
    khLxr: '赵六',
    khPhone: '13800000000',
    spBh: 'SP-001',
    spMc: '示例商品',
    ysMc: '蓝',
    ggxh: '标准',
    sl: 1,
    dwMc: '件',
    zt: 0,
    xszfBz: 0,
    zdrMc: '管理员',
    shrMc: '',
    shDate: '',
    remarks: '客户退回返修'
  }
])

const visibleRecords = computed(() => filters.xszfBz === '1' ? records.value : records.value.filter((item) => item.xszfBz !== 1))
const drawerTitle = computed(() => editingId.value ? '修改销售返修单' : '新增销售返修单')
const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: visibleRecords.value.length, showSizeChanger: true }))

const columns = [
  { key: 'operation', title: '操作', width: 150, fixed: 'left' as const },
  { dataIndex: 'djbh', key: 'djbh', title: '单据编号', width: 120, fixed: 'left' as const },
  { dataIndex: 'xsbh', key: 'xsbh', title: '销售单号', width: 120 },
  { dataIndex: 'khMc', key: 'khMc', title: '客户名称', width: 130 },
  { dataIndex: 'khLxr', key: 'khLxr', title: '联系人', width: 80 },
  { dataIndex: 'khPhone', key: 'khPhone', title: '联系电话', width: 110 },
  { dataIndex: 'spBh', key: 'spBh', title: '商品编号', width: 100 },
  { dataIndex: 'spMc', key: 'spMc', title: '商品名称', width: 130 },
  { dataIndex: 'ysMc', key: 'ysMc', title: '颜色', width: 60 },
  { dataIndex: 'ggxh', key: 'ggxh', title: '规格型号', width: 100 },
  { dataIndex: 'sl', key: 'sl', title: '返修数量', width: 90 },
  { dataIndex: 'dwMc', key: 'dwMc', title: '单位', width: 60 },
  { dataIndex: 'zt', key: 'zt', title: '单据状态', width: 90 },
  { dataIndex: 'zdrMc', key: 'zdrMc', title: '制单人', width: 80 },
  { dataIndex: 'shrMc', key: 'shrMc', title: '审核人', width: 90 },
  { dataIndex: 'shDate', key: 'shDate', title: '审核时间', width: 120 },
  { dataIndex: 'remarks', key: 'remarks', title: '备注', width: 140 }
]

const goodsList = [{ id: 1, spBh: 'SP-001', spMc: '示例商品', sptxm: '690000000001', ggxh: '标准', mrdwMc: '件' }]
const goodsColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 90 },
  { dataIndex: 'spMc', title: '商品名称', width: 130 },
  { dataIndex: 'sptxm', title: '商品条形码', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 120 },
  { dataIndex: 'mrdwMc', title: '单位', width: 60 }
]
const customers = [{ id: 1, khlbMc: '重点客户', khMc: '杭州示例客户', syLxr: '赵六', syPhone: '13800000000', qkje: '0.00' }]
const customerColumns = [
  { dataIndex: 'khlbMc', title: '客户类别', width: 100 },
  { dataIndex: 'khMc', title: '客户名称', width: 130 },
  { dataIndex: 'syLxr', title: '联系人', width: 100 },
  { dataIndex: 'syPhone', title: '手机号码', width: 120 },
  { dataIndex: 'qkje', title: '欠款(元)', width: 90 }
]

function query() { page.value = 1; message.success('查询完成') }
function resetForm(row?: Row) { Object.keys(form).forEach((key) => delete form[key]); Object.assign(form, row || { khMc: '', khLxr: '', khPhone: '', spMc: '', sl: 1, dwMc: '件', ysMc: '', remarks: '' }) }
function openCreate() { editingId.value = null; resetForm(); repairOpen.value = true }
function openEdit(row: Row) { editingId.value = row.id; resetForm(row); repairOpen.value = true }
function openDetail(row: Row) { openEdit(row) }
function saveRepair() { repairOpen.value = false; message.success('保存成功') }
function deleteRepair(row: Row) { records.value = records.value.filter((item) => item.id !== row.id); message.success('删除成功') }
function auditRepairs() { selectedRows.value.forEach((row) => { row.zt = 1; row.shrMc = '管理员'; row.shDate = '2026-05-10 11:00' }); message.success('审核成功') }
function reverseAuditRepairs() { selectedRows.value.forEach((row) => { row.zt = 0; row.shrMc = ''; row.shDate = '' }); message.success('反审核成功') }
function voidRepairs() { selectedRows.value.forEach((row) => { row.xszfBz = 1 }); message.success('作废成功') }
function repairProcess(row: Row) { row.zt = 2; message.success('维修处理完成') }
function repairAgain(row: Row) { row.zt = 1; message.success('已重新进入维修处理') }
function exportRepairs() { message.success('销售返修单列表导出任务已创建') }
function repairStatusText(value: number) { return value === 2 ? '完工' : value === 1 ? '处理中' : '未审核' }
function repairStatusColor(value: number) { return value === 2 ? 'green' : value === 1 ? 'blue' : 'red' }
function onSelectChange(keys: any[], rows: Row[]) { selectedRowKeys.value = keys as number[]; selectedRows.value = rows }
function handleTableChange(next: TablePaginationConfig) { page.value = next.current || 1; pageSize.value = next.pageSize || 20 }
function goodsRow(row: Row) { return { onClick: () => { selectedGoods.value = row } } }
function chooseGoods() { if (selectedGoods.value) Object.assign(form, { spBh: selectedGoods.value.spBh, spMc: selectedGoods.value.spMc, ggxh: selectedGoods.value.ggxh, dwMc: selectedGoods.value.mrdwMc }); goodsOpen.value = false }
function customerRow(row: Row) { return { onClick: () => { selectedCustomer.value = row } } }
function chooseCustomer() { if (selectedCustomer.value) Object.assign(form, { khMc: selectedCustomer.value.khMc, khLxr: selectedCustomer.value.syLxr, khPhone: selectedCustomer.value.syPhone }); customerOpen.value = false }
</script>

<style scoped>
.sales-repair-page { padding: 14px; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.field-label { color: #334155; font-size: 13px; }
.date-input { width: 128px; }
.date-separator { color: #94a3b8; }
.keyword-input { width: 140px; }
.status-select { width: 108px; }
.full-input { width: 100%; }
.form-selector-input { width: calc(100% - 34px); }
.modal-filter { width: 100%; margin-bottom: 12px; }
.modal-select { width: 150px; }
.modal-input-wide { width: 280px; }
</style>
