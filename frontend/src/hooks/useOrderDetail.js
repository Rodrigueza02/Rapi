/**
 * useOrderDetail.js
 * SRP: única responsabilidad — obtener el detalle de un pedido por ID.
 * Se activa solo cuando orderId tiene valor.
 */

import { useEffect, useState } from 'react';
import { fetchOrderById } from '../api/HttpDashboardApi.js';

export default function useOrderDetail(orderId) {
  const [order,   setOrder]   = useState(null);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    if (!orderId) { setOrder(null); return; }

    let cancelled = false;
    setLoading(true);
    setError(null);

    fetchOrderById(orderId)
      .then(data => { if (!cancelled) setOrder(data); })
      .catch(e   => { if (!cancelled) setError(e.message); })
      .finally(  () => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [orderId]);

  return { order, loading, error };
}
