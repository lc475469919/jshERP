<template>
  <div class="page sales-return-page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">销售退货单</h1>
        <p class="page-desc">参考系统路径：销售 / 批发业务 / 销售退货单。</p>
      </div>
    </div>

    <div class="filter-panel">
      <a-space wrap>
        <span class="field-label">退货日期</span>
        <a-date-picker v-model:value="filters.ddrqq" class="date-input" />
        <span class="date-separator">-</span>
        <a-date-picker v-model:value="filters.ddrqz" class="date-input" />
        <a-input-group compact class="selector-picker">
          <a-input v-model:value="filters.khMc" class="selector-input" placeholder="客户" allow-clear @press-enter="query" />
          <a-button @click="customerOpen = true">...</a-button>
        </a-input-group>
        <a-input-group compact class="selector-picker">
          <a-input v-model:value="filters.spMc" class="selector-input" placeholder="商品" allow-clear @press-enter="query" />
          <a-button @click="goodsOpen = true">...</a-button>
        </a-input-group>
        <a-select v-model:value="filters.shzt" class="status-select" placeholder="审核状态" @change="query">
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="0">未审核</a-select-option>
          <a-select-option value="1">已审核</a-select-option>
        </a-select>
        <a-button @click="advancedOpen = true">高级搜索</a-button>
        <a-button type="primary" @click="query">查询</a-button>
      </a-space>
    </div>

    <div class="action-toolbar">
      <a-space wrap>
        <a-button type="primary" @click="openCreate">新增</a-button>
        <a-button :disabled="!selectedRows.length" @click="copyReturn">复制</a-button>
        <a-button :disabled="!selectedRows.length" @click="auditReturns">审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="reverseAuditReturns">反审核</a-button>
        <a-dropdown>
          <a-button :disabled="!selectedRows.length">发票管理</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="normal" @click="setInvoiceNeed('需要普票')">需要普票</a-menu-item>
              <a-menu-item key="special" @click="setInvoiceNeed('需要专票')">需要专票</a-menu-item>
              <a-menu-item key="none" @click="setInvoiceNeed('不要发票')">不要发票</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button>批量打印</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="split" @click="printSplit">分单打印</a-menu-item>
              <a-menu-item key="merge" @click="printMerge">合并打印</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button :disabled="!selectedRows.length">更多操作</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="stock-in" @click="goStockIn">去入库</a-menu-item>
              <a-menu-item key="refund" @click="goRefund">去退款</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button @click="exportReturns">导出</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :columns="columns"
      :data-source="records"
      :pagination="pagination"
      :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ x: 1680 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, text }">
        <a-space v-if="column.key === 'operation'" size="small">
          <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
          <a-button v-if="record.shzt !== 1" type="link" size="small" @click="openEdit(record)">修改</a-button>
          <a-button v-if="record.shzt !== 1" type="link" size="small" danger @click="deleteReturn(record)">删除</a-button>
        </a-space>
        <a-button v-else-if="column.key === 'id2'" type="link" size="small" @click="attachmentOpen = true">附件</a-button>
        <a-tag v-else-if="column.key === 'rkzt'" :color="text === 1 ? 'green' : text === 2 ? 'blue' : 'default'">{{ stockInStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'fkzt'" :color="text === 1 ? 'green' : text === 2 ? 'blue' : 'default'">{{ refundStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'kpbz'" :color="text ? 'orange' : 'default'">{{ text ? '需要' : '不需要' }}</a-tag>
        <a-tag v-else-if="column.key === 'kpzt'" :color="text === 2 ? 'green' : text === 1 ? 'blue' : 'default'">{{ invoiceStatusText(text) }}</a-tag>
        <span v-else>{{ text }}</span>
      </template>
    </a-table>

    <a-drawer v-model:open="returnOpen" width="980" :title="editingId ? '修改销售退货单' : '新增销售退货单'">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="退货单号"><a-input v-model:value="form.djBh" placeholder="由编号规则生成" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="销售单号"><a-input v-model:value="form.ddBh" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="退货日期"><a-date-picker v-model:value="form.djrq" class="full-input" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="入库仓库"><a-input v-model:value="form.ckMc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="客户"><a-input v-model:value="form.khMc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="销售员"><a-input v-model:value="form.xsrMc" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="归属项目"><a-input v-model:value="form.xmmc" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="国家/地区"><a-input v-model:value="form.gjdqMc" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remarks" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>

      <div class="detail-title">退货明细</div>
      <a-table size="small" :columns="detailColumns" :data-source="detailRows" :pagination="false" :scroll="{ x: 1280 }" />

      <template #extra>
        <a-space>
          <a-button @click="returnOpen = false">取消</a-button>
          <a-button type="primary" @click="saveReturn">保存</a-button>
        </a-space>
      </template>
    </a-drawer>

    <a-modal v-model:open="advancedOpen" title="高级搜索" @ok="query">
      <a-form layout="vertical" :model="filters">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="退货单号"><a-input v-model:value="filters.djBh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="销售单号"><a-input v-model:value="filters.ddBh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="出库仓库"><a-select v-model:value="filters.ckId"><a-select-option value="">全部</a-select-option><a-select-option value="raw">原材料库</a-select-option><a-select-option value="finished">成品库</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="国家/地区"><a-input-group compact><a-input v-model:value="filters.gjdqMc" class="selector-input" readonly /><a-button @click="countryOpen = true">...</a-button></a-input-group></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="需要发票"><a-select v-model:value="filters.kpbz"><a-select-option value="">全部</a-select-option><a-select-option value="0">不需要</a-select-option><a-select-option value="1">需要</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="发票状态"><a-select v-model:value="filters.kpzt"><a-select-option value="">全部</a-select-option><a-select-option value="0">暂未开票</a-select-option><a-select-option value="1">部分已开</a-select-option><a-select-option value="2">全部已开</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="销售员"><a-input-group compact><a-input v-model:value="filters.xsrMc" class="selector-input" readonly /><a-button @click="employeeOpen = true">...</a-button></a-input-group></a-form-item></a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-modal v-model:open="customerOpen" title="选择客户" @ok="chooseCustomer">
      <a-space class="modal-filter" wrap>
        <a-select class="modal-select" placeholder="选择客户类别"><a-select-option value="">全部</a-select-option></a-select>
        <a-input class="modal-input" placeholder="客户名称/联系人/电话" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="customerColumns" :data-source="customers" :pagination="false" @row="customerRow" />
    </a-modal>

    <a-modal v-model:open="goodsOpen" title="选择商品" @ok="chooseGoods">
      <a-space class="modal-filter" wrap>
        <a-input class="modal-input-wide" placeholder="商品编号/名称/助记码/规格" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="goodsColumns" :data-source="goodsList" :pagination="false" @row="goodsRow" />
    </a-modal>

    <a-modal v-model:open="employeeOpen" title="选择销售员" @ok="chooseEmployee">
      <a-space class="modal-filter" wrap>
        <a-input class="modal-input" placeholder="员工姓名" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="employeeColumns" :data-source="employees" :pagination="false" @row="employeeRow" />
    </a-modal>

    <a-modal v-model:open="countryOpen" title="选择国家/地区" @ok="chooseCountry">
      <a-space class="modal-filter" wrap>
        <a-select class="modal-select" placeholder="全部"><a-select-option value="">全部</a-select-option><a-select-option value="China">中国</a-select-option><a-select-option value="Asia">亚洲</a-select-option></a-select>
        <a-input class="modal-input" placeholder="国家/地区名称" />
        <a-button type="primary">查询</a-button>
      </a-space>
      <a-table size="small" row-key="id" :columns="countryColumns" :data-source="countries" :pagination="false" @row="countryRow" />
    </a-modal>

    <a-modal v-model:open="invoiceOpen" title="发票管理" @ok="invoiceOpen = false">
      <a-form layout="vertical">
        <a-form-item label="需要发票"><a-radio-group v-model:value="invoiceState.kpbz"><a-radio :value="0">不需要</a-radio><a-radio :value="1">需要</a-radio></a-radio-group></a-form-item>
        <a-form-item label="发票状态"><a-radio-group v-model:value="invoiceState.kpzt"><a-radio :value="0">暂未开票</a-radio><a-radio :value="1">部分已开</a-radio><a-radio :value="2">全部已开</a-radio></a-radio-group></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="attachmentOpen" title="附件" @ok="attachmentOpen = false">
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
const invoiceState = reactive<Row>({ kpbz: 0, kpzt: 0 })
const returnOpen = ref(false)
const advancedOpen = ref(false)
const customerOpen = ref(false)
const goodsOpen = ref(false)
const employeeOpen = ref(false)
const countryOpen = ref(false)
const invoiceOpen = ref(false)
const attachmentOpen = ref(false)
const editingId = ref<number | null>(null)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<Row[]>([])
const selectedCustomer = ref<Row | null>(null)
const selectedGoods = ref<Row | null>(null)
const selectedEmployee = ref<Row | null>(null)
const selectedCountry = ref<Row | null>(null)
const page = ref(1)
const pageSize = ref(20)

const records = ref<Row[]>([
  {
    id: 1,
    djBh: 'XSTH-20260510-0001',
    ddBh: 'XS-20260428-0001',
    djrq: '2026-05-10',
    ckMc: '成品库',
    khMc: '杭州示例客户',
    xsslHj: 2,
    yhhje: '678.00',
    xmmc: '常规销售',
    rkzt: 0,
    fkzt: 0,
    kpbz: 1,
    kpzt: 0,
    xsrMc: '王五',
    shzt: 0,
    shrMc: '',
    shDate: '',
    remarks: '退回两件待入库',
    zdrMc: '管理员',
    createDate: '2026-05-10 10:00'
  }
])

const columns = [
  { key: 'operation', title: '操作', width: 100, fixed: 'left' as const },
  { dataIndex: 'djBh', key: 'djBh', title: '退货单号', width: 130, fixed: 'left' as const },
  { dataIndex: 'ddBh', key: 'ddBh', title: '销售单号', width: 130, fixed: 'left' as const },
  { dataIndex: 'djrq', key: 'djrq', title: '退货日期', width: 90 },
  { dataIndex: 'ckMc', key: 'ckMc', title: '入库仓库', width: 100 },
  { dataIndex: 'khMc', key: 'khMc', title: '客户', width: 140 },
  { dataIndex: 'xsslHj', key: 'xsslHj', title: '退货数量', width: 80 },
  { dataIndex: 'yhhje', key: 'yhhje', title: '总计金额(￥)', width: 110 },
  { dataIndex: 'id2', key: 'id2', title: '附件', width: 70 },
  { dataIndex: 'xmmc', key: 'xmmc', title: '归属项目', width: 110 },
  { dataIndex: 'rkzt', key: 'rkzt', title: '状态', width: 80 },
  { dataIndex: 'fkzt', key: 'fkzt', title: '退款状态', width: 80 },
  { dataIndex: 'kpbz', key: 'kpbz', title: '需要发票', width: 80 },
  { dataIndex: 'kpzt', key: 'kpzt', title: '发票状态', width: 80 },
  { dataIndex: 'xsrMc', key: 'xsrMc', title: '销售员', width: 80 },
  { dataIndex: 'shrMc', key: 'shrMc', title: '审核人', width: 80 },
  { dataIndex: 'shDate', key: 'shDate', title: '审核时间', width: 130 },
  { dataIndex: 'remarks', key: 'remarks', title: '备注', width: 180 },
  { dataIndex: 'zdrMc', key: 'zdrMc', title: '制单人', width: 80 },
  { dataIndex: 'createDate', key: 'createDate', title: '制单日期', width: 130 }
]

const detailColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 100 },
  { dataIndex: 'spMc', title: '商品名称', width: 150 },
  { dataIndex: 'sptxm', title: '商品条形码', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 110 },
  { dataIndex: 'xssl', title: '退货数量', width: 80 },
  { dataIndex: 'dwMc', title: '单位', width: 60 },
  { dataIndex: 'xsdj', title: '单价', width: 80 },
  { dataIndex: 'xsje', title: '金额', width: 80 },
  { dataIndex: 'spzk', title: '折扣(%)', width: 80 },
  { dataIndex: 'spzhje', title: '折后金额', width: 90 },
  { dataIndex: 'hsdj', title: '含税单价', width: 90 },
  { dataIndex: 'sjhj', title: '含税金额', width: 100 }
]
const detailRows = [{ spBh: 'SP-001', spMc: '示例商品', sptxm: '690000000001', ggxh: '标准', xssl: 2, dwMc: '件', xsdj: '300.00', xsje: '600.00', spzk: 100, spzhje: '600.00', hsdj: '339.00', sjhj: '678.00' }]
const customers = [{ id: 1, khlbMc: '重点客户', khMc: '杭州示例客户', syLxr: '赵六', syPhone: '13800000000', qkje: '0.00' }]
const customerColumns = [
  { dataIndex: 'khlbMc', title: '客户类别', width: 100 },
  { dataIndex: 'khMc', title: '客户名称', width: 130 },
  { dataIndex: 'syLxr', title: '联系人', width: 100 },
  { dataIndex: 'syPhone', title: '手机号码', width: 120 }
]
const goodsList = [{ id: 1, spBh: 'SP-001', spMc: '示例商品', sptxm: '690000000001', ggxh: '标准', mrdwMc: '件' }]
const goodsColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 90 },
  { dataIndex: 'spMc', title: '商品名称', width: 130 },
  { dataIndex: 'sptxm', title: '商品条形码', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 120 },
  { dataIndex: 'mrdwMc', title: '单位', width: 60 }
]
const employees = [{ id: 1, xm: '王五', xb: '男', orgName: '销售部', sjhm: '13900000000', yxFlag: '启用' }]
const employeeColumns = [
  { dataIndex: 'xm', title: '员工姓名', width: 100 },
  { dataIndex: 'xb', title: '性别', width: 60 },
  { dataIndex: 'orgName', title: '所属部门', width: 110 },
  { dataIndex: 'sjhm', title: '手机号码', width: 110 },
  { dataIndex: 'yxFlag', title: '状态', width: 80 }
]
const countries = [{ id: 1, gjdqMc: '中国', gjdqBh: 'China', lbMc: '中国' }]
const countryColumns = [
  { dataIndex: 'gjdqMc', title: '中文名', width: 120 },
  { dataIndex: 'gjdqBh', title: '英文名', width: 120 },
  { dataIndex: 'lbMc', title: '所属区域', width: 100 }
]
const attachments = [{ id: 1, tpmc: '退货附件.pdf' }]
const attachmentColumns = [
  { dataIndex: 'id', title: '操作', width: 80 },
  { dataIndex: 'tpmc', title: '附件', width: 350 }
]

const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: records.value.length, showSizeChanger: true }))

function query() { page.value = 1; message.success('查询完成') }
function resetForm(row?: Row) { Object.keys(form).forEach((key) => delete form[key]); Object.assign(form, row || { djBh: '', ddBh: '', ckMc: '', khMc: '', xsrMc: '', remarks: '' }) }
function openCreate() { editingId.value = null; resetForm(); returnOpen.value = true }
function openEdit(row: Row) { editingId.value = row.id; resetForm(row); returnOpen.value = true }
function openDetail(row: Row) { openEdit(row) }
function saveReturn() { returnOpen.value = false; message.success('保存成功') }
function deleteReturn(row: Row) { records.value = records.value.filter((item) => item.id !== row.id); message.success('删除成功') }
function copyReturn() { message.success('已复制为新销售退货单') }
function auditReturns() { selectedRows.value.forEach((row) => { row.shzt = 1; row.shrMc = '管理员'; row.shDate = '2026-05-10 10:30' }); message.success('审核成功') }
function reverseAuditReturns() { selectedRows.value.forEach((row) => { row.shzt = 0; row.shrMc = ''; row.shDate = '' }); message.success('反审核成功') }
function setInvoiceNeed(name: string) { selectedRows.value.forEach((row) => { row.kpbz = name === '不要发票' ? 0 : 1; row.kpzt = 0 }); invoiceOpen.value = true; message.success(name) }
function printSplit() { message.success('分单打印已进入队列') }
function printMerge() { message.success('合并打印已进入队列') }
function goStockIn() { selectedRows.value.forEach((row) => { row.rkzt = 1 }); message.success('已进入入库管理') }
function goRefund() { selectedRows.value.forEach((row) => { row.fkzt = 1 }); message.success('已进入退款处理') }
function exportReturns() { message.success('销售退货单列表导出任务已创建') }
function stockInStatusText(value: number) { return value === 1 ? '已全部入库' : value === 2 ? '部分入库' : '未入库' }
function refundStatusText(value: number) { return value === 1 ? '已退款' : value === 2 ? '部分退款' : '未退款' }
function invoiceStatusText(value: number) { return value === 2 ? '全部已开' : value === 1 ? '部分已开' : '暂未开票' }
function onSelectChange(keys: any[], rows: Row[]) { selectedRowKeys.value = keys as number[]; selectedRows.value = rows }
function handleTableChange(next: TablePaginationConfig) { page.value = next.current || 1; pageSize.value = next.pageSize || 20 }
function customerRow(row: Row) { return { onClick: () => { selectedCustomer.value = row } } }
function chooseCustomer() { if (selectedCustomer.value) filters.khMc = selectedCustomer.value.khMc; customerOpen.value = false }
function goodsRow(row: Row) { return { onClick: () => { selectedGoods.value = row } } }
function chooseGoods() { if (selectedGoods.value) filters.spMc = selectedGoods.value.spMc; goodsOpen.value = false }
function employeeRow(row: Row) { return { onClick: () => { selectedEmployee.value = row } } }
function chooseEmployee() { if (selectedEmployee.value) filters.xsrMc = selectedEmployee.value.xm; employeeOpen.value = false }
function countryRow(row: Row) { return { onClick: () => { selectedCountry.value = row } } }
function chooseCountry() { if (selectedCountry.value) filters.gjdqMc = selectedCountry.value.gjdqMc; countryOpen.value = false }
</script>

<style scoped>
.sales-return-page { padding: 14px; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.field-label { color: #334155; font-size: 13px; }
.date-input { width: 128px; }
.date-separator { color: #94a3b8; }
.selector-picker { display: inline-flex; width: 174px; }
.selector-input { width: 126px; }
.status-select { width: 108px; }
.full-input { width: 100%; }
.detail-title { margin: 8px 0 10px; font-weight: 600; color: #1f2d3d; }
.modal-filter { width: 100%; margin-bottom: 12px; }
.modal-select { width: 150px; }
.modal-input { width: 220px; }
.modal-input-wide { width: 280px; }
</style>
