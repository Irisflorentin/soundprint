import type { Tag } from './tag';

export interface Track {
  id: number;
  title: string;
  artistId: number | null;
  artistName: string | null;
  albumId: number | null;
  albumTitle: string | null;
  albumCoverUrl: string | null;
  coverUrl: string | null;
  format: string;
  duration: number;        // 秒
  bitrate: number;
  sampleRate: number;
  fileSizeBytes: number;
  createdAt: string;
}

export interface TrackDetail extends Track {
  trackNumber: number | null;
  channels: number | null;
  lyrics: string | null;
  tags: Tag[];
  favorited: boolean;      // 后端字段名是 favorited（非 isFavorited）
}

export interface TrackQuery {
  page?: number;
  size?: number;
  keyword?: string;
  artistId?: number;
  albumId?: number;
  format?: string;
}

export interface TrackUpdate {
  title?: string;
  artistId?: number;
  albumId?: number;
  trackNumber?: number;
  lyrics?: string;
}
