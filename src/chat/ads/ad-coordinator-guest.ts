// Mobile port: replaces the Electron IPC-based cross-window ad coordinator.
// Desktop Horizon uses this to serialize ad posting across multiple pop-out
// windows (via ipcRenderer/ipcMain). A single-Activity Android app has only
// one window, so there's nothing to coordinate with — every request is
// granted immediately. Same public API as upstream so chat/core.ts and
// chat/ads/ad-center.ts need no changes.
import core from '../core';

export class AdCoordinatorGuest {
  async requestTurnToPostAd(): Promise<void> {
    console.debug('adid.request.granted-immediately', core.characters.ownCharacter?.name);
    return Promise.resolve();
  }

  clear(): void {
    // no pending state to clear in the single-window model
  }
}
