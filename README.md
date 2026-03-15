# Mars Timer

A minimalist, high-contrast meditation timer application built with Flutter. Designed for simplicity and focused practice.

## Features

- **Adjustable Meditation Time**: Easily set your session duration with quick presets (5, 10, 15, 20 minutes) or fine-tune using the `+` and `−` controls.
- **Warmup Delay**: Optional preparation time (delay) before the meditation starts, adjustable in 5-second increments.
- **Visual Indicators**:
  - **Idle**: High-contrast white timer.
  - **Warmup**: Subtle gray thin font to indicate preparation.
  - **Running**: Sharp white font during the meditation session.
  - **Paused**: Subtle gray font to indicate the session is on hold.
- **Audio Feedback**: Authentic singing bowl sound to mark the beginning and end of each session.
- **Statistics**: Automatically tracks your total meditation time and maintains a history of your sessions.
- **Persistence**: Remembers your last used duration for quick starts.

## How it Works

1. **Select Duration**: Use the buttons below the timer or the `+`/`−` icons next to the time to set your desired meditation length.
2. **Set Delay (Optional)**: Tap `+ delay` at the bottom to add preparation time.
3. **Start**: Tap the empty space or the timer directly to start.
4. **During Session**: The screen stays awake. Tap anywhere to pause or resume.
5. **Completion**: A singing bowl sounds when the time is up. Your session is automatically saved to your statistics.
6. **Manual Stop**: While paused, you can choose to `clear` the session or `save` the elapsed time as a partial session.

## Technical Details

- **Framework**: Flutter
- **State Management**: Provider
- **Database**: SQLite (via sqflite) for session history.
- **Audio**: `audioplayers` for the singing bowl sound.
- **Icons**: Simple text-based UI for a distraction-free experience.
