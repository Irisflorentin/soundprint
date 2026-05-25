<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
</script>

<template>
  <div>
    <PageHeader title="专辑详情" />
    <EmptyState title="专辑详情 · 后续实装" description="将展示专辑信息 + 曲目列表（/api/albums/{id}）" />
  </div>
</template>
