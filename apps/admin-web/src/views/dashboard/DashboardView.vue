<template>
  <div class="page">
    <h1 class="page-title">系统首页</h1>
    <a-row :gutter="12">
      <a-col v-for="item in metrics" :key="item.title" :span="6">
        <a-card :bordered="false">
          <a-statistic :title="item.title" :value="item.value" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
const metrics = [
  { title: '当前阶段', value: 0 },
  { title: '系统模块', value: 7 },
  { title: '基础资料', value: 0 },
  { title: '待办事项', value: 0 }
]
</script>
