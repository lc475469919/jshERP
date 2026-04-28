<template>
  <div class="page online-order-page">
    <div class="page-toolbar">
      <div>
        <h1 class="page-title">网店订单</h1>
        <p class="page-desc">参考系统路径：销售 / 批发业务 / 网店订单。</p>
      </div>
    </div>

    <a-tabs v-model:active-key="activeTab" class="order-tabs" @change="switchTab">
      <a-tab-pane key="DZF" tab="待支付" />
      <a-tab-pane key="DCL" tab="待处理" />
      <a-tab-pane key="YFH" tab="已发货" />
      <a-tab-pane key="YWC" tab="已完成" />
    </a-tabs>

    <div class="filter-panel">
      <a-space wrap>
        <span class="field-label">订单日期</span>
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
        <a-select v-model:value="filters.xsdBz" class="status-select" placeholder="销售单" @change="query">
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="0">未生成</a-select-option>
          <a-select-option value="1">部分生成</a-select-option>
          <a-select-option value="2">全部生成</a-select-option>
          <a-select-option value="3">未生成/部分生成</a-select-option>
        </a-select>
        <a-button @click="advancedOpen = true">高级搜索</a-button>
        <a-button type="primary" @click="query">查询</a-button>
      </a-space>
    </div>

    <div class="action-toolbar">
      <a-space wrap>
        <a-button :disabled="!selectedRows.length" @click="auditOrders">审核</a-button>
        <a-button :disabled="!selectedRows.length" @click="reverseAuditOrders">反审核</a-button>
        <a-dropdown>
          <a-button danger :disabled="!selectedRows.length">生成单据</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="xsd" @click="generateBill('销售单')">生成销售单</a-menu-item>
              <a-menu-item key="cgd" @click="generateBill('采购单')">生成采购单</a-menu-item>
              <a-menu-item key="cgdd" @click="generateBill('采购订单')">生成采购订单</a-menu-item>
              <a-menu-item key="qgd" @click="generateBill('请购单')">生成请购单</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-dropdown>
          <a-button :disabled="!selectedRows.length">生成生产任务</a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="scrw" @click="productionTaskOpen = true">生产加工任务</a-menu-item>
              <a-menu-item key="wwjg" @click="productionTaskOpen = true">委外加工任务</a-menu-item>
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
              <a-menu-item key="finish" @click="finishDelivery">办结销售发货</a-menu-item>
              <a-menu-item key="deposit" @click="depositOpen = true">收取订金</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button :disabled="!selectedRows.length" @click="confirmReceipt">确认收货</a-button>
        <a-button @click="exportOrders">导出</a-button>
      </a-space>
    </div>

    <a-table
      row-key="id"
      size="middle"
      :columns="columns"
      :data-source="records"
      :pagination="pagination"
      :row-selection="{ type: 'checkbox', selectedRowKeys, onChange: onSelectChange }"
      :scroll="{ x: 1900 }"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record, text }">
        <a-space v-if="column.key === 'operation'" size="small">
          <a-button type="link" size="small" @click="openDetail(record)">查看</a-button>
          <a-button type="link" size="small" @click="openDetail(record)">处理</a-button>
        </a-space>
        <a-tag v-else-if="column.key === 'xsdZt'" :color="text === 2 ? 'green' : text === 1 ? 'blue' : 'default'">{{ documentStatusText(text) }}</a-tag>
        <a-tag v-else-if="column.key === 'ckzt'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '已出库' : '未出库' }}</a-tag>
        <a-tag v-else-if="column.key === 'cgdZt' || column.key === 'scjgscBz' || column.key === 'wwjgdZt'" :color="text === 1 ? 'green' : 'default'">{{ text === 1 ? '已生成' : '未生成' }}</a-tag>
        <a-button v-else-if="column.key === 'id2'" type="link" size="small" @click="attachmentOpen = true">附件</a-button>
        <span v-else>{{ text }}</span>
      </template>
    </a-table>

    <a-drawer v-model:open="detailOpen" width="900" title="网店订单详情">
      <a-form layout="vertical" :model="form">
        <a-row :gutter="12">
          <a-col :span="8"><a-form-item label="订单号"><a-input v-model:value="form.djBh" readonly /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="订单日期"><a-input v-model:value="form.djrq" readonly /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="客户名称"><a-input v-model:value="form.khMc" readonly /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="销售员"><a-input v-model:value="form.xsrMc" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="订金(￥)"><a-input v-model:value="form.ysJe" /></a-form-item></a-col>
          <a-col :span="8"><a-form-item label="金额(￥)"><a-input v-model:value="form.yhhje" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="备注"><a-textarea v-model:value="form.remarks" :rows="3" /></a-form-item></a-col>
        </a-row>
      </a-form>
      <div class="detail-title">商品明细</div>
      <a-table size="small" :columns="detailColumns" :data-source="detailRows" :pagination="false" :scroll="{ x: 980 }" />
    </a-drawer>

    <a-modal v-model:open="advancedOpen" title="高级搜索" @ok="query">
      <a-form layout="vertical" :model="filters">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item label="单据编号"><a-input v-model:value="filters.djBh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="项目"><a-input v-model:value="filters.xmmc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="销售员"><a-input v-model:value="filters.xsrMc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="客户订单号"><a-input v-model:value="filters.khddh" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="交货日期起"><a-date-picker v-model:value="filters.jhrqq" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="交货日期止"><a-date-picker v-model:value="filters.jhrqz" class="full-input" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="生产任务"><a-select v-model:value="filters.scrwBz"><a-select-option value="">全部</a-select-option><a-select-option value="0">未生成</a-select-option><a-select-option value="2">部分生成</a-select-option><a-select-option value="1">全部生成</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="委外加工"><a-select v-model:value="filters.wwjgBz"><a-select-option value="">全部</a-select-option><a-select-option value="0">未生成</a-select-option><a-select-option value="2">部分生成</a-select-option><a-select-option value="1">全部生成</a-select-option></a-select></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="国家/地区"><a-input v-model:value="filters.gjdqMc" allow-clear /></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="备注"><a-input v-model:value="filters.remarks" allow-clear /></a-form-item></a-col>
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

    <a-modal v-model:open="productionTaskOpen" title="订单中商品选择" @ok="generateProductionTask">
      <a-space class="modal-filter" wrap>
        <a-select class="modal-select" placeholder="仓库"><a-select-option value="">仓库</a-select-option></a-select>
        <a-select class="modal-select" placeholder="全部"><a-select-option value="">全部</a-select-option><a-select-option value="1">仅自制品</a-select-option><a-select-option value="2">仅委外品</a-select-option><a-select-option value="3">委外/自制品</a-select-option></a-select>
        <a-input class="modal-input-wide" placeholder="商品编号/名称/助记码/规格" />
      </a-space>
      <a-table size="small" row-key="id" :columns="productionColumns" :data-source="productionRows" :pagination="false" :scroll="{ x: 820 }" />
    </a-modal>

    <a-modal v-model:open="depositOpen" title="收取订金" @ok="collectDeposit">
      <a-form layout="vertical" :model="depositForm">
        <a-form-item label="单据日期"><a-date-picker v-model:value="depositForm.djRq" class="full-input" /></a-form-item>
        <a-form-item label="结算账户"><a-input v-model:value="depositForm.jszh" /></a-form-item>
        <a-form-item label="订金金额"><a-input-number v-model:value="depositForm.ysJe" class="full-input" :min="0" /></a-form-item>
        <a-form-item label="备注"><a-textarea v-model:value="depositForm.remarks" :rows="3" /></a-form-item>
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

const activeTab = ref('DCL')
const filters = reactive<Row>({ shzt: '', xsdBz: '', state: 'DCL', dhptDdbz: 1, ywlb: 0 })
const form = reactive<Row>({})
const depositForm = reactive<Row>({})
const detailOpen = ref(false)
const advancedOpen = ref(false)
const customerOpen = ref(false)
const goodsOpen = ref(false)
const productionTaskOpen = ref(false)
const depositOpen = ref(false)
const attachmentOpen = ref(false)
const selectedRowKeys = ref<number[]>([])
const selectedRows = ref<Row[]>([])
const selectedCustomer = ref<Row | null>(null)
const selectedGoods = ref<Row | null>(null)
const page = ref(1)
const pageSize = ref(20)

const records = ref<Row[]>([
  {
    id: 1,
    djBh: 'WD-20260428-0001',
    djrq: '2026-04-28',
    khMc: '网店示例客户',
    xsslHj: 6,
    yhhje: '1,288.00',
    ysJe: '1,288.00',
    xsrMc: '王五',
    shrMc: '',
    shDate: '',
    yskJe: '1,288.00',
    wskJe: '0.00',
    xsdZt: 0,
    ckzt: 0,
    cgdZt: 0,
    scjgscBz: 0,
    wwjgdZt: 0,
    dycs: 0,
    remarks: '按参考系统字段展示'
  }
])

const columns = [
  { key: 'operation', title: '操作', width: 90, fixed: 'left' as const },
  { dataIndex: 'djBh', key: 'djBh', title: '订单号', width: 120, fixed: 'left' as const },
  { dataIndex: 'djrq', key: 'djrq', title: '订单日期', width: 90, fixed: 'left' as const },
  { dataIndex: 'khMc', key: 'khMc', title: '客户名称', width: 140 },
  { dataIndex: 'xsslHj', key: 'xsslHj', title: '数量', width: 80 },
  { dataIndex: 'yhhje', key: 'yhhje', title: '金额(￥)', width: 90 },
  { dataIndex: 'ysJe', key: 'ysJe', title: '订金(￥)', width: 90 },
  { dataIndex: 'id2', key: 'id2', title: '附件', width: 70 },
  { dataIndex: 'xsrMc', key: 'xsrMc', title: '销售员', width: 80 },
  { dataIndex: 'shrMc', key: 'shrMc', title: '审核人', width: 80 },
  { dataIndex: 'shDate', key: 'shDate', title: '审核时间', width: 130 },
  { dataIndex: 'yskJe', key: 'yskJe', title: '已收款(￥)', width: 90 },
  { dataIndex: 'wskJe', key: 'wskJe', title: '未收款(￥)', width: 90 },
  { dataIndex: 'xsdZt', key: 'xsdZt', title: '销售单', width: 75 },
  { dataIndex: 'ckzt', key: 'ckzt', title: '出库状态', width: 70 },
  { dataIndex: 'cgdZt', key: 'cgdZt', title: '采购单', width: 75 },
  { dataIndex: 'scjgscBz', key: 'scjgscBz', title: '生产任务', width: 75 },
  { dataIndex: 'wwjgdZt', key: 'wwjgdZt', title: '委外加工', width: 75 },
  { dataIndex: 'dycs', key: 'dycs', title: '打印次数', width: 80 },
  { dataIndex: 'remarks', key: 'remarks', title: '备注', width: 180 }
]

const detailColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 100 },
  { dataIndex: 'spMc', title: '商品名称', width: 150 },
  { dataIndex: 'ggxh', title: '规格型号', width: 100 },
  { dataIndex: 'xssl', title: '数量', width: 70 },
  { dataIndex: 'dwMc', title: '单位', width: 60 },
  { dataIndex: 'xsdj', title: '单价', width: 80 },
  { dataIndex: 'xsje', title: '金额', width: 90 },
  { dataIndex: 'remarks', title: '备注', width: 150 }
]
const detailRows = [{ spBh: 'SP-WD-001', spMc: '网店商品', ggxh: '标准', xssl: 6, dwMc: '件', xsdj: '214.67', xsje: '1,288.00', remarks: '' }]
const customers = [{ id: 1, khlbMc: '零售客户', khMc: '网店示例客户', syLxr: '陈七', syPhone: '13900000000', qkje: '0.00' }]
const customerColumns = [
  { dataIndex: 'khlbMc', title: '客户类别', width: 100 },
  { dataIndex: 'khMc', title: '客户名称', width: 130 },
  { dataIndex: 'syLxr', title: '联系人', width: 100 },
  { dataIndex: 'syPhone', title: '手机号码', width: 120 }
]
const goodsList = [{ id: 1, spBh: 'SP-WD-001', spMc: '网店商品', sptxm: '690000000002', ggxh: '标准', mrdwMc: '件' }]
const goodsColumns = [
  { dataIndex: 'spBh', title: '商品编号', width: 90 },
  { dataIndex: 'spMc', title: '商品名称', width: 130 },
  { dataIndex: 'sptxm', title: '条形码', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 120 },
  { dataIndex: 'mrdwMc', title: '单位', width: 60 }
]
const productionRows = [{ id: 1, scWwSl: 2, dwMc: '件', spBh: 'SP-WD-001', spMc: '网店商品', ggxh: '标准', xsddSl: 6, kykcsl: 4, scsl: 0, wwsl: 0, splbMc: '成品' }]
const productionColumns = [
  { dataIndex: 'scWwSl', title: '数量', width: 70 },
  { dataIndex: 'dwMc', title: '单位', width: 50 },
  { dataIndex: 'spBh', title: '商品编号', width: 90 },
  { dataIndex: 'spMc', title: '商品名称', width: 120 },
  { dataIndex: 'ggxh', title: '规格型号', width: 90 },
  { dataIndex: 'xsddSl', title: '订购数量', width: 80 },
  { dataIndex: 'kykcsl', title: '可用库存', width: 80 },
  { dataIndex: 'scsl', title: '已转生产', width: 80 },
  { dataIndex: 'wwsl', title: '已转委外', width: 80 },
  { dataIndex: 'splbMc', title: '商品类别', width: 90 }
]
const attachments = [{ id: 1, tpmc: '平台订单截图.png' }]
const attachmentColumns = [
  { dataIndex: 'id', title: '操作', width: 80 },
  { dataIndex: 'tpmc', title: '附件', width: 350 }
]

const pagination = computed<TablePaginationConfig>(() => ({ current: page.value, pageSize: pageSize.value, total: records.value.length, showSizeChanger: true }))

function switchTab() {
  filters.state = activeTab.value
  query()
}

function query() { page.value = 1; message.success('查询完成') }
function documentStatusText(value: number) { return value === 2 ? '全部生成' : value === 1 ? '部分生成' : '未生成' }
function openDetail(row: Row) { Object.keys(form).forEach((key) => delete form[key]); Object.assign(form, row); detailOpen.value = true }
function auditOrders() { selectedRows.value.forEach((row) => { row.shrMc = '管理员'; row.shDate = '2026-04-28 13:00' }); message.success('审核成功') }
function reverseAuditOrders() { selectedRows.value.forEach((row) => { row.shrMc = ''; row.shDate = '' }); message.success('反审核成功') }
function generateBill(name: string) { selectedRows.value.forEach((row) => { if (name === '销售单') row.xsdZt = 2; if (name === '采购单') row.cgdZt = 1 }); message.success(`${name}已生成`) }
function generateProductionTask() { selectedRows.value.forEach((row) => { row.scjgscBz = 1 }); productionTaskOpen.value = false; message.success('生产任务已生成') }
function printSplit() { message.success('分单打印已进入队列') }
function printMerge() { message.success('合并打印已进入队列') }
function finishDelivery() { selectedRows.value.forEach((row) => { row.ckzt = 1 }); message.success('销售发货已办结') }
function collectDeposit() { depositOpen.value = false; message.success('订金已登记') }
function confirmReceipt() { activeTab.value = 'YWC'; selectedRows.value = []; selectedRowKeys.value = []; message.success('确认收货完成') }
function exportOrders() { message.success('导出任务已创建') }
function onSelectChange(keys: any[], rows: Row[]) { selectedRowKeys.value = keys as number[]; selectedRows.value = rows }
function handleTableChange(next: TablePaginationConfig) { page.value = next.current || 1; pageSize.value = next.pageSize || 20 }
function customerRow(row: Row) { return { onClick: () => { selectedCustomer.value = row } } }
function chooseCustomer() { if (selectedCustomer.value) filters.khMc = selectedCustomer.value.khMc; customerOpen.value = false }
function goodsRow(row: Row) { return { onClick: () => { selectedGoods.value = row } } }
function chooseGoods() { if (selectedGoods.value) filters.spMc = selectedGoods.value.spMc; goodsOpen.value = false }
</script>

<style scoped>
.online-order-page { padding: 14px; }
.order-tabs { margin-bottom: 8px; background: #fff; border: 1px solid #dfe6ee; padding: 0 12px; }
.filter-panel, .action-toolbar { margin-bottom: 12px; padding: 12px; background: #fff; border: 1px solid #dfe6ee; }
.field-label { color: #334155; font-size: 13px; }
.date-input { width: 128px; }
.date-separator { color: #94a3b8; }
.selector-picker { display: inline-flex; width: 170px; }
.selector-input { width: 122px; }
.status-select { width: 120px; }
.full-input { width: 100%; }
.detail-title { margin: 8px 0 10px; font-weight: 600; color: #1f2d3d; }
.modal-filter { width: 100%; margin-bottom: 12px; }
.modal-select { width: 150px; }
.modal-input { width: 220px; }
.modal-input-wide { width: 280px; }
</style>
