# FluentShell

> **Windows 11–style Shell for Android Tablets**
> Modern, beautiful, and functional desktop launcher built with Jetpack Compose.

[![CI](https://github.com/eabkju/fluentshell/actions/workflows/ci.yml/badge.svg)](https://github.com/eabkju/fluentshell/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## Disclaimer
> **Important:** FluentShell is an independent open-source launcher for Android tablets. It is **not** affiliated with, authorized, maintained, sponsored, or endorsed by Microsoft Corporation. Windows, Windows 11, and Fluent are trademarks or registered trademarks of Microsoft Corporation. All UI assets and fonts used conform to open-source licenses (MIT / SIL OFL).

---

## Features

### 🖥️ Desktop & Personalization
- **Windows 11 Aesthetic:** Mica and acrylic surfaces, subtle borders, rounded 8dp window geometry, and vibrant accent colors.
- **Desktop Grid:** App icons, shortcuts, wallpaper rendering, and snap-to-grid layout.
- **Desktop Context Menu:** Right-click or long-press for *View, Sort by, Refresh, Display settings, and Personalize*.
- **Refresh Command:** Rescans installed packages, invalidates and reloads icon caches, wallpaper, and theme (also accessible via keyboard `F5`).

### 📌 Taskbar & System Tray
- **Centered or Left-Aligned Taskbar:** Windows 11 style centered taskbar icons with optional left alignment.
- **Start & Search Buttons:** Immediate access to application launcher and instant search.
- **Pinned & Running Apps:** Pin favorites to the taskbar with active indicators.
- **System Tray:** Live digital clock, Wi-Fi status, volume status, battery percentage & charging indicators, and notification bell.
- **Quick Settings Flyout:** Windows 11 style quick action pills (Wi-Fi, Bluetooth, Airplane, Flashlight, Rotation lock, DND) and hardware sliders for Brightness and Volume.

### 🪟 Start Menu
- **Search Header:** Real-time filtering across all installed apps.
- **Pinned Applications:** Quick grid of pinned applications.
- **Recommended Section:** Recently used or installed applications.
- **All Apps View:** Alphabetically indexed A–Z app drawer.
- **App Management:** Context menu to Pin to Start, Pin to Taskbar, View App Info, or Uninstall.
- **User & Power Menu:** User profile display and fast device sleep/lock/power controls.

### ⚙️ Control Panel Settings App
- Windows 11 style dual-pane sidebar settings interface:
  - **System:** Display brightness, screen timeout, auto-rotation; Media/Ring/Alarm volumes; Battery & Storage status.
  - **Network & Internet:** Network connectivity status and quick launch to Android connectivity panels.
  - **Bluetooth & Devices:** Connected accessories and pairing deep link.
  - **Personalization:** Accent color chooser, Light/Dark/System theme switch, Taskbar alignment toggle, and Mica blur effect switch.
  - **Apps:** Installed application manager and default apps panel.
  - **About FluentShell:** Build details, system architecture, and license information.
- Transparent badges: Every setting clearly indicates whether it was **Applied Directly** or **Opens Android Settings** to respect Android platform boundaries.

### 🔔 Windows-Style Notifications & Toasts
- **Notification Mirroring:** Listens to incoming system notifications via `NotificationListenerService`.
- **Bottom-Right Toast Popups:** Windows 11 style floating toasts with app icon, header, preview body, and swipe-to-dismiss.
- **Action Center Flyout:** Grouped notification drawer accessible from the taskbar tray with "Clear All" functionality.

---

## Architecture & Technology Stack

- **UI Framework:** Kotlin + Jetpack Compose + Material 3 custom Fluent design system.
- **Architecture:** Clean Architecture + Modular MVVM with StateFlow & Coroutines.
- **Persistence:** Jetpack Room & DataStore Preferences for desktop arrangement, pinned items, and user personalization.
- **Platform Interop:** Android `LauncherApps`, `NotificationListenerService`, `AudioManager`, and `Settings.System`.
- **Minimum Requirements:** Android 10 (API level 29) or higher, optimized for ARM64 tablets.

---

## Build & Installation

### Download from GitHub Actions
FluentShell is built automatically on every commit via GitHub Actions:
1. Navigate to the **[Actions tab](https://github.com/eabkju/fluentshell/actions)**.
2. Select the latest build run.
3. Download the `fluentshell-debug-apk` artifact.
4. Sideload and install the `.apk` on your tablet.

### Building from Source
Ensure you have JDK 17 installed:
```bash
git clone https://github.com/eabkju/fluentshell.git
cd fluentshell
chmod +x gradlew
./gradlew testDebugUnitTest assembleDebug
```
The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Permissions & Privacy
FluentShell does not request `INTERNET` permission in the core launcher. All app state, pinned configurations, and notifications remain strictly offline on your local device.
- `ROLE_HOME`: Default launcher capability.
- `WRITE_SETTINGS`: Direct control of brightness, timeout, and rotation.
- `NotificationListenerService`: Toasts and Action Center notification reflection.
