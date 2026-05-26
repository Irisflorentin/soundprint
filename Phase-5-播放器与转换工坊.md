# Phase 5：播放器实装 + 转换工坊 + 静态文件服务 + 歌词

> Soundprint 项目的第六个阶段文档。
> 把整份文档完整复制粘贴给 Codex 作为主要执行者。
> Phase 4 模式延续：Codex 主力实现 → Claude Code 审计补漏。
> 本阶段完成后，Soundprint 真正"动起来"——能听音乐、能看波形、能转格式、能跟读歌词。

---

## 🎯 阶段目标

把项目从"骨架 + 数据展示"升级为"完整可用的音乐应用"：

1. **静态文件服务**（修复 Phase 4 遗留的封面图不显示问题）
2. **全局播放器内部逻辑**：基于 `<audio>` + wavesurfer.js，状态全在 Pinia
3. **播放队列**：上一首/下一首/单曲循环/列表循环/随机
4. **音量控制 + 进度条拖动**
5. **当前播放视图**（独立大图页面，点击底部播放器封面进入）
6. **歌词同步显示**（LRC 解析 + 跟随播放进度高亮当前行）
7. **FFmpeg 真集成**（替换 Phase 3 的模拟进度）
8. **转换工坊前端**：选曲 → 选格式/比特率 → 提交 → 实时进度 → 下载
9. **专辑/艺术家/歌单详情页内容补全**（之前是空架子）

**关键约束**：
- 本阶段**不集成 vue-bits**（Phase 7 才做）
- 不写 ECharts 统计（Phase 6）
- 不写真实登录认证（仍用默认用户）
- 所有视觉延续 Phase 4 的设计令牌，**不允许引入新颜色或新字体**

---

## 📋 任务清单

### 任务 0：环境核验 + 准备

```powershell
# 后端
cd D:\Claude_Playground\Soundprint\backend
mvn -version    # 3.9.16

# 前端
cd D:\Claude_Playground\Soundprint\frontend
npm -v          # 9+

# FFmpeg（Phase 5 必需）
ffmpeg -version
```

**如果 ffmpeg 命令不存在**，停下来报告。需要用户：
1. 下载 FFmpeg：https://www.gyan.dev/ffmpeg/builds/ 选 `release-full.7z`
2. 解压到 `D:\dev\ffmpeg\`
3. 把 `D:\dev\ffmpeg\bin` 加入 PATH
4. **重启所有终端 + VS Code**
5. 验证 `ffmpeg -version` 输出版本号

---

### 任务 1：后端 - 静态文件服务（封面图修复，10 分钟）

#### 1.1 创建 `config/WebMvcConfig.java`

```java
package com.soundprint.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：静态文件服务
 *
 * 将 D:/soundprint-storage/ 目录映射为 /files/** 访问路径，
 * 用户能通过 http://localhost:8080/files/cover/xxx.jpg 访问到本地文件。
 *
 * 安全注意：
 * - 该映射仅服务存储根目录下的文件，Spring 自动防止 ../ 路径穿越
 * - 生产环境如需鉴权，可在此 Handler 加 SecurityFilter
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storage;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 注意结尾斜杠很重要，否则 Spring 拒绝映射
        String baseDir = storage.getBaseDir();
        if (!baseDir.endsWith("/") && !baseDir.endsWith("\\")) {
            baseDir = baseDir + "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + baseDir)
                .setCachePeriod(3600);  // 浏览器缓存 1 小时
    }
}
```

#### 1.2 验证

启动后端后，浏览器访问（如果你的 cover/ 下有文件）：
```
http://localhost:8080/files/cover/9567d91bf91849769a638d75cdbbaa94.jpg
```

应该能看到 Imagine Dragons Origins 专辑封面图。

#### 1.3 前端工具函数 `src/utils/url.ts`

```ts
/**
 * 把数据库存的相对路径（如 "cover/xxx.jpg"）拼成完整 URL。
 * 因为前端通过 Vite 代理 /api 到 8080，/files/** 也通过代理走。
 */
export function fileUrl(path: string | null | undefined): string {
  if (!path) return '';
  if (path.startsWith('http://') || path.startsWith('https://')) return path;
  // 通过 Vite 代理，避免 CORS
  const clean = path.startsWith('/') ? path.slice(1) : path;
  return `/files/${clean}`;
}
```

**vite.config.ts 代理也要加 `/files`**：

```ts
server: {
  port: 5173,
  proxy: {
    '/api':   { target: 'http://localhost:8080', changeOrigin: true },
    '/files': { target: 'http://localhost:8080', changeOrigin: true },
  },
},
```

#### 1.4 替换所有用到封面的地方

搜索代码里所有 `track.coverUrl`、`track.albumCoverUrl`、`album.coverUrl`、`artist.avatarUrl` 用到的地方，套上 `fileUrl()`：

```vue
<!-- 改前 -->
<img :src="track.coverUrl" />

<!-- 改后 -->
<img :src="fileUrl(track.coverUrl || track.albumCoverUrl)" />
```

并加 fallback：图片加载失败时回退到原来的渐变占位（不要让用户看到破图）。**做法**：

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { fileUrl } from '@/utils/url';

const props = defineProps<{ src: string | null }>();
const failed = ref(false);
</script>

<template>
  <div class="cover-wrapper">
    <img
      v-if="props.src && !failed"
      :src="fileUrl(props.src)"
      @error="failed = true"
      class="cover-img"
    />
    <div v-else class="cover-fallback" />
  </div>
</template>
```

把这个抽成 `components/common/SmartCover.vue`，**全站统一使用**。

---

### 任务 2:前端 - 安装播放器依赖

```powershell
cd D:\Claude_Playground\Soundprint\frontend
npm install wavesurfer.js@7
```

wavesurfer 7.x 是最新主版本，API 比 6.x 简化很多。

---

### 任务 3：前端 - 重写 Pinia Player Store（核心）

`src/stores/player.ts` 完整重写：

```ts
import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';
import type { Track } from '@/types/track';
import { playHistoryApi } from '@/api/playHistory';

export type RepeatMode = 'off' | 'one' | 'all';

export const usePlayerStore = defineStore('player', () => {
  // ========== 状态 ==========
  const audio = ref<HTMLAudioElement | null>(null);   // 全局唯一 audio 元素
  const currentTrack = ref<Track | null>(null);
  const queue = ref<Track[]>([]);
  const queueIndex = ref(-1);

  const isPlaying = ref(false);
  const isLoading = ref(false);
  const currentTime = ref(0);
  const duration = ref(0);
  const volume = ref(0.8);
  const muted = ref(false);

  const repeatMode = ref<RepeatMode>('off');
  const shuffled = ref(false);

  // 已上报的播放时长，避免一次播放多次记录
  let lastReportedTrackId: number | null = null;
  let accumulatedSeconds = 0;

  // ========== 计算属性 ==========
  const progress = computed(() => duration.value > 0 ? currentTime.value / duration.value : 0);
  const hasNext = computed(() => queueIndex.value < queue.value.length - 1 || repeatMode.value === 'all');
  const hasPrev = computed(() => queueIndex.value > 0 || repeatMode.value === 'all');

  // ========== 初始化 ==========
  function initAudio() {
    if (audio.value) return;
    const el = new Audio();
    el.preload = 'metadata';
    el.volume = volume.value;

    el.addEventListener('loadedmetadata', () => {
      duration.value = el.duration;
    });
    el.addEventListener('timeupdate', () => {
      currentTime.value = el.currentTime;
    });
    el.addEventListener('play',  () => { isPlaying.value = true;  isLoading.value = false; });
    el.addEventListener('pause', () => { isPlaying.value = false; });
    el.addEventListener('waiting', () => { isLoading.value = true; });
    el.addEventListener('canplay', () => { isLoading.value = false; });
    el.addEventListener('ended', () => {
      reportPlayHistory(true);
      handleEnded();
    });

    audio.value = el;
  }

  // ========== 操作 ==========
  function playTrack(track: Track, newQueue?: Track[], startIndex?: number) {
    initAudio();
    reportPlayHistory(false);   // 切歌前上报上一首

    currentTrack.value = track;
    if (newQueue) {
      queue.value = newQueue;
      queueIndex.value = startIndex ?? newQueue.findIndex(t => t.id === track.id);
    } else if (queue.value.length === 0) {
      queue.value = [track];
      queueIndex.value = 0;
    }

    accumulatedSeconds = 0;
    lastReportedTrackId = track.id;

    if (audio.value) {
      audio.value.src = `/api/stream/${track.id}`;
      audio.value.play().catch(err => {
        console.error('播放失败', err);
        isPlaying.value = false;
      });
    }
  }

  function pause() {
    audio.value?.pause();
  }

  function resume() {
    if (audio.value && currentTrack.value) {
      audio.value.play();
    }
  }

  function toggle() {
    isPlaying.value ? pause() : resume();
  }

  function seek(seconds: number) {
    if (audio.value) audio.value.currentTime = seconds;
  }

  function setVolume(v: number) {
    volume.value = Math.max(0, Math.min(1, v));
    if (audio.value) audio.value.volume = volume.value;
    if (v > 0) muted.value = false;
  }

  function toggleMute() {
    muted.value = !muted.value;
    if (audio.value) audio.value.muted = muted.value;
  }

  function next() {
    if (queue.value.length === 0) return;
    if (queueIndex.value < queue.value.length - 1) {
      const t = queue.value[++queueIndex.value];
      playTrack(t);
    } else if (repeatMode.value === 'all') {
      queueIndex.value = 0;
      playTrack(queue.value[0]);
    }
  }

  function prev() {
    if (queue.value.length === 0) return;
    // 如果播放 > 3 秒，prev 等于"重头开始"
    if (currentTime.value > 3) {
      seek(0);
      return;
    }
    if (queueIndex.value > 0) {
      const t = queue.value[--queueIndex.value];
      playTrack(t);
    } else if (repeatMode.value === 'all') {
      queueIndex.value = queue.value.length - 1;
      playTrack(queue.value[queueIndex.value]);
    }
  }

  function handleEnded() {
    if (repeatMode.value === 'one') {
      seek(0);
      audio.value?.play();
      return;
    }
    if (hasNext.value) {
      next();
    } else {
      isPlaying.value = false;
      currentTime.value = 0;
    }
  }

  function setRepeatMode(m: RepeatMode) {
    repeatMode.value = m;
  }

  function shuffleQueue() {
    shuffled.value = !shuffled.value;
    if (shuffled.value) {
      // Fisher-Yates 洗牌，但保留当前歌曲在第一位
      const current = currentTrack.value;
      const rest = queue.value.filter(t => t.id !== current?.id);
      for (let i = rest.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [rest[i], rest[j]] = [rest[j], rest[i]];
      }
      queue.value = current ? [current, ...rest] : rest;
      queueIndex.value = 0;
    }
  }

  // 上报播放历史（切歌、结束、关闭页面时触发）
  function reportPlayHistory(completed: boolean) {
    if (!lastReportedTrackId || accumulatedSeconds < 5) return;  // 少于 5 秒不算
    playHistoryApi.record({
      trackId: lastReportedTrackId,
      playedSeconds: Math.round(accumulatedSeconds),
    }).catch(() => { /* 静默失败，不打扰用户 */ });
    accumulatedSeconds = 0;
  }

  // 监听 currentTime 累加播放时长
  watch(currentTime, (now, then) => {
    if (isPlaying.value && now > then && now - then < 2) {
      accumulatedSeconds += now - then;
    }
  });

  // 监听 volume 持久化到 localStorage
  watch(volume, (v) => localStorage.setItem('player.volume', String(v)));
  const saved = localStorage.getItem('player.volume');
  if (saved) volume.value = Number(saved);

  return {
    // state
    currentTrack, queue, queueIndex, isPlaying, isLoading,
    currentTime, duration, volume, muted, repeatMode, shuffled,
    // computed
    progress, hasNext, hasPrev,
    // actions
    playTrack, pause, resume, toggle, seek, setVolume, toggleMute,
    next, prev, setRepeatMode, shuffleQueue,
  };
});
```

**核心设计要点**（Codex 实现时必须遵守）：

1. **`audio.value` 是全局单例**——整个应用只有一个 `<audio>` 元素，由 store 持有，**绝不允许任何组件自己 new Audio()**
2. **流式 URL `/api/stream/{id}`**——浏览器原生支持 Range，Phase 3 的后端接口配合
3. **播放历史上报防抖**——少于 5 秒不上报，避免无效数据污染统计
4. **音量持久化**——刷新页面音量不重置

---

### 任务 4：前端 - 重写 `PlayerBar.vue`（核心 UI）

这是常驻底部的播放器，**全站可见**，体验关键。

```vue
<script setup lang="ts">
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import {
  VideoPlay, VideoPause, ArrowLeftBold, ArrowRightBold,
  RefreshLeft, Microphone, Mute,
} from '@element-plus/icons-vue';
import { usePlayerStore } from '@/stores/player';
import { useFormat } from '@/composables/useFormat';
import SmartCover from '@/components/common/SmartCover.vue';

const player = usePlayerStore();
const router = useRouter();
const {
  currentTrack, isPlaying, isLoading,
  currentTime, duration, volume, muted,
  repeatMode, hasNext, hasPrev,
} = storeToRefs(player);

const { formatTime } = useFormat();

const progressPercent = computed(() => {
  if (!duration.value) return 0;
  return (currentTime.value / duration.value) * 100;
});

function onProgressClick(e: MouseEvent) {
  const target = e.currentTarget as HTMLElement;
  const rect = target.getBoundingClientRect();
  const ratio = (e.clientX - rect.left) / rect.width;
  player.seek(ratio * duration.value);
}

function cycleRepeat() {
  const modes = ['off', 'all', 'one'] as const;
  const idx = modes.indexOf(repeatMode.value);
  player.setRepeatMode(modes[(idx + 1) % 3]);
}

function goNowPlaying() {
  if (currentTrack.value) router.push('/now-playing');
}
</script>

<template>
  <footer class="player-bar">
    <!-- 空状态 -->
    <div v-if="!currentTrack" class="placeholder">
      从音乐库选一首歌开始播放
    </div>

    <!-- 播放中 -->
    <div v-else class="now-playing">
      <!-- 左：封面 + 元信息 -->
      <div class="left" @click="goNowPlaying">
        <SmartCover
          :src="currentTrack.coverUrl || currentTrack.albumCoverUrl"
          class="cover"
        />
        <div class="meta">
          <div class="title">{{ currentTrack.title }}</div>
          <div class="artist">{{ currentTrack.artistName || '未知艺术家' }}</div>
        </div>
      </div>

      <!-- 中：控件 + 进度条 -->
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
          <span class="time">{{ formatTime(currentTime) }}</span>
          <div class="progress-track" @click="onProgressClick">
            <div class="progress-fill" :style="{ width: `${progressPercent}%` }" />
          </div>
          <span class="time">{{ formatTime(duration) }}</span>
        </div>
      </div>

      <!-- 右：循环 + 音量 -->
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
          :min="0" :max="1" :step="0.01"
          @input="(v: number) => player.setVolume(v)"
          class="volume-slider"
        />
      </div>
    </div>
  </footer>
</template>

<style lang="scss" scoped>
.player-bar {
  display: flex; align-items: center;
  padding: 0 var(--space-5);
  height: 88px;
  background: rgba(15, 15, 30, 0.92);
  backdrop-filter: blur(20px);
}

.placeholder {
  width: 100%; text-align: center;
  color: var(--color-fg-tertiary);
  font-size: 13px;
}

.now-playing {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  align-items: center;
  gap: var(--space-5);
}

/* 左：封面 + 元信息 */
.left {
  display: flex; align-items: center; gap: var(--space-3);
  cursor: pointer;
  transition: opacity 200ms;
  &:hover { opacity: 0.85; }
}
.cover {
  width: 56px; height: 56px;
  border-radius: 8px;
  flex-shrink: 0;
}
.meta {
  min-width: 0;
  .title {
    font-weight: 600; color: var(--color-fg-primary);
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }
  .artist {
    font-size: 12px; color: var(--color-fg-secondary);
  }
}

/* 中：控件 + 进度条 */
.center { display: flex; flex-direction: column; gap: 8px; align-items: center; }
.controls { display: flex; align-items: center; gap: var(--space-2); }

.progress-row {
  display: flex; align-items: center; gap: var(--space-3);
  width: 100%; max-width: 500px;
}
.time {
  font-size: 11px; font-variant-numeric: tabular-nums;
  color: var(--color-fg-tertiary);
  min-width: 36px;
}
.progress-track {
  flex: 1; height: 4px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 2px;
  cursor: pointer;
  position: relative;
  &:hover .progress-fill {
    background: var(--color-brand-hover);
  }
}
.progress-fill {
  height: 100%;
  background: var(--color-brand);
  border-radius: 2px;
  transition: background 200ms;
}

/* 右：循环 + 音量 */
.right {
  display: flex; align-items: center; justify-content: flex-end;
  gap: var(--space-2);
}
.volume-slider {
  width: 80px;
  --el-slider-main-bg-color: var(--color-brand);
}
.badge-1 {
  position: absolute;
  top: 2px; right: 2px;
  font-size: 9px;
  color: var(--color-brand);
}
.active :deep(.el-icon) { color: var(--color-brand); }
</style>
```

---

### 任务 5：前端 - "正在播放"页面 `NowPlayingView.vue`

点击底部播放器封面进入。**这是一个全屏沉浸视图**，包含大封面 + 元信息 + 波形 + 歌词。

#### 5.1 路由注册

`src/router/index.ts` 加：

```ts
{ path: '/now-playing', component: () => import('@/views/player/NowPlayingView.vue') }
```

#### 5.2 视图实现

```vue
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { Close } from '@element-plus/icons-vue';
import { usePlayerStore } from '@/stores/player';
import { trackApi } from '@/api/track';
import { useFormat } from '@/composables/useFormat';
import SmartCover from '@/components/common/SmartCover.vue';
import WaveformDisplay from '@/components/player/WaveformDisplay.vue';
import LyricsPanel from '@/components/player/LyricsPanel.vue';

const router = useRouter();
const player = usePlayerStore();
const { currentTrack, currentTime, duration } = storeToRefs(player);
const { formatTime } = useFormat();

const lyrics = ref<string>('');

watch(currentTrack, async (track) => {
  if (!track) {
    lyrics.value = '';
    return;
  }
  try {
    lyrics.value = await trackApi.getLyrics(track.id);
  } catch {
    lyrics.value = '';
  }
}, { immediate: true });

function close() { router.back(); }
</script>

<template>
  <div v-if="currentTrack" class="now-playing-view">
    <el-button :icon="Close" circle class="close-btn" @click="close" />

    <div class="content">
      <!-- 左：封面 -->
      <section class="cover-section">
        <SmartCover
          :src="currentTrack.coverUrl || currentTrack.albumCoverUrl"
          class="big-cover"
        />
        <div class="track-meta">
          <h1 class="title">{{ currentTrack.title }}</h1>
          <p class="artist">{{ currentTrack.artistName || '未知艺术家' }}</p>
          <p class="album">{{ currentTrack.albumTitle || '未知专辑' }}</p>
        </div>
      </section>

      <!-- 右：波形 + 歌词 -->
      <section class="info-section">
        <WaveformDisplay class="waveform" />
        <LyricsPanel :lyrics="lyrics" :current-time="currentTime" class="lyrics" />
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
  background: radial-gradient(circle at 30% 30%, #1A1530 0%, #0A0A14 60%);
  z-index: 100;
  padding: var(--space-6);
}
.close-btn {
  position: absolute;
  top: var(--space-5); right: var(--space-5);
  z-index: 10;
}
.content {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.2fr);
  gap: var(--space-8);
  max-width: 1400px;
  margin: var(--space-8) auto;
  align-items: center;
}
.cover-section {
  display: flex; flex-direction: column; align-items: center;
  gap: var(--space-5);
}
.big-cover {
  width: 100%; max-width: 480px;
  aspect-ratio: 1;
  border-radius: 24px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255,255,255,0.05);
}
.track-meta { text-align: center; }
.title {
  font-size: 36px; font-weight: 700;
  color: var(--color-fg-primary);
  margin: 0 0 var(--space-2);
}
.artist {
  font-size: 18px; color: var(--color-fg-secondary);
  margin: 0;
}
.album {
  font-size: 14px; color: var(--color-fg-tertiary);
  margin: var(--space-2) 0 0;
}

.info-section {
  display: flex; flex-direction: column;
  gap: var(--space-5);
  height: 100%;
  min-height: 540px;
}
.waveform { height: 120px; flex-shrink: 0; }
.lyrics   { flex: 1; }

.empty {
  height: 100vh;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: var(--space-4);
}
</style>
```

#### 5.3 `components/player/WaveformDisplay.vue`（wavesurfer.js）

```vue
<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import WaveSurfer from 'wavesurfer.js';
import { storeToRefs } from 'pinia';
import { usePlayerStore } from '@/stores/player';

const container = ref<HTMLDivElement>();
const player = usePlayerStore();
const { currentTrack, currentTime, isPlaying } = storeToRefs(player);

let ws: WaveSurfer | null = null;

onMounted(() => {
  if (!container.value) return;
  ws = WaveSurfer.create({
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
    // 不让 wavesurfer 自己持有 audio——只展示波形，播放由 store 的 audio 控制
    media: undefined,
  });

  // 点击波形跳转
  ws.on('click', (relativeX) => {
    player.seek(relativeX * player.duration);
  });

  loadCurrentTrack();
});

onUnmounted(() => {
  ws?.destroy();
});

watch(currentTrack, () => loadCurrentTrack());

async function loadCurrentTrack() {
  if (!ws || !currentTrack.value) return;
  // 加载音频文件，wavesurfer 会自己 fetch 解码
  try {
    await ws.load(`/api/stream/${currentTrack.value.id}`);
  } catch (e) {
    console.warn('波形加载失败', e);
  }
}

// 跟随 store 的播放进度更新光标位置
watch(currentTime, (t) => {
  if (ws && player.duration > 0) {
    ws.setTime(t);
  }
});
</script>

<template>
  <div class="waveform-wrapper">
    <div ref="container" class="waveform-canvas" />
  </div>
</template>

<style lang="scss" scoped>
.waveform-wrapper {
  width: 100%;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: 12px;
  padding: var(--space-3);
}
.waveform-canvas {
  width: 100%;
  cursor: pointer;
}
</style>
```

**关键架构决策**（Codex 必须理解）：

- **音频解码 wavesurfer 做一次**（只为渲染波形），**真正的播放由 store 里的 `<audio>` 元素负责**
- wavesurfer 只负责"画 + 显示进度光标 + 接受点击跳转"
- 这样**不会有两个音频同时播放**的灾难
- 缺点是文件被请求两次（一次 audio 流播放，一次 wavesurfer 全量下载）。FLAC 文件大时这会慢，但 Phase 5 接受这个开销
- **更高级方案**（Phase 5+ 可选优化）：后端加一个 `/api/tracks/{id}/peaks` 接口，返回预计算的波形峰值数据，wavesurfer 用 `peaks` 参数渲染，不下载真实文件

#### 5.4 `components/player/LyricsPanel.vue`（歌词同步）

```vue
<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue';

const props = defineProps<{
  lyrics: string;
  currentTime: number;
}>();

interface LrcLine {
  time: number;
  text: string;
}

// 解析 LRC 格式：[mm:ss.xx]歌词文本
const parsedLines = computed<LrcLine[]>(() => {
  if (!props.lyrics) return [];
  const lines: LrcLine[] = [];
  const regex = /\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\](.*)/;
  for (const raw of props.lyrics.split('\n')) {
    const m = raw.match(regex);
    if (m) {
      const [, mm, ss, ms = '0', text] = m;
      const time = Number(mm) * 60 + Number(ss) + Number(ms) / (ms.length === 3 ? 1000 : 100);
      lines.push({ time, text: text.trim() });
    } else if (raw.trim() && !raw.startsWith('[')) {
      // 纯文本歌词（无时间戳）
      lines.push({ time: -1, text: raw.trim() });
    }
  }
  return lines.sort((a, b) => a.time - b.time);
});

const hasTimestamps = computed(() => parsedLines.value.some(l => l.time >= 0));

const activeIndex = computed(() => {
  if (!hasTimestamps.value) return -1;
  let idx = -1;
  for (let i = 0; i < parsedLines.value.length; i++) {
    if (parsedLines.value[i].time <= props.currentTime) idx = i;
    else break;
  }
  return idx;
});

const container = ref<HTMLDivElement>();
watch(activeIndex, async () => {
  await nextTick();
  const el = container.value?.querySelector('.lrc-line.active') as HTMLElement | null;
  el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
});
</script>

<template>
  <div ref="container" class="lyrics-panel">
    <div v-if="parsedLines.length === 0" class="empty">暂无歌词</div>
    <div
      v-for="(line, i) in parsedLines"
      :key="i"
      class="lrc-line"
      :class="{ active: hasTimestamps && i === activeIndex }"
    >
      {{ line.text }}
    </div>
  </div>
</template>

<style lang="scss" scoped>
.lyrics-panel {
  height: 100%;
  overflow-y: auto;
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(255,255,255,0.04);
  border-radius: 12px;
  padding: var(--space-5);
  text-align: center;

  &::-webkit-scrollbar { display: none; }
}
.empty {
  color: var(--color-fg-tertiary);
  font-size: 14px;
  padding: var(--space-8) 0;
}
.lrc-line {
  font-size: 16px;
  line-height: 2;
  color: var(--color-fg-tertiary);
  transition: all 300ms var(--ease);
}
.lrc-line.active {
  color: var(--color-fg-primary);
  font-size: 20px;
  font-weight: 600;
  transform: scale(1.05);
}
</style>
```

---

### 任务 6：前端 - 在 `LibraryView`、`AlbumDetailView`、`PlaylistDetailView` 等位置接入播放

只要是显示曲目列表的地方，**每一行点击都要触发播放**。

修改方式：从这些 View 调用 `player.playTrack(track, listTracks, index)`，把当前列表作为播放队列。

例如 `LibraryView.vue` 表格的行点击：

```ts
function onRowClick(track: Track) {
  player.playTrack(track, currentPageTracks.value, currentPageTracks.value.findIndex(t => t.id === track.id));
}
```

---

### 任务 7：专辑/艺术家/歌单详情页内容补全

Phase 4 这些页面是占位状态。Phase 5 实装它们。

#### 7.1 `AlbumDetailView.vue`

调用 `/api/albums/{id}`，布局：

- 顶部 Hero 区：大封面 + 专辑名 + 艺术家 + 年份 + 流派
- 下方曲目表格：曲序 | 标题 | 时长 | 操作（播放/收藏）
- 点击行播放，"播放全部"按钮把整张专辑作为队列

#### 7.2 `ArtistDetailView.vue`

调用 `/api/artists/{id}`，布局：

- 顶部 Hero 区：头像 + 名字 + 国籍 + 简介
- 下方分两段：该艺术家的专辑（网格）+ 单曲列表

#### 7.3 `PlaylistDetailView.vue`

调用 `/api/playlists/{id}`，布局：

- 顶部 Hero：歌单名 + 描述 + 曲目数
- 下方曲目表格，**支持拖拽排序**（Element-Plus 的 `el-table` 不原生支持拖拽，用 `vuedraggable@next` 实现）
  - `npm install vuedraggable@next`
- 每行有"从歌单移除"按钮
- "添加曲目"按钮 → 弹出对话框，选择音乐库里的曲目添加

---

### 任务 8：后端 - FFmpeg 真集成

Phase 3 留下的转换任务是用 `CompletableFuture.runAsync` 模拟进度。Phase 5 替换成真 FFmpeg。

#### 8.1 创建 `util/FFmpegRunner.java`

```java
package com.soundprint.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FFmpeg 命令调用封装
 */
@Slf4j
@Component
public class FFmpegRunner {

    private static final Pattern DURATION_PATTERN =
            Pattern.compile("Duration: (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");
    private static final Pattern TIME_PATTERN =
            Pattern.compile("time=(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{2})");

    /**
     * 执行 FFmpeg 转换
     *
     * @param inputPath 源文件绝对路径
     * @param outputPath 输出文件绝对路径
     * @param targetFormat MP3/FLAC/WAV/AAC
     * @param targetBitrate 比特率（kbps），仅对有损格式有效；FLAC/WAV 传 null
     * @param targetSampleRate 采样率（Hz），可空
     * @param onProgress 进度回调（0-100）
     * @throws IOException FFmpeg 启动失败
     * @throws InterruptedException 转换被打断
     */
    public void convert(
            String inputPath,
            String outputPath,
            String targetFormat,
            Integer targetBitrate,
            Integer targetSampleRate,
            IntConsumer onProgress
    ) throws IOException, InterruptedException {

        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        cmd.add("-y");                       // 覆盖输出
        cmd.add("-i"); cmd.add(inputPath);
        cmd.add("-vn");                       // 忽略视频流（封面图）

        // 编码器选择
        switch (targetFormat.toUpperCase()) {
            case "MP3":
                cmd.add("-acodec"); cmd.add("libmp3lame");
                if (targetBitrate != null) {
                    cmd.add("-b:a"); cmd.add(targetBitrate + "k");
                }
                break;
            case "AAC":
                cmd.add("-acodec"); cmd.add("aac");
                if (targetBitrate != null) {
                    cmd.add("-b:a"); cmd.add(targetBitrate + "k");
                }
                break;
            case "FLAC":
                cmd.add("-acodec"); cmd.add("flac");
                cmd.add("-compression_level"); cmd.add("8");
                break;
            case "WAV":
                cmd.add("-acodec"); cmd.add("pcm_s16le");
                break;
            default:
                throw new IllegalArgumentException("不支持的格式: " + targetFormat);
        }

        if (targetSampleRate != null) {
            cmd.add("-ar"); cmd.add(String.valueOf(targetSampleRate));
        }

        cmd.add(outputPath);

        log.info("FFmpeg 命令: {}", String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd)
                .redirectErrorStream(true);   // FFmpeg 把进度写到 stderr，合并到 stdout
        Process proc = pb.start();

        // 解析输出获取进度
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            double totalSeconds = 0;
            while ((line = reader.readLine()) != null) {
                // 先找总时长（FFmpeg 启动时输出一次）
                if (totalSeconds == 0) {
                    Matcher m = DURATION_PATTERN.matcher(line);
                    if (m.find()) {
                        totalSeconds = parseTime(m);
                    }
                }
                // 找当前进度
                Matcher m = TIME_PATTERN.matcher(line);
                if (m.find() && totalSeconds > 0) {
                    double current = parseTime(m);
                    int percent = Math.min(99, (int) Math.round(current / totalSeconds * 100));
                    onProgress.accept(percent);
                }
            }
        }

        int code = proc.waitFor();
        if (code != 0) {
            throw new IOException("FFmpeg 退出码非 0: " + code);
        }
        onProgress.accept(100);
    }

    private double parseTime(Matcher m) {
        int h = Integer.parseInt(m.group(1));
        int min = Integer.parseInt(m.group(2));
        int sec = Integer.parseInt(m.group(3));
        int cs = Integer.parseInt(m.group(4));
        return h * 3600 + min * 60 + sec + cs / 100.0;
    }
}
```

#### 8.2 修改 `ConversionTaskServiceImpl`

把 Phase 3 那段 `Thread.sleep` 模拟进度的逻辑，换成调用 `FFmpegRunner`：

```java
@Async    // 让 Spring 在线程池里执行
public void executeConversion(Long taskId) {
    ConversionTask task = getById(taskId);
    if (task == null) return;

    try {
        // 标记为 RUNNING
        task.setStatus("RUNNING");
        task.setStartedAt(LocalDateTime.now());
        updateById(task);

        // 准备路径
        Track sourceTrack = trackService.getById(task.getSourceTrackId());
        String inputPath = storage.absolutePathOf(sourceTrack.getFilePath());
        String outputFileName = UUID.randomUUID() + "." + task.getTargetFormat().toLowerCase();
        String outputRelative = "conversion/" + outputFileName;
        String outputPath = storage.absolutePathOf(outputRelative);

        // 跑 FFmpeg
        ffmpegRunner.convert(
            inputPath,
            outputPath,
            task.getTargetFormat(),
            task.getTargetBitrate(),
            task.getTargetSampleRate(),
            (percent) -> {
                // 进度回调：每 5% 更新一次数据库，避免太频繁
                if (percent % 5 == 0 || percent >= 99) {
                    baseMapper.updateProgress(taskId, percent);
                }
            }
        );

        // 标记成功
        task.setStatus("SUCCESS");
        task.setProgress(100);
        task.setOutputPath(outputRelative);
        task.setFinishedAt(LocalDateTime.now());
        updateById(task);

    } catch (Exception e) {
        log.error("转换失败 taskId={}", taskId, e);
        task.setStatus("FAILED");
        task.setErrorMessage(e.getMessage());
        task.setFinishedAt(LocalDateTime.now());
        updateById(task);
    }
}
```

并在 `SoundprintApplication.java` 加 `@EnableAsync`，让 `@Async` 注解生效。

注意 `Mapper` 需要补一个 `updateProgress` 方法专门用来快速更新进度（不用整行 updateById）：

```xml
<update id="updateProgress">
    UPDATE conversion_task SET progress = #{progress} WHERE id = #{taskId}
</update>
```

---

### 任务 9：前端 - 转换工坊 `StudioView.vue`

```vue
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { trackApi } from '@/api/track';
import { conversionApi } from '@/api/conversion';
import type { Track } from '@/types/track';
import type { ConversionTask } from '@/types/conversion';
import { fileUrl } from '@/utils/url';
import SmartCover from '@/components/common/SmartCover.vue';
import PageHeader from '@/components/common/PageHeader.vue';

const tracks = ref<Track[]>([]);
const selectedTrack = ref<Track | null>(null);
const targetFormat = ref<'MP3' | 'FLAC' | 'WAV' | 'AAC'>('MP3');
const targetBitrate = ref<number>(320);
const targetSampleRate = ref<number>(44100);

const formatOptions = [
  { label: 'MP3 (有损，体积小)',  value: 'MP3'  },
  { label: 'FLAC (无损，体积大)', value: 'FLAC' },
  { label: 'WAV (无损未压缩)',    value: 'WAV'  },
  { label: 'AAC (有损，效率高)',  value: 'AAC'  },
];

const bitrateOptions = [128, 192, 256, 320];
const sampleRateOptions = [44100, 48000, 88200, 96000];

const needsBitrate = computed(() =>
  targetFormat.value === 'MP3' || targetFormat.value === 'AAC'
);

const currentTask = ref<ConversionTask | null>(null);
const taskHistory = ref<ConversionTask[]>([]);
let pollTimer: number | null = null;

onMounted(async () => {
  await loadTracks();
  await loadHistory();
});

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer);
});

async function loadTracks() {
  const res = await trackApi.page({ page: 1, size: 100 });
  tracks.value = res.records;
}

async function loadHistory() {
  const res = await conversionApi.page({ page: 1, size: 20 });
  taskHistory.value = res.records;
}

async function submit() {
  if (!selectedTrack.value) {
    ElMessage.warning('请先选择曲目');
    return;
  }
  const task = await conversionApi.submit({
    sourceTrackId: selectedTrack.value.id,
    targetFormat: targetFormat.value,
    targetBitrate: needsBitrate.value ? targetBitrate.value : null,
    targetSampleRate: targetSampleRate.value,
  });
  currentTask.value = task;
  ElMessage.success('转换任务已提交');
  startPolling();
}

function startPolling() {
  if (pollTimer) clearInterval(pollTimer);
  pollTimer = window.setInterval(async () => {
    if (!currentTask.value) return;
    const updated = await conversionApi.get(currentTask.value.id);
    currentTask.value = updated;
    if (updated.status === 'SUCCESS' || updated.status === 'FAILED') {
      clearInterval(pollTimer!);
      pollTimer = null;
      await loadHistory();
    }
  }, 800);  // 0.8 秒轮询一次
}

function download(task: ConversionTask) {
  window.open(`/api/conversions/${task.id}/download`, '_blank');
}
</script>

<template>
  <div class="studio-view">
    <PageHeader title="转换工坊" subtitle="基于 FFmpeg，无损 ↔ 有损 多格式互转" />

    <div class="layout">
      <!-- 左：选曲 -->
      <section class="track-picker">
        <h3>1. 选择曲目</h3>
        <div class="track-list">
          <div
            v-for="t in tracks"
            :key="t.id"
            class="track-card"
            :class="{ selected: selectedTrack?.id === t.id }"
            @click="selectedTrack = t"
          >
            <SmartCover :src="t.coverUrl || t.albumCoverUrl" class="thumb" />
            <div class="info">
              <div class="title">{{ t.title }}</div>
              <div class="meta">{{ t.artistName }} · {{ t.format }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 中：参数 -->
      <section class="param-form">
        <h3>2. 设置参数</h3>
        <el-form label-position="top">
          <el-form-item label="目标格式">
            <el-select v-model="targetFormat" placeholder="选择格式">
              <el-option v-for="o in formatOptions" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="比特率 (kbps)" v-if="needsBitrate">
            <el-select v-model="targetBitrate">
              <el-option v-for="b in bitrateOptions" :key="b" :label="`${b} kbps`" :value="b" />
            </el-select>
          </el-form-item>

          <el-form-item label="采样率 (Hz)">
            <el-select v-model="targetSampleRate">
              <el-option v-for="r in sampleRateOptions" :key="r" :label="`${r} Hz`" :value="r" />
            </el-select>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            :disabled="!selectedTrack"
            @click="submit"
            style="width:100%"
          >
            开始转换
          </el-button>
        </el-form>
      </section>

      <!-- 右：当前进度 + 历史 -->
      <section class="task-panel">
        <h3>3. 进度 & 历史</h3>

        <div v-if="currentTask" class="current-task">
          <div class="task-meta">
            <span class="status" :class="`status-${currentTask.status.toLowerCase()}`">
              {{ currentTask.status }}
            </span>
            <span>{{ currentTask.targetFormat }}</span>
          </div>
          <el-progress
            :percentage="currentTask.progress"
            :status="currentTask.status === 'SUCCESS' ? 'success' : currentTask.status === 'FAILED' ? 'exception' : ''"
          />
          <el-button
            v-if="currentTask.status === 'SUCCESS'"
            type="success"
            @click="download(currentTask)"
          >下载</el-button>
        </div>

        <el-divider>历史任务</el-divider>

        <div class="history-list">
          <div v-for="t in taskHistory" :key="t.id" class="history-item">
            <div class="hist-meta">
              <div class="hist-format">{{ t.targetFormat }} {{ t.targetBitrate ? `${t.targetBitrate}k` : '' }}</div>
              <div class="hist-status" :class="`status-${t.status.toLowerCase()}`">{{ t.status }}</div>
            </div>
            <el-button
              v-if="t.status === 'SUCCESS'"
              size="small"
              @click="download(t)"
            >下载</el-button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.studio-view { padding: 0; }

.layout {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: var(--space-5);
  margin-top: var(--space-5);
}

section {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.05);
  border-radius: var(--radius-card);
  padding: var(--space-5);

  h3 {
    margin: 0 0 var(--space-4);
    font-size: 14px;
    color: var(--color-fg-secondary);
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }
}

.track-list {
  display: flex; flex-direction: column; gap: var(--space-2);
  max-height: 500px;
  overflow-y: auto;
}
.track-card {
  display: flex; align-items: center; gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-btn);
  cursor: pointer;
  transition: all 200ms;
  &:hover { background: rgba(255,255,255,0.04); }
  &.selected {
    background: rgba(124,58,237,0.15);
    border: 1px solid var(--color-brand);
  }
  .thumb { width: 40px; height: 40px; border-radius: 6px; flex-shrink: 0; }
  .info {
    min-width: 0;
    .title { font-weight: 500; color: var(--color-fg-primary); }
    .meta { font-size: 12px; color: var(--color-fg-secondary); }
  }
}

.current-task {
  background: rgba(124,58,237,0.08);
  padding: var(--space-4);
  border-radius: var(--radius-btn);
  margin-bottom: var(--space-4);
}

.task-meta, .hist-meta {
  display: flex; align-items: center; gap: var(--space-3);
  margin-bottom: var(--space-2);
}
.status, .hist-status {
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: 11px;
  font-weight: 600;
}
.status-pending, .hist-status.status-pending { background: rgba(161,161,170,0.15); color: var(--color-fg-secondary); }
.status-running, .hist-status.status-running { background: rgba(124,58,237,0.15); color: var(--color-brand); }
.status-success, .hist-status.status-success { background: rgba(34,197,94,0.15); color: rgb(34,197,94); }
.status-failed,  .hist-status.status-failed  { background: rgba(239,68,68,0.15); color: rgb(239,68,68); }

.history-list {
  display: flex; flex-direction: column; gap: var(--space-2);
  max-height: 280px;
  overflow-y: auto;
}
.history-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-3);
  background: rgba(255,255,255,0.02);
  border-radius: var(--radius-btn);
}
</style>
```

---

### 任务 10：联调测试（**关键**）

启动后端 + 前端，验证以下场景：

1. **封面图显示**：首页/库页/专辑页应能看到真实专辑封面（你上传的 Imagine Dragons 至少能看到）
2. **基础播放**：库页点击一首歌 → 底部播放器出现 → 听到声音 → 进度条前进
3. **暂停/恢复/上下首/拖动进度条**：全部正常工作
4. **音量 + 循环模式**：能切换、能持久化（刷新页面音量保留）
5. **正在播放页面**：底部封面点击进入 → 看到大封面 + 波形（紫色波形带蓝色光标）+ 歌词面板
6. **波形点击跳转**：点波形某位置，音频跳到对应位置
7. **歌词同步**：如果当前歌曲有 LRC 歌词，当前播放行高亮 + 自动滚动
8. **专辑详情页**：能看到该专辑曲目列表 + 播放全部
9. **歌单详情页**：能看到歌单内曲目 + 拖拽排序生效
10. **转换工坊**：选曲 → 选 MP3 320k → 点开始转换 → 进度条 0→100 实时更新 → 完成后下载到本地，本地用任意播放器能正常播放
11. **播放历史**：听了几首歌后回首页，"最近播放"应该有数据
12. **多曲连播**：一首歌结束自动播放下一首

**重要**：步骤 10 是 Phase 5 的核心物证。**让用户用浏览器实际操作完成一次完整转换**，下载下来的文件能播放，才算真正通过。

---

### 任务 11：commit

```powershell
cd D:\Claude_Playground\Soundprint
git add backend/ frontend/
@"
feat: 播放器实装 + FFmpeg 转换 + 静态文件服务（Phase 5）

后端：
- WebMvcConfig：静态文件服务（/files/** 映射存储目录）
- FFmpegRunner：ProcessBuilder 调 ffmpeg，stderr 解析进度
- ConversionTaskServiceImpl：替换模拟，真 FFmpeg 异步转换
- @EnableAsync 启用，updateProgress 单字段更新避免频繁全行写

前端：
- 重写 player store：全局单例 audio 元素 + 队列 + 循环模式 + 音量持久化
- PlayerBar：底部常驻 + 进度条点击跳转 + 控件齐全
- NowPlayingView：大封面 + 波形 + 歌词同步
- WaveformDisplay：wavesurfer.js 7.x，波形渲染 + 点击跳转
- LyricsPanel：LRC 解析 + 跟随进度高亮 + 自动滚动
- StudioView：3 列布局，选曲 + 参数 + 进度/历史，0.8s 轮询进度
- 专辑/艺术家/歌单详情页内容补全
- SmartCover：封面图组件 + 失败回退
- fileUrl 工具 + vite 加 /files 代理

依赖：
- wavesurfer.js@7
- vuedraggable@next（歌单拖拽排序）

设计令牌延续 Phase 4，未引入新颜色或字体。
"@ | Out-File "D:\Claude_Playground\_msg.txt" -Encoding utf8NoBOM
git commit -F "D:\Claude_Playground\_msg.txt"
Remove-Item "D:\Claude_Playground\_msg.txt"
```

---

## 📚 边写边讲要求（必讲清单）

### 1. **全局单例 `<audio>` 元素**（必讲）
为什么所有播放走一个 audio，不允许组件自己 new？答辩话术：避免同时多个音频播放冲突、状态管理统一、wavesurfer 不能直接当播放器用。

### 2. **wavesurfer 渲染 ≠ wavesurfer 播放**（必讲）
本项目 wavesurfer 只画波形，**播放走 store 的 audio**。两者通过 `currentTime` 同步。代价是文件请求两次，**讲清楚为什么这么取舍**。

### 3. **HTTP Range Request 复用**（讲）
Phase 3 后端的流播放接口在 Phase 5 直接被前端 audio 元素和 wavesurfer 同时消费——**接口设计良好的回报**。

### 4. **LRC 歌词解析正则**（讲）
`[mm:ss.xx]文本` 格式，正则提取时间戳并换算秒数，按时间排序，运行时按当前播放时间二分找到当前行。

### 5. **FFmpeg ProcessBuilder + stderr 解析进度**（必讲，重点）
FFmpeg 的进度输出在 stderr（不是 stdout）。用 `redirectErrorStream(true)` 合并后正则提取 `time=` 字段，除以总时长得百分比。**这是答辩高频题**。

### 6. **`@Async` + Spring 线程池**（必讲）
转换是耗时操作，不能阻塞 Controller 线程（用户提交后立刻返回 task id，转换在后台跑）。`@Async` + `@EnableAsync` + Spring 默认线程池。

### 7. **前端轮询 vs WebSocket**（讲）
Phase 5 用 0.8 秒轮询查进度。**讲清楚为什么不用 WebSocket**：轮询简单、对短任务足够、不引入新依赖。如果未来要做长任务或者要求实时性更高，再升级 WebSocket/SSE。

### 8. **Pinia store 持久化 localStorage**（讲）
音量、循环模式这些"用户偏好"用 localStorage 持久化。**讲清楚和 sessionStorage、cookie 的区别**。

### 9. **CSS `grid-template-areas` 在 NowPlayingView 的应用**（讲）
大封面 + 信息区两栏布局，用 grid 比 flex 嵌套更清晰。

### 10. **静态资源 `setCachePeriod`**（讲）
封面图加 1 小时浏览器缓存，减少重复请求。讲清楚 HTTP `Cache-Control` 头工作原理。

---

## ✅ 完成检查清单

- [ ] FFmpeg 在 PATH 中
- [ ] 后端 WebMvcConfig 静态文件服务跑通
- [ ] 浏览器能直接访问 http://localhost:8080/files/cover/xxx.jpg
- [ ] vite.config 加 /files 代理
- [ ] SmartCover 组件 + fileUrl 工具
- [ ] 首页、库页、专辑页**真实封面可见**
- [ ] Player store 完整重写，全局单例 audio
- [ ] PlayerBar UI 实装，能播放、暂停、上下首、拖动、音量
- [ ] NowPlayingView：大封面 + 波形 + 歌词同步
- [ ] wavesurfer 波形渲染，点击跳转
- [ ] LRC 歌词解析 + 高亮 + 滚动
- [ ] 专辑详情、艺术家详情、歌单详情**内容齐全**
- [ ] 歌单拖拽排序生效
- [ ] FFmpegRunner 跑通真转换
- [ ] StudioView 前端完整，能选曲 → 设参数 → 提交 → 看进度 → 下载
- [ ] 一次完整 MP3 转换跑通，下载的文件本地能播
- [ ] 多曲连播工作
- [ ] commit 完成

---

## 📩 反馈给架构师的内容

1. **首页截图**（封面图应显示真实图片）
2. **正在播放页截图**（大封面 + 波形 + 歌词）
3. **转换工坊截图**（进度条 50% 以上的中间态最理想）
4. **转换完成后下载的文件能在本地播放**（拍个本地播放器界面 + 文件信息也行）
5. **任何卡壳或不理解的地方**
6. **是否准备进 Phase 6（ECharts 统计页）**

---

## ⚠️ 注意事项

- **绝对不要在组件里 `new Audio()`**，全部走 player store
- **不要修改 Phase 3 的 `/api/stream/{id}` 接口**——它已经支持 Range，前端 audio 和 wavesurfer 共用
- **不要把进度条做成"假进度"**，必须是真实 `currentTime / duration`
- **不要在 FFmpeg 输出还没结束前就标记任务为 SUCCESS**——必须等进程退出码 0 才算成功
- **FFmpeg 命令绝对不要拼接用户输入到 shell 字符串**，必须用 `ProcessBuilder` 数组形式（防注入）
- **设计令牌延续 Phase 4**，不引入新颜色
- **transition 时长统一 300ms cubic-bezier(0.4, 0, 0.2, 1)**，与 Phase 4 一致
- 如果 wavesurfer 加载大 FLAC 卡顿，**先记进 dev-notes**，不要立刻引入 peaks 接口（Phase 5 不优化）

---

**End of Phase 5 Document.**
