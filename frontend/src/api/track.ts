import client from './client';
import type { PageResult } from './types';
import type { Track, TrackDetail, TrackPeaks, TrackQuery, TrackUpdate } from '@/types/track';

export const trackApi = {
  page: (query: TrackQuery): Promise<PageResult<Track>> =>
    client.get('/tracks', { params: query }),

  detail: (id: number): Promise<TrackDetail> =>
    client.get(`/tracks/${id}`),

  search: (keyword: string, page = 1, size = 20): Promise<PageResult<Track>> =>
    client.get('/tracks/search', { params: { keyword, page, size } }),

  upload: (file: File, onProgress?: (percent: number) => void): Promise<Track> => {
    const formData = new FormData();
    formData.append('file', file);
    return client.post('/tracks/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (e.total && onProgress) onProgress(Math.round((e.loaded / e.total) * 100));
      },
    });
  },

  update: (id: number, body: TrackUpdate): Promise<Track> =>
    client.put(`/tracks/${id}`, body),

  remove: (id: number): Promise<void> =>
    client.delete(`/tracks/${id}`),

  getLyrics: (id: number): Promise<string> =>
    client.get(`/tracks/${id}/lyrics`),

  peaks: (id: number, samples = 1000): Promise<TrackPeaks> =>
    client.get(`/tracks/${id}/peaks`, { params: { samples } }),

  updateLyrics: (id: number, lyrics: string): Promise<void> =>
    client.put(`/tracks/${id}/lyrics`, lyrics, {
      headers: { 'Content-Type': 'text/plain' },
    }),

  assignTags: (trackId: number, tagIds: number[]): Promise<void> =>
    client.post(`/tracks/${trackId}/tags`, { tagIds }),
};
