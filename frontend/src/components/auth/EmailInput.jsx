import { Mail } from 'lucide-react';

export default function EmailInput({ value, onChange }) {
  return (
    <label className="field">
      <div className="field-control">
        <Mail size={18} />
        <input
          type="email"
          autoComplete="username"
          placeholder="Correo electrónico"
          value={value}
          onChange={e => onChange(e.target.value)}
          required
        />
      </div>
    </label>
  );
}
