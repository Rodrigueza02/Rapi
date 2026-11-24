/**
 * FormField.jsx
 * SRP: única responsabilidad — renderizar un campo de formulario con label.
 */

export default function FormField({ label, hint, children }) {
  return (
    <div className="form-field">
      <label className="form-label">{label}</label>
      {children}
      {hint && <small className="form-hint">{hint}</small>}
    </div>
  );
}
