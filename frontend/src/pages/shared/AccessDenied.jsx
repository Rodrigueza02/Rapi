export default function AccessDenied({ onLogout }) {
  return (
    <main className="access-denied">
      <h1>Acceso restringido</h1>
      <p>El panel administrativo requiere el rol ADMIN.</p>
      <button className="primary-button" onClick={onLogout}>
        Cerrar sesión
      </button>
    </main>
  );
}
