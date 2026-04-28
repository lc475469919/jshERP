<template>
  <div class="page purchase-order-page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">采购订单</h1>
        <p class="page-desc">参考系统路径：采购 / 采购业务 / 采购订单。</p>
      </div>
    </div>

    <div class="filter-panel">
      <a-space wrap>
        <a-input v-model:value="filters.djBh" class="filter-input" placeholder="采购订单号" allow-clear @press-enter="query" />
        <a-input-group compact class="supplier-picker">
          <a-input v-model:value="filters.gysMc" class="supplier-input" placeholder="供应商名称" allow-clear @press-enter="query" />
          <a-button @click="supplierOpen = true">...</a-button>
        </a-input-group>
        <a-select v-model:value="filters.shzt" class="status-select" placeholder="审核状态" @change="query">
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="0">未审核</a-select-option>
          <a-select-option value="1">已审核</a-select-option>
        </a-select>
        <a-input v-model:value="filters.spMc" class="goods-input" placeholder="商品编号/名称/助记码/规格" allow-clear @press-enter="query" />
        <a-button @click="advancedOpen = true">高级搜索</a-button>
        <a-button type="primary" @click="query">查询</a-button>
      </a-space>
    </div>

    <div class="action-toolbar">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增</a-button>
        <a-button :disabled="!selectedRows.length" @click="copyOrder">复制</a-button>
        <a-button :disabled="!selectedRows.length" @click="auditOrders">审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="reverseAuditOrders">反审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="generatePurchaseBill">生成采购单</a-button>
        <a-dropdown>
          <a-button>批量打印</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="split" @click="printSplit">分单打印</a-menu-item>
              <a-menu-item key="merge" @click="printMerge">合并打印</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button @click="columnOpen = true">设置</a-button>
        <a-button @click="exportOrders">导出</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :columns="visibleColumns"
      :data-source="records"
      :pagination="pagination"
      :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ x: 2360 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, text }">
        <a-space v-if="column.key === 'operation'" size="small">
          <a-button type="link" size="small" @click="openEdit(record)">修改</a-button>
          <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
        </a-space>
        <a-tag v-else-if="column.key === 'shzt'" :color="text === 1 ? 'green' : 'red'">{{ text === 1 ? '已审核' : '未审核' }}</a-tag>
        <a-tag v-else-if="column.key === 'cgdZt'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '已生成' : '未生成' }}</a-tag>
        <a-button v-else-if="column.key === 'id2'" type="link" size="small">附件</a-button>
        <span v-else>{{ text }}</span>
      </template>
    </a-table>

    <a-drawer v-model:open="orderOpen" width="920" :title="editingId ? '修改采购订单' : '新增采购订单'">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="订单号"><a-input v-model:value="form.djBh" placeholder="由编号规则生成" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="订单日期"><a-date-picker v-model:value="form.djrq" class="full-input" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="交货日期"><a-date-picker v-model:value="form.shrq" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="供应商"><a-input v-model:value="form.gysMc" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="供应商单号"><a-input v-model:value="form.gysDdh" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="结款方式"><a-input v-model:value="form.jkfs" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="归属项目"><a-input v-model:value="form.xmmc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="请购人"><a-input v-model:value="form.qgdZdrMc" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remarks" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="detail-title">商品明细</div>
      <a-table size="small" :columns="detailColumns" :data-source="detailRows" :pagination="false" :scroll="{ x: 1180 }" />

      <template #extra>
        <a-space>
          <a-button @click="orderOpen = false">取消</a-button>
          <a-button type="primary" @click="saveOrder">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="advancedOpen" title="高级搜索" @ok="query">
      <a-form layout="vertical" :model="filters">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="交货日期起"><a-date-picker v-model:value="filters.shrqStart" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="交货日期止"><a-date-picker v-model:value="filters.shrqEnd" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="采购单"><a-select v-model:value="filters.cgdZt"><a-select-option value="">全部</a-select-option><a-select-option value="0">未生成</a-select-option><a-select-option value="1">已生成</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="今需收货"><a-select v-model:value="filters.todayReceive"><a-select-option value="">全部</a-select-option><a-select-option value="1">是</a-select-option><a-select-option value="0">否</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-input v-model:value="filters.remarks" allow-clear /></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="supplierOpen" title="选择供应商" @ok="chooseSupplier">
      <a-table size="small" row-key="id" :columns="supplierColumns" :data-source="suppliers" :pagination="false" @row="supplierRow" />
    </a-modal>

    <a-modal v-model:open="columnOpen" title="列表设置" @ok="columnOpen = false">
      <a-checkbox-group v-model:value="checkedColumnKeys" class="column-setting">
        <a-checkbox v-for="column in configurableColumns" :key="column.key" :value="column.key">{{ column.title }}</a-checkbox>
      </a-checkbox-group>
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
const orderOpen = ref(false)
const advancedOpen = ref(false)
const supplierOpen = ref(false)
const columnOpen = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<Row[]>([])
const page = ref(1)
const pageSize = ref(20)
const selectedSupplier = ref<Row | null>(null)

const records = ref<Row[]>([
  {
    id: 1,
    djBh: 'PO-20260428-0001',
    djrq: '2026-04-28',
    gysMc: '浙江示例供应商',
    shrq: '2026-05-03',
    jkfs: '月结',
    cgslHj: 120,
    cgjeHj: '12,000.00',
    seHj: '1,560.00',
    qtfyje: '0.00',
    yhhje: '13,560.00',
    gysDdh: 'GYS-0428',
    xmmc: '常规采购',
    remarks: '按参考系统字段展示',
    shzt: 0,
    qgdZdrMc: '张三',
    qgdDjBh: 'QG-20260428-0001',
    zdrMc: '管理员',
    createDate: '2026-04-28 09:30',
    shrMc: '',
    shDate: '',
    cgdZt: 0,
    cgslScHj: 0
  }
])

const columns = [
  { key: 'operation', title: '操作', width: 110, fixed: 'left' as const },
  { dataIndex: 'djBh', key: 'djBh', title: '订单号', width: 110, fixed: 'left' as const },
  { dataIndex: 'djrq', key: 'djrq', title: '订单日期', width: 90 },
  { dataIndex: 'gysMc', key: 'gysMc', title: '供应商', width: 130 },
  { dataIndex: 'shrq', key: 'shrq', title: '交货日期', width: 90 },
  { dataIndex: 'jkfs', key: 'jkfs', title: '结款方式', width: 80 },
  { dataIndex: 'cgslHj', key: 'cgslHj', title: '数量', width: 70 },
  { dataIndex: 'cgjeHj', key: 'cgjeHj', title: '金额(￥)', width: 100 },
  { dataIndex: 'seHj', key: 'seHj', title: '税额(￥)', width: 90 },
  { dataIndex: 'qtfyje', key: 'qtfyje', title: '其他费用(￥)', width: 90 },
  { dataIndex: 'yhhje', key: 'yhhje', title: '合计金额(￥)', width: 100 },
  { dataIndex: 'gysDdh', key: 'gysDdh', title: '供应商单号', width: 110 },
  { dataIndex: 'xmmc', key: 'xmmc', title: '归属项目', width: 110 },
  { dataIndex: 'id2', key: 'id2', title: '附件', width: 70 },
  { dataIndex: 'remarks', key: 'remarks', title: '备注', width: 150 },
  { dataIndex: 'shzt', key: 'shzt', title: '状态', width: 70 },
  { dataIndex: 'qgdZdrMc', key: 'qgdZdrMc', title: '请购人', width: 80 },
  { dataIndex: 'qgdDjBh', key: 'qgdDjBh', title: '请购单号', width: 110 },
  { dataIndex: 'zdrMc', key: 'zdrMc', title: '制单人', width: 80 },
  { dataIndex: 'createDate', key: 'createDate', title: '制单时间', width: 130 },
  { dataIndex: 'shrMc', key: 'shrMc', title: '审核人', width: 80 },
  { dataIndex: 'shDate', key: 'shDate', title: '审核时间', width: 130 },
  { dataIndex: 'cgdZt', key: 'cgdZt', title: '采购单', width: 75 },
  { dataIndex: 'cgslScHj', key: 'cgslScHj', title: '转采购数量', width: 75 }
]

const detailColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 110 },
  { dataIndex: 'spMc', title: '商品名称', width: 150 },
  { dataIndex: 'ggxh', title: '规格型号', width: 120 },
  { dataIndex: 'unit', title: '单位', width: 70 },
  { dataIndex: 'sl', title: '数量', width: 80 },
  { dataIndex: 'dj', title: '单价(￥)', width: 100 },
  { dataIndex: 'je', title: '金额(￥)', width: 100 },
  { dataIndex: 'slv', title: '税率(%)', width: 90 },
  { dataIndex: 'se', title: '税额(￥)', width: 100 },
  { dataIndex: 'remark', title: '备注', width: 140 }
]

const detailRows = [{ spBh: 'M-001', spMc: '示例物料', ggxh: '常规', unit: '件', sl: 120, dj: '100.00', je: '12,000.00', slv: 13, se: '1,560.00', remark: '' }]
const suppliers = [{ id: 1, supplierCode: 'GYS-001', supplierName: '浙江示例供应商', contactName: '李四' }]
const supplierColumns = [
  { dataIndex: 'supplierCode', title: '供应商编号', width: 120 },
  { dataIndex: 'supplierName', title: '供应商名称', width: 180 },
  { dataIndex: 'contactName', title: '联系人', width: 100 }
]

const configurableColumns = columns.filter((item) => item.key !== 'operation')
const checkedColumnKeys = ref(configurableColumns.map((item) => item.key))
const visibleColumns = computed(() => columns.filter((item) => item.key === 'operation' || checkedColumnKeys.value.includes(item.key)))
const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: records.value.length, showSizeChanger: true }))

function query() {
  page.value = 1
  message.success('查询完成')
}

function resetForm(row?: Row) {
  Object.keys(form).forEach((key) => delete form[key])
  Object.assign(form, row || { djBh: '', gysMc: '', jkfs: '', remarks: '' })
}

function openCreate() {
  editingId.value = null
  resetForm()
  orderOpen.value = true
}

function openEdit(row: Row) {
  editingId.value = row.id
  resetForm(row)
  orderOpen.value = true
}

function openDetail(row: Row) {
  openEdit(row)
}

function saveOrder() {
  message.success('保存成功')
  orderOpen.value = false
}

function copyOrder() {
  message.success('已复制为新采购订单')
}

function auditOrders() {
  selectedRows.value.forEach((row) => { row.shzt = 1; row.shrMc = '管理员'; row.shDate = '2026-04-28 10:00' })
  message.success('审核成功')
}

function reverseAuditOrders() {
  selectedRows.value.forEach((row) => { row.shzt = 0; row.shrMc = ''; row.shDate = '' })
  message.success('反审核成功')
}

function generatePurchaseBill() {
  selectedRows.value.forEach((row) => { row.cgdZt = 1; row.cgslScHj = row.cgslHj })
  message.success('采购单已生成')
}

function printSplit() { message.success('分单打印已进入队列') }
function printMerge() { message.success('合并打印已进入队列') }
function exportOrders() { message.success('导出任务已创建') }

function onSelectChange(keys: any[], rows: Row[]) {
  selectedRowKeys.value = keys as number[]
  selectedRows.value = rows
}

function handleTableChange(next: TablePaginationConfig) {
  page.value = next.current || 1
  pageSize.value = next.pageSize || 20
}

function supplierRow(row: Row) {
  return {
    onClick: () => {
      selectedSupplier.value = row
    }
  }
}

function chooseSupplier() {
  if (selectedSupplier.value) filters.gysMc = selectedSupplier.value.supplierName
  supplierOpen.value = false
}
</script>

<style scoped>
.purchase-order-page { padding: 14px; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.filter-input { width: 170px; }
.supplier-picker { display: inline-flex; width: 220px; }
.supplier-input { width: 170px; }
.status-select { width: 110px; }
.goods-input { width: 230px; }
.full-input { width: 100%; }
.detail-title { margin: 8px 0 10px; font-weight: 600; color: #1f2d3d; }
.column-setting { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px 12px; }
</style>
