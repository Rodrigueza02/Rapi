/**
 * CancelOrderModal.jsx — bloquea la acción si el pedido ya fue entregado.
 */
import Modal        from '../common/Modal.jsx';
import ErrorMessage from '../common/ErrorMessage.jsx';
import useCancelOrder from '../../hooks/useCancelOrder.js';
import useOrderDetail from '../../hooks/useOrderDetail.js';

const NON_CANCELLABLE = new Set(['DELIVERED', 'CANCELLED']);

export default function CancelOrderModal({ orderId, onClose, onCancelled }) {
  const { order, loading: loadingDetail } = useOrderDetail(orderId);
  const { cancel, loading, error } = useCancelOrder((id) => {
    onCancelled(id);
    onClose();
  });

  const isDelivered  = order?.state === 'DELIVERED';
  const isCancelled  = order?.state === 'CANCELLED';
  const nonCancellable = order && NON_CANCELLABLE.has(order.state);

  return (
    <Modal title="Cancelar pedido" onClose={onClose} size="sm">
      <div className="confirm-body">
        <span className="confirm-icon" aria-hidden="true">{nonCancellable ? '🚫' : '⚠️'}</span>

        {isDelivered && (
          <p className="form-error" role="alert">
            Este pedido ya fue entregado y no puede cancelarse.
          </p>
        )}
        {isCancelled && (
          <p className="form-error" role="alert">
            Este pedido ya está cancelado.
          </p>
        )}
        {!nonCancellable && (
          <>
            <p>¿Seguro que deseas cancelar el pedido</p>
            <strong className="order-id-badge">{orderId?.slice(0, 8).toUpperCase()}</strong>
            <p className="confirm-note">
              Solo se puede cancelar si está en estado <em>Creado</em>, <em>En preparación</em> o <em>Asignado</em>.
            </p>
          </>
        )}
      </div>

      <ErrorMessage message={error} />

      <div className="modal-footer">
        <button className="btn-outline" onClick={onClose} disabled={loading}>
          {nonCancellable ? 'Cerrar' : 'No, mantener'}
        </button>
        {!nonCancellable && (
          <button
            className="btn-danger"
            onClick={() => cancel(orderId)}
            disabled={loading || loadingDetail}
          >
            {loading ? <span className="spin">⏳</span> : '✕ Sí, cancelar'}
          </button>
        )}
      </div>
    </Modal>
  );
}
