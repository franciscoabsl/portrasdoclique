import { StatusBadge } from './StatusBadge';
import { ChaosToggle } from './ChaosToggle';

export function Hero({ onIniciar, chaosMode, onChaosChange }) {
  return (
    <div
      className="max-w-xl mx-auto px-6 pt-16 pb-12"
      style={{ fontFamily: "'Geist', sans-serif" }}
    >
      <StatusBadge />

      <h1
        className="mt-6 text-4xl font-light leading-tight tracking-tight text-gray-900"
        style={{ letterSpacing: '-0.02em' }}
      >
        Por Trás
        <br />
        <span className="text-gray-400">do Clique</span>
      </h1>

      <p className="mt-5 text-sm leading-relaxed text-gray-500">
        Você está prestes a fazer algo simples: clicar em um botão. Mas, por
        trás de cada ação que você faz na web, existe um mundo em movimento que
        a maioria das pessoas nunca vê. Servidores acordando, caches sendo
        consultados, filas processando eventos, circuit breakers protegendo o
        sistema — tudo orquestrado em frações de segundo antes da resposta
        chegar até você. Clique no botão abaixo e acompanhe essa jornada em
        tempo real. Sem abstração. Sem simulação. O sistema real, exposto.
      </p>

      <p className="mt-3 text-sm leading-relaxed text-gray-500">
        Isso é o back-end. E você está prestes a vê-lo funcionar ao vivo.
      </p>

      <div className="mt-8 flex items-center gap-4 flex-wrap">
        <button
          onClick={onIniciar}
          className="bg-gray-900 text-white text-sm px-5 py-2.5 rounded-lg hover:bg-gray-700 transition-colors cursor-pointer border-0"
          style={{ fontFamily: "'Geist', sans-serif" }}
        >
          Fazer o clique
        </button>
        <ChaosToggle enabled={chaosMode} onChange={onChaosChange} />
      </div>

      <div className="mt-8 pt-6 border-t border-gray-100 grid grid-cols-4 gap-4">
        {[
          { label: 'Stack', value: 'Spring Boot 3' },
          { label: 'Banco', value: 'PostgreSQL' },
          { label: 'Cache', value: 'Redis' },
          { label: 'Fila', value: 'RabbitMQ' },
        ].map((item) => (
          <div key={item.label}>
            <p className="text-xs text-gray-400">{item.label}</p>
            <p
              className="text-xs font-medium text-gray-600 mt-0.5"
              style={{ fontFamily: "'Geist Mono', monospace" }}
            >
              {item.value}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
