import client from './client';
import type { PageResult } from './types';
import type { ConversionTask, ConversionSubmit } from '@/types/conversion';

export const conversionApi = {
  submit: (body: ConversionSubmit): Promise<ConversionTask> =>
    client.post('/conversions', body),

  get: (id: number): Promise<ConversionTask> =>
    client.get(`/conversions/${id}`),

  list: (page = 1, size = 10): Promise<PageResult<ConversionTask>> =>
    client.get('/conversions', { params: { page, size } }),

  page: (query: { page?: number; size?: number } = {}): Promise<PageResult<ConversionTask>> =>
    client.get('/conversions', { params: query }),

  download: (id: number): void => {
    window.open(`/api/conversions/${id}/download`, '_blank');
  },

  downloadUrl: (id: number): string => `/api/conversions/${id}/download`,
};
