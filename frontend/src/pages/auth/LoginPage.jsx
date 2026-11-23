import LoginForm from '../../components/auth/LoginForm';
import foodImg   from '/images/login-food.png';

export default function LoginPage({ onSuccess }) {
  return (
    <main className="login-screen">
      <section className="login-card">

        {/* Blobs decorativos esquinas */}
        <div className="blob blob-top"    aria-hidden="true" />
        <div className="blob blob-bottom" aria-hidden="true" />

        {/* ── Logo ───────────────────────────────────── */}
        <header className="login-brand">
          <div className="brand-logo" aria-hidden="true">
            {/* Ícono de moto SVG */}
            <svg viewBox="0 0 56 42" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="33" r="7" stroke="white" strokeWidth="3"/>
              <circle cx="45" cy="33" r="7" stroke="white" strokeWidth="3"/>
              <path d="M18 33h19M11 33V23l9-9h12l7 9v2" stroke="white" strokeWidth="3"
                    strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M32 14l5 9H18" stroke="white" strokeWidth="2.5" strokeLinecap="round"/>
              <circle cx="42" cy="9" r="4" fill="white"/>
            </svg>
          </div>
          <h1 className="brand-name">Delivery<span>Flow</span></h1>
          <p className="brand-sub">Plataforma de Gestión de Entregas</p>
        </header>

        {/* ── Ilustración de comida ───────────────────── */}
        <div className="food-illustration" aria-hidden="true">
          <img
            src={foodImg}
            alt=""
            className="food-img"
            draggable="false"
          />
        </div>

        {/* ── Título ─────────────────────────────────── */}
        <div className="login-title">
          <h2>Iniciar Sesión</h2>
          <p>Accede al panel administrativo</p>
        </div>

        {/* ── Formulario ─────────────────────────────── */}
        <LoginForm onSuccess={onSuccess} />

      </section>
    </main>
  );
}
