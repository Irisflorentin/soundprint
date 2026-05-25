import client from './client';
import type { PageResult } from './types';
import type { Track } from '@/types/track';

export const favoriteApi = {
  add: (trackId: number): Promise<void> =>
    client.post(`/favorites/${trackId}`),

  remove: (trackId: number): Promise<void> =>
    client.delete(`/favorites/${trackId}`),

  list: (page = 1, size = 20): Promise<PageResult<Track>> =>
    client.get('/favorites', { params: { page, size } }),

  check: (trackId: number): Promise<boolean> =>
    client.get(`/favorites/check/${trackId}`),
};
