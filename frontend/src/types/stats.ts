export interface StatsOverview {
  totalTracks: number;
  totalDurationSeconds: number;
  totalPlays: number;
  totalPlayedSeconds: number;
  favoriteCount: number;
  playlistCount: number;
}

export interface GenreDistributionItem {
  genre: string;
  count: number;
  percentage: number;
}

export interface TopArtistItem {
  artistId: number;
  artistName: string;
  playCount: number;
}

export interface MonthlyTrendItem {
  month: string;
  playCount: number;
  totalSeconds: number;
}

export interface HeatmapItem {
  date: string;
  count: number;
  totalSeconds: number;
}
