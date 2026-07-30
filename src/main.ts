import Vue from 'vue';
import Chat from './chat/Chat.vue';
import ThemeBackground from './components/ThemeBackground.vue';
import LockScreen from './components/LockScreen.vue';
import './scss/main.scss';
import { appearance } from './platform/appearance';
import { App as CapacitorApp } from '@capacitor/app';

// Root shell: background layer (z-index -1) behind Chat, lock screen
// (z-index 9999) on top of everything, mounted as siblings so neither
// requires modifying Chat.vue itself.
const Root = Vue.extend({
  render(h) {
    return h('div', [
      h(ThemeBackground),
      h(Chat),
      h(LockScreen, { ref: 'lockScreen' })
    ]);
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
