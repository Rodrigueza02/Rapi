import { useCallback, useEffect, useState } from 'react';
import { fetchDrivers, fetchRoundRobin } from '../api/HttpDashboardApi.js';

/**
 * useDrivers
 *
 * Obtiene la lista de repartidores conocidos por el aggregator
 * (GET /api/dashboard/drivers) y el estado del anillo Round Robin
 * (GET /api/dashboard/round-robin) en una sola carga.
 *
 * Lo que el backend puede garantizar de cada repartidor:
 *   - driver_id            → ID real (ej: driver-1)
 *   - status               → EN_RUTA | DESCONOCIDO
 *   - active_order_id      → ID del pedido activo, null si no hay
 *   - active_order_state   → ASSIGNED | ARRIVING, null si no hay
 *   - completed_orders     → pedidos entregados
 *   - total_orders         → total de pedidos asignados
 *   - last_activity        → timestamp ISO del último evento, null si no hay
 *
 * Lo que NO existe en el backend y no se inventa aquí:
 *   - nombre, zona, calificación, foto, disponibilidad precisa
 *
 * @returns {{ drivers, roundRobin, loading, error, reload }}
 */
export default function useDrivers() {
  const [drivers,    setDrivers]    = useState([]);
  const [roundRobin, setRoundRobin] = useState(null);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [driversData, rrData] = await Promise.all([
        fetchDrivers(),
        fetchRoundRobin(),
      ]);
      setDrivers(driversData);
      setRoundRobin(rrData);
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  return { drivers, roundRobin, loading, error, reload: load };
}
