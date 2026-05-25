<script setup lang="ts">
defineProps<{
  title: string;
  subtitle?: string;
}>();
</script>

<template>
  <header class="page-header">
    <div class="texts">
      <h1 class="title">{{ title }}</h1>
      <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
    </div>
    <div class="actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-fg-primary);
}
.subtitle {
  margin: var(--space-2) 0 0;
  font-size: 14px;
  color: var(--color-fg-secondary);
}
.actions { display: flex; align-items: center; gap: var(--space-3); flex-shrink: 0; }
</style>
