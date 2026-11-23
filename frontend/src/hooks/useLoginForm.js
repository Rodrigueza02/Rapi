import { useState } from 'react';
import { signIn } from '../services/authService';

/**
 * Hook que maneja el estado y la lógica del formulario de login.
 * Principio SRP: solo sabe de formulario + autenticación, no de rutas.
 *
 * @param {(session: Session) => void} onSuccess  callback al autenticar
 */
export default function useLoginForm(onSuccess) {
  const [email,    setEmail]    = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [message,  setMessage]  = useState('');
  const [loading,  setLoading]  = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setMessage('');
    setLoading(true);
    try {
      const session = await signIn(email, password, remember);
      onSuccess(session);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  }

  return {
    email,    setEmail,
    password, setPassword,
    remember, setRemember,
    message,  setMessage,
    loading,
    handleSubmit,
  };
}
