/**
 * Rueda Round Robin — visualiza el anillo de repartidores.
 *
 * Los datos vienen del backend (useRoundRobin).
 * React SOLO anima y posiciona — no calcula el próximo turno.
 *
 * Layout: círculos satélite alrededor de un círculo central.
 * El repartidor activo (next_driver_id) se resalta.
 */
export default function RoundRobinWheel({ roundRobin }) {
  if (!roundRobin || roundRobin.drivers.length === 0) {
    return (
      <div className="rr-empty">
        <span className="rr-empty-icon">🔄</span>
        <p>Sin repartidores registrados aún.</p>
        <small>Aparecerán cuando se procesen los primeros pedidos.</small>
      </div>
    );
  }

  const { drivers, current_index, next_driver_id } = roundRobin;
  const count  = drivers.length;

  return (
    <div className="rr-wheel-wrap" aria-label="Rueda Round Robin">
      <div className="rr-wheel">
        {/* Nodo central */}
        <div className="rr-center">
          <span className="rr-center-icon">👑</span>
          <span className="rr-center-label">Round Robin</span>
          <span className="rr-center-sub">Asignación activa</span>
        </div>

        {/* Nodos satélite */}
        {drivers.map((driverId, i) => {
          const angle  = (360 / count) * i - 90;   // -90 para empezar arriba
          const rad    = angle * (Math.PI / 180);
          const radius = count <= 3 ? 90 : 110;    // px desde el centro
          const x = Math.cos(rad) * radius;
          const y = Math.sin(rad) * radius;
          const isActive = driverId === next_driver_id;
          const isCurrent = i === current_index;

          return (
            <div
              key={driverId}
              className={`rr-node ${isActive ? 'rr-node--active' : ''} ${isCurrent ? 'rr-node--current' : ''}`}
              style={{ '--x': `${x}px`, '--y': `${y}px` }}
              title={isActive ? `Próxima asignación: ${driverId}` : driverId}
              aria-label={`Repartidor ${driverId}${isActive ? ' — próxima asignación' : ''}`}
            >
              <span className="rr-node-avatar" aria-hidden="true">
                {driverId[0]}
              </span>
              <span className="rr-node-label">{driverId}</span>
            </div>
          );
        })}

        {/* Líneas de conexión SVG */}
        <svg className="rr-lines" aria-hidden="true">
          {drivers.map((_, i) => {
            const angle  = (360 / count) * i - 90;
            const rad    = angle * (Math.PI / 180);
            const radius = count <= 3 ? 90 : 110;
            return (
              <line
                key={i}
                x1="50%" y1="50%"
                x2={`calc(50% + ${Math.cos(rad) * radius}px)`}
                y2={`calc(50% + ${Math.sin(rad) * radius}px)`}
                className="rr-line"
              />
            );
          })}
        </svg>
      </div>
    </div>
  );
}
