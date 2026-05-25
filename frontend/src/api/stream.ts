// 流式播放 URL 拼接（直接交给 <audio> / wavesurfer.js，不走 axios）
// 走 Vite 代理 → 后端 /api/stream/{id}
export const streamUrl = (trackId: number): string => `/api/stream/${trackId}`;
