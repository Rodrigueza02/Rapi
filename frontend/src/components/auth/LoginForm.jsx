import EmailInput    from './EmailInput';
import PasswordInput from './PasswordInput';
import LoginOptions  from './LoginOptions';
import LoginButton   from './LoginButton';
import useLoginForm  from '../../hooks/useLoginForm';

export default function LoginForm({ onSuccess }) {
  const form = useLoginForm(onSuccess);

  function handleForgotPassword() {
    form.setMessage('La recuperación de contraseña aún no está implementada.');
  }

  return (
    <form className="login-form" onSubmit={form.handleSubmit} noValidate>
      <EmailInput
        value={form.email}
        onChange={form.setEmail}
      />
      <PasswordInput
        value={form.password}
        onChange={form.setPassword}
      />
      <LoginOptions
        remember={form.remember}
        onRememberChange={form.setRemember}
        onForgotPassword={handleForgotPassword}
      />
      {form.message && (
        <p className="form-error" role="alert">{form.message}</p>
      )}
      <LoginButton loading={form.loading} />
    </form>
  );
}
