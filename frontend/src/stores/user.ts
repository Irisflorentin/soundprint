import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import type { User } from '@/types/user';

const USER_STORAGE_KEY = 'soundprint_user';
const TOKEN_STORAGE_KEY = 'soundprint_token';

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null);
  const token = ref('');

  const isLoggedIn = computed(() => Boolean(currentUser.value && token.value));
  const nickname = computed(() => currentUser.value?.nickname || currentUser.value?.username || '');
  const avatar = computed(() => currentUser.value?.avatarUrl || '');

  function login(_username: string, _password: string) {
    // 演示登录：课程项目暂不接后端鉴权，任意密码进入 admin 单用户。
    currentUser.value = {
      id: 1,
      username: 'admin',
      nickname: 'Kaidi',
      avatarUrl: null,
      registeredAt: '2026-05-26',
    };
    token.value = `demo-token-${Date.now()}`;
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(currentUser.value));
    localStorage.setItem(TOKEN_STORAGE_KEY, token.value);
  }

  function logout() {
    currentUser.value = null;
    token.value = '';
    localStorage.removeItem(USER_STORAGE_KEY);
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }

  function restoreFromStorage() {
    const userJson = localStorage.getItem(USER_STORAGE_KEY);
    const savedToken = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (!userJson || !savedToken) {
      logout();
      return;
    }

    try {
      const parsedUser = JSON.parse(userJson) as User;
      currentUser.value = {
        ...parsedUser,
        registeredAt: parsedUser.registeredAt || '2026-05-26',
      };
      token.value = savedToken;
    } catch {
      logout();
    }
  }

  return {
    currentUser,
    token,
    isLoggedIn,
    nickname,
    avatar,
    login,
    logout,
    restoreFromStorage,
  };
});
