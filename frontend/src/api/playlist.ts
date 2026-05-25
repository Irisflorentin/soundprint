import client from './client';
import type { PageResult } from './types';
import type { Playlist, PlaylistDetail, PlaylistCreate } from '@/types/playlist';

export const playlistApi = {
  page: (page = 1, size = 10): Promise<PageResult<Playlist>> =>
    client.get('/playlists', { params: { page, size } }),

  detail: (id: number): Promise<PlaylistDetail> =>
    client.get(`/playlists/${id}`),

  create: (body: PlaylistCreate): Promise<Playlist> =>
    client.post('/playlists', body),

  update: (id: number, body: Partial<PlaylistCreate>): Promise<Playlist> =>
    client.put(`/playlists/${id}`, body),

  remove: (id: number): Promise<void> =>
    client.delete(`/playlists/${id}`),

  addTrack: (id: number, trackId: number): Promise<void> =>
    client.post(`/playlists/${id}/tracks`, { trackId }),

  removeTrack: (id: number, trackId: number): Promise<void> =>
    client.delete(`/playlists/${id}/tracks/${trackId}`),

  reorder: (id: number, trackIds: number[]): Promise<void> =>
    client.put(`/playlists/${id}/reorder`, { trackIds }),
};
