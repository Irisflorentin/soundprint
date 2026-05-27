import type { Router } from 'vue-router';
import { useUserStore } from '@/stores/user';

/**
 * 路由守卫：未登录访问受保护页面时跳登录页。
 * Phase 7.8 改为从 localStorage 恢复登录态，避免初次访问绕过登录页。
 */
export function setupGuards(router: Router) {
  router.beforeEach((to) => {
    const userStore = useUserStore();

    if (!userStore.isLoggedIn && to.path !== '/login') {
      return '/login';
    }

    if (to.path === '/login' && userStore.isLoggedIn) {
      return '/';
    }

    return true;
  });
}
