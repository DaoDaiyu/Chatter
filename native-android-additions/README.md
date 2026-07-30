# Native Android additions (apply AFTER `npx cap add android`)

Capacitor only generates the `android/` project when you run `npx cap add
android` locally — it doesn't exist in this scaffold. These files are
written to be dropped into that generated project afterward. They are
**untested** — I don't have Android Studio/the SDK in this environment to
compile or run them. Treat this as a correct-looking starting point that
needs a real build-and-run pass, not finished code.

## Why this is needed at all

Android aggressively suspends or kills backgrounded apps' WebViews (Doze
mode, App Standby Buckets) to save battery. A pure-web/Capacitor app has no
way to prevent this — surviving in the background to keep the F-Chat
WebSocket alive requires a **foreground service**, which needs a
persistent notification (Android requires this — it's not optional, it's
how the user knows something is running and draining battery) and real
Kotlin code registered in the Android manifest, not JS.

## Files

- `app/src/main/java/moe/horizn/mobile/KeepAliveService.kt` — minimal
  foreground service holding a partial wake lock while the app is
  backgrounded.
- `AndroidManifest-additions.xml` — the `<service>` declaration and
  permissions to merge into the generated `android/app/src/main/
  AndroidManifest.xml` (don't overwrite the whole file — merge these
  entries in).
- `MainActivity-additions.kt` — snippet showing where to start/stop the
  service from `MainActivity.onPause`/`onResume` (or better, from a small
  Capacitor plugin you register and call from `src/platform/keepalive.ts`,
  which is stubbed but not implemented — this is the missing link between
  your Vue app and this native service).

## What's still missing

- The actual JS↔native bridge. This service can be started, but nothing in
  `src/` calls it yet — you need either a custom Capacitor plugin
  (`@capacitor/cli plugin:generate`) or `@capacitor-community/background-mode`
  (a existing plugin that does most of this — probably less work than
  hand-rolling the bridge for this Kotlin file).
- Testing whether Android's battery optimization still kills the WebSocket
  connection despite the foreground service on specific OEM skins (Samsung,
  Xiaomi, etc. are notorious for killing foreground services anyway) — this
  needs real-device testing, not something to assume works.
