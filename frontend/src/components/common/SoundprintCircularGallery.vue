<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import CircularGallery from '@/components/vue-bits/components/CircullarGallery.vue';
import { fileUrl } from '@/utils/url';
import { releaseWebglContexts } from '@/utils/webgl';

type GalleryType = 'album' | 'artist' | 'playlist';

const props = defineProps<{
  items: Array<{
    coverUrl?: string | null;
    avatarUrl?: string | null;
    title?: string | null;
    name?: string | null;
  }>;
  type: GalleryType;
}>();

const root = ref<HTMLElement | null>(null);

const galleryItems = computed(() => props.items.slice(0, 15).map((item) => {
  const rawImage = item.coverUrl || item.avatarUrl || '';
  const image = rawImage
    ? toAbsoluteAssetUrl(rawImage)
    : fallbackImage(props.type);
  const text = item.title || item.name || fallbackText(props.type);

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

function fallbackImage(type: GalleryType): string {
  const symbol = type === 'album' ? 'AL' : type === 'artist' ? 'AR' : 'PL';
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400">
    <defs>
      <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#F4F5F7"/>
        <stop offset="52%" stop-color="#94A3B8"/>
        <stop offset="100%" stop-color="#C8A862"/>
      </linearGradient>
    </defs>
    <rect width="600" height="400" fill="#15151A"/>
    <rect width="600" height="400" fill="url(#g)" opacity="0.72"/>
    <circle cx="470" cy="82" r="130" fill="#FFFFFF" opacity="0.08"/>
    <text x="50%" y="52%" font-size="86" font-family="Inter, Arial, sans-serif" font-weight="700"
      fill="white" text-anchor="middle" dominant-baseline="middle" opacity="0.92">${symbol}</text>
  </svg>`;
  return `data:image/svg+xml;base64,${btoa(svg)}`;
}
</script>

<template>
  <div ref="root" class="gallery-wrap">
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
  background:
    radial-gradient(at 0% 0%, rgba(244, 245, 247, 0.08) 0%, transparent 48%),
    radial-gradient(at 100% 100%, rgba(200, 168, 98, 0.12) 0%, transparent 52%),
    rgba(21, 21, 26, 0.72);
}
</style>
