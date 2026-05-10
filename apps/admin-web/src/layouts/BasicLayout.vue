<template>
  <div class="erp-shell">
    <aside class="module-bar" @mouseleave="hoverModule = ''">
      <button class="home-entry" type="button" :class="{ active: route.path === '/dashboard' }" @click="go('/dashboard')">
        <span>首</span>
        <strong>首页</strong>
      </button>

      <button
        v-for="module in referenceMenus"
        :key="module.name"
        class="module-item"
        type="button"
        :class="{ active: activeModule === module.name }"
        @mouseenter="hoverModule = module.name"
        @click="openModule(module)"
      >
        <span>{{ module.name.slice(0, 1) }}</span>
        <strong>{{ module.name }}</strong>
      </button>

      <div v-if="hoveredMenu" class="menu-flyout" :style="{ width: `${Math.max(hoveredMenu.groups.length, 1) * 210}px` }">
        <section v-for="group in hoveredMenu.groups" :key="group.name" class="flyout-column">
          <h3>{{ group.name }}</h3>
          <button
            v-for="item in group.items"
            :key="item.name"
            type="button"
            :class="{ current: isCurrentEntry(item, hoveredMenu.name, group.name) }"
            @click="selectEntry(item, hoveredMenu.name, group.name)"
          >
            {{ item.name }}
          </button>
        </section>
      </div>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div class="company">
          <span class="brand-mark">YZ</span>
          <strong>易泽产销一体公司</strong>
          <em>制造业版</em>
        </div>
        <nav class="top-links">
          <button type="button">新手向导</button>
          <button type="button">帮助中心</button>
          <button type="button">更多服务</button>
          <a-dropdown>
            <button type="button" class="user-button">{{ auth.user?.name || '管理员' }}</button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">账号信息</a-menu-item>
                <a-menu-item key="logout" @click="logout">安全退出</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </nav>
      </header>

      <div class="tabbar">
        <button class="tab-roll" type="button">‹</button>
        <div class="tabs">
          <button class="tab active" type="button">{{ currentTitle }}</button>
        </div>
        <button class="tab-roll" type="button">›</button>
        <a-dropdown>
          <button class="tab-action" type="button">关闭操作</button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="locate">定位当前选项卡</a-menu-item>
              <a-menu-item key="close-all">关闭全部</a-menu-item>
              <a-menu-item key="close-other">关闭其他</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <button class="tab-action" type="button" @click="refreshPage">刷新</button>
      </div>

      <main class="content">
        <router-view :key="viewKey" />
      </main>

      <footer class="footerbar">
        <div>
          <span>服务到期：2099-12-31</span>
          <span>授权用户：1/5</span>
        </div>
        <span>Copyright © 易泽 ERP</span>
      </footer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

interface MenuEntry {
  name: string
  route?: string
  referencePath?: string
}

interface MenuGroup {
  name: string
  items: MenuEntry[]
}

interface ReferenceMenu {
  name: string
  groups: MenuGroup[]
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const hoverModule = ref('')
const viewKey = ref(0)

const referenceMenus: ReferenceMenu[] = [
  {
    name: '采购',
    groups: [
      { name: '采购业务', items: entries([['请购单', '/inv/invQgd/list'], ['采购订单', '/inv/invCgdd/list?shzt=&ddzt=', '/purchase/orders'], ['采购单', '/inv/invCgd/list?shzt=&ddzt='], ['采购退货单', '/inv/invCgth/list?shzt=&ddzt='], ['采购发票登记', '/inv/cgdkp/list']]) },
      { name: '采购报表', items: entries([['采购汇总表', '/inv/invCgbb/invCghz'], ['采购订单明细', '/inv/invCgbb/invCgddmx'], ['采购单明细', '/inv/invCgbb/invCgmx'], ['采购开票统计', '/inv/invCgbb/invCgkptj']]) }
    ]
  },
  {
    name: '销售',
    groups: [
      { name: '批发业务', items: entries([['报价单', '/inv/invBjd/list', '/sales/quotes'], ['网店订单', '/inv/invWddd/list?shzt=&ddzt=', '/sales/online-orders'], ['销售订单', '/inv/invXsdd/list?shzt=&ddzt=', '/sales/orders'], ['销售单', '/inv/invXsd/list?shzt=&ddzt=', '/sales/bills'], ['销售退货单', '/inv/invXsth/list?shzt=&ddzt=', '/sales/returns'], ['销售返修单', '/inv/xsfxd/list'], ['销售发票登记', '/inv/xsdkp/list'], ['销售回款情况', '/inv/xshk/list']]) },
      { name: '零售业务', items: entries([['零售收银', '/inv/invDsdPos/toPos'], ['零售收银历史', '/inv/invDsdPos/sylsList']]) },
      { name: '其他', items: entries([['销售订单明细', '/inv/invXsbb/invXsddmx'], ['销售单明细', '/inv/invXsbb/invXsmx'], ['销售退货单明细', '/inv/invXsbb/invXsthmx'], ['无成本销售明细', '/inv/invXsbb/wcbXsmx']]) },
      { name: '销售报表', items: entries([['销售汇总', '/inv/invXsbb/invXshz'], ['销量分析表', '/inv/invSpxlfxb/splbxlfx'], ['进销对比统计', '/inv/invKcxx/spcrkHz'], ['销售趋势分析', '/inv/invSpxlqsfxb/list'], ['销量排行榜', '/inv/invSpxlphb/tab'], ['销售业绩分析', '/inv/invYjfxForXsr/invYjfx'], ['销售成本分析', '/inv/invXsbb/invXscb'], ['销售成本明细分析', '/inv/invXsbb/invXscbmx'], ['销售提成统计', '/inv/invXsbb/invXstcHz'], ['销售提成明细', '/inv/invXsbb/invXstcMx'], ['销售开票统计', '/inv/invXsbb/invXstjkp']]) }
    ]
  },
  {
    name: '生产',
    groups: [
      { name: '自制业务', items: entries([['生产任务', '/inv/invScrw/list'], ['生产领料', '/inv/scrw/jg/scllList'], ['用料登记', '/inv/scrw/jg/yldjList'], ['工序汇报', '/inv/scrw/jg/gxhbList'], ['生产质检', '/inv/scrw/jg/sczjList'], ['成品入库', '/inv/scrw/jg/scrkList']]) },
      { name: '委外业务', items: entries([['委外任务', '/inv/invScWwjg/list'], ['物料出库', '/inv/wwrw/jg/wlckList'], ['提请付款', '/inv/wwrw/jg/wwfkList'], ['成品质检', '/inv/wwrw/jg/cpzjList'], ['成品入库', '/inv/wwrw/jg/wwrkList'], ['委外付款情况', '/inv/invScWwjg/wwfkList']]) },
      { name: '其他', items: entries([['工序管理', '/inv/scgx/list'], ['BOM管理', '/inv/invScwl/list'], ['BOM模板', '/inv/scwlmb/list'], ['不良品项', '/inv/blpx/list'], ['计时工资', '/inv/jsgz/list'], ['计件工资', '/inv/jjgz/list'], ['工序汇报记录', '/inv/invScrw/gxhbList']]) },
      { name: '生产报表', items: entries([['生产看板', '/inv/scrwbb/toSckbList'], ['生产成本统计', '/inv/scrwbb/toSccbtjList'], ['订单进度跟踪表', '/inv/invScrw/toJdgz'], ['委外进度跟踪表', '/inv/invScWwjg/toJdgz'], ['生产进度跟踪表', '/inv/scrwbb/toJgjdList'], ['物料追踪表', '/inv/scrwbb/toScWlzzList'], ['缺料追踪表', '/inv/scrwbb/toScqlzzList'], ['计件工资统计表', '/inv/jjgz/toJjgzHzList'], ['计时工资统计表', '/inv/jsgz/toJsgzHzList']]) }
    ]
  },
  {
    name: '库管',
    groups: [
      { name: '库管单据', items: entries([['入库管理', '/inv/invQtrk/list'], ['出库管理', '/inv/invQtck/list'], ['盘点单', '/inv/invPdd/list'], ['调拨单', '/inv/invDbd/list?shzt='], ['组装拆卸单', '/inv/invZzcxd/list?shzt=&ddzt='], ['报损单', '/inv/invBsd/list'], ['补货申请', '/inv/bhsq/list'], ['补货审理', '/inv/invMdbh/bhlist']]) },
      { name: '库存查询', items: entries([['商品库存查询', '/inv/invKcxx/list'], ['序列号跟踪', '/inv/xlh/list'], ['批号/保质期查询', '/inv/phkcxx/list'], ['在途商品查询', '/inv/invDbd/ztSpxxList']]) },
      { name: '仓库报表', items: entries([['仓库库存统计', '/inv/invCrk/ckCrktjList'], ['商品出入库统计', '/inv/invCrk/spCrktjList'], ['项目出入库统计', '/inv/invCrk/xmCrktjList'], ['出入库流水', '/inv/invCrk/list'], ['入库单明细', '/inv/invCrk/rkdmxList'], ['出库单明细', '/inv/invCrk/ckdmxList'], ['调拨单明细', '/inv/invDbd/reportDbdlist']]) }
    ]
  },
  {
    name: '物流',
    groups: [
      { name: '物流发货', items: entries([['按销售单', '/inv/xsfh/list'], ['按出库单', '/inv/ckdfh/list']]) },
      { name: '信息管理', items: entries([['物流公司', '/inv/invWlxx/list', '/master-data/logistics'], ['物流查询', '/wlgl/wlcx/list']]) },
      { name: '参数配置', items: entries([['物流平台设置', '/wlgl/wlpz/list']]) }
    ]
  },
  {
    name: '财务',
    groups: [
      { name: '往来账务', items: entries([['应付账款', '/inv/invYfzk/list'], ['付款单', '/inv/invFkd/list?shzt=&ddzt='], ['应收账款', '/inv/invYszk/list'], ['收款单', '/inv/invSkd/list?shzt=&ddzt='], ['其他应付单', '/inv/invQtyfk/list'], ['其他应收单', '/inv/invQtysk/list'], ['应收抵应付', '/inv/dxd/ysyfcd/list']]) },
      { name: '资金业务', items: entries([['日常支出', '/inv/invRcsr/zclist'], ['日常收入', '/inv/invRcsr/list'], ['预收款', '/inv/ysd/list'], ['预收退款', '/inv/ystk/list'], ['预付款', '/inv/yfd/list'], ['预付退款', '/inv/yftk/list'], ['借入款', '/inv/invJrk/list'], ['借出款', '/inv/invJck/list']]) },
      { name: '费用报销', items: entries([['报销申请', '/inv/fybx/bxdList'], ['报销审批', '/inv/fybx/bxspList']]) },
      { name: '其他信息', items: entries([['结算账户', '/inv/invYwlb/jszhlist'], ['账户互转', '/inv/invZhzz/list'], ['固定资产', '/inv/invGdzc/list'], ['支出类别', '/inv/invYwlb/zclist'], ['收入类别', '/inv/invYwlb/srlist'], ['资金流水', '/inv/invJszh/jszhLoglist']]) },
      { name: '财务报表', items: entries([['客户对账统计', '/inv/invKhdztj/khdztjGroup'], ['客户对账明细', '/inv/invKhdztj/mxlist'], ['供应商对账统计', '/inv/invGys/gysDztjGroupList'], ['供应商对账明细', '/inv/invGys/gysDzmxlist'], ['未收账款统计', '/inv/invKhdztj/invWszkList'], ['未付账款统计', '/inv/invGys/invWfzkTjList'], ['利润统计', '/inv/invLrtj/list'], ['费用报销统计', '/inv/fybx/bxtjList'], ['日常收支统计', '/inv/invRcsr/tjlist']]) }
    ]
  },
  {
    name: '总账',
    groups: [
      { name: '凭证', items: entries([['新增凭证', '/cwgl/kjpzmx/toEdit'], ['查看凭证', '/cwgl/kjpz/list'], ['凭证汇总', '/cwgl/kjpz/pzhz']]) },
      { name: '账簿', items: entries([['明细账', '/cwgl/kjzb/mxzList'], ['总账', '/cwgl/kjzb/zzList'], ['科目余额表', '/cwgl/kjzb/yebList'], ['核算项目明细账', '/cwgl/kjzb/fzhsMxzList'], ['核算项目余额表', '/cwgl/kjzb/fzhsYebList']]) },
      { name: '结账', items: entries([['期末结账', '/cwgl/qmjz/list']]) },
      { name: '报表', items: entries([['资产负债表', '/cwgl/cwbb/zcfzb'], ['利润表', '/cwgl/cwbb/lrb'], ['部门利润表', '/cwgl/cwbb/orgLrb'], ['项目利润表', '/cwgl/cwbb/xmLrb']]) },
      { name: '其他', items: entries([['科目', '/cwgl/km/list'], ['科目期初', '/cwgl/km/qcList'], ['常用凭证模板', '/cwgl/pzmb/list'], ['总账参数', '/cwgl/gncs/list']]) }
    ]
  },
  {
    name: '客户',
    groups: [
      { name: '客户管理', items: entries([['客户信息', '/inv/invKh/list', '/customer/customers'], ['潜在商机', '/inv/invKh/listGh'], ['客户跟进', '/inv/khgj/list']]) },
      { name: '会员管理', items: entries([['会员等级维护', '/inv/invHydj/list'], ['会员信息管理', '/inv/invHyxx/list'], ['会员参数', '/inv/invHyxx/hycsList']]) },
      { name: '客户报表', items: entries([['睡眠客户统计', '/inv/khreport/smkhList'], ['流失客户统计', '/inv/khreport/lskhList']]) },
      { name: '其他信息', items: entries([['跟进类别', '/inv/khgjlb/list']]) }
    ]
  },
  {
    name: '人事',
    groups: [
      { name: '人事管理', items: entries([['薪资设置', '/sys/sysygxzxx/list', '/hr/salary-settings'], ['业绩目标', '/inv/invYgxxYjzb/list'], ['月度工资', '/sys/sysGzhz/list']]) },
      { name: '工作奖惩', items: entries([['惩罚单', '/sys/jcglCfd/list'], ['奖励单', '/sys/jcglJld/list']]) }
    ]
  },
  {
    name: '基础',
    groups: [
      {
        name: '商品管理',
        items: [
          { name: '商品信息', route: '/master-data/products' },
          { name: '商品属性', route: '/master-data/product-attrs' }
        ]
      },
      {
        name: '信息管理',
        items: [
          { name: '供应商管理', route: '/master-data/suppliers' },
          { name: '项目信息', route: '/master-data/projects' },
          { name: '物流公司', route: '/master-data/logistics' }
        ]
      },
      {
        name: '工具',
        items: entries([['条形码打印', '/inv/xygj/toPrint4sptxm'], ['序列号打印', '/inv/xlh/geneList']])
      }
    ]
  },
  {
    name: '设置',
    groups: [
      { name: '期初设置', items: entries([['库存期初', '/inv/invSpxx/qclist'], ['供应商期初', '/inv/invGys/qclist'], ['客户期初', '/inv/invKh/qclist'], ['结算账户期初', '/inv/invYwlb/jszhQclist']]) },
      { name: '其他', items: entries([['标记设置', '/inv/qybz/list']]) }
    ]
  }
]

const hoveredMenu = computed(() => referenceMenus.find((item) => item.name === hoverModule.value))
const activeModule = computed(() => {
  if (route.path === '/reference-pending' && typeof route.query.module === 'string') return route.query.module
  const menu = referenceMenus.find((module) =>
    module.groups.some((group) => group.items.some((item) => item.route === route.path))
  )
  return hoveredMenu.value?.name || menu?.name || ''
})
const currentTitle = computed(() => {
  if (route.path === '/dashboard') return '首页'
  if (route.path === '/reference-pending' && typeof route.query.name === 'string') return route.query.name
  for (const module of referenceMenus) {
    for (const group of module.groups) {
      const item = group.items.find((entry) => entry.route === route.path)
      if (item) return item.name
    }
  }
  return '首页'
})

function entries(items: Array<[string, string, string?]>): MenuEntry[] {
  return items.map(([name, referencePath, route]) => ({ name, referencePath, route }))
}

function openModule(module: ReferenceMenu) {
  hoverModule.value = module.name
}

async function selectEntry(item: MenuEntry, moduleName: string, groupName: string) {
  hoverModule.value = ''
  if (item.route) {
    await router.push(item.route)
    return
  }
  await router.push({
    path: '/reference-pending',
    query: {
      module: moduleName,
      group: groupName,
      name: item.name,
      referencePath: item.referencePath || ''
    }
  })
}

function isCurrentEntry(item: MenuEntry, moduleName: string, groupName: string) {
  if (item.route) return route.path === item.route
  return route.path === '/reference-pending'
    && route.query.module === moduleName
    && route.query.group === groupName
    && route.query.name === item.name
}

async function go(path: string) {
  hoverModule.value = ''
  await router.push(path)
}

function refreshPage() {
  viewKey.value += 1
}

async function logout() {
  await auth.logout()
  await router.push('/login')
}
</script>

<style scoped>
.erp-shell {
  display: flex;
  min-width: 1180px;
  min-height: 100vh;
  background: #eef2f6;
}

.module-bar {
  position: fixed;
  z-index: 20;
  top: 0;
  bottom: 0;
  left: 0;
  width: 100px;
  background: #203447;
  box-shadow: 2px 0 10px rgb(15 23 42 / 16%);
}

.home-entry,
.module-item {
  display: grid;
  width: 100%;
  height: 60px;
  place-items: center;
  padding: 8px 0;
  color: rgb(255 255 255 / 78%);
  background: transparent;
  border: 0;
  border-bottom: 1px solid rgb(255 255 255 / 7%);
  cursor: pointer;
}

.home-entry span,
.module-item span {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  background: rgb(255 255 255 / 10%);
  border-radius: 2px;
  font-size: 13px;
}

.home-entry strong,
.module-item strong {
  font-size: 13px;
  font-weight: 500;
}

.home-entry.active,
.module-item.active,
.home-entry:hover,
.module-item:hover {
  color: #fff;
  background: #1677ff;
}

.menu-flyout {
  position: absolute;
  top: 0;
  left: 100px;
  display: flex;
  min-height: 100vh;
  padding: 22px 18px;
  background: #fff;
  border-right: 1px solid #d9e1ea;
  box-shadow: 10px 0 24px rgb(15 23 42 / 12%);
}

.flyout-column {
  width: 174px;
  margin-right: 18px;
}

.flyout-column h3 {
  margin: 0 0 12px;
  padding-bottom: 10px;
  color: #1f2937;
  border-bottom: 1px solid #edf1f5;
  font-size: 15px;
  font-weight: 600;
}

.flyout-column button {
  display: block;
  width: 100%;
  height: 32px;
  padding: 0 8px;
  color: #475467;
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 2px;
  cursor: pointer;
}

.flyout-column button:hover,
.flyout-column button.current {
  color: #1677ff;
  background: #eef6ff;
}

.workspace {
  display: flex;
  flex: 1;
  min-height: 100vh;
  margin-left: 100px;
  flex-direction: column;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 54px;
  padding: 0 18px;
  background: #fff;
  border-bottom: 1px solid #dce3ea;
}

.company {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-mark {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  color: #fff;
  background: #1677ff;
  border-radius: 2px;
  font-size: 12px;
  font-weight: 700;
}

.company strong {
  color: #1f2937;
  font-size: 16px;
}

.company em {
  display: inline-flex;
  height: 22px;
  align-items: center;
  padding: 0 8px;
  color: #1677ff;
  background: #eef6ff;
  border: 1px solid #b7dcff;
  border-radius: 2px;
  font-size: 12px;
  font-style: normal;
}

.top-links {
  display: flex;
  align-items: center;
  gap: 16px;
}

.top-links button {
  color: #475467;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.top-links button:hover {
  color: #1677ff;
}

.user-button {
  font-weight: 600;
}

.tabbar {
  display: flex;
  height: 38px;
  align-items: center;
  background: #f8fafc;
  border-bottom: 1px solid #dce3ea;
}

.tab-roll,
.tab-action {
  height: 100%;
  padding: 0 12px;
  color: #475467;
  background: #fff;
  border: 0;
  border-right: 1px solid #dce3ea;
  cursor: pointer;
}

.tabs {
  display: flex;
  flex: 1;
  height: 100%;
  align-items: center;
  overflow: hidden;
}

.tab {
  height: 100%;
  min-width: 118px;
  padding: 0 18px;
  color: #1677ff;
  background: #fff;
  border: 0;
  border-right: 1px solid #dce3ea;
  border-top: 2px solid #1677ff;
  cursor: pointer;
}

.content {
  flex: 1;
  overflow: auto;
  background: #eef2f6;
}

.footerbar {
  display: flex;
  height: 30px;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  color: #667085;
  background: #fff;
  border-top: 1px solid #dce3ea;
  font-size: 12px;
}

.footerbar div {
  display: flex;
  gap: 18px;
}
</style>
