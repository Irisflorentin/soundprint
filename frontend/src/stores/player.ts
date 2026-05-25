import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Track } from '@/types/track';

// Phase 4 仅占位状态，Phase 5 接 wavesurfer.js 实装播放
export const usePlayerStore = defineStore('player', () => {
  const currentTrack = ref<Track | null>(null);
  const isPlaying = ref(false);
  const currentTime = ref(0);
  const duration = ref(0);
  const volume = ref(0.8);
  const queue = ref<Track[]>([]);
  const queueIndex = ref(-1);

  function play(track: Track) {
    currentTrack.value = track;
    isPlaying.value = true;
  }

  function pause() {
    isPlaying.value = false;
  }

  function resume() {
    isPlaying.value = true;
  }

  return {
    currentTrack, isPlaying, currentTime, duration, volume, queue, queueIndex,
    play, pause, resume,
  };
});
