import { useState } from 'react';
import { Hero } from './components/Hero';
import { Timeline } from './components/Timeline';
import { FilmeResult } from './components/FilmeResult';
import { Dashboard } from './components/Dashboard';
import { useFluxo } from './hooks/useFluxo';
import { useMetricas } from './hooks/useMetricas';

export default function App() {
  const [chaosMode, setChaosMode] = useState(false);
  const [tela, setTela] = useState('hero');
  const { etapas, filme, status, iniciar, resetar } = useFluxo();
  const metricas = useMetricas();

  const handleIniciar = () => {
    setTela('fluxo');
    iniciar(chaosMode);
  };

  const handleReset = () => {
    resetar();
    setTela('hero');
  };

  return (
    <div className="min-h-screen bg-white">
      <style>{`
        @keyframes slideDown {
          from { opacity: 0; transform: translateY(-8px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>

      {tela === 'hero' && (
        <Hero
          onIniciar={handleIniciar}
          chaosMode={chaosMode}
          onChaosChange={setChaosMode}
        />
      )}

      {tela === 'fluxo' && (
        <div
          className="max-w-xl mx-auto px-6 pt-10 pb-12"
          style={{ fontFamily: "'Geist', sans-serif" }}
        >
          <div className="flex items-center justify-between mb-2">
            <span
              className="text-xs text-gray-400"
              style={{ fontFamily: "'Geist Mono', monospace" }}
            >
              GET /api/fluxo/iniciar{chaosMode ? '?chaos=true' : ''}
            </span>
            <span
              className="text-xs text-gray-400"
              style={{ fontFamily: "'Geist Mono', monospace" }}
            >
              {status === 'running'
                ? '⏳ processando...'
                : status === 'done'
                  ? '✓ concluído'
                  : '✗ erro'}
            </span>
          </div>

          <Timeline etapas={etapas} status={status} />

          {filme && <FilmeResult filme={filme} onReset={handleReset} />}

          {status === 'done' && metricas && <Dashboard metricas={metricas} />}

          {status === 'error' && (
            <div className="mt-4 text-sm text-red-400 text-center">
              Algo deu errado.{' '}
              <button
                onClick={handleReset}
                className="underline cursor-pointer bg-transparent border-0 text-red-400"
              >
                Tentar novamente
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
