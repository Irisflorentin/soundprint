<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { fileUrl } from '@/utils/url';

const props = withDefaults(defineProps<{
  src?: string | null;
  alt?: string;
  fallbackText?: string;
  rounded?: 'square' | 'circle';
}>(), {
  src: null,
  alt: '',
  fallbackText: '',
  rounded: 'square',
});

const failed = ref(false);
const resolvedSrc = computed(() => fileUrl(props.src));
const fallbackLabel = computed(() => props.fallbackText?.trim().slice(0, 1) || '');

watch(() => props.src, () => {
  failed.value = false;
});
</script>

<template>
  <div class="smart-cover" :class="{ circle: rounded === 'circle' }">
    <img
      v-if="resolvedSrc && !failed"
      :src="resolvedSrc"
      :alt="alt"
      class="cover-img"
      @error="failed = true"
    >
    <div v-else class="cover-fallback">
      <span v-if="fallbackLabel">{{ fallbackLabel }}</span>
    </div>
    <slot />
  </div>
</template>

<style scoped lang="scss">
.smart-cover {
  position: relative;
  overflow: hidden;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.82), rgba(6, 182, 212, 0.68));
}

.smart-cover.circle {
  border-radius: 50%;
}

.cover-img,
.cover-fallback {
  width: 100%;
  height: 100%;
}

.cover-img {
  display: block;
  object-fit: cover;
}

.cover-fallback {
  display: grid;
  place-items: center;
  color: var(--color-fg-primary);
  font-size: clamp(18px, 25%, 42px);
  font-weight: 800;
}
</style>
