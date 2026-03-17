import { TimelineCard } from './TimelineCard';

export function Timeline({ etapas, status }) {
  if (etapas.length === 0 && status === 'running') {
    return (
      <div
        className="flex items-center gap-2 py-4"
        style={{ fontFamily: "'Geist Mono', monospace" }}
      >
        <div className="w-2 h-2 rounded-full bg-gray-300 animate-pulse" />
        <span className="text-xs text-gray-400">aguardando resposta...</span>
      </div>
    );
  }

  return (
    <div className="mt-6">
      {etapas.map((etapa, index) => (
        <div
          key={`${etapa.numero}-${index}`}
          style={{
            animation: 'slideDown 0.35s ease',
          }}
        >
          <TimelineCard etapa={etapa} isLast={index === etapas.length - 1} />
        </div>
      ))}
    </div>
  );
}
