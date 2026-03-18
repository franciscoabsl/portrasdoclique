import { formatMs, formatTimestamp } from '../utils/formatters';

export function Dashboard({ metricas }) {
  if (!metricas) return null;

  return (
    <div
      className="mt-8 border border-gray-100 rounded-xl p-4"
      style={{ fontFamily: "'Geist', sans-serif" }}
    >
      <div className="flex items-center justify-between mb-4">
        <span className="text-xs font-medium text-gray-500">
          O sistema agora
        </span>
        <div className="flex items-center gap-1.5">
          <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
          <span
            className="text-xs text-gray-400"
            style={{ fontFamily: "'Geist Mono', monospace" }}
          >
            ao vivo
          </span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-3 mb-4">
        <div className="bg-gray-50 rounded-lg p-3">
          <p className="text-xs text-gray-400 mb-1">total cliques</p>
          <p className="text-xl font-medium text-gray-800">
            {metricas.totalCliques ?? 0}
          </p>
        </div>
        <div className="bg-gray-50 rounded-lg p-3">
          <p className="text-xs text-gray-400 mb-1">cache hit rate</p>
          <p className="text-xl font-medium text-gray-800">
            {metricas.cacheHitRate?.toFixed(1) ?? 0}%
          </p>
        </div>
        <div className="bg-gray-50 rounded-lg p-3">
          <p className="text-xs text-gray-400 mb-1">tempo médio</p>
          <p className="text-xl font-medium text-gray-800">
            {metricas.tempoMedioMs
              ? formatMs(Math.round(metricas.tempoMedioMs))
              : '—'}
          </p>
        </div>
      </div>

      {metricas.topFilmes?.length > 0 && (
        <div className="mb-4">
          <p className="text-xs text-gray-400 mb-2">filmes mais buscados</p>
          <div className="space-y-1.5">
            {metricas.topFilmes.map((filme, index) => (
              <div key={filme.titulo} className="flex items-center gap-2">
                <span
                  className="text-xs text-gray-300 w-3"
                  style={{ fontFamily: "'Geist Mono', monospace" }}
                >
                  {index + 1}
                </span>
                <div className="flex-1 bg-gray-100 rounded-full h-1.5 overflow-hidden">
                  <div
                    className="h-full bg-gray-300 rounded-full"
                    style={{
                      width: `${Math.min((filme.total / metricas.topFilmes[0].total) * 100, 100)}%`,
                    }}
                  />
                </div>
                <span className="text-xs text-gray-500 truncate max-w-32">
                  {filme.titulo}
                </span>
                <span
                  className="text-xs text-gray-400"
                  style={{ fontFamily: "'Geist Mono', monospace" }}
                >
                  {filme.total}x
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {metricas.buscasRecentes?.length > 0 && (
        <div>
          <p className="text-xs text-gray-400 mb-2">buscas recentes</p>
          <div className="space-y-1">
            {metricas.buscasRecentes.slice(0, 5).map((busca, index) => (
              <div
                key={index}
                className="flex items-center justify-between text-xs"
                style={{ fontFamily: "'Geist Mono', monospace" }}
              >
                <span className="text-gray-400">
                  {formatTimestamp(busca.timestamp)}
                </span>
                <span className="text-gray-600 truncate mx-2 flex-1">
                  {busca.titulo}
                </span>
                <span
                  className={`${busca.cacheHit ? 'text-green-500' : 'text-gray-400'}`}
                >
                  {busca.cacheHit
                    ? 'CACHE'
                    : busca.fallbackUsed
                      ? 'FALLBACK'
                      : formatMs(busca.tempoMs)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
