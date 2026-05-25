import client from './client';
import type { PlayHistoryItem, PlayHistoryRecord } from '@/types/playHistory';

export const playHistoryApi = {
  record: (body: PlayHistoryRecord): Promise<void> =>
    client.post('/play-history', body),

  recent: (limit = 10): Promise<PlayHistoryItem[]> =>
    client.get('/play-history/recent', { params: { limit } }),
};
