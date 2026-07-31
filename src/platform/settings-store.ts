// localStorage-backed Settings.Store for the Capacitor WebView build. Replaces
// the Electron electron-settings file store that core.init used to receive.
//
// Keys in Settings.Keys are per-character (settings, pinned, recent, …). We
// namespace every stored value by character — the connected character when the
// caller doesn't pass one explicitly (Logs.vue browses other characters by
// passing the name). An index of characters that have saved data backs
// getAvailableCharacters().
import { Settings } from '../chat/interfaces';
import core from '../chat/core';

const PREFIX = 'horizon.settings';
const INDEX_KEY = `${PREFIX}.__characters`;
const GLOBAL = '__global';

function currentCharacter(): string {
  try {
    return core.connection?.character || GLOBAL;
  } catch {
    return GLOBAL;
  }
}

export class SettingsStore implements Settings.Store {
  async get<K extends keyof Settings.Keys>(
    key: K,
    character?: string
  ): Promise<Settings.Keys[K] | undefined> {
    const c = character ?? currentCharacter();
    const raw = localStorage.getItem(`${PREFIX}.${c}.${String(key)}`);
    if (raw === null) return undefined;
    try {
      return JSON.parse(raw) as Settings.Keys[K];
    } catch {
      return undefined;
    }
  }

  async set<K extends keyof Settings.Keys>(
    key: K,
    value: Settings.Keys[K]
  ): Promise<void> {
    const c = currentCharacter();
    localStorage.setItem(`${PREFIX}.${c}.${String(key)}`, JSON.stringify(value));
    if (c !== GLOBAL) this.rememberCharacter(c);
  }

  async getAvailableCharacters(): Promise<ReadonlyArray<string>> {
    const raw = localStorage.getItem(INDEX_KEY);
    if (raw === null) return [];
    try {
      const list = JSON.parse(raw);
      return Array.isArray(list) ? list : [];
    } catch {
      return [];
    }
  }

  private rememberCharacter(character: string): void {
    const raw = localStorage.getItem(INDEX_KEY);
    let list: string[] = [];
    try {
      const parsed = raw === null ? [] : JSON.parse(raw);
      if (Array.isArray(parsed)) list = parsed;
    } catch {
      list = [];
    }
    if (!list.includes(character)) {
      list.push(character);
      localStorage.setItem(INDEX_KEY, JSON.stringify(list));
    }
  }
}
