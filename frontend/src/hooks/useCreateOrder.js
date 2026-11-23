/**
 * useCreateOrder.js
 * SRP: única responsabilidad — estado del formulario de creación de pedido.
 * Llama a HttpOrderApi y notifica al padre cuando el pedido fue aceptado.
 */

import { useState } from 'react';
import { createOrder } from '../api/HttpOrderApi.js';

const INITIAL = {
  customerId:      '',
  customerPhone:   '',
  deliveryAddress: '',
  items:           '',
  totalAmount:     '',
};

export default function useCreateOrder(onSuccess) {
  const [form,    setForm]    = useState(INITIAL);
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);

  function setField(field, value) {
    setForm(prev => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const payload = {
        ...form,
        items:       form.items.split(',').map(s => s.trim()).filter(Boolean),
        totalAmount: form.totalAmount,
      };
      const result = await createOrder(payload);
      setForm(INITIAL);
      onSuccess(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return { form, setField, loading, error, handleSubmit };
}
