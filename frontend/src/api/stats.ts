import client from './client';
import type {
  StatsOverview, GenreDistributionItem, TopArtistItem, MonthlyTrendItem, HeatmapItem,
} from '@/types/stats';

export const statsApi = {
  overview: (): Promise<StatsOverview> =>
    client.get('/stats/overview'),

  genres: (): Promise<GenreDistributionItem[]> =>
    client.get('/stats/genres'),

  topArtists: (limit = 10): Promise<TopArtistItem[]> =>
    client.get('/stats/top-artists', { params: { limit } }),

  monthlyTrend: (months = 12): Promise<MonthlyTrendItem[]> =>
    client.get('/stats/monthly-trend', { params: { months } }),

  heatmap: (days = 365): Promise<HeatmapItem[]> =>
    client.get('/stats/heatmap', { params: { days } }),
};
