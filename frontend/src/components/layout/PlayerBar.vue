<script setup lang="ts">
import { usePlayerStore } from '@/stores/player';
import { storeToRefs } from 'pinia';
import { VideoPlay, VideoPause } from '@element-plus/icons-vue';

const playerStore = usePlayerStore();
const { currentTrack, isPlaying } = storeToRefs(playerStore);
</script>

<template>
  <footer class="player-bar">
    <div v-if="!currentTrack" class="placeholder">
      <span>从音乐库选一首歌开始播放</span>
    </div>

    <div v-else class="now-playing">
      <div class="cover-info">
        <div class="cover" />
        <div class="meta">
          <div class="title">{{ currentTrack.title }}</div>
          <div class="artist">{{ currentTrack.artistName }}</div>
        </div>
      </div>

      <div class="controls">
        <el-button :icon="isPlaying ? VideoPause : VideoPlay" circle size="large"
          @click="isPlaying ? playerStore.pause() : playerStore.resume()" />
        <div class="progress-placeholder">
          <span class="badge">Phase 5 将实装波形播放器</span>
        </div>
      </div>

      <div class="right" />
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.player-bar {
  display: flex; align-items: center;
  padding: 0 var(--space-5);
  height: 88px;
  background: rgba(15,15,30,0.9);
  backdrop-filter: blur(20px);
}
.placeholder {
  width: 100%; text-align: center;
  color: var(--color-fg-tertiary);
  font-size: 13px;
}
.now-playing { width: 100%; display: grid; grid-template-columns: 1fr 2fr 1fr; align-items: center; gap: var(--space-5); }

.cover-info { display: flex; align-items: center; gap: var(--space-3); }
.cover { width: 56px; height: 56px; border-radius: 8px;
  background: linear-gradient(135deg, #7C3AED, #06B6D4); flex-shrink: 0; }
.meta .title { font-weight: 600; color: var(--color-fg-primary); }
.meta .artist { font-size: 12px; color: var(--color-fg-secondary); }

.controls { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.progress-placeholder { width: 100%; text-align: center; }
.badge {
  display: inline-block; padding: 2px 8px;
  font-size: 11px; color: var(--color-brand);
  background: rgba(124,58,237,0.12); border-radius: var(--radius-pill);
}
</style>
