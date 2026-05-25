import { ref } from 'vue';

/**
 * 分页状态管理（current/size/total），配合 Element-Plus el-pagination 用
 */
export function usePagination(initialSize = 20) {
  const current = ref(1);
  const size = ref(initialSize);
  const total = ref(0);

  function setTotal(t: number) {
    total.value = t;
  }

  function reset() {
    current.value = 1;
    total.value = 0;
  }

  return { current, size, total, setTotal, reset };
}
