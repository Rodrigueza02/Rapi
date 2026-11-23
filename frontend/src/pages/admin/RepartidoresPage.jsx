/**
 * RepartidoresPage.jsx
 *
 * Módulo de gestión de repartidores — Fase E de DeliveryFlow.
 *
 * Datos reales disponibles (derivados de /api/dashboard/drivers y /api/dashboard/round-robin):
 *   - driver_id, status (EN_RUTA | DESCONOCIDO), active_order_id,
 *     completed_orders, total_orders, last_activity
 *
 * Datos que NO existen en el backend y no se inventan aquí:
 *   - nombre, zona, calificación, foto, disponibilidad precisa, estado conectado/desconectado
 *
 * El botón "Registrar repartidor" es visual; al presionarlo informa
 * honestamente que el registro requiere endpoint de backend.
 */

import { useCallback, useMemo, useState } from 'react';
import {
  Bike, Eye, Info, Plus, RefreshCw, RotateCcw,
  Search, SlidersHorizontal, Users, Zap,
} from 'lucide-react';

import Modal        from '../../components/common/Modal.jsx';
import useDrivers   from '../../hooks/useDrivers.js';
import useWebSocket from '../../hooks/useWebSocket.js';

import '../../styles/pages/repartidores.css';

/* ── Constantes ──────────────────────────────────────────────── */
const PAGE_SIZE = 8;

const STATUS_OPTIONS = [
  { value: 'TODOS',       label: 'Todos' },
  { value: 'EN_RUTA',     label: 'En ruta' },
  { value: 'DESCONOCIDO', label: 'Estado desconocido' },
];

/* ── Helpers ─────────────────────────────────────────────────── */

/** Iniciales del ID para el avatar genérico: "driver-1" → "D1" */
function avatarInitials(driverId) {
  if (!driverId) return '?';
  const parts = driverId.split('-').filter(Boolean);
  if (parts.length >= 2) {
    return (parts[0][0] + parts[parts.length - 1]).toUpperCase().slice(0, 2);
  }
  return driverId.slice(0, 2).toUpperCase();
}

/** Formatea un timestamp ISO a "Hoy, HH:MM" / "Ayer, HH:MM" / fecha corta */
function formatActivity(isoString) {
  if (!isoString) return null;
  try {
    const d   = new Date(isoString);
    const now = new Date();
    const hm  = d.toLocaleTimeString('es', { hour: '2-digit', minute: '2-digit' });

    const startOfToday     = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startOfYesterday = new Date(startOfToday - 86_400_000);

    if (d >= startOfToday)     return `Hoy, ${hm}`;
    if (d >= startOfYesterday) return `Ayer, ${hm}`;

    return d.toLocaleDateString('es', { day: '2-digit', month: 'short' }) + `, ${hm}`;
  } catch {
    return null;
  }
}

/** Posición (1-based) del repartidor en el anillo, o null */
function rrPosition(driverId, roundRobin) {
  if (!roundRobin?.drivers) return null;
  const idx = roundRobin.drivers.indexOf(driverId);
  return idx === -1 ? null : idx + 1;
}

/* ══════════════════════════════════════════════════════════════
   COMPONENTES INTERNOS
   ══════════════════════════════════════════════════════════════ */

/* ── Tarjeta de estadística ──────────────────────────────────── */
function DriverStatCard({ label, value, icon: Icon, color, note }) {
  return (
    <article className={`rep-stat-card rep-stat-card--${color}`}>
      <div className="rep-stat-top">
        <span className="rep-stat-label">{label}</span>
        <span className={`rep-stat-icon rep-stat-icon--${color}`}>
          <Icon size={20} />
        </span>
      </div>
      <div className="rep-stat-value">{value}</div>
      {note && <p className="rep-stat-note">{note}</p>}
    </article>
  );
}

/* ── Badge de estado ─────────────────────────────────────────── */
function StatusBadge({ status }) {
  if (status === 'EN_RUTA') {
    return (
      <span className="rep-status-badge rep-status-badge--en-ruta">
        <span className="rep-status-dot" />
        🏍 En ruta
      </span>
    );
  }
  return (
    <span className="rep-status-badge rep-status-badge--desconocido">
      <span className="rep-status-dot" />
      Estado desconocido
    </span>
  );
}

/* ── Modal: detalle del repartidor ───────────────────────────── */
function DriverDetailModal({ driver, roundRobin, onClose }) {
  const pos      = rrPosition(driver.driver_id, roundRobin);
  const isNext   = roundRobin?.next_driver_id === driver.driver_id;
  const activity = formatActivity(driver.last_activity);

  return (
    <Modal title="Detalle del repartidor" onClose={onClose} size="md">
      {/* Cabecera con avatar */}
      <div className="rep-modal-avatar-row">
        <div className="rep-modal-avatar">{avatarInitials(driver.driver_id)}</div>
        <div>
          <div className="rep-modal-driver-id">{driver.driver_id}</div>
          <div className="rep-modal-driver-hint">
            Repartidor conocido por el sistema
          </div>
        </div>
      </div>

      {/* Datos derivados del backend */}
      <div className="rep-modal-grid">
        <div className="rep-modal-field">
          <span className="rep-modal-field-label">ID del repartidor</span>
          <span className="rep-modal-field-value">{driver.driver_id}</span>
        </div>

        <div className="rep-modal-field">
          <span className="rep-modal-field-label">Estado</span>
          <StatusBadge status={driver.status} />
        </div>

        <div className="rep-modal-field">
          <span className="rep-modal-field-label">Pedidos completados</span>
          <span className="rep-modal-field-value">{driver.completed_orders}</span>
        </div>

        <div className="rep-modal-field">
          <span className="rep-modal-field-label">Total asignados</span>
          <span className="rep-modal-field-value">{driver.total_orders}</span>
        </div>

        <div className="rep-modal-field">
          <span className="rep-modal-field-label">Pedido activo</span>
          {driver.active_order_id
            ? <span className="rep-modal-field-value">{driver.active_order_id}</span>
            : <span className="rep-modal-field-value rep-modal-field-value--na">Ninguno</span>
          }
        </div>

        <div className="rep-modal-field">
          <span className="rep-modal-field-label">Estado del pedido</span>
          {driver.active_order_state
            ? <span className="rep-modal-field-value">{driver.active_order_state}</span>
            : <span className="rep-modal-field-value rep-modal-field-value--na">—</span>
          }
        </div>

        <div className="rep-modal-field">
          <span className="rep-modal-field-label">Última actividad</span>
          {activity
            ? <span className="rep-modal-field-value">{activity}</span>
            : <span className="rep-modal-field-value rep-modal-field-value--na">Sin registros</span>
          }
        </div>

        <div className="rep-modal-field">
          <span className="rep-modal-field-label">Nombre</span>
          <span className="rep-modal-field-value rep-modal-field-value--na">No disponible</span>
        </div>
      </div>

      {/* Datos de Round Robin */}
      <div className="rep-modal-rr">
        <div className="rep-modal-rr-title">
          <RotateCcw size={15} />
          Asignación Round Robin
        </div>
        <div className="rep-modal-rr-row">
          <span>Posición en el anillo</span>
          <strong>{pos !== null ? `${pos} / ${roundRobin.drivers.length}` : 'No en anillo'}</strong>
        </div>
        <div className="rep-modal-rr-row">
          <span>Próximo en recibir pedido</span>
          <strong>{isNext ? '✅ Sí' : 'No'}</strong>
        </div>
        <div className="rep-modal-rr-row">
          <span>Turno actual del sistema</span>
          <strong>{roundRobin?.turn_label ?? '—'}</strong>
        </div>
      </div>

      {/* Aviso datos no disponibles */}
      <div className="rep-error" style={{ marginTop: 14, background: '#F0F9FF', borderColor: '#BAE6FD', color: '#0369A1' }}>
        <Info size={15} style={{ flexShrink: 0 }} />
        Nombre, zona, calificación y foto no existen en el backend actual. Se muestran
        únicamente los datos que el sistema puede garantizar.
      </div>
    </Modal>
  );
}

/* ── Modal: Registrar repartidor (informativo) ───────────────── */
function RegisterDriverModal({ onClose }) {
  return (
    <Modal title="Registrar repartidor" onClose={onClose} size="sm">
      <div className="rep-register-modal">
        <div className="rep-register-modal-icon">
          <Bike size={32} />
        </div>
        <h3>Función no disponible aún</h3>
        <p>
          El sistema no cuenta con un endpoint de registro de repartidores.
          Los repartidores actuales (<strong>driver-1</strong> a <strong>driver-4</strong>)
          están configurados estáticamente en <code>order-processor</code>.
        </p>
        <div className="rep-register-proposal">
          <strong>Para habilitar el registro se necesita:</strong><br />
          1. Entidad <code>Driver</code> en PostgreSQL con nombre, zona, estado.<br />
          2. <code>POST /api/drivers</code> en un microservicio.<br />
          3. Integrar ese ID en <code>app.drivers</code> del order-processor.<br /><br />
          Solicita autorización al administrador del proyecto para implementar
          esta funcionalidad de backend.
        </div>
      </div>
    </Modal>
  );
}

/* ══════════════════════════════════════════════════════════════
   COMPONENTE PRINCIPAL
   ══════════════════════════════════════════════════════════════ */
export default function RepartidoresPage() {
  /* ── Datos ────────────────────────────────────────────────── */
  const { drivers, roundRobin, loading, error, reload } = useDrivers();

  /* ── WebSocket: recarga cuando llega un evento real ──────── */
  const onWsEvent = useCallback(() => { reload(); }, [reload]);
  useWebSocket(onWsEvent);

  /* ── UI state ─────────────────────────────────────────────── */
  const [search,         setSearch]         = useState('');
  const [filterStatus,   setFilterStatus]   = useState('TODOS');
  const [page,           setPage]           = useState(1);
  const [detailDriver,   setDetailDriver]   = useState(null);
  const [showRegister,   setShowRegister]   = useState(false);

  /* ── Estadísticas derivadas ───────────────────────────────── */
  const totalDrivers    = drivers.length;
  const enRutaCount     = drivers.filter(d => d.status === 'EN_RUTA').length;
  const completedTotal  = drivers.reduce((s, d) => s + d.completed_orders, 0);
  const nextDriver      = roundRobin?.next_driver_id ?? null;

  /* ── Filtrado + búsqueda ─────────────────────────────────── */
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return drivers.filter(d => {
      const matchSearch = !q || d.driver_id.toLowerCase().includes(q);
      const matchStatus = filterStatus === 'TODOS' || d.status === filterStatus;
      return matchSearch && matchStatus;
    });
  }, [drivers, search, filterStatus]);

  /* ── Paginación ───────────────────────────────────────────── */
  const totalPages  = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage    = Math.min(page, totalPages);
  const pageDrivers = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  function handleSearch(val) {
    setSearch(val);
    setPage(1);
  }
  function handleFilterStatus(val) {
    setFilterStatus(val);
    setPage(1);
  }

  /* ── Render ───────────────────────────────────────────────── */
  return (
    <div className="repartidores-page">

      {/* ── Tarjetas estadísticas ─────────────────────────────── */}
      <div className="rep-stats">
        <DriverStatCard
          label="Repartidores en anillo"
          value={loading ? '…' : totalDrivers}
          icon={Users}
          color="orange"
          note={
            totalDrivers === 0 && !loading
              ? 'Sin pedidos desde el último reinicio'
              : 'Repartidores con al menos un pedido asignado'
          }
        />
        <DriverStatCard
          label="En ruta ahora"
          value={loading ? '…' : enRutaCount}
          icon={Bike}
          color="green"
          note="Pedido en estado ASSIGNED o ARRIVING"
        />
        <DriverStatCard
          label="Pedidos completados"
          value={loading ? '…' : completedTotal}
          icon={Zap}
          color="blue"
          note="Total de entregas DELIVERED en el sistema"
        />
        <DriverStatCard
          label="Próximo turno"
          value={loading ? '…' : (nextDriver ?? '—')}
          icon={RotateCcw}
          color={nextDriver ? 'orange' : 'red'}
          note={
            nextDriver
              ? `Turno: ${roundRobin?.turn_label ?? '—'}`
              : 'Sin asignaciones desde el reinicio'
          }
        />
      </div>

      {/* ── Barra de herramientas ─────────────────────────────── */}
      <div className="rep-toolbar">
        {/* Búsqueda */}
        <div className="rep-search-wrap">
          <Search size={15} className="rep-search-icon" />
          <input
            type="search"
            className="rep-search"
            placeholder="Buscar por ID del repartidor..."
            value={search}
            onChange={e => handleSearch(e.target.value)}
            aria-label="Buscar repartidor"
          />
        </div>

        {/* Filtro disponibilidad */}
        <div className="rep-filter-group">
          <span className="rep-filter-label">
            <SlidersHorizontal size={13} />
            Disponibilidad:
          </span>
          <select
            className="rep-filter-select"
            value={filterStatus}
            onChange={e => handleFilterStatus(e.target.value)}
            aria-label="Filtrar por disponibilidad"
          >
            {STATUS_OPTIONS.map(opt => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>
        </div>

        {/* Botón refrescar */}
        <button
          className="rep-action-btn"
          onClick={reload}
          aria-label="Refrescar datos"
          title="Refrescar"
          style={{ width: 40, height: 40 }}
        >
          <RefreshCw size={15} />
        </button>

        <span className="rep-toolbar-spacer" />

        {/* Botón registrar */}
        <button
          className="rep-btn-register"
          onClick={() => setShowRegister(true)}
          aria-label="Registrar nuevo repartidor"
        >
          <Plus size={16} />
          Registrar repartidor
        </button>
      </div>

      {/* ── Tabla principal ───────────────────────────────────── */}
      <section className="rep-section">

        <div className="rep-section-header">
          <div className="rep-section-icon">
            <Users size={20} />
          </div>
          <div>
            <h2 className="rep-section-title">Gestión de repartidores</h2>
            <p className="rep-section-subtitle">
              Listado de repartidores, su estado actual y rendimiento.
            </p>
          </div>
        </div>

        {/* Estado cargando */}
        {loading && (
          <div className="rep-loading">
            <span className="loading-spinner" />
            Cargando repartidores…
          </div>
        )}

        {/* Estado error */}
        {!loading && error && (
          <div className="rep-error">
            <Info size={16} />
            Error al cargar los datos: {error}. Verifica que el servicio esté activo.
          </div>
        )}

        {/* Tabla */}
        {!loading && !error && (
          <div className="rep-table-wrap">
            <table className="rep-table" aria-label="Tabla de repartidores">
              <thead>
                <tr>
                  <th>Repartidor</th>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Zona</th>
                  <th>Estado</th>
                  <th>Pedidos completados</th>
                  <th>Calificación</th>
                  <th>Última actividad</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {pageDrivers.length === 0 ? (
                  <tr>
                    <td colSpan={9}>
                      <div className="rep-empty">
                        <div className="rep-empty-icon">🏍</div>
                        <p className="rep-empty-title">
                          {drivers.length === 0
                            ? 'Sin repartidores registrados aún'
                            : 'Sin resultados para esta búsqueda'}
                        </p>
                        <p className="rep-empty-desc">
                          {drivers.length === 0
                            ? 'El aggregator no ha recibido ningún evento de asignación desde el último reinicio. Crea un pedido para ver los repartidores en acción.'
                            : 'Prueba con otro ID o cambia el filtro de disponibilidad.'}
                        </p>
                      </div>
                    </td>
                  </tr>
                ) : (
                  pageDrivers.map(driver => {
                    const initials = avatarInitials(driver.driver_id);
                    const activity = formatActivity(driver.last_activity);
                    const isNext   = roundRobin?.next_driver_id === driver.driver_id;

                    return (
                      <tr key={driver.driver_id}>
                        {/* Repartidor — avatar */}
                        <td>
                          <div className="rep-driver-cell">
                            <div
                              className="rep-avatar"
                              title={driver.driver_id}
                              aria-label={`Avatar de ${driver.driver_id}`}
                            >
                              {initials}
                            </div>
                            {isNext && (
                              <span
                                title="Próximo en turno Round Robin"
                                style={{ fontSize: 12, color: 'var(--orange)' }}
                              >
                                🔄
                              </span>
                            )}
                          </div>
                        </td>

                        {/* ID */}
                        <td>
                          <span className="rep-driver-id">{driver.driver_id}</span>
                        </td>

                        {/* Nombre — no disponible */}
                        <td><span className="rep-na">No disponible</span></td>

                        {/* Zona — no disponible */}
                        <td><span className="rep-na">No disponible</span></td>

                        {/* Estado */}
                        <td><StatusBadge status={driver.status} /></td>

                        {/* Pedidos completados */}
                        <td>
                          <span className="rep-num">{driver.completed_orders}</span>
                          {driver.total_orders > 0 && (
                            <span
                              style={{ fontSize: 11, color: 'var(--text-muted)', marginLeft: 4 }}
                            >
                              / {driver.total_orders}
                            </span>
                          )}
                        </td>

                        {/* Calificación — no disponible */}
                        <td><span className="rep-na">—</span></td>

                        {/* Última actividad */}
                        <td>
                          {activity
                            ? <span className="rep-time">{activity}</span>
                            : <span className="rep-na">Sin registros</span>
                          }
                        </td>

                        {/* Acciones */}
                        <td>
                          <div className="rep-actions">
                            <button
                              className="rep-action-btn"
                              onClick={() => setDetailDriver(driver)}
                              aria-label={`Ver detalle de ${driver.driver_id}`}
                              title="Ver detalles"
                            >
                              <Eye size={15} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        )}

        {/* Paginación */}
        {!loading && !error && filtered.length > 0 && (
          <div className="rep-pagination">
            <span className="rep-pagination-info">
              Mostrando {Math.min((safePage - 1) * PAGE_SIZE + 1, filtered.length)}–
              {Math.min(safePage * PAGE_SIZE, filtered.length)} de {filtered.length} repartidor
              {filtered.length !== 1 ? 'es' : ''}
            </span>

            <div className="rep-pagination-controls" role="navigation" aria-label="Paginación">
              <button
                className="rep-page-btn"
                onClick={() => setPage(p => Math.max(1, p - 1))}
                disabled={safePage === 1}
                aria-label="Página anterior"
              >
                ‹
              </button>

              {Array.from({ length: totalPages }, (_, i) => i + 1).map(n => (
                <button
                  key={n}
                  className={`rep-page-btn${n === safePage ? ' rep-page-btn--active' : ''}`}
                  onClick={() => setPage(n)}
                  aria-current={n === safePage ? 'page' : undefined}
                  aria-label={`Página ${n}`}
                >
                  {n}
                </button>
              ))}

              <button
                className="rep-page-btn"
                onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                disabled={safePage === totalPages}
                aria-label="Página siguiente"
              >
                ›
              </button>
            </div>
          </div>
        )}
      </section>

      {/* ── Modales ───────────────────────────────────────────── */}
      {detailDriver && (
        <DriverDetailModal
          driver={detailDriver}
          roundRobin={roundRobin}
          onClose={() => setDetailDriver(null)}
        />
      )}

      {showRegister && (
        <RegisterDriverModal onClose={() => setShowRegister(false)} />
      )}
    </div>
  );
}
