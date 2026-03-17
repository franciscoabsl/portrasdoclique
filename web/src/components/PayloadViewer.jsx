import { useState } from 'react';

export function PayloadViewer({ detalhes }) {
  const [aberto, setAberto] = useState(false);

  if (!detalhes) return null;

  return (
    <div>
      <button
        onClick={() => setAberto(!aberto)}
        className="text-xs text-gray-400 hover:text-gray-600 transition-colors cursor-pointer bg-transparent border-0 p-0"
        style={{ fontFamily: "'Geist', sans-serif" }}
      >
        {aberto ? '↑ ocultar detalhes' : '↓ ver detalhes'}
      </button>

      {aberto && (
        <div
          className="mt-2 rounded-lg p-3 text-xs leading-relaxed border border-gray-100"
          style={{
            fontFamily: "'Geist Mono', monospace",
            backgroundColor: '#f9fafb',
          }}
        >
          {Object.entries(detalhes).map(([key, value]) => (
            <div key={key}>
              <span className="text-gray-400">"{key}"</span>
              <span className="text-gray-500">: </span>
              <span
                className={
                  typeof value === 'string' ? 'text-green-600' : 'text-blue-500'
                }
              >
                {typeof value === 'string' ? `"${value}"` : String(value)}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
