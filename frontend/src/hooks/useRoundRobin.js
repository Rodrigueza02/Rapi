import { useEffect, useState } from 'react';
import { fetchRoundRobin } from '../api/HttpDashboardApi.js';

/**
 * Obtiene el estado del anillo Round Robin del backend.
 * Refresca cada vez que el componente se monta o se llama a `reload`.
 *
 * @returns {{ roundRobin, loading, error, reload }}
 */
export default function useRoundRobin() {
  const [roundRobin, setRoundRobin] = useState(null);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState(null);

  async function load() {
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
  }

  useEffect(() => { load(); }, []);

  return { roundRobin, loading, error, reload: load };
}
