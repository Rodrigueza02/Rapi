/**
 * useCancelOrder.js
 * SRP: única responsabilidad — manejar el estado de una cancelación.
 */

import { useState } from 'react';
import { cancelOrder } from '../api/HttpCancelApi.js';

export default function useCancelOrder(onSuccess) {
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  async function cancel(orderId) {
    setLoading(true);
    setError(null);
    try {
      await cancelOrder(orderId);
      onSuccess(orderId);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return { cancel, loading, error };
}
