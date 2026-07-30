// Stub for Node's 'fs', aliased in vite.config.ts for the handful of
// upstream files that still import it directly (learn/eicon/store.ts,
// chat/SettingsView.vue). There is no synchronous filesystem API in a
// browser/WebView, so these always throw — every call site that reaches
// this was checked and is wrapped in try/catch that degrades gracefully
// (see README's "Known stubs" section). Do NOT add new unguarded callers
// of this without also adding a try/catch, or they'll crash instead of
// degrading.
function unsupported(): never {
  throw new Error('fs is not available on this platform (mobile build)');
}

export const readFileSync = unsupported;
export const writeFileSync = unsupported;
export const readdirSync = unsupported;
export const existsSync = () => false;
export default { readFileSync, writeFileSync, readdirSync, existsSync };
