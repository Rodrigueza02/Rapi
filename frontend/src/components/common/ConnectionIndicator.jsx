/**
 * Indicador de estado de conexión WebSocket.
 * Muestra "Sistema en línea" / "Reconectando..." / "Sin conexión"
 */
export default function ConnectionIndicator({ status }) {
  const map = {
    open:        { label: 'Sistema en línea',  cls: 'conn--open'        },
    connecting:  { label: 'Conectando...',     cls: 'conn--connecting'  },
    closed:      { label: 'Sin conexión',      cls: 'conn--closed'      },
    error:       { label: 'Error de conexión', cls: 'conn--error'       },
  };

  const { label, cls } = map[status] ?? map.closed;

  return (
    <div className={`conn-indicator ${cls}`} role="status" aria-live="polite">
      <span className="conn-dot" aria-hidden="true" />
      {label}
    </div>
  );
}
