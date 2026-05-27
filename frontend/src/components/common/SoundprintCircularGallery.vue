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
const mouseDownPos = ref<{ x: number; y: number } | null>(null);
const DRAG_THRESHOLD = 5;
const GALLERY_SCROLL_SPEED = 2.5;
const scrollTarget = ref(0);
const scrollIndex = ref(0);
const dragStartTarget = ref(0);
let wheelSnapTimer: number | undefined;

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
  if (wheelSnapTimer !== undefined) {
    window.clearTimeout(wheelSnapTimer);
  }
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

function handleMouseDown(event: MouseEvent) {
  mouseDownPos.value = { x: event.clientX, y: event.clientY };
  dragStartTarget.value = scrollTarget.value;
}

function handleMouseUp(event: MouseEvent) {
  if (!mouseDownPos.value) return;

  const dx = Math.abs(event.clientX - mouseDownPos.value.x);
  const dy = Math.abs(event.clientY - mouseDownPos.value.y);

  if (dx < DRAG_THRESHOLD && dy < DRAG_THRESHOLD) {
    handleClick(event);
  } else {
    const distance = (mouseDownPos.value.x - event.clientX) * (GALLERY_SCROLL_SPEED * 0.025);
    scrollTarget.value = dragStartTarget.value + distance;
    snapScrollTarget();
  }

  mouseDownPos.value = null;
}

function handleWheel(event: WheelEvent) {
  if (Math.abs(event.deltaY) < 1 && Math.abs(event.deltaX) < 1) return;
  const direction = Math.abs(event.deltaY) >= Math.abs(event.deltaX)
    ? event.deltaY
    : event.deltaX;
  scrollTarget.value += direction > 0 ? GALLERY_SCROLL_SPEED : -GALLERY_SCROLL_SPEED;
  scheduleScrollSnap();
}

function handleClick(event: MouseEvent) {
  if (!root.value || displayedItems.value.length === 0) return;

  const rect = root.value.getBoundingClientRect();
  if (rect.width <= 0) return;

  const clickX = Math.max(0, Math.min(rect.width, event.clientX - rect.left));
  const slotCount = Math.min(5, displayedItems.value.length);
  const slotWidth = rect.width / slotCount;
  const maxOffset = Math.floor(slotCount / 2);
  const rawOffset = Math.round((clickX - rect.width / 2) / slotWidth);
  const indexOffset = Math.max(-maxOffset, Math.min(maxOffset, rawOffset));
  const targetIndex = normalizeIndex(scrollIndex.value + indexOffset, displayedItems.value.length);
  const item = displayedItems.value[targetIndex];
  if (!item) return;

  emit('select', item, targetIndex);
}

function scheduleScrollSnap() {
  if (wheelSnapTimer !== undefined) {
    window.clearTimeout(wheelSnapTimer);
  }
  wheelSnapTimer = window.setTimeout(() => {
    snapScrollTarget();
    wheelSnapTimer = undefined;
  }, 240);
}

function snapScrollTarget() {
  const itemWidth = getGalleryItemWidth();
  const itemIndex = Math.round(Math.abs(scrollTarget.value) / itemWidth);
  scrollTarget.value = scrollTarget.value < 0 ? -itemWidth * itemIndex : itemWidth * itemIndex;
  scrollIndex.value = scrollTarget.value < 0 ? -itemIndex : itemIndex;
}

function getGalleryItemWidth() {
  const cameraFov = 45 * Math.PI / 180;
  const cameraZ = 20;
  const viewportHeight = 2 * Math.tan(cameraFov / 2) * cameraZ;
  const planeWidth = viewportHeight * 700 / 1500;
  return planeWidth + 2;
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
    @mousedown="handleMouseDown"
    @mouseup="handleMouseUp"
    @wheel="handleWheel"
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
