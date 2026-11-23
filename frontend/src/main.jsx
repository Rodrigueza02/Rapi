import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';

/* ── Estilos base ─────────────────────────────────────────────── */
import './styles/common/base.css';
import './styles/common/animations.css';
import './styles/common/buttons.css';
import './styles/common/modal.css';

/* ── Login ────────────────────────────────────────────────────── */
import './styles/login/login.css';

/* ── Panel admin ──────────────────────────────────────────────── */
import './styles/admin/shell.css';
import './styles/admin/header.css';

/* ── Dashboard ────────────────────────────────────────────────── */
import './styles/dashboard/layout.css';
import './styles/dashboard/stats.css';
import './styles/dashboard/table.css';
import './styles/dashboard/roundrobin.css';
import './styles/dashboard/panels.css';
import './styles/dashboard/connection.css';

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
