<script setup lang="ts">
withDefaults(defineProps<{
  hoverable?: boolean;
  padding?: 'sm' | 'md' | 'lg' | 'none';
}>(), {
  hoverable: false,
  padding: 'md',
});
</script>

<template>
  <div
    class="glass-card"
    :class="[
      `padding-${padding}`,
      hoverable && 'hoverable',
    ]"
  >
    <slot />
  </div>
</template>

<style lang="scss" scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-card);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: all var(--duration-base) var(--ease);

  &.padding-sm  { padding: var(--space-3); }
  &.padding-md  { padding: var(--space-5); }
  &.padding-lg  { padding: var(--space-6); }
  &.padding-none { padding: 0; }

  &.hoverable {
    cursor: pointer;
    &:hover {
      background: rgba(255, 255, 255, 0.05);
      border-color: rgba(244, 245, 247, 0.16);
      transform: translateY(-2px);
      box-shadow: var(--shadow-glass);
    }
  }
}
</style>
