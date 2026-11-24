import { useEffect, useCallback, useState } from 'react';
import { fetchStats } from '../api/HttpDashboardApi.js';

/**
 * Obtiene las métricas globales del dashboard desde el backend.
 *
 * Expone `reload` para que el WS pueda forzar un refresco inmediato
 * cuando llegue un evento, sin esperar el intervalo automático.
 *
 * @param {number} intervalMs  intervalo de refresco automático (ms)
 * @returns {{ stats, loading, error, reload }}
 */
export default function useDashboardStats(intervalMs = 15_000) {
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  const load = useCallback(async () => {
    try {
      const data = await fetchStats();
      setStats(data);
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    const id = setInterval(load, intervalMs);
    return () => clearInterval(id);
  }, [load, intervalMs]);

  return { stats, loading, error, reload: load };
}
