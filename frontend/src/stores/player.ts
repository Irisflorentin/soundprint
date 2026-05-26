import { defineStore } from 'pinia';
import { computed, ref, watch } from 'vue';
import type { Track } from '@/types/track';
import { playHistoryApi } from '@/api/playHistory';

export type RepeatMode = 'off' | 'one' | 'all';

export const usePlayerStore = defineStore('player', () => {
  const audio = ref<HTMLAudioElement | null>(null);
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

  let lastReportedTrackId: number | null = null;
  let accumulatedSeconds = 0;

  const savedVolume = localStorage.getItem('player.volume');
  if (savedVolume !== null) {
    const parsed = Number(savedVolume);
    if (!Number.isNaN(parsed)) volume.value = Math.max(0, Math.min(1, parsed));
  }

  const progress = computed(() => duration.value > 0 ? currentTime.value / duration.value : 0);
  const hasNext = computed(() => queueIndex.value < queue.value.length - 1 || repeatMode.value === 'all');
  const hasPrev = computed(() => queueIndex.value > 0 || repeatMode.value === 'all');

  function initAudio() {
    if (audio.value) return;

    const el = new Audio();
    el.preload = 'metadata';
    el.volume = volume.value;
    el.muted = muted.value;

    el.addEventListener('loadedmetadata', () => {
      duration.value = Number.isFinite(el.duration) ? el.duration : 0;
    });
    el.addEventListener('timeupdate', () => {
      currentTime.value = el.currentTime;
    });
    el.addEventListener('play', () => {
      isPlaying.value = true;
      isLoading.value = false;
    });
    el.addEventListener('pause', () => {
      isPlaying.value = false;
    });
    el.addEventListener('waiting', () => {
      isLoading.value = true;
    });
    el.addEventListener('canplay', () => {
      isLoading.value = false;
    });
    el.addEventListener('ended', () => {
      reportPlayHistory();
      handleEnded();
    });

    window.addEventListener('beforeunload', () => reportPlayHistory());
    audio.value = el;
  }

  function playTrack(track: Track, newQueue?: Track[], startIndex?: number) {
    initAudio();
    reportPlayHistory();

    currentTrack.value = track;
    currentTime.value = 0;
    duration.value = 0;
    isLoading.value = true;

    if (newQueue) {
      queue.value = newQueue;
      const resolvedIndex = startIndex ?? newQueue.findIndex((item) => item.id === track.id);
      queueIndex.value = resolvedIndex >= 0 ? resolvedIndex : 0;
    } else if (queue.value.length === 0) {
      queue.value = [track];
      queueIndex.value = 0;
    }

    accumulatedSeconds = 0;
    lastReportedTrackId = track.id;

    if (!audio.value) return;
    audio.value.src = `/api/stream/${track.id}`;
    audio.value.load();
    audio.value.play().catch((error) => {
      console.error('播放失败', error);
      isPlaying.value = false;
      isLoading.value = false;
    });
  }

  function play(track: Track) {
    playTrack(track, [track], 0);
  }

  function pause() {
    audio.value?.pause();
  }

  function resume() {
    if (audio.value && currentTrack.value) {
      audio.value.play().catch((error) => {
        console.error('恢复播放失败', error);
      });
    }
  }

  function toggle() {
    isPlaying.value ? pause() : resume();
  }

  function seek(seconds: number) {
    if (!audio.value) return;
    const nextTime = Math.max(0, Math.min(seconds, duration.value || seconds));
    audio.value.currentTime = nextTime;
    currentTime.value = nextTime;
  }

  function setVolume(value: number) {
    volume.value = Math.max(0, Math.min(1, value));
    if (audio.value) audio.value.volume = volume.value;
    if (volume.value > 0) muted.value = false;
  }

  function toggleMute() {
    muted.value = !muted.value;
    if (audio.value) audio.value.muted = muted.value;
  }

  function next() {
    if (queue.value.length === 0) return;
    if (queueIndex.value < queue.value.length - 1) {
      queueIndex.value += 1;
      playTrack(queue.value[queueIndex.value]);
    } else if (repeatMode.value === 'all') {
      queueIndex.value = 0;
      playTrack(queue.value[0]);
    }
  }

  function prev() {
    if (queue.value.length === 0) return;
    if (currentTime.value > 3) {
      seek(0);
      return;
    }
    if (queueIndex.value > 0) {
      queueIndex.value -= 1;
      playTrack(queue.value[queueIndex.value]);
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

  function setRepeatMode(mode: RepeatMode) {
    repeatMode.value = mode;
  }

  function shuffleQueue() {
    shuffled.value = !shuffled.value;
    if (!shuffled.value) return;

    const current = currentTrack.value;
    const rest = queue.value.filter((track) => track.id !== current?.id);
    for (let i = rest.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [rest[i], rest[j]] = [rest[j], rest[i]];
    }
    queue.value = current ? [current, ...rest] : rest;
    queueIndex.value = current ? 0 : -1;
  }

  function reportPlayHistory() {
    if (!lastReportedTrackId || accumulatedSeconds < 5) return;
    playHistoryApi.record({
      trackId: lastReportedTrackId,
      playedSeconds: Math.round(accumulatedSeconds),
    }).catch(() => {
      // 播放历史失败不打断用户听歌。
    });
    accumulatedSeconds = 0;
  }

  watch(currentTime, (now, then = 0) => {
    if (isPlaying.value && now > then && now - then < 2) {
      accumulatedSeconds += now - then;
    }
  });

  watch(volume, (value) => {
    localStorage.setItem('player.volume', String(value));
  });

  watch(muted, (value) => {
    if (audio.value) audio.value.muted = value;
  });

  return {
    currentTrack,
    queue,
    queueIndex,
    isPlaying,
    isLoading,
    currentTime,
    duration,
    volume,
    muted,
    repeatMode,
    shuffled,
    progress,
    hasNext,
    hasPrev,
    playTrack,
    play,
    pause,
    resume,
    toggle,
    seek,
    setVolume,
    toggleMute,
    next,
    prev,
    setRepeatMode,
    shuffleQueue,
  };
});
