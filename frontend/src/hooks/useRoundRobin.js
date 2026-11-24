import { useCallback, useEffect, useState } from 'react';
import { fetchRoundRobin } from '../api/HttpDashboardApi.js';

/**
 * Obtiene el estado del anillo Round Robin del backend.
 *
 * `reload` es estable (useCallback con deps vacías) para que los
 * componentes llamadores puedan incluirla en sus propios useCallback
 * sin provocar re-renders en cadena.
 *
 * @returns {{ roundRobin, loading, error, reload }}
 */
export default function useRoundRobin() {
  const [roundRobin, setRoundRobin] = useState(null);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchRoundRobin();
      setRoundRobin(data);
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []); // sin dependencias externas: fetchRoundRobin es un módulo estable

  useEffect(() => { load(); }, [load]);

  return { roundRobin, loading, error, reload: load };
}
