<template>
  <div class="page">
    <h1 class="page-title">生产制造</h1>
    <a-tabs>
      <a-tab-pane key="self" tab="自制业务">
        <a-list bordered :data-source="selfMade">
          <template #renderItem="{ item }">
            <a-list-item>{{ item }}</a-list-item>
          </template>
        </a-list>
      </a-tab-pane>
      <a-tab-pane key="outsource" tab="委外业务">
        <a-list bordered :data-source="outsourcing">
          <template #renderItem="{ item }">
            <a-list-item>{{ item }}</a-list-item>
          </template>
        </a-list>
      </a-tab-pane>
      <a-tab-pane key="base" tab="基础资料">
        <a-list bordered :data-source="baseData">
          <template #renderItem="{ item }">
            <a-list-item>{{ item }}</a-list-item>
          </template>
        </a-list>
      </a-tab-pane>
      <a-tab-pane key="reports" tab="生产报表">
        <a-list bordered :data-source="reports">
          <template #renderItem="{ item }">
            <a-list-item>{{ item }}</a-list-item>
          </template>
        </a-list>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
const selfMade = ['生产任务', '生产领料', '用料登记', '工序汇报', '生产质检', '成品入库']
const outsourcing = ['委外任务', '物料出库', '提请付款', '成品质检', '成品入库', '委外付款情况']
const baseData = ['工序管理', 'BOM管理', 'BOM模板', '不良品项', '计时工资', '计件工资', '工序汇报记录']
const reports = ['生产看板', '生产成本统计', '订单进度跟踪表', '委外进度跟踪表', '生产进度跟踪表', '物料追踪表', '缺料追踪表']
</script>
