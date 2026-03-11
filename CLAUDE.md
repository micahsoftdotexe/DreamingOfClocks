# DreamingOfClocks

Android screensaver (DreamService) app featuring digital and analog clock modes with multiple background options.

## Project Structure

```
app/src/main/java/com/micahsoftdotexe/dreamingofclocks/
  activities/          - MainActivity (launcher), SettingsActivity (Compose UI)
  services/
    screensaver/       - ScreensaverService, PreferencesManager, ClockConfigurator, AnalogClockConfigurator
    media/             - MediaDisplayManager (now-playing display)
    template/          - TemplateManager (built-in analog clock template lookup)
  models/              - ClockTemplate (5 built-in templates + data classes)
  weather/             - WeatherApiClient, WeatherCache, WeatherUpdateScheduler, WeatherCondition, LocationHelper
  uicomponents/
    analogclock/       - AnalogClockView (custom Canvas rendering)
    weatherbackground/ - WeatherBackgroundView (animated particle backgrounds)
    colorpicker/       - ColorPicker, ColorWheelDialog
    fontpicker/        - FontPicker
    ResizableTextClock - Auto-sizing TextClock
  utils/               - BackgroundRenderer, AlarmHelper, FontHelper, MediaMetadataHelper, MediaNotificationListener, ImagePickerHelper
  ui/theme/            - Compose theme (Color, Type, Theme)

app/src/main/res/
  layout/              - screensaver_layout.xml (digital), screensaver_analog_layout.xml (analog)
  xml/                 - dream.xml (DreamService config)

app/src/main/assets/fonts/  - DSEG7 and DSEG14 segment display fonts

app/src/test/           - Unit tests (models, templates, weather, fonts)
app/src/androidTest/    - Instrumented tests

tmp/                    - Generated files (analysis, reports). Not in source control.
```

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material 3 (settings), XML layouts (screensaver)
- **Build:** Gradle KTS, compile/target SDK 36, min SDK 24
- **Dependencies:** Coil3 (images), org.json (JSON parsing), androidx core/lifecycle/compose
- **Weather API:** Open-Meteo (free, no auth required)

## Build & Test

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests
```

## Key Concepts

- **DreamService:** Android screensaver API. ScreensaverService is the entry point, configured via `res/xml/dream.xml`.
- **Clock modes:** Digital (ResizableTextClock) and analog (AnalogClockView with Canvas drawing).
- **Analog templates:** 5 built-in (Classic, Minimal, Modern, Elegant, Compact).
- **Background modes:** Solid color, user-selected image, or animated weather (WeatherBackgroundView with particle effects).
- **Weather pipeline:** LocationHelper (GPS/text) -> WeatherApiClient (Open-Meteo) -> WeatherCache -> WeatherUpdateScheduler -> WeatherBackgroundView.
- **Media display:** MediaNotificationListener (NotificationListenerService) feeds metadata to MediaDisplayManager.
- **Preferences:** All config stored via PreferencesManager (SharedPreferences wrapper).

## Conventions

- Do NOT run git commands without explicit user consent
- Singleton objects for service classes (PreferencesManager, TemplateManager, WeatherApiClient, etc.)
- Custom views use Handler-based frame loops for animation
