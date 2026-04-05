# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew testDebugUnitTest --tests "dev.hogballs.impatience.ExampleUnitTest"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean
```

## Release Process

```bash
# Build signed release APK
./gradlew assembleRelease

# Upload to a new GitHub release (use full semver tags, e.g. v1.0.0)
gh release create v1.0.0 app/build/outputs/apk/release/app-release.apk --title "v1.0.0" --notes "Release notes here"

# Replace the APK on an existing release
gh release delete-asset v1.0.0 app-release.apk --yes
gh release upload v1.0.0 app/build/outputs/apk/release/app-release.apk
```

Release signing is configured in `app/build.gradle.kts` using `app/release-keystore.jks` (gitignored). The keystore alias and password are both `impatience`.

## Architecture

Single-module Android app (`app/`) with two activities:

**MainActivity** — Timer screen. Handler-based 1-second tick triggers drink notifications at a configurable interval (default 60s). Notifications use TTS, alarm sound, or custom TTS based on user settings. Two buttons: "NEW TURN" resets the timer, "PAUSE" toggles the timer. Timer state is preserved across activity recreations via `onSaveInstanceState`.

**SettingsActivity** — Configurable timer interval, notification sound type (TTS "Drink!", alarm, custom TTS), and 10 color themes (5 dark, 5 light — one per MTG color). Settings persist via SharedPreferences (`impatience_settings`). Theme changes trigger `recreate()` on both activities.

**Theme system** — 10 themes defined in `themes.xml` (and `values-night/`), mapped by key in `MainActivity.resolveTheme()`. Themes set `windowBackground`, `statusBarColor`, `navigationBarColor`, `textColorPrimary/Secondary/Hint`, and Material color attributes. Layouts use `?android:attr/` references so text and icons adapt to light/dark themes.

**UI**: XML layouts with View Binding (not Compose). Portrait-only, screen kept on.

## Key Tech

- **Kotlin** with **AGP 9.0.1**, **Gradle 9.1.0**
- **compileSdk 36**, minSdk 24, targetSdk 36, Java 11
- Dependencies managed via version catalog (`gradle/libs.versions.toml`)
- AndroidX + Material Design, no third-party libraries
- Kotlin code style: official (set in `gradle.properties`)
