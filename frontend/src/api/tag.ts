import client from './client';
import type { Tag, TagCreate } from '@/types/tag';

export const tagApi = {
  list: (): Promise<Tag[]> =>
    client.get('/tags'),

  create: (body: TagCreate): Promise<Tag> =>
    client.post('/tags', body),

  update: (id: number, body: Partial<TagCreate>): Promise<Tag> =>
    client.put(`/tags/${id}`, body),

  remove: (id: number): Promise<void> =>
    client.delete(`/tags/${id}`),
};
