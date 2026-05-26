/**
 * 主动释放某个容器内的 WebGL canvas。
 *
 * vue-bits 组件本身会做基础清理；包装层额外调用 WEBGL_lose_context，
 * 是为了在路由切换时尽快释放浏览器 WebGL 上下文，避免上下文数量累积。
 */
export function releaseWebglContexts(root: HTMLElement | null | undefined) {
  if (!root) return;

  root.querySelectorAll('canvas').forEach((canvas) => {
    const context = (
      canvas.getContext('webgl2') ||
      canvas.getContext('webgl') ||
      canvas.getContext('experimental-webgl')
    ) as WebGLRenderingContext | WebGL2RenderingContext | null;

    context
      ?.getExtension('WEBGL_lose_context')
      ?.loseContext();
  });
}
