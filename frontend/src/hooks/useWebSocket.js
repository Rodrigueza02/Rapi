import { useEffect, useRef, useState } from 'react';
import WebSocketOrderStream from '../websocket/WebSocketOrderStream.js';

/**
 * Abre el WebSocket al dashboard-backend (:8086/ws) y devuelve
 * el último evento recibido y el estado de la conexión.
 *
 * @param {function} onEvent  callback que recibe cada evento { order_id, driver_id, state, last_updated }
 * @returns {{ wsStatus }}    'connecting' | 'open' | 'closed' | 'error'
 */
export default function useWebSocket(onEvent) {
  const [wsStatus, setWsStatus] = useState('connecting');
  const streamRef = useRef(null);

  useEffect(() => {
    // En desarrollo: usa VITE_WS_URL si está definida, o conecta al puerto local.
    // En producción (Nginx sobre HTTPS): deriva la URL del mismo dominio con wss://.
    // Vite sustituye import.meta.env.* en tiempo de build; si la variable no está
    // definida, se infiere del origen del navegador para no hardcodear localhost.
    let url = import.meta.env.VITE_WS_URL;
    if (!url) {
      const proto  = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const host   = window.location.host;          // ej: rappi-stream.duckdns.org
      url = `${proto}//${host}/ws`;
    }

    const stream = new WebSocketOrderStream(url, onEvent, setWsStatus);
    streamRef.current = stream;
    stream.connect();

    return () => stream.disconnect();
  }, [onEvent]);

  return { wsStatus };
}
