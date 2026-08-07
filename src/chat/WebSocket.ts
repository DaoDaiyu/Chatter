import { WebSocketConnection } from '../fchat';
import log from '../platform/log'; //tslint:disable-line:match-default-export-name

// How long to wait for the chat WebSocket to reach OPEN before treating it as
// a failed connection. Without this, a socket that never connects (blocked or
// silently dropped) sits in CONNECTING forever and the UI shows a permanent
// "connecting" spinner with no error. Surfacing an error lets the connection
// layer report it and retry.
const CONNECT_TIMEOUT_MS = 20000;

export default class Socket implements WebSocketConnection {
  static host = 'wss://chat.f-list.net/chat2';
  private socket: WebSocket;
  private lastHandler: Promise<void> = Promise.resolve();
  private connectTimer: ReturnType<typeof setTimeout> | undefined;
  private errorHandler: ((error: Error) => void) | undefined;
  private opened = false;

  constructor() {
    log.debug('socket.connecting', { host: Socket.host });
    this.socket = new WebSocket(Socket.host);

    this.connectTimer = setTimeout(() => {
      if (!this.opened) {
        const err = new Error(
          `Chat server connection timed out after ${CONNECT_TIMEOUT_MS / 1000}s ` +
            `(state ${this.socket.readyState}) connecting to ${Socket.host}`
        );
        log.error('socket.connectTimeout', {
          readyState: this.socket.readyState
        });
        if (this.errorHandler !== undefined) this.errorHandler(err);
        try {
          this.socket.close();
        } catch {
          /* ignore */
        }
      }
    }, CONNECT_TIMEOUT_MS);
  }

  get readyState(): WebSocketConnection.ReadyState {
    return this.socket.readyState;
  }

  close(): void {
    log.debug('socket.close');
    if (this.connectTimer !== undefined) clearTimeout(this.connectTimer);
    this.socket.close();
  }

  onMessage(handler: (message: string) => void): void {
    this.socket.addEventListener('message', e => {
      this.lastHandler = this.lastHandler.then(
        () => handler(<string>e.data),
        err => {
          window.requestAnimationFrame(() => {
            throw err;
          });
          handler(<string>e.data);
        }
      );
    });
  }

  onOpen(handler: () => void): void {
    this.socket.addEventListener('open', () => {
      this.opened = true;
      if (this.connectTimer !== undefined) clearTimeout(this.connectTimer);
      log.debug('socket.open', { host: Socket.host });
      handler();
    });
  }

  onClose(handler: (e: CloseEvent) => void): void {
    this.socket.addEventListener('close', handler);
  }

  onError(handler: (error: Error) => void): void {
    this.errorHandler = handler;
    //The Socket class itself does not pass its error message back when instantiating it fails and only prints it in the console
    //So unfortunately you get this hardcoded nonsense
    this.socket.addEventListener('error', () =>
      handler(
        new Error('Unable to create Websocket handler for ' + Socket.host)
      )
    );
  }

  send(message: string): void {
    this.socket.send(message);
  }
}
