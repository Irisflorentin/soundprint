<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
</script>

<template>
  <div>
    <PageHeader title="艺术家详情" />
    <EmptyState title="艺术家详情 · 后续实装" description="将展示艺术家信息 + 专辑 + 曲目（/api/artists/{id}）" />
  </div>
</template>
