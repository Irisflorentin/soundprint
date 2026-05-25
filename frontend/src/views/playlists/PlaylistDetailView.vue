<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
</script>

<template>
  <div>
    <PageHeader title="歌单详情" />
    <EmptyState title="歌单详情 · 后续实装" description="将展示歌单曲目 + 拖拽排序（/api/playlists/{id}）" />
  </div>
</template>
