import client from './client';
import type { PageResult } from './types';
import type { Album, AlbumDetail, AlbumQuery, AlbumCreate } from '@/types/album';

export const albumApi = {
  page: (query: AlbumQuery): Promise<PageResult<Album>> =>
    client.get('/albums', { params: query }),

  detail: (id: number): Promise<AlbumDetail> =>
    client.get(`/albums/${id}`),

  create: (body: AlbumCreate): Promise<Album> =>
    client.post('/albums', body),

  update: (id: number, body: Partial<AlbumCreate>): Promise<Album> =>
    client.put(`/albums/${id}`, body),

  remove: (id: number): Promise<void> =>
    client.delete(`/albums/${id}`),
};
