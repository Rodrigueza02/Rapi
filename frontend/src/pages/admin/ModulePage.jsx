import { Info, ArrowUpRight } from 'lucide-react';

const MODULE_CONFIG = {
  pedidos:        ['Pedidos',        'Crear, consultar, cancelar y seguir pedidos.'],
  repartidores:   ['Repartidores',   'Disponibilidad y asignaciones de repartidores.'],
  pagos:          ['Pagos',          'Transacciones y pagos simulados.'],
  notificaciones: ['Notificaciones', 'Historial de correos y notificaciones.'],
  reclamaciones:  ['Reclamaciones',  'Evidencias fotográficas e incidencias de entregas.'],
  reportes:       ['Reportes',       'Indicadores y reportes operativos.'],
  configuracion:  ['Configuración',  'Preferencias de la plataforma.'],
};

export default function ModulePage({ module }) {
  const [title, description] = MODULE_CONFIG[module] || ['Módulo', 'Próximamente'];

  return (
    <section className="module-card">
      <div className="module-icon"><Info size={25} /></div>
      <h2>{title}</h2>
      <p>{description}</p>
      <div className="module-note">
        <ArrowUpRight size={18} />
        Interfaz preparada. Las operaciones reales requieren endpoints del backend
        que aún están en desarrollo.
      </div>
    </section>
  );
}
