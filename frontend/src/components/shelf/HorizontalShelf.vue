<script setup lang="ts">
defineProps<{
  title: string;
  more?: string;
}>();
</script>

<template>
  <section class="shelf">
    <header class="shelf-header">
      <h2 class="title">{{ title }}</h2>
      <router-link v-if="more" :to="more" class="more">查看全部 →</router-link>
    </header>
    <div class="shelf-scroll">
      <div class="shelf-content">
        <slot />
      </div>
    </div>
  </section>
</template>

<style lang="scss" scoped>
.shelf {
  margin-bottom: var(--space-6);
}
.shelf-header {
  display: flex; justify-content: space-between; align-items: baseline;
  margin-bottom: var(--space-4);
}
.title {
  font-size: 22px; font-weight: 700;
  color: var(--color-fg-primary);
  margin: 0;
}
.more {
  color: var(--color-brand);
  text-decoration: none;
  font-size: 13px;
  &:hover { color: var(--color-brand-hover); }
}
.shelf-scroll {
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
}
.shelf-content {
  display: flex; gap: var(--space-4);
}
</style>
