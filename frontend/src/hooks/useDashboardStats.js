import { useEffect, useState } from 'react';
import { fetchStats } from '../api/HttpDashboardApi.js';

/**
 * Obtiene las métricas del dashboard desde el backend.
 * Refresca automáticamente cada `intervalMs` milisegundos.
 *
 * @returns {{ stats, loading, error }}
 */
export default function useDashboardStats(intervalMs = 15_000) {
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const data = await fetchStats();
        if (!cancelled) { setStats(data); setError(null); }
      } catch (e) {
        if (!cancelled) setError(e.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    const id = setInterval(load, intervalMs);
    return () => { cancelled = true; clearInterval(id); };
  }, [intervalMs]);

  return { stats, loading, error };
}
