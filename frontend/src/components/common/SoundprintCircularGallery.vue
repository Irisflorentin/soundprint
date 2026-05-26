<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import CircularGallery from '@/components/vue-bits/components/CircullarGallery.vue';
import { fileUrl } from '@/utils/url';
import { releaseWebglContexts } from '@/utils/webgl';
import { placeholderImage } from '@/utils/placeholder';

type GalleryType = 'album' | 'artist' | 'playlist';
type GallerySourceItem = {
  id?: number;
  coverUrl?: string | null;
  avatarUrl?: string | null;
  title?: string | null;
  name?: string | null;
};

const props = defineProps<{
  items: GallerySourceItem[];
  type: GalleryType;
}>();

const emit = defineEmits<{
  (e: 'select', item: GallerySourceItem, index: number): void;
}>();

const root = ref<HTMLElement | null>(null);
const pointerStart = ref<{ x: number; y: number } | null>(null);

const displayedItems = computed(() => props.items.slice(0, 15));

const galleryItems = computed(() => displayedItems.value.map((item) => {
  const rawImage = item.coverUrl || item.avatarUrl || '';
  const text = item.title || item.name || fallbackText(props.type);
  const image = rawImage
    ? toAbsoluteAssetUrl(rawImage)
    : fallbackImage(item, props.type);

  return { image, text };
}));

onBeforeUnmount(() => {
  releaseWebglContexts(root.value);
});

function toAbsoluteAssetUrl(path: string) {
  const url = fileUrl(path);
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url;
  }
  return `${window.location.origin}${url}`;
}

function fallbackText(type: GalleryType) {
  if (type === 'album') return '未命名专辑';
  if (type === 'artist') return '未知艺术家';
  return '未命名歌单';
}

function fallbackImage(item: GallerySourceItem, type: GalleryType): string {
  return placeholderImage(item.title || item.name || fallbackText(type), type);
}

function rememberPointerStart(event: PointerEvent) {
  pointerStart.value = { x: event.clientX, y: event.clientY };
}

function handleClick(event: MouseEvent) {
  if (!root.value || displayedItems.value.length === 0) return;
  if (pointerStart.value) {
    const movedX = Math.abs(event.clientX - pointerStart.value.x);
    const movedY = Math.abs(event.clientY - pointerStart.value.y);
    pointerStart.value = null;
    if (movedX > 8 || movedY > 8) return;
  }

  const rect = root.value.getBoundingClientRect();
  if (rect.width <= 0) return;

  const clickX = Math.max(0, Math.min(rect.width, event.clientX - rect.left));
  const ratio = clickX / rect.width;
  const targetIndex = Math.min(
    displayedItems.value.length - 1,
    Math.floor(ratio * displayedItems.value.length)
  );
  const item = displayedItems.value[targetIndex];
  if (!item) return;

  emit('select', item, targetIndex);
}
</script>

<template>
  <div
    ref="root"
    class="gallery-wrap"
    role="button"
    tabindex="0"
    @pointerdown="rememberPointerStart"
    @click="handleClick"
  >
    <CircularGallery
      :items="galleryItems"
      :bend="2"
      text-color="#F4F5F7"
      :border-radius="0.04"
      font="bold 22px Inter"
      :scroll-speed="2.5"
      :scroll-ease="0.05"
    />
  </div>
</template>

<style lang="scss" scoped>
.gallery-wrap {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: var(--radius-card);
  cursor: pointer;
  background:
    radial-gradient(at 0% 0%, rgba(244, 245, 247, 0.08) 0%, transparent 48%),
    radial-gradient(at 100% 100%, rgba(200, 168, 98, 0.12) 0%, transparent 52%),
    rgba(21, 21, 26, 0.72);
}
</style>
