/**
 * App.jsx
 * SRP: única responsabilidad — punto de entrada de la aplicación.
 * Solo instancia el router y el proveedor de BrowserRouter.
 * Toda la lógica vive en useSession, AppRouter y los layouts.
 */

import { BrowserRouter, useNavigate } from 'react-router-dom';
import useSession from './hooks/useSession.js';
import AppRouter  from './router/AppRouter.jsx';

function AppRoutes() {
  const navigate                       = useNavigate();
  const { session, checking, login, logout } = useSession();

  function onLogin(s)  { login(s);  navigate(s.role === 'ADMIN' ? '/admin' : '/sin-permiso', { replace: true }); }
  function onLogout()  { logout();  navigate('/login', { replace: true }); }

  return <AppRouter session={session} checking={checking} onLogin={onLogin} onLogout={onLogout} />;
}

export default function App() {
  return <BrowserRouter><AppRoutes /></BrowserRouter>;
}
