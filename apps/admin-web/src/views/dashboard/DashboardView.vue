<template>
  <div class="home-page">
    <section class="business-row">
      <article v-for="card in businessCards" :key="card.title" class="business-card">
        <span class="business-icon">{{ card.icon }}</span>
        <div>
          <strong>{{ card.title }}</strong>
          <p>{{ card.text }}</p>
        </div>
        <em>{{ card.value }}</em>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel inventory-total">
        <header>
          <h2>库存总额</h2>
          <button type="button">刷新</button>
        </header>
        <div class="amount">￥0.00</div>
        <div class="sub-metrics">
          <span>库存数量：0</span>
          <span>商品种类：0</span>
        </div>
      </article>

      <article class="panel">
        <header><h2>库存分布-仓库</h2></header>
        <div class="bar-list">
          <div v-for="item in warehouseStats" :key="item.name">
            <span>{{ item.name }}</span>
            <i :style="{ width: item.rate }"></i>
            <em>{{ item.value }}</em>
          </div>
        </div>
      </article>

      <article class="panel">
        <header><h2>库存分布-品类</h2></header>
        <div class="bar-list">
          <div v-for="item in categoryStats" :key="item.name">
            <span>{{ item.name }}</span>
            <i :style="{ width: item.rate }"></i>
            <em>{{ item.value }}</em>
          </div>
        </div>
      </article>

      <article class="panel ranking">
        <header><h2>客户销售排行榜</h2></header>
        <ol>
          <li v-for="item in customerRanks" :key="item.name">
            <span>{{ item.name }}</span>
            <em>{{ item.amount }}</em>
          </li>
        </ol>
      </article>

      <article class="panel progress-panel">
        <header><h2>销售业绩达成</h2></header>
        <div class="progress-ring">
          <strong>0%</strong>
          <span>本月达成率</span>
        </div>
        <div class="target-row">
          <span>目标：￥0.00</span>
          <span>完成：￥0.00</span>
        </div>
      </article>

      <article class="panel trend-panel">
        <header><h2>销售趋势</h2></header>
        <div class="trend-lines">
          <span v-for="point in trendPoints" :key="point" :style="{ height: `${point}px` }"></span>
        </div>
      </article>

      <article class="panel todo-panel">
        <header><h2>我的待办</h2></header>
        <div class="todo-grid">
          <button v-for="todo in todos" :key="todo.name" type="button">
            <strong>{{ todo.count }}</strong>
            <span>{{ todo.name }}</span>
          </button>
        </div>
      </article>

      <article class="panel shortcuts">
        <header><h2>快捷入口</h2></header>
        <div class="shortcut-groups">
          <section v-for="group in quickEntries" :key="group.name">
            <h3>{{ group.name }}</h3>
            <div>
              <button v-for="entry in group.items" :key="entry" type="button">{{ entry }}</button>
            </div>
          </section>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
const businessCards = [
  { title: '销售单', text: '今日待处理销售业务', value: '0', icon: '销' },
  { title: '采购单', text: '今日待处理采购业务', value: '0', icon: '采' },
  { title: '生产任务', text: '进行中生产任务', value: '0', icon: '产' },
  { title: '库存', text: '库存预警与可用库存', value: '0', icon: '库' }
]

const warehouseStats = [
  { name: '成品仓', value: '0', rate: '72%' },
  { name: '原料仓', value: '0', rate: '52%' },
  { name: '次品仓', value: '0', rate: '18%' }
]

const categoryStats = [
  { name: '成品', value: '0', rate: '68%' },
  { name: '半成品', value: '0', rate: '42%' },
  { name: '原材料', value: '0', rate: '36%' }
]

const customerRanks = [
  { name: '暂无客户', amount: '￥0.00' },
  { name: '暂无客户', amount: '￥0.00' },
  { name: '暂无客户', amount: '￥0.00' },
  { name: '暂无客户', amount: '￥0.00' }
]

const trendPoints = [38, 54, 44, 70, 58, 86, 64, 92, 76, 104, 88, 116]

const todos = [
  { name: '库存预警', count: 0 },
  { name: '待办出库', count: 0 },
  { name: '待办入库', count: 0 },
  { name: '采购订单', count: 0 },
  { name: '销售订单', count: 0 },
  { name: '生产任务', count: 0 }
]

const quickEntries = [
  { name: '采购管理', items: ['请购单', '采购订单', '采购单', '采购退货单'] },
  { name: '销售管理', items: ['销售订单', '销售单', '销售退货单', '报价单'] },
  { name: '生产管理', items: ['生产任务', '生产领料', '工序汇报', '成品入库'] },
  { name: '库存管理', items: ['库存查询', '调拨单', '盘点单', '库存预警'] }
]
</script>

<style scoped>
.home-page {
  padding: 14px;
}

.business-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.business-card {
  display: grid;
  grid-template-columns: 44px 1fr auto;
  min-height: 86px;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #dfe6ee;
}

.business-icon {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  color: #1677ff;
  background: #eef6ff;
  border-radius: 2px;
  font-weight: 700;
}

.business-card strong {
  display: block;
  color: #1f2937;
  font-size: 16px;
}

.business-card p {
  margin: 6px 0 0;
  color: #667085;
  font-size: 13px;
}

.business-card em {
  color: #1677ff;
  font-size: 26px;
  font-style: normal;
  font-weight: 700;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: 12px;
}

.panel {
  min-height: 230px;
  padding: 14px;
  background: #fff;
  border: 1px solid #dfe6ee;
}

.panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel h2 {
  margin: 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
}

.panel header button {
  color: #1677ff;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.amount {
  margin: 30px 0 16px;
  color: #1677ff;
  font-size: 38px;
  font-weight: 700;
}

.sub-metrics,
.target-row {
  display: flex;
  gap: 20px;
  color: #667085;
}

.bar-list {
  display: grid;
  gap: 18px;
  padding-top: 8px;
}

.bar-list div {
  display: grid;
  grid-template-columns: 74px 1fr 42px;
  align-items: center;
  gap: 10px;
  color: #475467;
}

.bar-list i {
  display: block;
  height: 10px;
  background: #1677ff;
  border-radius: 1px;
}

.bar-list em,
.ranking em {
  color: #667085;
  font-style: normal;
}

.ranking ol {
  display: grid;
  gap: 12px;
  margin: 0;
  padding-left: 22px;
}

.ranking li {
  padding-bottom: 10px;
  border-bottom: 1px solid #edf1f5;
}

.ranking li span {
  display: inline-block;
  width: calc(100% - 84px);
}

.progress-panel {
  display: grid;
  align-content: start;
}

.progress-ring {
  display: grid;
  width: 138px;
  height: 138px;
  place-items: center;
  margin: 4px auto 18px;
  border: 12px solid #e8f2ff;
  border-top-color: #1677ff;
  border-radius: 50%;
}

.progress-ring strong,
.progress-ring span {
  grid-column: 1;
}

.progress-ring strong {
  align-self: end;
  color: #1677ff;
  font-size: 26px;
}

.progress-ring span {
  align-self: start;
  color: #667085;
  font-size: 12px;
}

.trend-lines {
  display: flex;
  height: 170px;
  align-items: end;
  gap: 12px;
  padding: 12px 6px 0;
  border-bottom: 1px solid #dfe6ee;
}

.trend-lines span {
  flex: 1;
  min-width: 10px;
  background: linear-gradient(#1677ff, #91caff);
  border-radius: 2px 2px 0 0;
}

.todo-panel,
.shortcuts {
  grid-column: span 3;
  min-height: auto;
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.todo-grid button {
  height: 74px;
  background: #f8fafc;
  border: 1px solid #e3e9f1;
  cursor: pointer;
}

.todo-grid strong,
.todo-grid span {
  display: block;
}

.todo-grid strong {
  color: #1677ff;
  font-size: 24px;
}

.todo-grid span {
  color: #475467;
}

.shortcut-groups {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.shortcut-groups h3 {
  margin: 0 0 10px;
  color: #1f2937;
  font-size: 14px;
}

.shortcut-groups div {
  display: grid;
  gap: 8px;
}

.shortcut-groups button {
  height: 32px;
  color: #475467;
  text-align: left;
  background: #f8fafc;
  border: 1px solid #e3e9f1;
  cursor: pointer;
}
</style>
