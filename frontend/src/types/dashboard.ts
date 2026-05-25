import type { Track } from './track';
import type { Album } from './album';
import type { Artist } from './artist';
import type { PlayHistoryItem } from './playHistory';

export interface Dashboard {
  greeting: string;
  recentTracks: Track[];
  recentlyPlayed: PlayHistoryItem[];
  favorites: Track[];
  featuredAlbums: Album[];
  featuredArtists: Artist[];
}
