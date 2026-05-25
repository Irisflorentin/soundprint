import client from './client';
import type { PageResult } from './types';
import type { Artist, ArtistDetail, ArtistQuery, ArtistCreate } from '@/types/artist';

export const artistApi = {
  page: (query: ArtistQuery): Promise<PageResult<Artist>> =>
    client.get('/artists', { params: query }),

  detail: (id: number): Promise<ArtistDetail> =>
    client.get(`/artists/${id}`),

  create: (body: ArtistCreate): Promise<Artist> =>
    client.post('/artists', body),

  update: (id: number, body: Partial<ArtistCreate>): Promise<Artist> =>
    client.put(`/artists/${id}`, body),

  remove: (id: number): Promise<void> =>
    client.delete(`/artists/${id}`),
};
