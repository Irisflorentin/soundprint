import { ref } from 'vue';

/**
 * 通用 API 调用封装：自动管理 loading / error 状态。
 * 用法：const { loading, run } = useApi(); await run(() => trackApi.page(query));
 */
export function useApi() {
  const loading = ref(false);
  const error = ref<unknown>(null);

  async function run<T>(fn: () => Promise<T>): Promise<T | undefined> {
    loading.value = true;
    error.value = null;
    try {
      return await fn();
    } catch (e) {
      error.value = e;
      return undefined;
    } finally {
      loading.value = false;
    }
  }

  return { loading, error, run };
}
