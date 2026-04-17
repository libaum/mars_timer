# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run the app
flutter run

# Run on a specific device
flutter run -d <device-id>

# Build Android APK
flutter build apk

# Run tests
flutter test

# Run a single test file
flutter test test/widget_test.dart

# Analyze code
flutter analyze

# Get dependencies
flutter pub get

# Regenerate launcher icons (after changing assets/icon/app_icon.png)
flutter pub run flutter_launcher_icons
```

## Architecture

Mars Timer is a minimalist meditation timer. The architecture uses **Provider** for state management with three singleton services injected at startup.

### Data flow

`main.dart` initializes three services and passes them into a single `TimerProvider` (a `ChangeNotifier`) that is provided at the root. Screens consume the provider via `Consumer<TimerProvider>`.

### Key files

- **`lib/providers/timer_provider.dart`** — All timer logic lives here. `TimerState` enum drives the UI: `idle → prep → running → paused → finished`. The timer uses wall-clock targeting (`_targetTime`) rather than a simple countdown, so pausing and resuming stays accurate. `_isTestDuration` mode runs a 5-second timer for dev testing (toggled via the `5s` quick-select button).

- **`lib/screens/timer_screen.dart`** — Single-screen UI. Navigation between the timer view and stats view is handled inline with a `_showStats` bool (no named routes). Tap = start/pause/resume; long-press (idle) = show stats.

- **`lib/screens/statistics_screen.dart`** — Read-only stats view with a cumulative minutes graph drawn via `CustomPainter`.

- **`lib/services/database_service.dart`** — Singleton wrapping sqflite. Stores `meditation_sessions` (id, date ms, duration seconds). On Linux/Windows, `sqflite_common_ffi` is initialized in `main.dart` before the service is used.

- **`lib/services/audio_service.dart`** — Singleton wrapping `audioplayers`. Plays `assets/audio/singing_bowl.mp3` at session start and end.

- **`lib/services/preferences_service.dart`** — Wraps `SharedPreferences` to persist only `last_duration` (int, minutes).

- **`lib/theme/app_theme.dart`** — All colors (black/white/gray/darkGray) and `TextStyle` getters (Noto Sans, various weights). Always use these instead of inline colors or font definitions.

### Timer state transitions

```
idle ──[start]──► prep (if prepTime > 0) ──[prep ends]──► running ──[complete]──► finished
                                                      ↕
                                                   paused
idle ──[start]──► running (if prepTime == 0)
finished ──[tap]──► idle (remainingTime resets)
paused ──[clear]──► idle | paused ──[save]──► idle (saves partial session)
```

Sessions are only saved if the user meditated past the prep phase (`wasPausedDuringPrep` flag prevents saving prep-only time).
