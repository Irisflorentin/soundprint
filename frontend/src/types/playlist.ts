import type { Track } from './track';

export interface Playlist {
  id: number;
  name: string;
  description: string | null;
  coverUrl: string | null;
  trackCount: number;
  createdAt: string;
}

export interface PlaylistDetail extends Playlist {
  tracks: Track[];
}

export interface PlaylistCreate {
  name: string;
  description?: string;
  coverUrl?: string;
}
