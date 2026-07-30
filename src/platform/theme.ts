// Custom background theming: solid color, PNG/image, or video, plus the
// readability controls (dim overlay + panel translucency/blur) needed to
// keep chat text legible over a busy background.
import { Preferences } from '@capacitor/preferences';
import { Filesystem, Directory } from '@capacitor/filesystem';
import { Capacitor } from '@capacitor/core';

export type BackgroundType = 'none' | 'color' | 'image' | 'video';

export interface ThemeSettings {
  backgroundType: BackgroundType;
  backgroundColor: string;     // used when type === 'color'
  backgroundUri: string;       // local file:// or content:// uri, image/video
  overlayOpacity: number;      // 0-1, darkens the background for contrast
  panelOpacity: number;        // 0-1, translucency of chat panels over bg
  panelBlur: number;           // px, backdrop-filter blur on chat panels
}

const KEY = 'theme.settings';

export const DEFAULT_THEME: ThemeSettings = {
  backgroundType: 'none',
  backgroundColor: '#1e1e1e',
  backgroundUri: '',
  overlayOpacity: 0.35,
  panelOpacity: 0.85,
  panelBlur: 8
};

function applyCssVars(settings: ThemeSettings): void {
  const root = document.documentElement.style;
  root.setProperty('--horizon-overlay-opacity', String(settings.overlayOpacity));
  root.setProperty('--horizon-panel-opacity', String(settings.panelOpacity));
  root.setProperty('--horizon-panel-blur', `${settings.panelBlur}px`);
}

export const theme = {
  async load(): Promise<ThemeSettings> {
    const res = await Preferences.get({ key: KEY });
    const settings: ThemeSettings = res.value
      ? { ...DEFAULT_THEME, ...JSON.parse(res.value) }
      : DEFAULT_THEME;
    applyCssVars(settings);
    return settings;
  },

  async save(settings: ThemeSettings): Promise<void> {
    await Preferences.set({ key: KEY, value: JSON.stringify(settings) });
    applyCssVars(settings);
  },

  // Copies a picked background file into the app's private storage so it
  // survives even if the original file (e.g. from Downloads) gets deleted,
  // and so we're not depending on a content:// uri's permission grant
  // staying valid across app restarts.
  async importBackgroundFile(sourceUri: string, extension: string): Promise<string> {
    const destPath = `theme-background.${extension}`;
    const response = await fetch(sourceUri);
    const blob = await response.blob();
    const base64 = await blobToBase64(blob);
    await Filesystem.writeFile({
      path: destPath,
      directory: Directory.Data,
      data: base64
    });
    const uriRes = await Filesystem.getUri({ path: destPath, directory: Directory.Data });
    return Capacitor.convertFileSrc(uriRes.uri);
  }
};

function blobToBase64(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      const result = reader.result as string;
      resolve(result.split(',')[1]); // strip the data: prefix
    };
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}
