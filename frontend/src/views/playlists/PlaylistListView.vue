<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
</script>

<template>
  <div>
    <PageHeader title="歌单" subtitle="我的歌单" />
    <EmptyState title="歌单 · 后续实装" description="将调用 /api/playlists" />
  </div>
</template>
