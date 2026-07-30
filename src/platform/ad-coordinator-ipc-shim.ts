// chat/ads/ad-coordinator-guest.ts and -host.ts use Electron's ipcRenderer /
// IpcMainEvent to sync ad-posting state between Horizon's *multiple windows*
// (main window + popped-out windows). A single-Activity Android app has no
// equivalent multi-window IPC need — the simplest correct port is to make ad
// coordination a no-op / single-instance-only feature here, OR reimplement it
// with a tiny in-memory EventTarget if you keep multi-window support via
// Capacitor's browser tabs. Flagging this as a deliberate scope decision
// rather than silently stubbing it.
export {};
