import { PayloadViewer } from './PayloadViewer';
import { formatMs } from '../utils/formatters';

const statusConfig = {
  ok: { dot: 'bg-green-400', border: 'border-gray-100' },
  error: { dot: 'bg-red-400', border: 'border-red-100' },
  loading: { dot: 'bg-gray-300 animate-pulse', border: 'border-gray-100' },
};

export function TimelineCard({ etapa, isLast }) {
  const config = statusConfig[etapa.status] || statusConfig.ok;

  return (
    <div className="flex gap-4">
      <div className="flex flex-col items-center">
        <div className={`w-2 h-2 rounded-full mt-1.5 shrink-0 ${config.dot}`} />
        {!isLast && <div className="w-px flex-1 bg-gray-100 mt-1" />}
      </div>

      <div
        className={`flex-1 border rounded-xl p-3 mb-2 transition-colors hover:border-gray-200 ${config.border}`}
        style={{ fontFamily: "'Geist', sans-serif" }}
      >
        <div className="flex items-center justify-between mb-1">
          <span className="text-sm font-medium text-gray-800">
            {etapa.nome}
          </span>
          <span
            className="text-xs text-gray-400"
            style={{ fontFamily: "'Geist Mono', monospace" }}
          >
            {formatMs(etapa.tempoMs)}
          </span>
        </div>

        <div
          className="text-xs text-gray-400 mb-2"
          style={{ fontFamily: "'Geist Mono', monospace" }}
        >
          {etapa.resumo}
        </div>

        <PayloadViewer detalhes={etapa.detalhes} />
      </div>
    </div>
  );
}
