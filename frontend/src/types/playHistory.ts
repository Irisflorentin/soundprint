export interface PlayHistoryItem {
  trackId: number;
  title: string;
  artistName: string | null;
  albumTitle: string | null;
  coverUrl: string | null;
  playedSeconds: number | null;
  playedAt: string;
}

export interface PlayHistoryRecord {
  trackId: number;
  playedSeconds?: number;
}
