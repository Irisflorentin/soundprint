import type { Router } from 'vue-router';
import { useUserStore } from '@/stores/user';

/**
 * 路由守卫：未登录访问受保护页面时跳登录页。
 * Phase 4 默认已登录（user store isLoggedIn=true），守卫基本放行；
 * 等接入真实鉴权后这里自然生效。
 */
export function setupGuards(router: Router) {
  router.beforeEach((to) => {
    const userStore = useUserStore();
    if (!userStore.isLoggedIn && to.path !== '/login') {
      return '/login';
    }
    return true;
  });
}
