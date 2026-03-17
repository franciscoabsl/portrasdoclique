import { useState, useRef } from 'react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export function useFluxo() {
  const [etapas, setEtapas] = useState([]);
  const [filme, setFilme] = useState(null);
  const [status, setStatus] = useState('idle');
  const sourceRef = useRef(null);

  const iniciar = (chaosMode = false) => {
    if (sourceRef.current) {
      sourceRef.current.close();
    }

    setStatus('running');
    setEtapas([]);
    setFilme(null);

    const url = `${API_URL}/api/fluxo/iniciar?chaos=${chaosMode}`;
    const source = new EventSource(url);
    sourceRef.current = source;

    source.addEventListener('etapa', (e) => {
      const etapa = JSON.parse(e.data);
      setEtapas((prev) => [...prev, etapa]);
    });

    source.addEventListener('resultado', (e) => {
      const resultado = JSON.parse(e.data);
      setFilme(resultado);
      setStatus('done');
      source.close();
    });

    source.onerror = () => {
      setStatus('error');
      source.close();
    };
  };

  const resetar = () => {
    if (sourceRef.current) {
      sourceRef.current.close();
    }
    setEtapas([]);
    setFilme(null);
    setStatus('idle');
  };

  return { etapas, filme, status, iniciar, resetar };
}
