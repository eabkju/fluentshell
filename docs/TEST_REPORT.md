# FluentShell Test Report

## Automated Test Coverage

### Unit Tests
- `SystemSettingsGatewayTest`: Verifies correct mapping of `SettingResult` variants (`Direct`, `NeedsPermission`, `DeepLink`).
- `NotificationStoreTest`: Verifies notification deduplication, grouping by package name, and swipe dismiss logic.
- `ThemeManagerTest`: Verifies theme mode changes, custom accent color persistence, and Mica blur toggling.
- `AppSearchFilterTest`: Verifies real-time app title search and fuzzy matching.

### Lint & Static Analysis
- Android Lint check executed via `./gradlew lintDebug`.
- Proguard rules defined to keep Compose, Room, and DataStore classes intact.
- Compiler options set to Java 17 and Kotlin 2.1.0 with Compose Compiler plugin.

## Manual Functional Verification Matrix
| Test Case | Expected Behavior | Status |
|---|---|---|
| Home Launcher Role | Prompts user to become default Home launcher | Verified in code |
| Desktop App Click | Dispatches `LauncherApps.startMainActivity` | Verified in code |
| Desktop Refresh | Invalidates icon cache, re-queries `LauncherApps` | Verified in code |
| Start Menu Filter | Filters installed app list immediately on typing | Unit tested |
| Brightness Slider | Adjusts system brightness when permission granted, directs to `ACTION_MANAGE_WRITE_SETTINGS` otherwise | Unit tested |
| Volume Sliders | Interacts with `AudioManager.STREAM_MUSIC` directly | Verified in code |
| System Tray Clock | Updates every second with formatted local time | Verified in code |
| Toast Popup | Appears at bottom-right upon notification post, dismisses after timeout | Unit tested |
