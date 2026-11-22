/**
 * useSession.js
 * SRP: única responsabilidad — verificar y mantener el estado de sesión.
 * No sabe nada de rutas ni de layout.
 */

import { useEffect, useState } from 'react';
import { readSession, clearSession, verifySession } from '../services/authService.js';

export default function useSession() {
  const [session,  setSession]  = useState(readSession);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    let active = true;
    verifySession(readSession()).then(s => {
      if (active) { setSession(s); setChecking(false); }
    });
    return () => { active = false; };
  }, []);

  function login(s)  { setSession(s); }
  function logout()  { clearSession(); setSession(null); }

  return { session, checking, login, logout };
}
