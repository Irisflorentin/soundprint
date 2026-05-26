<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
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
const suppressNextClick = ref(false);
const centerIndex = ref(0);

const displayedItems = computed(() => props.items.slice(0, 15));
const orderedItems = computed(() => rotateItems(displayedItems.value, centerIndex.value));

const galleryItems = computed(() => orderedItems.value.map((item) => {
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

watch(
  () => displayedItems.value.length,
  () => {
    centerIndex.value = 0;
  }
);

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
  event.stopPropagation();
  pointerStart.value = { x: event.clientX, y: event.clientY };
}

function handlePointerUp(event: PointerEvent) {
  if (!pointerStart.value) return;
  const movedX = event.clientX - pointerStart.value.x;
  if (Math.abs(movedX) < 40) return;

  event.preventDefault();
  event.stopPropagation();

  const rect = root.value?.getBoundingClientRect();
  const slotWidth = rect && rect.width > 0
    ? rect.width / Math.min(5, displayedItems.value.length || 1)
    : 120;
  const steps = Math.max(1, Math.round(Math.abs(movedX) / slotWidth));
  centerIndex.value = normalizeIndex(
    centerIndex.value + (movedX < 0 ? steps : -steps),
    displayedItems.value.length
  );
  pointerStart.value = null;
  suppressNextClick.value = true;
}

function handleWheel(event: WheelEvent) {
  if (Math.abs(event.deltaY) < 1 && Math.abs(event.deltaX) < 1) return;
  event.preventDefault();
  event.stopPropagation();

  const direction = Math.abs(event.deltaY) >= Math.abs(event.deltaX)
    ? event.deltaY
    : event.deltaX;
  centerIndex.value = normalizeIndex(
    centerIndex.value + (direction > 0 ? 1 : -1),
    displayedItems.value.length
  );
}

function handleClick(event: MouseEvent) {
  event.stopPropagation();
  if (!root.value || orderedItems.value.length === 0) return;
  if (suppressNextClick.value) {
    suppressNextClick.value = false;
    return;
  }

  if (pointerStart.value) {
    const movedX = Math.abs(event.clientX - pointerStart.value.x);
    const movedY = Math.abs(event.clientY - pointerStart.value.y);
    pointerStart.value = null;
    if (movedX > 8 || movedY > 8) return;
  }

  const rect = root.value.getBoundingClientRect();
  if (rect.width <= 0) return;

  const clickX = Math.max(0, Math.min(rect.width, event.clientX - rect.left));
  const itemWidth = rect.width / Math.min(5, orderedItems.value.length);
  const indexOffset = Math.round((clickX - rect.width / 2) / itemWidth);
  const orderedIndex = normalizeIndex(indexOffset, orderedItems.value.length);
  const item = orderedItems.value[orderedIndex];
  if (!item) return;

  const sourceIndex = displayedItems.value.findIndex(source => source.id === item.id);
  emit('select', item, sourceIndex >= 0 ? sourceIndex : orderedIndex);
}

function blockNativeGalleryScroll(event: Event) {
  event.preventDefault();
  event.stopPropagation();
}

function rotateItems<T>(items: T[], startIndex: number): T[] {
  if (items.length === 0) return [];
  const start = normalizeIndex(startIndex, items.length);
  return items.slice(start).concat(items.slice(0, start));
}

function normalizeIndex(index: number, length: number): number {
  if (length <= 0) return 0;
  return ((index % length) + length) % length;
}
</script>

<template>
  <div
    ref="root"
    class="gallery-wrap"
    role="button"
    tabindex="0"
    @pointerdown.capture="rememberPointerStart"
    @pointerup.capture="handlePointerUp"
    @mousedown.capture="blockNativeGalleryScroll"
    @touchstart.capture="blockNativeGalleryScroll"
    @touchmove.capture="blockNativeGalleryScroll"
    @touchend.capture="blockNativeGalleryScroll"
    @wheel.capture="handleWheel"
    @click.capture="handleClick"
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
