import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'moe.horizn.mobile',   // placeholder — change before publishing
  appName: 'Horizon',
  webDir: 'dist',
  android: {
    allowMixedContent: false
  },
  plugins: {
    // Route fetch/XHR (hence Axios) through native HTTP so the app's requests
    // to f-list.net — the login ticket, character data, the site session —
    // aren't blocked by the WebView's CORS policy. The chat WebSocket is
    // unaffected either way.
    CapacitorHttp: {
      enabled: true
    }
  }
};

export default config;
