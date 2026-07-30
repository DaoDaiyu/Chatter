// Replaces electron/common.ts GeneralSettings for the Android/Capacitor build.
// Trimmed to the fields chat/core.ts and learn/cache-manager.ts actually read.
// Cross-check against horizon-src/electron/common.ts if Horizon adds new settings
// and you rebase onto a newer upstream version.
export interface GeneralSettings {
    account: string;
    host: string;
    theme: string;
    // ...extend as needed by diffing against upstream electron/common.ts
    [key: string]: any;
}

export const defaultGeneralSettings: GeneralSettings = {
    account: '',
    host: 'wss://chat.f-list.net/chat2',
    theme: 'default'
};
