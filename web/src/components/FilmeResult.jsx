import { formatMs } from '../utils/formatters';

export function FilmeResult({ filme, onReset }) {
  if (!filme) return null;

  return (
    <div
      className="mt-4 border border-gray-100 rounded-xl overflow-hidden"
      style={{
        fontFamily: "'Geist', sans-serif",
        animation: 'slideDown 0.4s ease',
      }}
    >
      <div className="flex gap-4 p-4">
        <div
          className="w-18 h-28 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0"
          style={{ width: '72px', height: '108px' }}
        >
          {filme.posterUrl && (
            <img
              src={filme.posterUrl}
              alt={filme.titulo}
              className="w-full h-full object-cover"
              onError={(e) => (e.target.style.display = 'none')}
            />
          )}
        </div>

        <div className="flex-1 min-w-0">
          <h3 className="text-sm font-medium text-gray-900 mb-0.5">
            {filme.titulo}
          </h3>
          <p className="text-xs text-gray-400 mb-2">
            {filme.dataLancamento?.substring(0, 4)}
            {filme.nota && ` · ★ ${filme.nota.toFixed(1)}`}
            {filme.rottenTomatoes && ` · RT ${filme.rottenTomatoes}`}
          </p>
          <p className="text-xs text-gray-500 leading-relaxed line-clamp-3 mb-3">
            {filme.sinopse}
          </p>
        </div>
      </div>

      <div
        className="flex items-center justify-between px-4 py-2.5 border-t border-gray-100"
        style={{ fontFamily: "'Geist Mono', monospace" }}
      >
        <div className="flex items-center gap-2 text-xs text-gray-400">
          <span className="text-green-500">200 OK</span>
          <span>·</span>
          <span>{formatMs(filme.tempoTotalMs)}</span>
          <span>·</span>
          <span>{filme.fonte}</span>
        </div>

        <button
          onClick={onReset}
          className="text-xs text-gray-400 hover:text-gray-600 transition-colors cursor-pointer bg-transparent border-0"
        >
          tentar novamente →
        </button>
      </div>
    </div>
  );
}
