// Persisted appearance settings: font scale/family, background theme.
// Backs the (not-yet-built) settings UI — wire a slider/picker to these
// setters. Values are applied as CSS custom properties on :root so every
// existing Horizon component picks them up for free, with no per-component
// changes needed (see scss/mobile/_fonts.scss and _responsive.scss).
import { Preferences } from '@capacitor/preferences';

const KEYS = {
  fontScale: 'appearance.fontScale',
  fontFamily: 'appearance.fontFamily'
};

export interface AppearanceSettings {
  fontScale: number;      // e.g. 0.85 - 1.5
  fontFamily: string;     // CSS font-family value, or 'inherit'
}

const DEFAULTS: AppearanceSettings = {
  fontScale: 1,
  fontFamily: 'inherit'
};

function apply(settings: AppearanceSettings): void {
  document.documentElement.style.setProperty(
    '--horizon-font-scale',
    String(settings.fontScale)
  );
  document.documentElement.style.setProperty(
    '--horizon-font-family',
    settings.fontFamily
  );
}

export const appearance = {
  async load(): Promise<AppearanceSettings> {
    const [scale, family] = await Promise.all([
      Preferences.get({ key: KEYS.fontScale }),
      Preferences.get({ key: KEYS.fontFamily })
    ]);
    const settings: AppearanceSettings = {
      fontScale: scale.value ? Number(scale.value) : DEFAULTS.fontScale,
      fontFamily: family.value ?? DEFAULTS.fontFamily
    };
    apply(settings);
    return settings;
  },

  async setFontScale(scale: number): Promise<void> {
    await Preferences.set({ key: KEYS.fontScale, value: String(scale) });
    document.documentElement.style.setProperty('--horizon-font-scale', String(scale));
  },

  async setFontFamily(family: string): Promise<void> {
    await Preferences.set({ key: KEYS.fontFamily, value: family });
    document.documentElement.style.setProperty('--horizon-font-family', family);
  }
};
