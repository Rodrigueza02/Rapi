import { useEffect, useRef, useState } from 'react';
import WebSocketOrderStream from '../websocket/WebSocketOrderStream.js';

/**
 * Abre el WebSocket al dashboard-backend (:8086/ws) y devuelve el estado
 * de la conexión.
 *
 * DISEÑO CLAVE — sin reconexiones por inestabilidad del callback:
 *   El callback `onEvent` se guarda en un ref interno. El useEffect solo
 *   depende de la URL (estable entre renders) y no de `onEvent`, por lo que
 *   la conexión se crea exactamente una vez al montar y se destruye al
 *   desmontar. Los cambios posteriores en `onEvent` (ej: cuando los hooks
 *   internos de los hijos actualizan sus referencias) se ven automáticamente
 *   a través del ref, sin abrir ni cerrar conexiones adicionales.
 *
 * @param {function} onEvent  callback que recibe cada evento del WebSocket.
 *                            No necesita ser estable; puede ser una función
 *                            inline o un useCallback.
 * @returns {{ wsStatus }}    'connecting' | 'open' | 'closed' | 'error'
 */
export default function useWebSocket(onEvent) {
  const [wsStatus, setWsStatus] = useState('connecting');

  // Mantiene siempre la referencia más reciente del callback sin
  // forzar una re-ejecución del efecto.
  const onEventRef = useRef(onEvent);
  useEffect(() => { onEventRef.current = onEvent; }, [onEvent]);

  useEffect(() => {
    // Derivar la URL del origen del navegador para que funcione tanto en
    // desarrollo (ws://localhost) como en producción (wss://dominio.org).
    // VITE_WS_URL permite sobreescribir en casos especiales.
    let url = import.meta.env.VITE_WS_URL;
    if (!url) {
      const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const host  = window.location.host;
      url = `${proto}//${host}/ws`;
    }

    // Adaptador estable: siempre delega al ref más reciente.
    const stableHandler = (event) => onEventRef.current?.(event);

    const stream = new WebSocketOrderStream(url, stableHandler, setWsStatus);
    stream.connect();

    return () => stream.disconnect();
  }, []); // ← sin dependencias: una sola conexión por ciclo de vida del componente

  return { wsStatus };
}
