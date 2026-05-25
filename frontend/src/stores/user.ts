import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { User } from '@/types/user';

export const useUserStore = defineStore('user', () => {
  // Phase 4 暂时硬编码默认用户，等末尾加登录后从后端取
  const currentUser = ref<User>({
    id: 1,
    username: 'admin',
    nickname: 'Kaidi',
    avatarUrl: null,
  });

  const isLoggedIn = ref(true);   // Phase 4 默认已登录，登录页只走形式

  function login(_username: string, _password: string) {
    // 占位实现：任何账号密码都成功
    isLoggedIn.value = true;
  }

  function logout() {
    isLoggedIn.value = false;
  }

  return { currentUser, isLoggedIn, login, logout };
});
