// PIN-based app lock. Hashes the PIN with Web Crypto (SHA-256 + salt)
// before storing — never stores the raw PIN. This is app-level access
// control, not encryption of chat data at rest; it stops casual access to
// an unlocked phone, not a targeted attacker with device access.
//
// Biometric (fingerprint/face) unlock is NOT implemented here — Capacitor
// core has no biometrics API. Add capacitor-native-biometric or
// @aparajita/capacitor-biometric-auth and call it before falling back to
// this PIN screen; the pieces here (isEnabled/unlock state) are written
// so that plugging biometrics in later doesn't require restructuring.
import { Preferences } from '@capacitor/preferences';

const HASH_KEY = 'lock.pinHash';
const SALT_KEY = 'lock.salt';

async function sha256(text: string): Promise<string> {
  const data = new TextEncoder().encode(text);
  const digest = await crypto.subtle.digest('SHA-256', data);
  return Array.from(new Uint8Array(digest))
    .map(b => b.toString(16).padStart(2, '0'))
    .join('');
}

function randomSalt(): string {
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  return Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('');
}

export const lock = {
  async isEnabled(): Promise<boolean> {
    const res = await Preferences.get({ key: HASH_KEY });
    return !!res.value;
  },

  async setPin(pin: string): Promise<void> {
    const salt = randomSalt();
    const hash = await sha256(salt + pin);
    await Preferences.set({ key: SALT_KEY, value: salt });
    await Preferences.set({ key: HASH_KEY, value: hash });
  },

  async disable(): Promise<void> {
    await Preferences.remove({ key: HASH_KEY });
    await Preferences.remove({ key: SALT_KEY });
  },

  async verify(pin: string): Promise<boolean> {
    const [saltRes, hashRes] = await Promise.all([
      Preferences.get({ key: SALT_KEY }),
      Preferences.get({ key: HASH_KEY })
    ]);
    if (!hashRes.value) return true; // lock not enabled
    const candidate = await sha256((saltRes.value ?? '') + pin);
    return candidate === hashRes.value;
  }
};
