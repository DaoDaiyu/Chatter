// Drop-in replacement for the 'electron-log' import used across chat/, fchat/,
// learn/ and site/. It MUST expose every log level those modules call —
// electron-log has error/warn/info/verbose/debug/silly. A missing method is
// not harmless: fchat/connection.ts calls log.silly('socket.recv', …) on every
// incoming WebSocket message, so an undefined log.silly threw on the first
// server message, aborting message handling and hanging the client forever on
// "connecting". Keep all levels defined.
//
// Swap for a real mobile logger (e.g. capacitor-native-log-writer) once you
// need persisted logs for bug reports.
/* eslint-disable @typescript-eslint/no-explicit-any */
const log = {
  error: (...args: any[]) => console.error('[fchat]', ...args),
  warn: (...args: any[]) => console.warn('[fchat]', ...args),
  info: (...args: any[]) => console.info('[fchat]', ...args),
  verbose: (...args: any[]) => console.debug('[fchat]', ...args),
  debug: (...args: any[]) => console.debug('[fchat]', ...args),
  // Per-message firehose (e.g. every socket frame) — keep it a no-op so it
  // stays callable without flooding the console.
  silly: (..._args: any[]) => {
    /* intentionally quiet */
  }
};
export default log;
