import { useState, useEffect } from 'react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export function useMetricas() {
  const [metricas, setMetricas] = useState(null);

  useEffect(() => {
    const source = new EventSource(`${API_URL}/api/metricas/stream`);

    source.addEventListener('metricas', (e) => {
      setMetricas(JSON.parse(e.data));
    });

    source.addEventListener('connected', () => {
      console.log('SSE métricas conectado');
    });

    source.onerror = () => {
      source.close();
    };

    return () => source.close();
  }, []);

  return metricas;
}
