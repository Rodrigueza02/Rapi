import { useState } from 'react';
import { X } from 'lucide-react';
import { sendEmail } from '../../api/HttpEmailApi.js';

const EMPTY = { recipientEmail: '', subject: '', bodyText: '', orderId: '' };

export default function ComposeEmailModal({ onClose, onSent }) {
  const [form,    setForm]    = useState(EMPTY);
  const [sending, setSending] = useState(false);
  const [error,   setError]   = useState(null);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSending(true);
    setError(null);
    try {
      await sendEmail(form);
      onSent?.();
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" aria-label="Redactar correo">
      <div className="modal-container">
        <div className="modal-header">
          <h2 className="modal-title">Redactar correo</h2>
          <button className="modal-close" onClick={onClose} aria-label="Cerrar"><X size={18} /></button>
        </div>
        <form onSubmit={handleSubmit} className="compose-form" noValidate>
          <label className="compose-label">
            Destinatario
            <input name="recipientEmail" type="email" required value={form.recipientEmail}
              onChange={handleChange} className="compose-input" placeholder="correo@ejemplo.com" />
          </label>
          <label className="compose-label">
            Asunto
            <input name="subject" type="text" required maxLength={255} value={form.subject}
              onChange={handleChange} className="compose-input" placeholder="Asunto del mensaje" />
          </label>
          <label className="compose-label">
            Mensaje
            <textarea name="bodyText" required value={form.bodyText}
              onChange={handleChange} className="compose-textarea" rows={5}
              placeholder="Escribe el contenido del correo…" />
          </label>
          <label className="compose-label">
            ID de pedido <span className="compose-optional">(opcional)</span>
            <input name="orderId" type="text" value={form.orderId}
              onChange={handleChange} className="compose-input" placeholder="ej. abc-123" />
          </label>
          {error && <p className="compose-error">{error}</p>}
          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>Cancelar</button>
            <button type="submit" className="btn-primary" disabled={sending}>
              {sending ? 'Enviando…' : 'Enviar correo'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
