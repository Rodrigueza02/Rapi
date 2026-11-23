import { useEffect, useCallback, useState } from 'react';
import { fetchOrders } from '../api/HttpDashboardApi.js';

/**
 * Obtiene la lista de pedidos del backend.
 * Expone `reload` para refrescar manualmente (ej: cuando llega un WS event).
 *
 * @returns {{ orders, loading, error, reload }}
 */
export default function useOrders() {
  const [orders,  setOrders]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchOrders();
      setOrders(data);
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  return { orders, loading, error, reload: load };
}
