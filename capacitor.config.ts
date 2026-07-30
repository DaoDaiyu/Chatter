import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'moe.horizn.mobile',   // placeholder — change before publishing
  appName: 'Horizon',
  webDir: 'dist',
  android: {
    allowMixedContent: false
  }
};

export default config;
