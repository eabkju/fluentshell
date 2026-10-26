# FluentShell Architecture Documentation

## Overview
FluentShell is built as a single-activity modern Jetpack Compose application designed for Android tablets. It provides a full desktop experience modeling Windows 11 Fluent design principles while conforming strictly to Android platform capabilities and security sandboxing.

## Module Graph

```
                   +------------------------+
                   |          :app          |
                   +-----------+------------+
                               |
       +-----------------------+-----------------------+
       |           |           |           |           |
       v           v           v           v           v
:feature:desktop :feature:taskbar :feature:start :feature:settings :feature:notifications
       \           |           /           /          /
        +----------+----------+-----------+----------+
                              |
                              v
                       +-------------+
                       |:core:system |
                       +------+------+
                              |
                              v
                       +-------------+
                       | :core:data  |
                       +------+------+
                              |
                              v
                       +-------------+
                       |:core:design |
                       +-------------+
```

## Layer Responsibilities

### `:core:design`
- Fluent design color palettes (Light, Dark, High Contrast).
- Mica, Acrylic, and Smoke surface composables.
- Standard 8dp corner radius shapes and elevation curves.
- Fluent UI vector icons and typography.

### `:core:data`
- Persistent settings storage via Jetpack DataStore Preferences (`ThemePreferences`, `TaskbarPreferences`).
- Desktop item layout and pinned taskbar apps persistence via Room Database (`FluentDatabase`).
- `AppRepository`: querying `LauncherApps`, caching package icons and titles, observing package add/remove/update broadcasts.

### `:core:system`
- `SystemSettingsGateway`: single point of integration for hardware/OS controls (Brightness, Volume, Timeout, Auto-Rotate, Flashlight).
- Explicit `SettingResult` return types: `Direct(value)`, `NeedsPermission(intent)`, and `DeepLink(intent)`.
- System broadcast listeners (Battery level/charging, Wi-Fi status, DND status).

### `:feature:desktop`
- Wallpaper surface layer.
- Draggable/re-arrangable icon grid with snap-to-grid.
- Desktop context menu (View, Sort by, Refresh, Display settings, Personalize).
- Keyboard shortcut support (`F5` refresh, `Win` / `Ctrl+Esc` Start menu).

### `:feature:taskbar`
- Centered / left-aligned taskbar icon row.
- Start button, Search trigger, Task View launcher.
- System Tray cluster (Clock, Wi-Fi icon, Audio icon, Battery pill, Notification bell).
- Quick Settings flyout and Action Center flyout anchor points.

### `:feature:start`
- Start menu surface with Mica background.
- Live search input box.
- Pinned applications grid.
- Recommended applications feed.
- Alphabetical A-Z All Apps list.
- User profile badge and system power flyout.

### `:feature:settings`
- Control Panel style dual-pane interface (Navigation sidebar on left, Page content on right).
- Pages: System, Network, Bluetooth, Personalization, Apps, About.
- Clear badge indicators: "Applied" vs "Opens Android Settings".

### `:feature:notifications`
- `FluentNotificationListenerService` hooks into Android notification stream.
- Reactive `NotificationStore` holds notification state as `StateFlow`.
- Bottom-right floating toasts with auto-dismiss countdown.
- Full Action Center side panel with app grouping.
