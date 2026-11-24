import { STATUS_LABEL, STATUS_COLOR } from '../../constants/orderStatus.js';

export default function StatusBadge({ status }) {
  const label = STATUS_LABEL[status] ?? status;
  const cls   = STATUS_COLOR[status] ?? '';
  return <span className={`status-badge ${cls}`}>{label}</span>;
}
