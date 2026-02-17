# Task: Fix UX Glitches and Timing Issues

## Objectives
- [ ] Fix Instant Prep-to-Main Timer Transition (No "00:00" display)
- [ ] Fix Save Button Flicker on Pause (Synchronous state update)

## Steps
- [/] Analyze `TimerService.kt` for loop timing and pause logic <!-- id: 0 -->
- [x] Modify `TimerService.kt` <!-- id: 1 -->
    - [x] Update prep loop to break when `remaining < 1000` (skip "0")
    - [x] Update `pauseTimer` to set `_wasPausedDuringPrep` synchronously before state change
- [ ] Verify changes with `install.sh` <!-- id: 2 -->

# Task: Fix Timer Initialization (No "00:00" on Start)

## Objectives
- [ ] Initialize `TimerUiState` with valid defaults (20 min)
- [ ] Ensure `init` block handles 0/null from persistence correctly

## Steps
- [x] Modify `TimerViewModel.kt` <!-- id: 3 -->
    - [x] Set `TimerUiState` default `meditationTime` to 20
    - [x] Update `init` block to valid `lastDuration` (if <= 0 or null, use 20)
- [x] Verify with `install.sh` <!-- id: 4 -->

# Task: Fix Timer Layout Shift

## Objectives
- [ ] Prevent timer position shift between Idle and Running states
- [ ] Ensure timer digits don't jitter (Monospace/tnum)

## Steps
- [x] Modify `TimerScreen.kt` <!-- id: 5 -->
    - [x] Use `FontFamily.Monospace` for timer text
    - [x] Remove extra `.padding(24.dp)` from Running state text
    - [x] Ensure consistent width/alignment between states
- [x] Verify with `install.sh` <!-- id: 6 -->

# Task: Fix Timer Countdown Speed (Running too fast)

## Objectives
- [ ] Display starting second for full duration (Round Up/Ceiling logic)
- [ ] Revert loop break condition to allows running until 0 (since 0 is now handled by rounding)

## Steps
- [x] Modify `TimerService.kt` <!-- id: 7 -->
    - [x] Revert prep loop break to `remaining <= 0`
    - [x] Update `createNotification` format logic to round up
- [x] Modify `TimerScreen.kt` <!-- id: 8 -->
    - [x] Update `formatTime` to round up (`millis + 999`)
- [x] Verify with `install.sh` <!-- id: 9 -->

# Task: Restrict Statistics Access

## Objectives
- [ ] Allow navigation to statistics ONLY when timer state is IDLE

## Steps
- [x] Modify `TimerScreen.kt` <!-- id: 10 -->
    - [x] Wrap `onLongClick` navigation in state check (`uiState.timerState == TimerState.IDLE`)
- [x] Verify with `install.sh` <!-- id: 11 -->

# Task: Revert App Icon

## Objectives
- [ ] Use `ic_launcher` instead of `icon.png`
- [ ] Remove `icon.png` file

## Steps
- [x] Modify `AndroidManifest.xml` <!-- id: 12 -->
    - [x] Set `android:icon` and `android:roundIcon` back to `ic_launcher`
- [x] Delete `icon.png` <!-- id: 13 -->
- [x] Verify with `install.sh` <!-- id: 14 -->

# Task: Implement Native Splash Screen

## Objectives
- [ ] Show branded splash screen on startup using `androidx.core:core-splashscreen`
- [ ] Handle smooth transition to app theme

## Steps
- [x] Add Dependency <!-- id: 15 -->
    - [x] Add `androidx.core:core-splashscreen:1.0.1` to `build.gradle.kts`
- [x] Configure Themes <!-- id: 16 -->
    - [x] Create `Theme.App.Starting` in `values/themes.xml`
- [x] Update Manifest <!-- id: 17 -->
    - [x] Set `MainActivity` theme to `@style/Theme.App.Starting`
- [x] Update Logic <!-- id: 18 -->
    - [x] Call `installSplashScreen()` in `MainActivity` (Note: Used `setTheme` fallback)
    - [x] (Optional) Add `keepOnScreenCondition` (Skipped due to fallback)
- [x] Verify with `install.sh` <!-- id: 19 -->

# Task: Refine Splash Screen Icon

## Objectives
- [x] Use `icon_foreground.png` for splash screen

## Steps
- [x] Modify `themes.xml` <!-- id: 20 -->
    - [x] Change `windowSplashScreenAnimatedIcon` to `@drawable/ic_launcher_foreground`
- [x] Verify with `install.sh` <!-- id: 21 -->

# Task: Design Overhaul (TimerScreen)

## Objectives
- [ ] Implement "High-End Minimalist" look
- [ ] Typography: Use Monospace (Thin) for Timer, Sans-Serif for text
- [ ] Layout: Refine Timer, Controls, Presets, and Prep Time

## Steps
- [x] Modify `TimerScreen.kt` <!-- id: 22 -->
    - [x] Update Timer Text (96sp+, Thin, Monospace, White)
    - [x] Update Control Icons (+/-, Vertical Center, Transparent White)
    - [x] Update Presets (Spacing 32dp, Circular Bg, Active/Inactive States)
    - [x] Update Prep Time (Move down, Small, Gray, Uppercase Spaced)
- [x] Verify with `install.sh` <!-- id: 23 -->

# Task: Refine Design (Buttons, Timer, Presets)

## Objectives
- [ ] Match "Clear" & "Save" buttons to "Prep Time" style (Gray, Small, Discrete)
- [ ] Make Main Timer smaller and thinner (Clean look)
- [ ] Reduce spacing for Quick Select presets

## Steps
- [x] Modify `TimerScreen.kt` <!-- id: 24 -->
    - [x] Update Clear/Save buttons (Gray, small font)
    - [x] Reduce Timer font size (e.g., ~72sp) and ensure Thin weight
    - [x] Reduce Preset spacing (e.g., 16dp)
- [x] Verify with `install.sh` <!-- id: 25 -->















