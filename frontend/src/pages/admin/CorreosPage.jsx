import { useState } from 'react';
import { Plus } from 'lucide-react';

import useEmails          from '../../hooks/useEmails.js';
import EmailStats         from '../../components/emails/EmailStats.jsx';
import EmailFilters       from '../../components/emails/EmailFilters.jsx';
import EmailList          from '../../components/emails/EmailList.jsx';
import EmailDetails       from '../../components/emails/EmailDetails.jsx';
import ComposeEmailModal  from '../../components/emails/ComposeEmailModal.jsx';

import '../../styles/pages/correos.css';

const PAGE_SIZE = 20;

export default function CorreosPage() {
  const { emails, stats, loading, error, reload, applyFilter, goToPage, filters } = useEmails();
  const [selected,  setSelected]  = useState(null);
  const [composing, setComposing] = useState(false);

  const totalPages = emails?.totalPages ?? 0;

  function handleFilterChange(status) {
    applyFilter({ status });
    setSelected(null);
  }

  function handleSent() {
    reload();
    setComposing(false);
  }

  return (
    <div className="admin-dashboard correos-page">

      <div className="correos-header">
        <div>
          <h2 className="correos-title">Correos</h2>
          {error && <p className="correos-error">Error: {error}</p>}
        </div>
        <button className="btn-primary correos-btn-compose" onClick={() => setComposing(true)}>
          <Plus size={16} /> Redactar
        </button>
      </div>

      <EmailStats stats={stats} loading={loading} />
      <EmailFilters status={filters.status} onChange={handleFilterChange} />

      <div className={`correos-body ${selected ? 'correos-body--split' : ''}`}>
        <div className="correos-list-col">
          <EmailList emails={emails} loading={loading} onSelect={setSelected} />

          {totalPages > 1 && (
            <div className="pedidos-pagination" aria-label="Paginación">
              <button className="page-btn" disabled={filters.page === 0}
                onClick={() => goToPage(filters.page - 1)}>‹</button>
              {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => (
                <button key={i} className={`page-btn ${filters.page === i ? 'page-btn--active' : ''}`}
                  onClick={() => goToPage(i)}>{i + 1}</button>
              ))}
              <button className="page-btn" disabled={filters.page >= totalPages - 1}
                onClick={() => goToPage(filters.page + 1)}>›</button>
            </div>
          )}
        </div>

        {selected && (
          <EmailDetails email={selected} onClose={() => setSelected(null)} />
        )}
      </div>

      {composing && (
        <ComposeEmailModal onClose={() => setComposing(false)} onSent={handleSent} />
      )}
    </div>
  );
}
