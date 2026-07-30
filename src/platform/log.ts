// Drop-in replacement for the 'electron-log' import used by fchat/connection.ts.
// Swap for a real mobile logger (e.g. capacitor-native-log-writer) once you
// need persisted logs for bug reports.
const log = {
    info: (...args: any[]) => console.info('[fchat]', ...args),
    warn: (...args: any[]) => console.warn('[fchat]', ...args),
    error: (...args: any[]) => console.error('[fchat]', ...args),
    debug: (...args: any[]) => console.debug('[fchat]', ...args)
};
export default log;
