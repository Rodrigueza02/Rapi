import { Bell, CircleUserRound } from 'lucide-react';

export default function DashboardHeader({ title, user }) {
  return (
    <header className="dashboard-header">
      <div>
        <p className="eyebrow">PANEL ADMINISTRATIVO</p>
        <h1>{title}</h1>
        <p>Gestión y seguimiento de las entregas</p>
      </div>
      <div className="user-chip">
        <Bell size={19} />
        <CircleUserRound size={27} />
        <span>{user?.name || 'Administrador'}</span>
      </div>
    </header>
  );
}
