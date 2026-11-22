import { Search, SlidersHorizontal, Plus } from 'lucide-react';

/**
 * Barra superior de la tabla de pedidos.
 * El estado de búsqueda y filtro vive en el padre (AdminDashboard).
 */
export default function OrdersToolbar({ search, onSearch, onNewOrder }) {
  return (
    <div className="orders-toolbar">
      <div className="orders-search">
        <Search size={16} />
        <input
          type="search"
          placeholder="Buscar pedido, cliente o repartidor..."
          value={search}
          onChange={e => onSearch(e.target.value)}
          aria-label="Buscar pedido"
        />
      </div>

      <div className="orders-toolbar-actions">
        <button className="btn-outline" aria-label="Filtrar">
          <SlidersHorizontal size={16} /> Filtrar
        </button>
        <button className="btn-primary" onClick={onNewOrder}>
          <Plus size={16} /> Nuevo pedido
        </button>
      </div>
    </div>
  );
}
