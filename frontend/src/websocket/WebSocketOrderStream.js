/**
 * WebSocketOrderStream — una sola conexión, reconexión con backoff.
 * No abre una segunda conexión si ya hay una activa (OPEN o CONNECTING).
 */
export default class WebSocketOrderStream {
  #ws         = null;
  #url        = null;
  #onMsg      = null;
  #onStatus   = null;
  #retries    = 0;
  #maxRetries = 5;
  #baseDelay  = 2000;
  #timer      = null;
  #destroyed  = false;

  constructor(url, onMessage, onStatus = () => {}) {
    this.#url      = url;
    this.#onMsg    = onMessage;
    this.#onStatus = onStatus;
  }

  connect() {
    if (this.#destroyed) return;
    // Evitar conexión duplicada
    if (this.#ws && (this.#ws.readyState === WebSocket.OPEN ||
                     this.#ws.readyState === WebSocket.CONNECTING)) return;

    this.#onStatus('connecting');
    this.#ws = new WebSocket(this.#url);

    this.#ws.onopen = () => {
      this.#retries = 0;
      this.#onStatus('open');
    };

    this.#ws.onmessage = (ev) => {
      try { this.#onMsg(JSON.parse(ev.data)); } catch { /* ignorar no-JSON */ }
    };

    this.#ws.onerror = () => { this.#onStatus('error'); };

    this.#ws.onclose = () => {
      this.#onStatus('closed');
      if (!this.#destroyed) this.#scheduleReconnect();
    };
  }

  disconnect() {
    this.#destroyed = true;
    clearTimeout(this.#timer);
    this.#ws?.close();
    this.#ws = null;
  }

  #scheduleReconnect() {
    if (this.#retries >= this.#maxRetries) return;
    this.#retries++;
    const delay = this.#baseDelay * this.#retries;
    this.#timer = setTimeout(() => this.connect(), delay);
  }
}
