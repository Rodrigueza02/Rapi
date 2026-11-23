/**
 * CancelOrderModal.jsx
 * SRP: única responsabilidad — confirmar y ejecutar la cancelación de un pedido.
 */

import Modal        from '../common/Modal.jsx';
import ErrorMessage from '../common/ErrorMessage.jsx';
import useCancelOrder from '../../hooks/useCancelOrder.js';

export default function CancelOrderModal({ orderId, onClose, onCancelled }) {
  const { cancel, loading, error } = useCancelOrder((id) => {
    onCancelled(id);
    onClose();
  });

  return (
    <Modal title="Cancelar pedido" onClose={onClose} size="sm">
      <div className="confirm-body">
        <span className="confirm-icon" aria-hidden="true">⚠️</span>
        <p>¿Seguro que deseas cancelar el pedido</p>
        <strong className="order-id-badge">{orderId?.slice(0, 8).toUpperCase()}</strong>
        <p className="confirm-note">
          Solo se puede cancelar si está en estado <em>Creado</em> o <em>Asignado</em>.
        </p>
      </div>

      <ErrorMessage message={error} />

      <div className="modal-footer">
        <button className="btn-outline" onClick={onClose} disabled={loading}>
          No, mantener
        </button>
        <button
          className="btn-danger"
          onClick={() => cancel(orderId)}
          disabled={loading}
        >
          {loading ? <span className="spin">⏳</span> : '✕ Sí, cancelar'}
        </button>
      </div>
    </Modal>
  );
}
