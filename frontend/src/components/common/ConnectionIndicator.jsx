/**
 * ConnectionIndicator.jsx
 * Muestra el estado real de la conexión WebSocket.
 *
 * Distingue con precisión:
 *   open        → WebSocket activo, tiempo real disponible
 *   connecting  → intentando conectar al WebSocket
 *   closed      → WebSocket no conectado (REST puede seguir funcionando)
 *   error       → error en la conexión WebSocket
 *
 * No dice «Sistema en línea» si el WebSocket no está conectado.
 * No dice «Sin conexión» si solo falla el WebSocket pero REST funciona.
 */
export default function ConnectionIndicator({ status }) {
  const map = {
    open:       { label: 'Sistema en línea',          cls: 'conn--open'        },
    connecting: { label: 'Conectando...',              cls: 'conn--connecting'  },
    closed:     { label: 'Tiempo real no disponible', cls: 'conn--closed'      },
    error:      { label: 'Error de conexión',          cls: 'conn--error'       },
  };

  const { label, cls } = map[status] ?? map.closed;

  return (
    <div className={`conn-indicator ${cls}`} role="status" aria-live="polite">
      <span className="conn-dot" aria-hidden="true" />
      {label}
    </div>
  );
}
