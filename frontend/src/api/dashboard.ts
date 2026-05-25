import client from './client';
import type { Dashboard } from '@/types/dashboard';

export const dashboardApi = {
  get: (): Promise<Dashboard> =>
    client.get('/dashboard'),
};
