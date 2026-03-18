export function formatMs(ms) {
  if (ms < 1000) return `${ms}ms`;
  return `${(ms / 1000).toFixed(1)}s`;
}

export function formatTtl(seconds) {
  if (seconds < 60) return `${seconds}s`;
  const min = Math.floor(seconds / 60);
  const sec = seconds % 60;
  return `${min}m ${sec}s`;
}

export function formatTimestamp(iso) {
  if (!iso) return '';

  const date = new Date(iso);
  const agora = new Date();
  const diff = Math.floor((agora - date) / 1000);

  if (diff < 5) return 'agora';
  if (diff < 60) return `há ${diff}s`;
  if (diff < 3600) return `há ${Math.floor(diff / 60)}min`;
  if (diff < 86400) return `há ${Math.floor(diff / 3600)}h`;
  return `há ${Math.floor(diff / 86400)}d`;
}

export function formatCacheHitRate(rate) {
  return `${rate.toFixed(1)}%`;
}
