<script setup lang="ts">
withDefaults(defineProps<{
  title?: string;
  description?: string;
}>(), {
  title: '暂无数据',
  description: '',
});
</script>

<template>
  <div class="empty-state">
    <div class="icon">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M9 18V5l12-2v13" stroke-linecap="round" stroke-linejoin="round" />
        <circle cx="6" cy="18" r="3" />
        <circle cx="18" cy="16" r="3" />
      </svg>
    </div>
    <h3 class="title">{{ title }}</h3>
    <p v-if="description" class="desc">{{ description }}</p>
    <div class="extra"><slot /></div>
  </div>
</template>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-10) var(--space-5);
  color: var(--color-fg-tertiary);
  text-align: center;
}
.icon {
  width: 56px;
  height: 56px;
  margin-bottom: var(--space-4);
  color: var(--color-brand);
  opacity: 0.6;
  svg { width: 100%; height: 100%; }
}
.title { margin: 0; font-size: 16px; color: var(--color-fg-secondary); font-weight: 600; }
.desc { margin: var(--space-2) 0 0; font-size: 13px; color: var(--color-fg-tertiary); }
.extra { margin-top: var(--space-4); }
</style>
