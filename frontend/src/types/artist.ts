import type { Album } from './album';
import type { Track } from './track';

export interface Artist {
  id: number;
  name: string;
  bio: string | null;
  avatarUrl: string | null;
  country: string | null;
  formedYear: number | null;
  createdAt: string;
}

export interface ArtistDetail extends Artist {
  albumCount: number;
  trackCount: number;
  albums: Album[];
  tracks: Track[];
}

export interface ArtistQuery {
  page?: number;
  size?: number;
  keyword?: string;
}

export interface ArtistCreate {
  name: string;
  bio?: string;
  avatarUrl?: string;
  country?: string;
  formedYear?: number;
}
