/**
 * 把数据库里的相对文件路径拼成前端可访问 URL。
 *
 * 后端把存储根目录映射成 /files/**，Vite 开发服务器再代理 /files 到 8080，
 * 所以前端不用知道真实磁盘路径，也不会遇到 CORS。
 */
export function fileUrl(path: string | null | undefined): string {
  if (!path) return '';
  if (path.startsWith('http://') || path.startsWith('https://')) return path;
  if (path.startsWith('/files/')) return path;
  if (path.startsWith('files/')) return `/${path}`;

  const clean = path.startsWith('/') ? path.slice(1) : path;
  return `/files/${clean}`;
}
