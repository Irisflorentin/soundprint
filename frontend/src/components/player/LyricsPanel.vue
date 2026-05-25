<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';

const props = defineProps<{
  lyrics: string;
  currentTime: number;
}>();

interface LrcLine {
  time: number;
  text: string;
}

const parsedLines = computed<LrcLine[]>(() => {
  if (!props.lyrics) return [];

  const lines: LrcLine[] = [];
  const regex = /\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\](.*)/;
  for (const raw of props.lyrics.split('\n')) {
    const match = raw.match(regex);
    if (match) {
      const [, minutes, seconds, milliseconds = '0', text] = match;
      const scale = milliseconds.length === 3 ? 1000 : 100;
      lines.push({
        time: Number(minutes) * 60 + Number(seconds) + Number(milliseconds) / scale,
        text: text.trim(),
      });
    } else if (raw.trim() && !raw.startsWith('[')) {
      lines.push({ time: -1, text: raw.trim() });
    }
  }
  return lines.sort((a, b) => a.time - b.time);
});

const hasTimestamps = computed(() => parsedLines.value.some((line) => line.time >= 0));

const activeIndex = computed(() => {
  if (!hasTimestamps.value) return -1;
  let index = -1;
  for (let i = 0; i < parsedLines.value.length; i++) {
    const line = parsedLines.value[i];
    if (line.time < 0) continue;
    if (line.time <= props.currentTime) index = i;
    else break;
  }
  return index;
});

const container = ref<HTMLDivElement | null>(null);

watch(activeIndex, async () => {
  await nextTick();
  const line = container.value?.querySelector('.lrc-line.active') as HTMLElement | null;
  line?.scrollIntoView({ behavior: 'smooth', block: 'center' });
});
</script>

<template>
  <div ref="container" class="lyrics-panel">
    <div v-if="parsedLines.length === 0" class="empty">暂无歌词</div>
    <div
      v-for="(line, index) in parsedLines"
      :key="`${line.time}-${index}`"
      class="lrc-line"
      :class="{ active: hasTimestamps && index === activeIndex }"
    >
      {{ line.text }}
    </div>
  </div>
</template>

<style lang="scss" scoped>
.lyrics-panel {
  height: 100%;
  overflow-y: auto;
  padding: var(--space-5);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.02);
  text-align: center;

  &::-webkit-scrollbar {
    display: none;
  }
}

.empty {
  padding: var(--space-8) 0;
  color: var(--color-fg-tertiary);
  font-size: 14px;
}

.lrc-line {
  color: var(--color-fg-tertiary);
  font-size: 16px;
  line-height: 2;
  transition: all var(--duration-base) var(--ease);
}

.lrc-line.active {
  color: var(--color-fg-primary);
  font-size: 20px;
  font-weight: 600;
  transform: scale(1.05);
}
</style>
