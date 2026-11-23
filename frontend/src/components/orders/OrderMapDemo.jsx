/**
 * OrderMapDemo.jsx
 * Vista demostrativa del recorrido del pedido.
 *
 * IMPORTANTE: Este componente NO muestra geolocalización real.
 * No hay integración GPS ni coordenadas del driver-simulator.
 * Es una ilustración SVG estática para la exposición universitaria.
 * La etiqueta "Vista demostrativa" es visible siempre.
 */

export default function OrderMapDemo({ state }) {
  /* Color de la ruta según estado del pedido */
  const routeColor = state === 'DELIVERED' ? '#10B981'
    : state === 'ARRIVING'                 ? '#F58232'
    : '#94A3B8';

  return (
    <div className="map-demo-wrap">

      {/* Etiqueta obligatoria de demostrativo */}
      <span className="map-demo-badge" aria-label="Vista demostrativa sin seguimiento GPS real">
        📍 Vista demostrativa — sin seguimiento GPS real
      </span>

      {/* Mapa SVG ilustrativo */}
      <svg
        className="map-demo-svg"
        viewBox="0 0 400 220"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
        role="img"
      >
        {/* Fondo tipo mapa */}
        <rect width="400" height="220" fill="#E8F0E8" />

        {/* Bloques de ciudad simulados */}
        <rect x="20"  y="20"  width="80" height="50"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />
        <rect x="120" y="20"  width="60" height="40"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />
        <rect x="200" y="15"  width="90" height="55"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />
        <rect x="310" y="20"  width="70" height="45"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />
        <rect x="20"  y="100" width="70" height="60"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />
        <rect x="110" y="110" width="80" height="50"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />
        <rect x="210" y="100" width="75" height="55"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />
        <rect x="310" y="105" width="70" height="55"  rx="4" fill="#D4E6D4" stroke="#B8D4B8" strokeWidth="1" />

        {/* Calles */}
        <rect x="0"   y="80"  width="400" height="10" fill="#FFFFFF" opacity="0.7" />
        <rect x="0"   y="170" width="400" height="10" fill="#FFFFFF" opacity="0.7" />
        <rect x="100" y="0"   width="10"  height="220" fill="#FFFFFF" opacity="0.7" />
        <rect x="200" y="0"   width="10"  height="220" fill="#FFFFFF" opacity="0.7" />
        <rect x="300" y="0"   width="10"  height="220" fill="#FFFFFF" opacity="0.7" />

        {/* Ruta punteada del recorrido */}
        <path
          d="M 55 160 C 55 85 105 85 155 85 C 205 85 205 50 250 50 C 295 50 345 50 345 60"
          fill="none"
          stroke={routeColor}
          strokeWidth="3"
          strokeDasharray="8 5"
          strokeLinecap="round"
          opacity="0.85"
        />

        {/* Pin origen (tienda) */}
        <circle cx="55" cy="162" r="12" fill="#F58232" />
        <text x="55" y="167" textAnchor="middle" fontSize="12" fill="white">🏪</text>

        {/* Pin destino */}
        <circle cx="345" cy="58" r="12" fill="#EF4444" />
        <text x="345" y="63" textAnchor="middle" fontSize="12" fill="white">📦</text>

        {/* Icono del repartidor sobre la ruta — posición según estado */}
        {state === 'ARRIVING' && (
          <>
            <circle cx="200" cy="85" r="14" fill="white" stroke={routeColor} strokeWidth="2" />
            <text x="200" y="90" textAnchor="middle" fontSize="13">🛵</text>
          </>
        )}
        {state === 'ASSIGNED' && (
          <>
            <circle cx="90" cy="85" r="14" fill="white" stroke={routeColor} strokeWidth="2" />
            <text x="90" y="90" textAnchor="middle" fontSize="13">🛵</text>
          </>
        )}
        {state === 'DELIVERED' && (
          <>
            <circle cx="345" cy="58" r="14" fill="#D1FAE5" stroke="#10B981" strokeWidth="2" />
            <text x="345" y="63" textAnchor="middle" fontSize="13">✅</text>
          </>
        )}
      </svg>
    </div>
  );
}
