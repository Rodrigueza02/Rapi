import { STATUS_LABEL, STATUS_COLOR } from '../../constants/orderStatus.js';

/**
 * Pastilla de estado para la tabla de pedidos.
 * El color lo define la clase CSS según el estado del backend.
 */
export default function StatusBadge({ status }) {
  const label = STATUS_LABEL[status] ?? status;
  const cls   = STATUS_COLOR[status] ?? '';

  return (
    <span className={`status-badge ${cls}`}>
      {label}
    </span>
  );
}
