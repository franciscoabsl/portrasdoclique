export function ChaosToggle({ enabled, onChange }) {
  return (
    <div className="flex items-center gap-2">
      <button
        onClick={() => onChange(!enabled)}
        className={`relative w-8 h-4.5 rounded-full transition-colors duration-200 border-0 cursor-pointer ${
          enabled ? 'bg-red-400' : 'bg-gray-200'
        }`}
        style={{ width: '32px', height: '18px' }}
      >
        <span
          className="absolute top-0.5 w-3 h-3 rounded-full bg-white transition-all duration-200"
          style={{ left: enabled ? '16px' : '3px' }}
        />
      </button>
      <span
        style={{ fontFamily: "'Geist', sans-serif" }}
        className="text-xs text-gray-400"
      >
        Modo caos
      </span>
    </div>
  );
}
