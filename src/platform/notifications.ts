// Native notification backend for Android, backing chat/notifications.ts.
// window.Notification (the desktop/browser API Horizon uses natively)
// isn't reliably available inside a Capacitor WebView, and won't fire at
// all once Android backgrounds the app — so real push-style behavior on
// mobile has to go through @capacitor/local-notifications instead.
import { Capacitor } from '@capacitor/core';
import { LocalNotifications } from '@capacitor/local-notifications';

let nextId = 1;
// Capacitor local notifications need a stable numeric id per "slot" so a
// new PM from the same conversation replaces the old notification instead
// of stacking duplicates — mirrors the `tag`/`renotify` behavior Horizon's
// desktop notify() already relies on.
const idByTag = new Map<string, number>();

function idForTag(tag: string): number {
  let id = idByTag.get(tag);
  if (id === undefined) {
    id = nextId++;
    idByTag.set(tag, id);
  }
  return id;
}

// Resolve with `fallback` if `p` doesn't settle within `ms`. Used so a native
// call that never resolves can't block the caller.
function withTimeout<T>(p: Promise<T>, ms: number, fallback: T): Promise<T> {
  return Promise.race([
    p.catch(() => fallback),
    new Promise<T>(resolve => setTimeout(() => resolve(fallback), ms))
  ]);
}

export const nativeNotifications = {
  isSupported(): boolean {
    return Capacitor.isNativePlatform();
  },

  async requestPermission(): Promise<boolean> {
    // This runs inside the connection's 'connecting' phase, which the socket
    // creation awaits — so it must never hang. On Android 13+ the native
    // permission request can stall; cap it so connecting always proceeds.
    const res = await withTimeout(
      LocalNotifications.requestPermissions(),
      4000,
      { display: 'denied' as const }
    );
    return res.display === 'granted';
  },

  async notify(params: {
    tag: string;
    title: string;
    body: string;
    onTap?: () => void;
  }): Promise<void> {
    const id = idForTag(params.tag);
    await LocalNotifications.schedule({
      notifications: [
        {
          id,
          title: params.title,
          body: params.body,
          // fires immediately — this isn't a scheduled reminder, it's how
          // we surface a chat event that already happened
          schedule: { at: new Date(Date.now() + 10) }
        }
      ]
    });
    // NOTE: wiring params.onTap requires listening for
    // LocalNotifications.addListener('localNotificationActionPerformed', ...)
    // once at app startup (in main.ts) and dispatching by notification id —
    // not done per-call here since the listener should be registered once.
  }
};
