<script setup lang="ts">
import PageHeader from '@/components/common/PageHeader.vue';
import EmptyState from '@/components/common/EmptyState.vue';
</script>

<template>
  <div>
    <PageHeader title="转换工坊" subtitle="FLAC ↔ MP3 ↔ WAV ↔ AAC" />
    <EmptyState title="转换工坊 · Phase 5 实装" description="后端骨架已就绪（/api/conversions），前端转换 UI 在 Phase 5" />
  </div>
</template>
