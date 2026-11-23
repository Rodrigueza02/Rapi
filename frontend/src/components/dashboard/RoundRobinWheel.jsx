/**
 * RoundRobinWheel.jsx
 * SRP: visualiza el anillo Round Robin con fotos reales de repartidores.
 * Los datos vienen del backend — React solo anima y posiciona.
 */

const DRIVER_PHOTOS = {
  'driver-1': '/images/drivers/rep-01.png',
  'driver-2': '/images/drivers/rep-02.png',
  'driver-3': '/images/drivers/rep-03.png',
  'driver-4': '/images/drivers/rep-04.png',
  'driver-5': '/images/drivers/rep-05.png',
};

function driverPhoto(driverId) {
  return DRIVER_PHOTOS[driverId] ?? null;
}

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

  const { drivers, next_driver_id } = roundRobin;
  const count  = drivers.length;
  const radius = count <= 3 ? 90 : 110;

  return (
    <div className="rr-wheel-wrap" aria-label="Rueda Round Robin">
      <div className="rr-wheel">

        {/* Nodo central */}
        <div className="rr-center">
          <span className="rr-center-icon">👑</span>
          <span className="rr-center-label">Round Robin</span>
          <span className="rr-center-sub">Asignación activa</span>
        </div>

        {/* Líneas SVG */}
        <svg className="rr-lines" aria-hidden="true">
          {drivers.map((_, i) => {
            const angle = (360 / count) * i - 90;
            const rad   = angle * (Math.PI / 180);
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

        {/* Nodos satélite */}
        {drivers.map((driverId, i) => {
          const angle    = (360 / count) * i - 90;
          const rad      = angle * (Math.PI / 180);
          const x        = Math.cos(rad) * radius;
          const y        = Math.sin(rad) * radius;
          const isActive = driverId === next_driver_id;
          const photo    = driverPhoto(driverId);

          return (
            <div
              key={driverId}
              className={`rr-node ${isActive ? 'rr-node--active' : ''}`}
              style={{ '--x': `${x}px`, '--y': `${y}px` }}
              title={isActive ? `Próxima asignación: ${driverId}` : driverId}
            >
              {photo ? (
                <img
                  src={photo}
                  alt={driverId}
                  className={`rr-node-photo ${isActive ? 'rr-node-photo--active' : ''}`}
                  onError={e => { e.currentTarget.style.display = 'none'; }}
                />
              ) : (
                <span className="rr-node-avatar">
                  {driverId[0]?.toUpperCase()}
                </span>
              )}
              <span className="rr-node-label">{driverId}</span>
            </div>
          );
        })}

      </div>
    </div>
  );
}
