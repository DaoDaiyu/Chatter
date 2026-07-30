// Stub for 'adm-zip', used by chat/Logs.vue's log-export-as-zip feature
// (downloadConversation/downloadCharacter). adm-zip depends on Node's fs
// and Buffer internals — it doesn't run in a browser/WebView at all, and
// unlike the other stubs in this folder, the call sites in Logs.vue are
// NOT wrapped in try/catch upstream, so this throws with a clear message
// on first use rather than failing confusingly deep inside a fake zip
// implementation.
//
// This is a real, unported feature, not just a shim gap: chat/zip.ts
// (already in this codebase) is a browser-safe Blob-based zip writer used
// for other exports — Logs.vue's export buttons should be switched to use
// that (or 'jszip') instead of adm-zip. Until then, clicking "export as
// zip" in Logs will throw this error rather than silently produce a
// broken file.
export default class AdmZip {
  constructor() {
    throw new Error(
      'Zip export is not implemented on mobile yet — adm-zip depends on Node APIs. ' +
      'See src/platform/node-shims/adm-zip.ts for the fix needed (switch Logs.vue to chat/zip.ts or jszip).'
    );
  }
}
