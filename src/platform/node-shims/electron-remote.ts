// Stub for '@electron/remote', used by learn/eicon/store.ts for two things:
// remote.app.getPath('userData') (building a cache file path — irrelevant
// since fs is also stubbed and that whole call is inside a try/catch) and
// remote.ipcMain.emit('eicons.reload', ...) (cross-window notification,
// same "no other windows to notify" situation as the ad-coordinator —
// see chat/ads/ad-coordinator-guest.ts's comment).
export const app = {
  getPath: (_name: string) => '/unsupported'
};
export const ipcMain = {
  emit: (_event: string, ..._args: any[]) => {}
};
// learn/profile-cache.ts uses these to push your own avatar/color updates
// to other open desktop windows (`parent.webContents.send(...)`). Single-
// window mobile has no other windows to push to — returning undefined here
// correctly falls through profile-cache.ts's own existing
// `if (parent) { ... }` guard, so this is a real no-op, not a crash.
export const getCurrentWindow = () => undefined;
export const BrowserWindow = {
  getAllWindows: () => [] as any[]
};
export default { app, ipcMain, getCurrentWindow, BrowserWindow };
