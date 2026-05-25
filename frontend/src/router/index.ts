import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { hideChrome: true },
  },
  { path: '/',            component: () => import('@/views/dashboard/DashboardView.vue') },
  { path: '/library',     component: () => import('@/views/library/LibraryView.vue') },
  { path: '/albums',      component: () => import('@/views/albums/AlbumListView.vue') },
  { path: '/albums/:id',  component: () => import('@/views/albums/AlbumDetailView.vue') },
  { path: '/artists',     component: () => import('@/views/artists/ArtistListView.vue') },
  { path: '/artists/:id', component: () => import('@/views/artists/ArtistDetailView.vue') },
  { path: '/playlists',   component: () => import('@/views/playlists/PlaylistListView.vue') },
  { path: '/playlists/:id', component: () => import('@/views/playlists/PlaylistDetailView.vue') },
  { path: '/studio',      component: () => import('@/views/studio/StudioView.vue') },
  { path: '/stats',       component: () => import('@/views/stats/StatsView.vue') },
  { path: '/settings',    component: () => import('@/views/settings/SettingsView.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
