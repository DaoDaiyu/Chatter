# Impression Lab — voice impression practice app

A sleek Android app (built for the Pixel 8 Pro, works on any modern Android 10+ phone)
for practicing character voice impressions like Rivet from Ratchet & Clank.

## How it works

The app mirrors a simple folder structure:

```
Character (e.g. "Rivet")
└── Reference voice line (e.g. "Rivet voice lines 1")   ← imported audio file
    ├── ▶ the reference audio (play, pause, scrub)
    ├── 🎙 your recordings, auto-named "Attempt 1 ; Rivet voice lines 1", "Attempt 2 ; …"
    └── 📝 notes on what to improve (auto-saved)
```

- **Home screen** — your characters as folder cards. Long-press to delete.
- **Character screen** — the reference voice lines you've imported. The play button
  previews a line right from the list; tapping the card opens its practice space.
- **Practice screen** — play/scrub the reference, hit the big mic button to record an
  attempt (auto-named and numbered), re-listen to any attempt, and keep improvement
  notes that save as you type.

## Sleek bits

- Animated splash screen (system splash + sonar-ring logo intro)
- Animated screen transitions, pulsing record button driven by live mic amplitude
- **6 switchable color schemes** ("Rivet Rust" is the default) + light/dark/system mode
- **User-set background image** (photo picker) shown behind every screen with a
  readability scrim, glass-style cards on top

## Building & installing

1. Open the `rivet-voice-app` folder in Android Studio (Ladybug or newer).
2. Let Gradle sync, then hit **Run** with your Pixel 8 Pro connected
   (USB debugging enabled), or build an APK:
   ```
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

No account, no network — everything is stored privately in the app's own storage.

## Adding reference voice lines

Save the audio clips (mp3/m4a/wav/ogg) anywhere on the phone (Downloads works),
then in the app: open a character → **+ Voice line** → pick the file. The file's
name becomes the voice line's name.
