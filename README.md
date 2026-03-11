# DreamingOfClocks

An Android screensaver (DreamService) app featuring digital and analog clock modes with animated weather backgrounds, media info, and deep customization.

> **Note:** This project has been primarily generated using AI tools. You should expect rough edges and bugs. If you encounter any issues, please report them on the [Issues page](https://github.com/micahsoftdotexe/DreamingOfClocks/issues).

---

## Features

- **Digital clock** — 12/24-hour format, optional seconds, auto-sizing text
- **Analog clock** — 5 built-in templates (Classic, Minimal, Modern, Elegant, Compact) plus custom JSON templates
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
   git clone git@github.com:micahsoftdotexe/DreamingOfClocks.git
   ```
2. Open in **Android Studio**
3. Build and run on a device or emulator

---

## Configuration

Access settings via the settings icon next to DreamingOfClocks in the screensaver menu, or launch the app directly.

| Setting | Description |
|---|---|
| Clock type | Digital or Analog |
| Analog template | Choose a built-in template or import a custom JSON template |
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

## Custom Analog Templates

You can create your own analog clock template as a JSON file and import it via the settings screen.

### Schema

```json
{
  "name": "My Template",
  "version": 1,
  "face": {
    "radiusFraction": 0.4,
    "fillColor": null,
    "borderColor": null,
    "borderWidthDp": 3.0,
    "showHourNumbers": true,
    "numberStyle": "arabic",
    "numberFontSizeFraction": 0.08,
    "showMinuteTicks": true,
    "minuteTickLengthFraction": 0.03,
    "minuteTickWidthDp": 1.0,
    "showHourTicks": true,
    "hourTickLengthFraction": 0.06,
    "hourTickWidthDp": 2.5,
    "showCenterDot": true,
    "centerDotRadiusFraction": 0.02
  },
  "hands": {
    "hour": {
      "lengthFraction": 0.5,
      "widthDp": 6.0,
      "tailFraction": 0.1,
      "color": null,
      "cap": "round"
    },
    "minute": {
      "lengthFraction": 0.7,
      "widthDp": 4.0,
      "tailFraction": 0.1,
      "color": null,
      "cap": "round"
    },
    "second": {
      "show": true,
      "lengthFraction": 0.8,
      "widthDp": 1.5,
      "tailFraction": 0.15,
      "color": "#FF0000",
      "cap": "butt"
    }
  },
  "widgets": {
    "date": {
      "position": "below",
      "offsetXFraction": 0.0,
      "offsetYFraction": 0.1,
      "fontSizeSp": 20
    },
    "alarm": {
      "position": "below",
      "offsetXFraction": 0.0,
      "offsetYFraction": 0.15,
      "fontSizeSp": 16
    },
    "media": {
      "position": "above",
      "offsetXFraction": 0.0,
      "offsetYFraction": -0.1,
      "fontSizeSp": 14
    }
  }
}
```

### Field Reference

**`face`**
- `radiusFraction` *(float 0–1)* — Clock face radius as a fraction of the view size
- `fillColor` *(string or null)* — Face fill color as `#RRGGBB` / `#AARRGGBB`, or `null` for transparent
- `borderColor` *(string or null)* — Border color, or `null` to use the text color
- `borderWidthDp` *(float)* — Border stroke width in dp
- `showHourNumbers` *(boolean)* — Show hour numbers on the face
- `numberStyle` *(string)* — `"arabic"`, `"roman"`, or `"none"`
- `numberFontSizeFraction` *(float 0–1)* — Number font size as a fraction of the view size
- `showMinuteTicks` *(boolean)* — Show minute tick marks
- `minuteTickLengthFraction` *(float 0–1)* — Minute tick length as a fraction of the view size
- `minuteTickWidthDp` *(float)* — Minute tick stroke width in dp
- `showHourTicks` *(boolean)* — Show hour tick marks
- `hourTickLengthFraction` *(float 0–1)* — Hour tick length as a fraction of the view size
- `hourTickWidthDp` *(float)* — Hour tick stroke width in dp
- `showCenterDot` *(boolean)* — Show a dot at the center of the clock
- `centerDotRadiusFraction` *(float 0–1)* — Center dot radius as a fraction of the view size

**`hands.hour` and `hands.minute`**
- `lengthFraction` *(float 0–1)* — Hand length as a fraction of the face radius
- `widthDp` *(float)* — Hand stroke width in dp
- `tailFraction` *(float 0–1)* — Tail length behind center as a fraction of the face radius
- `color` *(string or null)* — Hand color as `#RRGGBB`, or `null` to use the configured hand color
- `cap` *(string)* — Stroke cap: `"round"`, `"square"`, or `"butt"`

**`hands.second`** — same fields as above, plus:
- `show` *(boolean)* — Whether to show the second hand

**`widgets.date`, `widgets.alarm`, `widgets.media`**
- `position` *(string)* — `"above"`, `"below"`, `"left"`, `"right"`, or `"on_face"`
- `offsetXFraction` *(float)* — Horizontal offset as a fraction of the view size
- `offsetYFraction` *(float)* — Vertical offset as a fraction of the view size
- `fontSizeSp` *(int)* — Font size in sp

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
