// Replaces electron/secure-store.ts (which used Electron's safeStorage).
// Uses @capacitor/preferences for now — swap in
// capacitor-secure-storage-plugin (Android Keystore-backed) before shipping
// anything that stores real F-List passwords.
import { Preferences } from '@capacitor/preferences';

export class SecureStore {
    constructor(protected storeName: string) {}

    private getKey(domain: string, account: string): string {
        return `${this.storeName}__${domain}__${account}`.replace(/[^a-zA-Z0-9_]/g, '__');
    }

    async setPassword(domain: string, account: string, password: string): Promise<void> {
        await Preferences.set({ key: this.getKey(domain, account), value: password });
    }

    async getPassword(domain: string, account: string): Promise<string | undefined> {
        const { value } = await Preferences.get({ key: this.getKey(domain, account) });
        return value ?? undefined;
    }

    async deletePassword(domain: string, account: string): Promise<void> {
        await Preferences.remove({ key: this.getKey(domain, account) });
    }
}
