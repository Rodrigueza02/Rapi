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
    // En desarrollo el WS conecta directo al puerto 8086 (no pasa por el proxy de Vite)
    const url    = import.meta.env.VITE_WS_URL ?? 'ws://localhost:8086/ws';
    const stream = new WebSocketOrderStream(url, onEvent, setWsStatus);
    streamRef.current = stream;
    stream.connect();

    return () => stream.disconnect();
  }, [onEvent]);

  return { wsStatus };
}
