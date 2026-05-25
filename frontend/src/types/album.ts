import type { Track } from './track';

export interface Album {
  id: number;
  title: string;
  artistId: number | null;
  artistName: string | null;
  coverUrl: string | null;
  releaseYear: number | null;
  genre: string | null;
  description: string | null;
  createdAt: string;
}

export interface AlbumDetail extends Album {
  trackCount: number;
  tracks: Track[];
}

export interface AlbumQuery {
  page?: number;
  size?: number;
  keyword?: string;
  artistId?: number;
}

export interface AlbumCreate {
  title: string;
  artistId?: number;
  coverUrl?: string;
  releaseYear?: number;
  genre?: string;
  description?: string;
}
