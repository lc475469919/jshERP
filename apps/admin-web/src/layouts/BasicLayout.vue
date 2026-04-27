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
            :class="{ current: item.route === route.path }"
            @click="selectEntry(item)"
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
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'

interface MenuEntry {
  name: string
  route?: string
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
      { name: '采购业务', items: named(['请购单', '采购订单', '采购单', '采购退货单', '采购发票登记']) },
      { name: '采购报表', items: named(['采购明细表', '采购汇总表', '采购订单跟踪表', '供应商采购排行']) }
    ]
  },
  {
    name: '销售',
    groups: [
      { name: '批发业务', items: named(['销售订单', '销售单', '销售退货单', '销售发票登记']) },
      { name: '零售业务', items: named(['零售开单', '零售退货', '零售流水']) },
      { name: '其他', items: named(['报价单', '销售费用', '销售目标']) },
      { name: '销售报表', items: named(['销售明细表', '销售汇总表', '客户销售排行', '销售业绩达成']) }
    ]
  },
  {
    name: '生产',
    groups: [
      { name: '自制业务', items: named(['生产任务', '生产领料', '用料登记', '工序汇报', '生产质检', '成品入库']) },
      { name: '委外业务', items: named(['委外任务', '物料出库', '提请付款', '成品质检', '成品入库', '委外付款情况']) },
      { name: '其他', items: named(['工序管理', 'BOM管理', 'BOM模板', '不良品项', '计时工资', '计件工资', '工序汇报记录']) },
      { name: '生产报表', items: named(['生产看板', '生产成本统计', '订单进度跟踪表', '委外进度跟踪表', '生产进度跟踪表', '物料追踪表', '缺料追踪表', '计件工资统计表', '计时工资统计表']) }
    ]
  },
  {
    name: '库管',
    groups: [
      { name: '库存业务', items: named(['其他入库', '其他出库', '调拨单', '盘点单', '组装拆卸']) },
      { name: '库存报表', items: named(['库存查询', '库存流水', '库存预警', '收发存汇总表']) }
    ]
  },
  {
    name: '物流',
    groups: [
      { name: '物流业务', items: named(['物流单', '物流对账', '运费登记']) },
      { name: '物流报表', items: named(['物流明细表', '物流汇总表']) }
    ]
  },
  {
    name: '财务',
    groups: [
      { name: '收付款', items: named(['收款单', '付款单', '其他收入', '费用支出']) },
      { name: '往来报表', items: named(['应收款明细', '应付款明细', '客户对账', '供应商对账']) }
    ]
  },
  {
    name: '总账',
    groups: [
      { name: '凭证', items: named(['凭证录入', '凭证审核', '凭证查询']) },
      { name: '账簿报表', items: named(['总账', '明细账', '科目余额表', '资产负债表', '利润表']) }
    ]
  },
  {
    name: '客户',
    groups: [
      { name: '客户管理', items: named(['客户资料', '客户分类', '客户跟进', '联系人']) },
      { name: '客户报表', items: named(['客户销售排行', '客户应收分析']) }
    ]
  },
  {
    name: '人事',
    groups: [
      { name: '人事管理', items: named(['员工信息', '薪资设置', '业绩目标', '月度工资']) },
      { name: '考勤工资', items: named(['打卡记录', '考勤统计', '计件工资', '计时工资', '工资汇总']) }
    ]
  },
  {
    name: '基础',
    groups: [
      {
        name: '商品管理',
        items: [
          { name: '商品信息', route: '/master-data/products' },
          { name: '商品属性', route: '/master-data/product-attrs' },
          ...named(['条形码打印', '序列号打印'])
        ]
      },
      {
        name: '信息管理',
        items: [
          { name: '供应商管理', route: '/master-data/suppliers' },
          { name: '项目信息', route: '/master-data/projects' },
          { name: '物流公司', route: '/master-data/logistics' },
          ...named(['仓库管理', '客户管理', '职员管理'])
        ]
      }
    ]
  },
  {
    name: '设置',
    groups: [
      { name: '系统设置', items: named(['公司信息', '打印模板', '编号规则', '权限设置']) },
      { name: '其他', items: named(['标记设置', '消息设置', '操作日志']) }
    ]
  }
]

const hoveredMenu = computed(() => referenceMenus.find((item) => item.name === hoverModule.value))
const activeModule = computed(() => {
  const menu = referenceMenus.find((module) =>
    module.groups.some((group) => group.items.some((item) => item.route === route.path))
  )
  return hoveredMenu.value?.name || menu?.name || ''
})
const currentTitle = computed(() => {
  if (route.path === '/dashboard') return '首页'
  for (const module of referenceMenus) {
    for (const group of module.groups) {
      const item = group.items.find((entry) => entry.route === route.path)
      if (item) return item.name
    }
  }
  return '首页'
})

function named(names: string[]): MenuEntry[] {
  return names.map((name) => ({ name }))
}

function openModule(module: ReferenceMenu) {
  hoverModule.value = module.name
}

async function selectEntry(item: MenuEntry) {
  if (!item.route) {
    message.info(`${item.name}：参考入口已建立，页面按顺序采集后开发`)
    return
  }
  hoverModule.value = ''
  await router.push(item.route)
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
