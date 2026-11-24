/**
 * useCreateOrder.js
 * Gestiona el estado, validaciones y envío del formulario de creación de pedido.
 *
 * CATÁLOGO DE PRODUCTOS:
 *   El proyecto no tiene un endpoint de catálogo de productos.
 *   Se usa una lista fija de productos disponibles definida en el frontend.
 *   Los productos se corresponden con los pedidos simulados del sistema.
 *   No se inventan productos externos.
 *
 * VALIDACIONES DE TELÉFONO:
 *   El backend acepta `+?[0-9]{7,15}`.
 *   El frontend valida 10 dígitos nacionales colombianos (3XXXXXXXXX)
 *   o el formato internacional +573XXXXXXXXX.
 *
 * VALOR TOTAL:
 *   Máximo 6 dígitos → $999.999 COP.
 */

import { useState, useCallback } from 'react';
import { createOrder } from '../api/HttpOrderApi.js';

/* ── Productos disponibles en el sistema ──────────────────────────────────
   Fuente: imágenes y pedidos simulados del proyecto.
   No hay endpoint de catálogo — lista definida en frontend.           */
export const PRODUCTS_CATALOG = [
  { id: 'hamburguesa', name: 'Hamburguesa',  price: 18000 },
  { id: 'pizza',       name: 'Pizza',         price: 25000 },
  { id: 'sushi',       name: 'Sushi',          price: 35000 },
  { id: 'papas',       name: 'Papas',          price: 8000  },
];

const PRODUCT_IDS = new Set(PRODUCTS_CATALOG.map(p => p.id));

/* ── Estado inicial ───────────────────────────────────────────────────── */
const INITIAL_FORM = {
  customerId:      '',
  customerPhone:   '',
  deliveryAddress: '',
  items:           [],   /* array de IDs de productos seleccionados */
  totalAmount:     '',
};

const INITIAL_ERRORS = {
  customerId:      '',
  customerPhone:   '',
  deliveryAddress: '',
  items:           '',
  totalAmount:     '',
};

/* ── Validadores individuales ─────────────────────────────────────────── */

export function validateCustomerId(value) {
  const v = value?.trim() ?? '';
  if (!v) return 'El ID del cliente es obligatorio.';
  if (v.length < 3) return 'El ID del cliente debe tener al menos 3 caracteres.';
  /* Formato libre — el backend no impone UUID.
     Solo rechazamos espacios internos y caracteres inusuales. */
  if (/\s/.test(v)) return 'El ID del cliente no puede contener espacios.';
  return '';
}

export function validatePhone(value) {
  const v = value?.trim() ?? '';
  if (!v) return 'El teléfono del cliente es obligatorio.';

  /* Eliminar prefijo +57 para contar los dígitos nacionales */
  const withoutPrefix = v.startsWith('+57') ? v.slice(3) : v;
  const digitsOnly = withoutPrefix.replace(/\D/g, '');

  if (/[a-zA-Z]/.test(v)) return 'El número telefónico solo puede contener números.';
  if (/[^+0-9\s]/.test(v)) return 'El número telefónico contiene caracteres no válidos.';

  if (digitsOnly.length < 10) return 'El número telefónico debe contener exactamente 10 dígitos.';
  if (digitsOnly.length > 10) return 'El número telefónico no debe superar 10 dígitos.';

  /* Teléfonos móviles colombianos inician en 3 */
  if (!digitsOnly.startsWith('3')) return 'El número debe ser un celular colombiano (inicia con 3).';

  return '';
}

export function validateAddress(value) {
  const v = value?.trim() ?? '';
  if (!v) return 'La dirección de entrega es obligatoria.';
  if (v.length < 8) return 'Ingresa una dirección de entrega válida y completa.';

  /* Validar estructura mínima colombiana: tipo de vía + número */
  const pattern = /^(cra?|carrera|calle?|cl?|av(enida)?|av\.|diagonal|dg|transversal|tr|via)\s*\.?\s*\d/i;
  if (!pattern.test(v)) {
    return 'Ingresa una dirección de entrega válida y completa (ej: Cra 14 # 18-40).';
  }
  return '';
}

export function validateItems(items) {
  if (!items || items.length === 0) return 'Selecciona al menos un producto.';
  const invalid = items.filter(id => !PRODUCT_IDS.has(id));
  if (invalid.length > 0) return `Producto no disponible: ${invalid.join(', ')}.`;
  return '';
}

export function validateTotalAmount(value) {
  const v = String(value ?? '').trim();
  if (!v) return 'El valor total es obligatorio.';
  if (/[^0-9]/.test(v)) return 'El valor total solo puede contener números enteros.';
  const num = parseInt(v, 10);
  if (isNaN(num) || num <= 0) return 'Ingresa un valor válido mayor que cero.';
  if (num > 999999) return 'El valor total no puede superar $999.999 COP.';
  return '';
}

/* ── Hook principal ───────────────────────────────────────────────────── */

export default function useCreateOrder(onSuccess) {
  const [form,    setForm]    = useState(INITIAL_FORM);
  const [errors,  setErrors]  = useState(INITIAL_ERRORS);
  const [touched, setTouched] = useState({});
  const [loading, setLoading] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  /* ── Actualizar campo y limpiar error en tiempo real ──────────────── */
  const setField = useCallback((field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
    /* Limpiar error cuando el usuario corrige */
    setErrors(prev => ({ ...prev, [field]: '' }));
    setSubmitError(null);
  }, []);

  /* ── Marcar campo como tocado y validar al salir ──────────────────── */
  const handleBlur = useCallback((field) => {
    setTouched(prev => ({ ...prev, [field]: true }));
    const err = validateField(field, form[field]);
    setErrors(prev => ({ ...prev, [field]: err }));
  }, [form]);

  /* ── Validar un campo por nombre ──────────────────────────────────── */
  function validateField(field, value) {
    switch (field) {
      case 'customerId':      return validateCustomerId(value);
      case 'customerPhone':   return validatePhone(value);
      case 'deliveryAddress': return validateAddress(value);
      case 'items':           return validateItems(value);
      case 'totalAmount':     return validateTotalAmount(value);
      default: return '';
    }
  }

  /* ── Validar todo el formulario ───────────────────────────────────── */
  function validateAll() {
    const newErrors = {
      customerId:      validateCustomerId(form.customerId),
      customerPhone:   validatePhone(form.customerPhone),
      deliveryAddress: validateAddress(form.deliveryAddress),
      items:           validateItems(form.items),
      totalAmount:     validateTotalAmount(form.totalAmount),
    };
    setErrors(newErrors);
    setTouched({ customerId: true, customerPhone: true, deliveryAddress: true, items: true, totalAmount: true });
    return Object.values(newErrors).every(e => !e);
  }

  /* ── Envío del formulario ──────────────────────────────────────────── */
  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitError(null);

    if (!validateAll()) return;

    setLoading(true);
    try {
      const payload = {
        customerId:      form.customerId.trim(),
        customerPhone:   normalizePhone(form.customerPhone.trim()),
        deliveryAddress: form.deliveryAddress.trim(),
        items:           form.items,
        totalAmount:     form.totalAmount,
      };
      const result = await createOrder(payload);
      setForm(INITIAL_FORM);
      setErrors(INITIAL_ERRORS);
      setTouched({});
      onSuccess(result);
    } catch (err) {
      setSubmitError(formatServerError(err.message));
    } finally {
      setLoading(false);
    }
  }

  /* Normaliza el teléfono: si tiene +57, mantiene; si no, deja como está */
  function normalizePhone(phone) {
    if (phone.startsWith('+57')) return phone;
    return phone;
  }

  /* Convierte mensajes de error HTTP a texto amigable */
  function formatServerError(msg) {
    if (!msg) return 'Error inesperado. Intenta de nuevo.';
    if (msg.includes('401')) return 'Tu sesión ha expirado. Por favor vuelve a iniciar sesión.';
    if (msg.includes('403')) return 'No tienes permiso para realizar esta acción.';
    if (msg.includes('409')) return 'El pedido ya fue registrado. Verifica antes de intentar de nuevo.';
    if (msg.includes('422')) return 'Los datos del pedido no son válidos. Revisa el formulario.';
    if (msg.includes('400')) return 'Datos inválidos. Revisa todos los campos del formulario.';
    if (msg.includes('500')) return 'Error del servidor. Intenta de nuevo en unos momentos.';
    if (msg.includes('conectar')) return 'No se pudo conectar con el servidor. Verifica que el servicio esté activo.';
    return msg;
  }

  return {
    form,
    errors,
    touched,
    loading,
    submitError,
    setField,
    handleBlur,
    handleSubmit,
  };
}
