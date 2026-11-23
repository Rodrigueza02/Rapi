/**
 * CreateOrderModal.jsx
 * Formulario de creación de pedido con validaciones completas.
 *
 * CATÁLOGO: No existe endpoint de catálogo en el proyecto.
 * Se usa una lista fija definida en useCreateOrder.js.
 * Los productos son: Hamburguesa, Pizza, Sushi, Papas.
 *
 * TELÉFONO: 10 dígitos nacionales colombianos o +573XXXXXXXXX.
 * PRECIO:   Mínimo $1 COP, máximo $999.999 COP (6 dígitos).
 * CLIENTE:  Texto libre, mínimo 3 caracteres, sin espacios.
 * DIRECCIÓN: Formato colombiano estructurado (Cra / Calle / Av.).
 */

import Modal     from '../common/Modal.jsx';
import useCreateOrder, { PRODUCTS_CATALOG } from '../../hooks/useCreateOrder.js';

/* ── Componente de campo con error ──────────────────────────────────── */
function Field({ label, hint, error, touched, children }) {
  const showError = touched && error;
  return (
    <div className="create-order-field">
      <label className="create-order-label">{label}</label>
      {hint && <span className="create-order-hint">{hint}</span>}
      {children}
      {showError && (
        <span className="create-order-error" role="alert">{error}</span>
      )}
    </div>
  );
}

/* ── Modal principal ─────────────────────────────────────────────────── */
export default function CreateOrderModal({ onClose, onCreated }) {
  const {
    form, errors, touched, loading, submitError,
    setField, handleBlur, handleSubmit,
  } = useCreateOrder((result) => {
    onCreated(result);
    onClose();
  });

  /* ── Selector de productos ─────────────────────────────────────────── */
  function toggleProduct(productId) {
    const current = form.items;
    const updated = current.includes(productId)
      ? current.filter(id => id !== productId)
      : [...current, productId];
    setField('items', updated);
  }

  /* ── Formato visual del precio con separador de miles ──────────────── */
  function formatPrice(raw) {
    const digits = String(raw ?? '').replace(/\D/g, '');
    if (!digits) return '';
    return Number(digits).toLocaleString('es-CO');
  }

  function handlePriceChange(e) {
    /* Permitir solo hasta 6 dígitos */
    const digits = e.target.value.replace(/\D/g, '').slice(0, 6);
    setField('totalAmount', digits);
  }

  return (
    <Modal title="Nuevo pedido" onClose={onClose} size="md">
      <form
        className="create-order-form"
        onSubmit={handleSubmit}
        noValidate
        aria-label="Formulario de creación de pedido"
      >

        {/* ── ID del cliente ──────────────────────────────────────── */}
        <Field
          label="ID del cliente"
          hint="Identificador único del cliente (ej: cliente-001)"
          error={errors.customerId}
          touched={touched.customerId}
        >
          <input
            className={`form-input ${touched.customerId && errors.customerId ? 'form-input--error' : ''}`}
            placeholder="ej: cliente-001"
            value={form.customerId}
            onChange={e => setField('customerId', e.target.value)}
            onBlur={() => handleBlur('customerId')}
            aria-invalid={!!(touched.customerId && errors.customerId)}
            aria-describedby="customerId-error"
            autoComplete="off"
          />
        </Field>

        {/* ── Teléfono ────────────────────────────────────────────── */}
        <Field
          label="Teléfono del cliente"
          hint="10 dígitos nacionales (ej: 3001234567) o formato +573001234567"
          error={errors.customerPhone}
          touched={touched.customerPhone}
        >
          <input
            className={`form-input ${touched.customerPhone && errors.customerPhone ? 'form-input--error' : ''}`}
            placeholder="3001234567"
            value={form.customerPhone}
            onChange={e => setField('customerPhone', e.target.value)}
            onBlur={() => handleBlur('customerPhone')}
            type="tel"
            inputMode="tel"
            aria-invalid={!!(touched.customerPhone && errors.customerPhone)}
            maxLength={13}
          />
        </Field>

        {/* ── Dirección ───────────────────────────────────────────── */}
        <Field
          label="Dirección de entrega"
          hint="Formato colombiano (ej: Cra 14 # 18-40, Calle 20 # 15-30)"
          error={errors.deliveryAddress}
          touched={touched.deliveryAddress}
        >
          <input
            className={`form-input ${touched.deliveryAddress && errors.deliveryAddress ? 'form-input--error' : ''}`}
            placeholder="Cra 14 # 18-40, Bogotá"
            value={form.deliveryAddress}
            onChange={e => setField('deliveryAddress', e.target.value)}
            onBlur={() => handleBlur('deliveryAddress')}
            aria-invalid={!!(touched.deliveryAddress && errors.deliveryAddress)}
          />
        </Field>

        {/* ── Productos ───────────────────────────────────────────── */}
        <Field
          label="Productos"
          hint="Selecciona uno o más productos disponibles"
          error={errors.items}
          touched={touched.items}
        >
          <div
            className="create-order-products"
            role="group"
            aria-label="Productos disponibles"
            onBlur={() => handleBlur('items')}
          >
            {PRODUCTS_CATALOG.map(product => {
              const selected = form.items.includes(product.id);
              return (
                <button
                  key={product.id}
                  type="button"
                  className={`product-chip ${selected ? 'product-chip--selected' : ''}`}
                  onClick={() => { toggleProduct(product.id); handleBlur('items'); }}
                  aria-pressed={selected}
                  title={`$${product.price.toLocaleString('es-CO')} COP`}
                >
                  {product.name}
                  {selected && <span className="product-chip-check" aria-hidden="true">✓</span>}
                </button>
              );
            })}
          </div>
          {form.items.length > 0 && (
            <p className="create-order-selected-items" aria-live="polite">
              Seleccionados: {form.items.map(id =>
                PRODUCTS_CATALOG.find(p => p.id === id)?.name
              ).join(', ')}
            </p>
          )}
        </Field>

        {/* ── Valor total ─────────────────────────────────────────── */}
        <Field
          label="Valor total (COP)"
          hint="Máximo $999.999 COP — solo números enteros"
          error={errors.totalAmount}
          touched={touched.totalAmount}
        >
          <div className="create-order-price-wrap">
            <span className="create-order-price-prefix">$</span>
            <input
              className={`form-input create-order-price-input ${touched.totalAmount && errors.totalAmount ? 'form-input--error' : ''}`}
              placeholder="42.900"
              value={form.totalAmount ? Number(form.totalAmount).toLocaleString('es-CO') : ''}
              onChange={handlePriceChange}
              onBlur={() => handleBlur('totalAmount')}
              type="text"
              inputMode="numeric"
              aria-invalid={!!(touched.totalAmount && errors.totalAmount)}
              maxLength={9} /* 999.999 con separadores */
            />
            <span className="create-order-price-suffix">COP</span>
          </div>
        </Field>

        {/* ── Error de servidor ────────────────────────────────────── */}
        {submitError && (
          <div className="create-order-submit-error" role="alert" aria-live="assertive">
            {submitError}
          </div>
        )}

        {/* ── Acciones ─────────────────────────────────────────────── */}
        <div className="modal-footer">
          <button
            type="button"
            className="btn-outline"
            onClick={onClose}
            disabled={loading}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn-primary"
            disabled={loading}
            aria-busy={loading}
          >
            {loading
              ? <><span className="loading-spinner" style={{width:16,height:16}} /> Procesando...</>
              : '+ Crear pedido'}
          </button>
        </div>

      </form>
    </Modal>
  );
}
