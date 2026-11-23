export default function LoginOptions({ remember, onRememberChange, onForgotPassword }) {
  return (
    <div className="login-options">
      <label className="remember-label">
        <input
          type="checkbox"
          checked={remember}
          onChange={e => onRememberChange(e.target.checked)}
        />
        Recordarme
      </label>
      <button type="button" className="text-button" onClick={onForgotPassword}>
        ¿Olvidaste tu contraseña?
      </button>
    </div>
  );
}
