import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchEmails, fetchEmailStats } from '../api/HttpEmailApi.js';

const POLL_INTERVAL_MS = 10_000;

export default function useEmails() {
  const [emails,  setEmails]  = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);
  const [filters, setFilters] = useState({ status: '', page: 0 });
  const inFlight = useRef(false);

  const load = useCallback(async (silent = false) => {
    if (inFlight.current) return;
    inFlight.current = true;
    if (!silent) setLoading(true);
    try {
      const [emailData, statsData] = await Promise.all([
        fetchEmails(filters),
        fetchEmailStats(),
      ]);
      setEmails(emailData);
      setStats(statsData);
      setError(null);
    } catch (e) {
      setError(e.message);
    } finally {
      inFlight.current = false;
      if (!silent) setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    load();
    const id = setInterval(() => load(true), POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [load]);

  function applyFilter(newFilters) {
    setFilters(prev => ({ ...prev, ...newFilters, page: 0 }));
  }

  function goToPage(page) {
    setFilters(prev => ({ ...prev, page }));
  }

  return { emails, stats, loading, error, reload: load, applyFilter, goToPage, filters };
}
