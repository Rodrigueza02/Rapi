/**
 * ErrorMessage.jsx
 * SRP: única responsabilidad — mostrar un mensaje de error estandarizado.
 */

export default function ErrorMessage({ message }) {
  if (!message) return null;
  return (
    <p className="form-error" role="alert">
      {message}
    </p>
  );
}
