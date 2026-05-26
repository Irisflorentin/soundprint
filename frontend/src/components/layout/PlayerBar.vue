<script setup lang="ts">
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import {
  ArrowLeftBold,
  ArrowRightBold,
  Microphone,
  Mute,
  RefreshLeft,
  VideoPause,
  VideoPlay,
} from '@element-plus/icons-vue';
import { usePlayerStore, type RepeatMode } from '@/stores/player';
import { useFormat } from '@/composables/useFormat';
import SmartCover from '@/components/common/SmartCover.vue';

const player = usePlayerStore();
const router = useRouter();
const {
  currentTrack,
  isPlaying,
  isLoading,
  currentTime,
  duration,
  volume,
  muted,
  repeatMode,
  hasNext,
  hasPrev,
} = storeToRefs(player);
const { formatDuration } = useFormat();

const progressPercent = computed(() => {
  if (!duration.value) return 0;
  return (currentTime.value / duration.value) * 100;
});

function onProgressClick(event: MouseEvent) {
  if (!duration.value) return;
  const target = event.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const ratio = Math.max(0, Math.min(1, (event.clientX - rect.left) / rect.width));
  player.seek(ratio * duration.value);
}

function cycleRepeat() {
  const modes: RepeatMode[] = ['off', 'all', 'one'];
  const index = modes.indexOf(repeatMode.value);
  player.setRepeatMode(modes[(index + 1) % modes.length]);
}

function goNowPlaying() {
  if (currentTrack.value) router.push('/now-playing');
}
</script>

<template>
  <footer class="player-bar">
    <div v-if="!currentTrack" class="placeholder">
      从音乐库选一首歌开始播放
    </div>

    <div v-else class="now-playing">
      <button class="left" type="button" @click="goNowPlaying">
        <SmartCover
          :src="currentTrack.coverUrl || currentTrack.albumCoverUrl"
          :alt="currentTrack.title"
          :fallback-text="currentTrack.title"
          class="cover"
        />
        <span class="meta">
          <strong>{{ currentTrack.title }}</strong>
          <small>{{ currentTrack.artistName || '未知艺术家' }}</small>
        </span>
      </button>

      <div class="center">
        <div class="controls">
          <el-button
            :icon="ArrowLeftBold"
            circle
            text
            :disabled="!hasPrev"
            @click="player.prev()"
          />
          <el-button
            :icon="isPlaying ? VideoPause : VideoPlay"
            circle
            type="primary"
            size="large"
            :loading="isLoading"
            @click="player.toggle()"
          />
          <el-button
            :icon="ArrowRightBold"
            circle
            text
            :disabled="!hasNext"
            @click="player.next()"
          />
        </div>

        <div class="progress-row">
          <span class="time">{{ formatDuration(currentTime) }}</span>
          <button class="progress-track" type="button" @click="onProgressClick">
            <span class="progress-fill" :style="{ width: `${progressPercent}%` }" />
          </button>
          <span class="time">{{ formatDuration(duration) }}</span>
        </div>
      </div>

      <div class="right">
        <el-button
          :icon="RefreshLeft"
          circle
          text
          :class="{ active: repeatMode !== 'off' }"
          @click="cycleRepeat"
        >
          <span v-if="repeatMode === 'one'" class="badge-1">1</span>
        </el-button>

        <el-button
          :icon="muted ? Mute : Microphone"
          circle
          text
          @click="player.toggleMute()"
        />
        <el-slider
          v-model="volume"
          :min="0"
          :max="1"
          :step="0.01"
          class="volume-slider"
          @input="(value: number | number[]) => player.setVolume(Array.isArray(value) ? value[0] : value)"
        />
      </div>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.player-bar {
  display: flex;
  align-items: center;
  height: 88px;
  padding: 0 var(--space-5);
  border-top: 1px solid rgba(200, 168, 98, 0.28);
  background:
    linear-gradient(90deg, rgba(10, 10, 11, 0.96), rgba(21, 21, 26, 0.94)),
    rgba(10, 10, 11, 0.94);
  backdrop-filter: blur(20px);
  box-shadow: 0 -18px 42px rgba(0, 0, 0, 0.28);
}

.placeholder {
  width: 100%;
  color: var(--color-fg-tertiary);
  font-size: 13px;
  text-align: center;
}

.now-playing {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 2fr) minmax(160px, 1fr);
  align-items: center;
  gap: var(--space-5);
}

.left {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0;
  color: inherit;
  background: transparent;
  border: 0;
  text-align: left;
  cursor: pointer;
  transition: opacity var(--duration-base) var(--ease);

  &:hover {
    opacity: 0.85;
  }
}

.cover {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: 8px;
}

.meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong,
  small {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  strong {
    color: var(--color-fg-primary);
    font-weight: 600;
  }

  small {
    color: var(--color-fg-secondary);
    font-size: 12px;
  }
}

.center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.controls {
  display: flex;
  align-items: center;
  gap: var(--space-2);

  :deep(.el-button--primary) {
    --el-button-text-color: #0A0A0B;
    --el-button-bg-color: #F4F5F7;
    --el-button-border-color: #F4F5F7;
    --el-button-hover-text-color: #0A0A0B;
    --el-button-hover-bg-color: #D4C5A0;
    --el-button-hover-border-color: #D4C5A0;
    --el-button-active-bg-color: #C8A862;
    --el-button-active-border-color: #C8A862;
    box-shadow: 0 0 24px rgba(200, 168, 98, 0.18);
  }
}

.progress-row {
  width: 100%;
  max-width: 500px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.time {
  min-width: 42px;
  color: var(--color-fg-tertiary);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}

.progress-track {
  position: relative;
  flex: 1;
  height: 8px;
  padding: 0;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
  border: 0;
  border-radius: 4px;
  cursor: pointer;
}

.progress-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #F4F5F7 0%, #C8A862 100%);
  border-radius: inherit;
  transition: background var(--duration-base) var(--ease);
}

.progress-track:hover .progress-fill {
  background: linear-gradient(90deg, #FFFFFF 0%, #D4C5A0 100%);
}

.right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
}

.volume-slider {
  width: 84px;
  --el-slider-main-bg-color: #C8A862;
  --el-slider-runway-bg-color: rgba(255, 255, 255, 0.14);
  --el-slider-button-bg-color: #F4F5F7;
  --el-slider-button-border-color: #D4C5A0;
}

.badge-1 {
  position: absolute;
  top: 2px;
  right: 2px;
  color: #D4C5A0;
  font-size: 9px;
}

.active :deep(.el-icon) {
  color: #D4C5A0;
}

@media (max-width: 900px) {
  .now-playing {
    grid-template-columns: minmax(0, 1fr) minmax(220px, 1.4fr);
  }

  .right {
    display: none;
  }
}
</style>
