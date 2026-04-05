# Impatience App - Turn Timer

<img width="672" height="936" alt="image" src="https://github.com/user-attachments/assets/518bae71-7753-4e61-a4c9-22809b0592c8" />

A drink reminder timer for EDH (Commander) Magic: The Gathering games. Nudges you to take a sip every time your turn runs long.

## Download
- [v1.0.0](https://github.com/h0gballs/impatience-app/releases/download/v1.0.0/app-release-unsigned.apk)

## Features

- **Turn timer** — starts automatically, shows elapsed time in large monospace text
- **Drink notifications** — visual overlay, text-to-speech, and vibration when the timer fires
- **Pause / New Turn** — pause the timer or reset it for a new turn
- **Configurable interval** — set the drink reminder to any number of seconds
- **3 notification modes** — TTS "Drink!", alarm sound (1-second burst), or custom TTS with your own phrase (displayed on screen)
- **10 color themes** — 5 dark and 5 light themes, one for each MTG color (Plains/gold, Island/blue, Swamp/purple, Mountain/red, Forest/green)
- **Persistent settings** — all preferences saved via SharedPreferences, no extra permissions needed

## Building

Requires Android SDK with compileSdk 36. Uses Gradle wrapper (9.1.0).

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew installDebug         # Build and install on connected device
```

## Requirements

- Android 7.0+ (API 24)
- No special permissions beyond `VIBRATE`
