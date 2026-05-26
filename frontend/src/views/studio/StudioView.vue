<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import PageHeader from '@/components/common/PageHeader.vue';
import LoadingBlock from '@/components/common/LoadingBlock.vue';
import SmartCover from '@/components/common/SmartCover.vue';
import { trackApi } from '@/api/track';
import { conversionApi } from '@/api/conversion';
import type { Track } from '@/types/track';
import type { ConversionTask } from '@/types/conversion';

type TargetFormat = 'MP3' | 'FLAC' | 'WAV' | 'AAC';

const tracks = ref<Track[]>([]);
const tracksLoading = ref(false);
const selectedTrack = ref<Track | null>(null);
const targetFormat = ref<TargetFormat>('MP3');
const targetBitrate = ref(320);
const targetSampleRate = ref(44100);

const currentTask = ref<ConversionTask | null>(null);
const taskHistory = ref<ConversionTask[]>([]);
const submitting = ref(false);

let pollTimer: number | null = null;

const formatOptions: { label: string; value: TargetFormat }[] = [
  { label: 'MP3（有损，体积小）', value: 'MP3' },
  { label: 'FLAC（无损，体积大）', value: 'FLAC' },
  { label: 'WAV（无损未压缩）', value: 'WAV' },
  { label: 'AAC（有损，效率高）', value: 'AAC' },
];
const bitrateOptions = [128, 192, 256, 320];
const sampleRateOptions = [44100, 48000, 88200, 96000];

const needsBitrate = computed(() => targetFormat.value === 'MP3' || targetFormat.value === 'AAC');

onMounted(async () => {
  await Promise.all([loadTracks(), loadHistory()]);
});

onUnmounted(() => {
  stopPolling();
});

async function loadTracks() {
  tracksLoading.value = true;
  try {
    const result = await trackApi.page({ page: 1, size: 100 });
    tracks.value = result.records;
  } finally {
    tracksLoading.value = false;
  }
}

async function loadHistory() {
  const result = await conversionApi.page({ page: 1, size: 20 });
  taskHistory.value = result.records;
}

async function submit() {
  if (!selectedTrack.value) {
    ElMessage.warning('请先选择曲目');
    return;
  }

  submitting.value = true;
  try {
    currentTask.value = await conversionApi.submit({
      sourceTrackId: selectedTrack.value.id,
      targetFormat: targetFormat.value,
      targetBitrate: needsBitrate.value ? targetBitrate.value : null,
      targetSampleRate: targetSampleRate.value,
    });
    ElMessage.success('转换任务已提交');
    startPolling();
  } finally {
    submitting.value = false;
  }
}

function startPolling() {
  stopPolling();
  pollTimer = window.setInterval(async () => {
    if (!currentTask.value) return;
    const updated = await conversionApi.get(currentTask.value.id);
    currentTask.value = updated;
    if (updated.status === 'SUCCESS' || updated.status === 'FAILED') {
      stopPolling();
      await loadHistory();
    }
  }, 800);
}

function stopPolling() {
  if (pollTimer != null) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

function download(task: ConversionTask) {
  conversionApi.download(task.id);
}
</script>

<template>
  <div class="studio-view">
    <PageHeader title="转换工坊" subtitle="基于 FFmpeg 的多格式音频转换" />

    <div class="layout">
      <section class="track-picker">
        <h3>1. 选择曲目</h3>
        <LoadingBlock v-if="tracksLoading" text="正在加载音乐库..." />
        <div v-else class="track-list">
          <button
            v-for="track in tracks"
            :key="track.id"
            type="button"
            class="track-card"
            :class="{ selected: selectedTrack?.id === track.id }"
            @click="selectedTrack = track"
          >
            <SmartCover
              :src="track.coverUrl || track.albumCoverUrl"
              :alt="track.title"
              :fallback-text="track.title"
              class="thumb"
            />
            <span class="info">
              <strong>{{ track.title }}</strong>
              <small>{{ track.artistName || '未知艺术家' }} · {{ track.format }}</small>
            </span>
          </button>
        </div>
      </section>

      <section class="param-form">
        <h3>2. 设置参数</h3>
        <el-form label-position="top">
          <el-form-item label="目标格式">
            <el-select v-model="targetFormat" placeholder="选择格式">
              <el-option
                v-for="option in formatOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>

          <el-form-item v-if="needsBitrate" label="比特率（kbps）">
            <el-select v-model="targetBitrate">
              <el-option
                v-for="bitrate in bitrateOptions"
                :key="bitrate"
                :label="`${bitrate} kbps`"
                :value="bitrate"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="采样率（Hz）">
            <el-select v-model="targetSampleRate">
              <el-option
                v-for="sampleRate in sampleRateOptions"
                :key="sampleRate"
                :label="`${sampleRate} Hz`"
                :value="sampleRate"
              />
            </el-select>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :disabled="!selectedTrack"
            :loading="submitting"
            @click="submit"
          >
            开始转换
          </el-button>
        </el-form>
      </section>

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
            :status="currentTask.status === 'SUCCESS' ? 'success' : currentTask.status === 'FAILED' ? 'exception' : undefined"
          />
          <p v-if="currentTask.errorMessage" class="error-message">{{ currentTask.errorMessage }}</p>
          <el-button
            v-if="currentTask.status === 'SUCCESS'"
            type="success"
            @click="download(currentTask)"
          >
            下载
          </el-button>
        </div>

        <el-divider>历史任务</el-divider>

        <div class="history-list">
          <div v-for="task in taskHistory" :key="task.id" class="history-item">
            <div class="hist-meta">
              <strong>{{ task.targetFormat }} {{ task.targetBitrate ? `${task.targetBitrate}k` : '' }}</strong>
              <span class="hist-status" :class="`status-${task.status.toLowerCase()}`">{{ task.status }}</span>
            </div>
            <el-button
              v-if="task.status === 'SUCCESS'"
              size="small"
              @click="download(task)"
            >
              下载
            </el-button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.layout {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr;
  gap: var(--space-5);
  margin-top: var(--space-5);
}

section {
  padding: var(--space-5);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-card);
  background: rgba(255, 255, 255, 0.03);

  h3 {
    margin: 0 0 var(--space-4);
    color: var(--color-fg-secondary);
    font-size: 14px;
    letter-spacing: 0.05em;
    text-transform: uppercase;
  }
}

.track-list {
  max-height: 500px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.track-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  color: inherit;
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-btn);
  text-align: left;
  cursor: pointer;
  transition: all var(--duration-base) var(--ease);

  &:hover {
    background: rgba(255, 255, 255, 0.04);
  }

  &.selected {
    border-color: var(--color-brand);
    background: rgba(124, 58, 237, 0.15);
  }
}

.thumb {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 6px;
}

.info {
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

  strong { color: var(--color-fg-primary); font-size: 13px; }
  small { color: var(--color-fg-secondary); font-size: 12px; }
}

.submit-btn {
  width: 100%;
}

.current-task {
  margin-bottom: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-btn);
  background: rgba(124, 58, 237, 0.08);
}

.task-meta,
.hist-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.status,
.hist-status {
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: 11px;
  font-weight: 600;
}

.status-pending { background: var(--color-info-bg); color: var(--color-info); }
.status-running { background: color-mix(in srgb, var(--color-brand) 15%, transparent); color: var(--color-brand); }
.status-success { background: var(--color-success-bg); color: var(--color-success); }
.status-failed  { background: var(--color-danger-bg); color: var(--color-danger); }

.error-message {
  margin: var(--space-3) 0 0;
  color: var(--color-danger);
  font-size: 12px;
  line-height: 1.5;
}

.history-list {
  max-height: 280px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-btn);
  background: rgba(255, 255, 255, 0.02);
}

.hist-meta {
  min-width: 0;
  margin-bottom: 0;

  strong {
    color: var(--color-fg-primary);
    font-size: 13px;
  }
}

@media (max-width: 1120px) {
  .layout {
    grid-template-columns: 1fr;
  }
}
</style>
