<template>
  <div class="page">
    <h1 class="page-title">经营看板</h1>
    <a-row :gutter="12">
      <a-col :span="6" v-for="item in metrics" :key="item.title">
        <a-card :bordered="false">
          <a-statistic :title="item.title" :value="item.value" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
const metrics = [
  { title: '待建基础资料', value: 0 },
  { title: '待配置用户', value: 0 },
  { title: '待审批单据', value: 0 },
  { title: '系统待办', value: 0 }
]
</script>
