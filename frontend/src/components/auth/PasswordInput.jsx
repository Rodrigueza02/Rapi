import { useState } from 'react';
import { LockKeyhole, Eye, EyeOff } from 'lucide-react';

export default function PasswordInput({ value, onChange }) {
  const [show, setShow] = useState(false);

  return (
    <label className="field">
      <div className="field-control">
        <LockKeyhole size={18} />
        <input
          type={show ? 'text' : 'password'}
          autoComplete="current-password"
          placeholder="••••••••••"
          value={value}
          onChange={e => onChange(e.target.value)}
          required
        />
        <button
          type="button"
          className="eye"
          onClick={() => setShow(v => !v)}
          aria-label={show ? 'Ocultar contraseña' : 'Mostrar contraseña'}
        >
          {show ? <EyeOff size={18} /> : <Eye size={18} />}
        </button>
      </div>
    </label>
  );
}
