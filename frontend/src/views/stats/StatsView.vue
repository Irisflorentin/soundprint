<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
</script>

<template>
  <div>
    <PageHeader title="听歌报告" subtitle="流派分布 / 月度趋势 / Top 艺术家 / 热力图" />
    <EmptyState title="听歌报告 · Phase 6 实装" description="后端统计接口已就绪（/api/stats/*），ECharts 可视化在 Phase 6" />
  </div>
</template>
