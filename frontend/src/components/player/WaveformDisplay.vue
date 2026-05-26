<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { storeToRefs } from 'pinia';
import WaveSurfer from 'wavesurfer.js';
import { usePlayerStore } from '@/stores/player';
import { trackApi } from '@/api/track';

const container = ref<HTMLDivElement | null>(null);
const player = usePlayerStore();
const { currentTrack, currentTime } = storeToRefs(player);

let wavesurfer: WaveSurfer | null = null;
let loadVersion = 0;

onMounted(() => {
  if (!container.value) return;
  wavesurfer = WaveSurfer.create({
    container: container.value,
    waveColor: 'rgba(124, 58, 237, 0.4)',
    progressColor: '#7C3AED',
    cursorColor: '#06B6D4',
    barWidth: 2,
    barGap: 2,
    barRadius: 2,
    height: 100,
    normalize: true,
    interact: true,
  });

  wavesurfer.on('click', (relativeX: number) => {
    player.seek(relativeX * player.duration);
  });

  loadCurrentTrack();
});

onUnmounted(() => {
  wavesurfer?.destroy();
  wavesurfer = null;
});

watch(currentTrack, () => loadCurrentTrack());

watch(currentTime, (time) => {
  if (wavesurfer && player.duration > 0) {
    wavesurfer.setTime(time);
  }
});

async function loadCurrentTrack() {
  if (!wavesurfer || !currentTrack.value) return;
  const track = currentTrack.value;
  const version = ++loadVersion;
  try {
    const peaksData = await trackApi.peaks(track.id, 1000);
    if (version !== loadVersion) return;
    await wavesurfer.load(
      `/api/stream/${track.id}`,
      [peaksData.peaks],
      peaksData.duration
    );
  } catch (error) {
    console.warn('波形加载失败', error);
    if (version !== loadVersion) return;
    try {
      await wavesurfer.load(`/api/stream/${track.id}`);
    } catch (fallbackError) {
      console.warn('波形回退加载失败', fallbackError);
    }
  }
}
</script>

<template>
  <div class="waveform-wrapper">
    <div ref="container" class="waveform-canvas" />
  </div>
</template>

<style lang="scss" scoped>
.waveform-wrapper {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
}

.waveform-canvas {
  width: 100%;
  cursor: pointer;
}
</style>
