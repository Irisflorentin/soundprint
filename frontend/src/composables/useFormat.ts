import dayjs from 'dayjs';

/**
 * 时长、文件大小、日期格式化工具集
 */
export function useFormat() {
  /** 秒 → m:ss 或 h:mm:ss */
  function formatDuration(seconds: number | null | undefined): string {
    if (!seconds || seconds < 0) return '0:00';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);
    const ss = String(s).padStart(2, '0');
    if (h > 0) return `${h}:${String(m).padStart(2, '0')}:${ss}`;
    return `${m}:${ss}`;
  }

  /** 字节 → 人类可读（KB/MB/GB） */
  function formatFileSize(bytes: number | null | undefined): string {
    if (!bytes || bytes <= 0) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB'];
    let v = bytes;
    let i = 0;
    while (v >= 1024 && i < units.length - 1) {
      v /= 1024;
      i++;
    }
    return `${v.toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
  }

  /** 总秒数 → "X 小时 Y 分"（用于统计页总时长） */
  function formatHours(seconds: number | null | undefined): string {
    if (!seconds || seconds <= 0) return '0 分钟';
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    if (h > 0) return `${h} 小时 ${m} 分`;
    return `${m} 分钟`;
  }

  function formatDate(date: string | null | undefined, pattern = 'YYYY-MM-DD'): string {
    if (!date) return '';
    return dayjs(date).format(pattern);
  }

  return { formatDuration, formatFileSize, formatHours, formatDate };
}
