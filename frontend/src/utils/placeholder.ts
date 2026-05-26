export type PlaceholderType = 'album' | 'artist' | 'playlist' | 'track';

const PLACEHOLDER_COLORS: Record<PlaceholderType, [string, string]> = {
  album: ['#F7F4EA', '#A7B1C0'],
  artist: ['#F4F5F7', '#D4C5A0'],
  playlist: ['#E5E7EB', '#C8A862'],
  track: ['#F4F5F7', '#D4C5A0'],
};

export function placeholderInitial(name: string): string {
  const trimmed = name.trim();
  if (!trimmed) return '?';

  if (/[\u4e00-\u9fa5]/.test(trimmed[0])) {
    return trimmed[0];
  }

  const words = trimmed.split(/\s+/).filter(Boolean);
  if (words.length >= 2 && words[0][0] && words[1][0]) {
    return `${words[0][0]}${words[1][0]}`.toUpperCase();
  }

  return trimmed.substring(0, 2).toUpperCase();
}

export function placeholderGradient(name: string, type: PlaceholderType): string {
  const angle = simpleHash(name) % 360;
  const [from, to] = PLACEHOLDER_COLORS[type];
  return `linear-gradient(${angle}deg, ${from} 0%, #94A3B8 48%, ${to} 100%)`;
}

export function placeholderImage(name: string, type: PlaceholderType): string {
  const safeName = name.trim() || '?';
  const initial = escapeSvgText(placeholderInitial(safeName));
  const angle = simpleHash(safeName) % 360;
  const [from, to] = PLACEHOLDER_COLORS[type];
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="600" height="600" viewBox="0 0 600 600">
      <defs>
        <linearGradient id="g" gradientTransform="rotate(${angle})">
          <stop offset="0%" stop-color="${from}"/>
          <stop offset="48%" stop-color="#94A3B8"/>
          <stop offset="100%" stop-color="${to}"/>
        </linearGradient>
        <radialGradient id="vignette" cx="50%" cy="50%" r="60%">
          <stop offset="0%" stop-color="rgba(0,0,0,0)"/>
          <stop offset="100%" stop-color="rgba(0,0,0,0.26)"/>
        </radialGradient>
        <linearGradient id="sheen" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="rgba(255,255,255,0.24)"/>
          <stop offset="45%" stop-color="rgba(255,255,255,0)"/>
          <stop offset="100%" stop-color="rgba(200,168,98,0.18)"/>
        </linearGradient>
      </defs>
      <rect width="600" height="600" fill="url(#g)"/>
      <rect width="600" height="600" fill="url(#sheen)"/>
      <rect width="600" height="600" fill="url(#vignette)"/>
      <text x="50%" y="50%"
            font-family="Inter, 'PingFang SC', system-ui, sans-serif"
            font-size="240"
            font-weight="600"
            fill="#0A0A0B"
            fill-opacity="0.65"
            text-anchor="middle"
            dominant-baseline="central">${initial}</text>
    </svg>
  `.trim();

  return `data:image/svg+xml;base64,${base64EncodeUtf8(svg)}`;
}

export function simpleHash(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i += 1) {
    hash = ((hash << 5) - hash + str.charCodeAt(i)) | 0;
  }
  return Math.abs(hash);
}

function escapeSvgText(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

function base64EncodeUtf8(text: string): string {
  return btoa(unescape(encodeURIComponent(text)));
}
