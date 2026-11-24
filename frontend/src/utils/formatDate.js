/**
 * Formatea un ISO string a hora legible "HH:MM".
 * Devuelve "—" si el valor es nulo o inválido.
 */
export function formatTime(isoString) {
  if (!isoString) return '—';
  try {
    return new Date(isoString).toLocaleTimeString('es-CO', {
      hour:   '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  } catch {
    return '—';
  }
}

/**
 * Formatea un ISO string a fecha corta "DD/MM/YYYY".
 */
export function formatDate(isoString) {
  if (!isoString) return '—';
  try {
    return new Date(isoString).toLocaleDateString('es-CO', {
      day:   '2-digit',
      month: '2-digit',
      year:  'numeric',
    });
  } catch {
    return '—';
  }
}
