/**
 * WebSocketOrderStream
 *
 * Abre una conexión WebSocket a ws://localhost:8086/ws y llama a
 * onMessage(event) cada vez que llega un evento de pedido.
 *
 * El backend envía objetos con forma:
 *   { order_id, driver_id, state, last_updated }
 *
 * Principio SRP: solo gestiona la conexión WS, no toca React.
 * Principio OCP: se puede extender pasando distintos handlers sin modificar.
 */
export default class WebSocketOrderStream {
  #ws       = null;
  #url      = null;
  #onMsg    = null;
  #onStatus = null;
  #retries  = 0;
  #maxRetries = 5;
  #retryDelay = 2000;  // ms, se duplica en cada intento

  /**
   * @param {string}   url       URL del WebSocket (ws://localhost:8086/ws)
   * @param {function} onMessage handler que recibe el objeto de evento ya parseado
   * @param {function} onStatus  handler para estado: 'connecting' | 'open' | 'closed' | 'error'
   */
  constructor(url, onMessage, onStatus = () => {}) {
    this.#url      = url;
    this.#onMsg    = onMessage;
    this.#onStatus = onStatus;
  }

  connect() {
    this.#onStatus('connecting');
    this.#ws = new WebSocket(this.#url);

    this.#ws.onopen = () => {
      this.#retries = 0;
      this.#onStatus('open');
    };

    this.#ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        this.#onMsg(data);
      } catch {
        // mensaje no JSON — ignorar
      }
    };

    this.#ws.onerror = () => {
      this.#onStatus('error');
    };

    this.#ws.onclose = () => {
      this.#onStatus('closed');
      this.#scheduleReconnect();
    };
  }

  disconnect() {
    this.#retries = this.#maxRetries; // evita reconexión
    this.#ws?.close();
    this.#ws = null;
  }

  #scheduleReconnect() {
    if (this.#retries >= this.#maxRetries) return;
    this.#retries++;
    const delay = this.#retryDelay * this.#retries;
    setTimeout(() => this.connect(), delay);
  }
}
