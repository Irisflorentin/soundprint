<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { Close } from '@element-plus/icons-vue';
import { usePlayerStore } from '@/stores/player';
import { trackApi } from '@/api/track';
import SmartCover from '@/components/common/SmartCover.vue';
import SoundprintAntigravity from '@/components/common/SoundprintAntigravity.vue';
import WaveformDisplay from '@/components/player/WaveformDisplay.vue';
import LyricsPanel from '@/components/player/LyricsPanel.vue';

const router = useRouter();
const player = usePlayerStore();
const { currentTrack, currentTime } = storeToRefs(player);
const lyrics = ref('');

watch(currentTrack, async (track) => {
  if (!track) {
    lyrics.value = '';
    return;
  }
  try {
    lyrics.value = await trackApi.getLyrics(track.id) || '';
  } catch {
    lyrics.value = '';
  }
}, { immediate: true });

function close() {
  router.back();
}
</script>

<template>
  <div v-if="currentTrack" class="now-playing-view">
    <el-button :icon="Close" circle class="close-btn" @click="close" />

    <div class="content">
      <section class="cover-section">
        <SmartCover
          :src="currentTrack.coverUrl || currentTrack.albumCoverUrl"
          :alt="currentTrack.title"
          :fallback-text="currentTrack.title"
          class="big-cover"
        />
        <div class="track-meta">
          <h1>{{ currentTrack.title }}</h1>
          <p>{{ currentTrack.artistName || '未知艺术家' }}</p>
          <span>{{ currentTrack.albumTitle || '未知专辑' }}</span>
        </div>
      </section>

      <section class="info-section">
        <SoundprintAntigravity class="antigravity-bg" />
        <div class="info-overlay">
          <WaveformDisplay class="waveform" />
          <LyricsPanel :lyrics="lyrics" :current-time="currentTime" class="lyrics" />
        </div>
      </section>
    </div>
  </div>

  <div v-else class="empty">
    <p>当前没有播放中的曲目</p>
    <el-button @click="router.push('/library')">去音乐库</el-button>
  </div>
</template>

<style lang="scss" scoped>
.now-playing-view {
  position: fixed;
  inset: 0;
  z-index: 100;
  padding: var(--space-6);
  background: radial-gradient(circle at 30% 30%, #1A1530 0%, #0A0A14 60%);
}

.close-btn {
  position: absolute;
  top: var(--space-5);
  right: var(--space-5);
  z-index: 200;
}

.content {
  max-width: 1400px;
  height: calc(100vh - var(--space-10));
  margin: var(--space-5) auto 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.2fr);
  gap: var(--space-8);
  align-items: center;
}

.cover-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-5);
}

.big-cover {
  width: min(100%, 440px, calc(100vh - 250px));
  min-width: 260px;
  aspect-ratio: 1;
  border-radius: 24px;
  box-shadow:
    0 24px 64px rgba(0, 0, 0, 0.5),
    0 0 56px color-mix(in srgb, var(--color-brand) 28%, transparent),
    0 0 0 1px rgba(255, 255, 255, 0.05);
}

.track-meta {
  max-width: min(100%, 560px);
  text-align: center;

  h1 {
    margin: 0 0 var(--space-2);
    color: var(--color-fg-primary);
    font-size: 36px;
    font-weight: 700;
  }

  p {
    margin: 0;
    color: var(--color-fg-secondary);
    font-size: 18px;
  }

  span {
    display: block;
    margin-top: var(--space-2);
    color: var(--color-fg-tertiary);
    font-size: 14px;
  }
}

.info-section {
  position: relative;
  height: 100%;
  min-height: 540px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  background: rgba(10, 10, 20, 0.42);
}

.antigravity-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  opacity: 0.72;
}

.info-overlay {
  position: relative;
  z-index: 10;
  height: 100%;
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  background: linear-gradient(180deg, rgba(10, 10, 20, 0.34), rgba(10, 10, 20, 0.68));
  backdrop-filter: blur(2px);
}

.waveform {
  height: 120px;
  flex-shrink: 0;
}

.lyrics {
  flex: 1;
}

.empty {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
}

@media (max-width: 900px) {
  .content {
    height: auto;
    grid-template-columns: 1fr;
    align-items: start;
  }

  .info-section {
    min-height: 420px;
  }
}
</style>
