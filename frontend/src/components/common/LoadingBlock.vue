<script setup lang="ts">
withDefaults(defineProps<{
  text?: string;
}>(), {
  text: '加载中...',
});
</script>

<template>
  <div class="loading-block">
    <span class="spinner" />
    <span class="text">{{ text }}</span>
  </div>
</template>

<style lang="scss" scoped>
.loading-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-10);
  color: var(--color-fg-tertiary);
}
.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid rgba(244, 245, 247, 0.18);
  border-top-color: var(--color-brand);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.text { font-size: 13px; }
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
