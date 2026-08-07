import Vue from 'vue';
import Chat from './chat/Chat.vue';
import Login from './chat/Login.vue';
import ThemeBackground from './components/ThemeBackground.vue';
import LockScreen from './components/LockScreen.vue';
import './scss/main.scss';
import l, { lp } from './chat/localize';
import core, { init as initCore } from './chat/core';
import Connection from './fchat/connection';
import Socket from './chat/WebSocket';
import Notifications from './chat/notifications';
import { SettingsStore } from './platform/settings-store';
import { MemoryLogs } from './platform/logs';
import { defaultGeneralSettings } from './platform/common';
import { SimpleCharacter } from './interfaces';
import { appearance } from './platform/appearance';
import { App as CapacitorApp } from '@capacitor/app';

// Templates across the app call l()/lp() on the component instance; upstream's
// Electron entry registered them on Vue's prototype and the scaffold's stub
// main.ts dropped that, which blanked every child component that uses them.
(Vue.prototype as any).l = l;
(Vue.prototype as any).lp = lp;

// On-device debug overlay: surfaces uncaught errors, promise rejections,
// console.error, and Vue render/lifecycle errors on screen, so failures that
// otherwise freeze or blank the WebView are visible without USB debugging.
// TEMPORARY diagnostic — remove once the mobile issues are resolved.
function setupDebugOverlay(): void {
  try {
    const box = document.createElement('div');
    box.id = 'horizon-debug-overlay';
    box.style.cssText =
      'position:fixed;left:0;right:0;bottom:0;max-height:45vh;overflow:auto;' +
      'z-index:2147483647;background:rgba(140,0,0,.92);color:#fff;' +
      'font:11px/1.35 monospace;padding:6px;white-space:pre-wrap;' +
      'padding-bottom:calc(6px + env(safe-area-inset-bottom,0px));';
    const header = document.createElement('div');
    header.textContent = 'debug — tap to hide';
    header.style.cssText = 'font-weight:bold;cursor:pointer;margin-bottom:4px;';
    header.addEventListener('click', () => (box.style.display = 'none'));
    box.appendChild(header);
    const list = document.createElement('div');
    box.appendChild(list);

    let lastText = '';
    let lastLine: HTMLDivElement | undefined;
    let repeat = 0;
    const add = (msg: string) => {
      if (!box.isConnected && document.body) document.body.appendChild(box);
      box.style.display = 'block';
      if (msg === lastText && lastLine) {
        repeat++;
        lastLine.textContent = `${ts()} (x${repeat + 1}) ${msg}`;
        return;
      }
      repeat = 0;
      lastText = msg;
      const line = document.createElement('div');
      line.textContent = `${ts()} ${msg}`;
      list.appendChild(line);
      lastLine = line;
      while (list.childNodes.length > 40) list.removeChild(list.firstChild!);
    };
    const ts = () => new Date().toISOString().substr(11, 8);
    const str = (v: any) =>
      v instanceof Error ? v.stack || v.message : String(v);

    window.addEventListener('error', e =>
      add('ERROR: ' + ((e as ErrorEvent).error?.stack || (e as ErrorEvent).message))
    );
    window.addEventListener('unhandledrejection', e =>
      add('REJECT: ' + str((e as PromiseRejectionEvent).reason))
    );
    const origError = console.error.bind(console);
    console.error = (...args: any[]) => {
      add('console.error: ' + args.map(str).join(' '));
      origError(...args);
    };
    Vue.config.errorHandler = (err, _vm, info) => {
      add(`VUE[${info}]: ` + str(err));
      origError(err);
    };

    if (document.body) document.body.appendChild(box);
    else
      window.addEventListener('DOMContentLoaded', () =>
        document.body.appendChild(box)
      );
  } catch {
    /* overlay must never break the app */
  }
}
setupDebugOverlay();

const CLIENT_NAME = 'Horizon';
const CLIENT_VERSION = '0.1.0';

// Login → core.init → character select. This orchestration lived in the
// Electron renderer entry (electron/chat.ts), which wasn't part of the
// scaffold; without it Chat.vue mounts with no characters and no initialized
// core, so nothing renders. `session` drives which screen is shown.
const session = Vue.observable({
  ready: false,
  characters: [] as SimpleCharacter[],
  defaultCharacter: 0
});

interface LoginPayload {
  account: string;
  password: string;
  characters: SimpleCharacter[];
  defaultCharacter: number;
}

function onLogin(payload: LoginPayload): void {
  const connection = new Connection(CLIENT_NAME, CLIENT_VERSION, Socket);
  // The string form makes the connection acquire (and refresh) its own API
  // ticket from the password — matching upstream's ticket provider.
  connection.setCredentials(payload.account, payload.password);

  const settings = { ...defaultGeneralSettings, account: payload.account };
  initCore(connection, settings, MemoryLogs, SettingsStore, Notifications);

  session.characters = payload.characters;
  session.defaultCharacter = payload.defaultCharacter;
  session.ready = true;
}

const Root = Vue.extend({
  render(h) {
    return h(
      'div',
      {
        // Keep interactive UI out from under the status bar, navigation bar and
        // display cutout. Android 15 (targetSdk 35) forces edge-to-edge, so the
        // WebView fills the whole screen; viewport-fit=cover (index.html) exposes
        // these insets. ThemeBackground is position:fixed and stays full-bleed
        // behind this padding, which is what we want.
        style: {
          height: '100%',
          boxSizing: 'border-box',
          paddingTop: 'env(safe-area-inset-top, 0px)',
          paddingBottom: 'env(safe-area-inset-bottom, 0px)',
          paddingLeft: 'env(safe-area-inset-left, 0px)',
          paddingRight: 'env(safe-area-inset-right, 0px)'
        }
      },
      [
        h(ThemeBackground),
        session.ready
          ? h(Chat, {
              props: {
                ownCharacters: session.characters,
                defaultCharacter: session.defaultCharacter,
                version: CLIENT_VERSION
              }
            })
          : h(Login, { on: { login: onLogin } }),
        h(LockScreen, { ref: 'lockScreen' })
      ]
    );
  }
});

async function bootstrap(): Promise<void> {
  await appearance.load(); // applies saved font scale/family before mount

  const root = new Root().$mount('#app');

  // Re-lock on returning from background. isActive === false covers both
  // backgrounding and the app being killed/restarted by Android.
  CapacitorApp.addListener('appStateChange', ({ isActive }) => {
    if (!isActive) {
      (root.$refs.lockScreen as any)?.show?.();
    }
  });
}

bootstrap();
