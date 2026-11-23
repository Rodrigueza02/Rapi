/**
 * CreateOrderModal.jsx
 * SRP: única responsabilidad — formulario para crear un pedido nuevo.
 * Delega lógica a useCreateOrder y estructura a Modal + FormField.
 */

import Modal        from '../common/Modal.jsx';
import FormField    from '../common/FormField.jsx';
import ErrorMessage from '../common/ErrorMessage.jsx';
import useCreateOrder from '../../hooks/useCreateOrder.js';

export default function CreateOrderModal({ onClose, onCreated }) {
  const { form, setField, loading, error, handleSubmit } = useCreateOrder((result) => {
    onCreated(result);
    onClose();
  });

  return (
    <Modal title="Nuevo pedido" onClose={onClose} size="md">
      <form className="create-order-form" onSubmit={handleSubmit} noValidate>

        <FormField label="ID del cliente" >
          <input
            className="form-input"
            placeholder="ej: cliente-001"
            value={form.customerId}
            onChange={e => setField('customerId', e.target.value)}
            required
          />
        </FormField>

        <FormField label="Teléfono del cliente" hint="Formato: +573001234567">
          <input
            className="form-input"
            placeholder="+573001234567"
            value={form.customerPhone}
            onChange={e => setField('customerPhone', e.target.value)}
            required
          />
        </FormField>

        <FormField label="Dirección de entrega">
          <input
            className="form-input"
            placeholder="Cra 7 #45-21, Bogotá"
            value={form.deliveryAddress}
            onChange={e => setField('deliveryAddress', e.target.value)}
            required
          />
        </FormField>

        <FormField label="Ítems" hint="Separados por coma: Hamburguesa, Papas, Bebida">
          <input
            className="form-input"
            placeholder="Hamburguesa, Papas, Bebida"
            value={form.items}
            onChange={e => setField('items', e.target.value)}
            required
          />
        </FormField>

        <FormField label="Valor total (COP)">
          <input
            className="form-input"
            type="number"
            min="1"
            placeholder="42900"
            value={form.totalAmount}
            onChange={e => setField('totalAmount', e.target.value)}
            required
          />
        </FormField>

        <ErrorMessage message={error} />

        <div className="modal-footer">
          <button type="button" className="btn-outline" onClick={onClose}>
            Cancelar
          </button>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? <span className="spin">⏳</span> : '+ Crear pedido'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
