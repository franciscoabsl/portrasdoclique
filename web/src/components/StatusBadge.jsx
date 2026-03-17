export function StatusBadge() {
  return (
    <div
      style={{ fontFamily: "'Geist Mono', monospace" }}
      className="inline-flex items-center gap-2 text-xs text-gray-400 border border-gray-200 rounded-full px-3 py-1"
    >
      <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
      demo ao vivo
    </div>
  );
}
