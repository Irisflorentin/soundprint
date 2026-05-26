<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { fileUrl } from '@/utils/url';
import { placeholderGradient, placeholderInitial, type PlaceholderType } from '@/utils/placeholder';

const props = withDefaults(defineProps<{
  src?: string | null;
  alt?: string;
  fallbackText?: string;
  rounded?: 'square' | 'circle';
  placeholderType?: PlaceholderType;
}>(), {
  src: null,
  alt: '',
  fallbackText: '',
  rounded: 'square',
  placeholderType: 'track',
});

const failed = ref(false);
const resolvedSrc = computed(() => fileUrl(props.src));
const fallbackLabel = computed(() => placeholderInitial(props.fallbackText || props.alt || '?'));
const fallbackStyle = computed(() => ({
  background: placeholderGradient(props.fallbackText || props.alt || '?', props.placeholderType),
}));

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
    <div v-else class="cover-fallback" :style="fallbackStyle">
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
  background: linear-gradient(135deg, rgba(244, 245, 247, 0.72), rgba(148, 163, 184, 0.62));
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
  position: relative;
  overflow: hidden;
  display: grid;
  place-items: center;
  color: rgba(10, 10, 11, 0.65);
  font-size: clamp(18px, 25%, 42px);
  font-weight: 800;

  &::after {
    position: absolute;
    inset: 0;
    content: '';
    background:
      radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0), rgba(0, 0, 0, 0.24)),
      linear-gradient(120deg, rgba(255, 255, 255, 0.18), transparent 46%, rgba(255, 255, 255, 0.08));
  }

  span {
    position: relative;
    z-index: 1;
  }
}
</style>
