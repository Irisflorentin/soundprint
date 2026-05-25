import { defineStore } from 'pinia';
import { ref } from 'vue';

// 音乐库页的筛选状态（跨导航保留）
export const useLibraryStore = defineStore('library', () => {
  const keyword = ref('');
  const format = ref('');
  const page = ref(1);
  const size = ref(20);

  function reset() {
    keyword.value = '';
    format.value = '';
    page.value = 1;
  }

  return { keyword, format, page, size, reset };
});
