# DreamingOfClocks

An Android screensaver (DreamService) app featuring digital and analog clock modes with animated weather backgrounds, media info, and deep customization.

> **Note:** This project has been primarily generated using AI tools. You should expect rough edges and bugs. If you encounter any issues, please report them on the [Issues page](https://github.com/micahsoftdotexe/DreamingOfClocks/issues).

---

## Features

- **Digital clock** — 12/24-hour format, optional seconds, auto-sizing text
- **Analog clock** — 5 built-in templates (Classic, Minimal, Modern, Elegant, Compact)
- **Background modes:**
  - Solid color
  - Custom image from device storage
  - Animated weather (rain, snow, clouds, fog, thunderstorm with particle effects)
- **Weather integration** — powered by [Open-Meteo](https://open-meteo.com/), uses GPS or a text-based location
- **Now-playing display** — shows current media title and artist
- **Next alarm countdown** — displays time until next alarm
- **Custom fonts** — includes [DSEG](https://github.com/keshikan/DSEG) 7-segment and 14-segment display fonts, plus system fonts
- **Full color customization** — text color, background color, clock hand color

---

## Screenshots

| Digital Mode | Analog Mode | Weather Background | Settings |
|---|---|---|---|
| *(coming soon)* | *(coming soon)* | *(coming soon)* | *(coming soon)* |

---

## Requirements

- Android 7.0+ (API 24)
- **Location permission** *(optional)* — required for GPS-based weather
- **Notification listener permission** *(optional)* — required for now-playing media display

---

## Installation

### From GitHub Releases
Download the latest APK from the [Releases page](https://github.com/micahsoftdotexe/DreamingOfClocks/releases) and sideload it onto your device.

To sideload:
1. Enable **Install unknown apps** for your browser or file manager in Android Settings
2. Open the downloaded APK and follow the prompts

### Enable as Screensaver
Once installed, enable DreamingOfClocks as your screensaver:

1. Go to **Settings > Display > Screen saver** (may vary by device)
2. Select **DreamingOfClocks**
3. Tap the settings icon to configure

### Build from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/micahsoftdotexe/DreamingOfClocks
   ```
2. Open in **Android Studio**
3. Build and run on a device or emulator

---

## Configuration

Access settings via the settings icon next to DreamingOfClocks in the screensaver menu, or launch the app directly.

| Setting | Description |
|---|---|
| Clock type | Digital or Analog |
| Analog template | Choose from 5 built-in styles |
| Time format | 12/24-hour, show/hide seconds |
| Font | System fonts or DSEG 7/14-segment display fonts |
| Background | Solid color, device image, or animated weather |
| Weather location | GPS or manual text entry |
| Weather update frequency | How often to refresh weather data |
| Text color | Color for clock and date text |
| Hand color | Color override for analog clock hands |
| Show alarm | Toggle next alarm countdown display |
| Show media | Toggle now-playing media display |

---

## Building

**Prerequisites:** Android Studio, Android SDK 36

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew test             # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests (device/emulator required)
```

For more detail on the project structure, see [CLAUDE.md](CLAUDE.md).

---

## License

**App source code** is licensed under the [MIT License](LICENSE).

**DSEG fonts** (`app/src/main/assets/fonts/`) are licensed under the [SIL Open Font License 1.1](app/src/main/assets/fonts/DSEG-LICENSE.txt), © 2020 [keshikan](https://www.keshikan.net). See [DSEG on GitHub](https://github.com/keshikan/DSEG).
