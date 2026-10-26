# FluentShell Audit Report

## 1. Security Audit
- **Permissions Audit:** No `INTERNET` permission is declared in `AndroidManifest.xml`. All data remains strictly local to the device.
- **Query All Packages:** Avoided dangerous `QUERY_ALL_PACKAGES` permission; package discovery is handled via standard launcher intents (`android.intent.action.MAIN` + `android.intent.category.LAUNCHER`) under `<queries>`.
- **Exported Components:** Only `HomeActivity` (with `HOME` and `DEFAULT` categories) and `FluentNotificationListenerService` (with `BIND_NOTIFICATION_LISTENER_SERVICE` system permission) are exported. No open unprotected components exist.
- **Data Protection:** Database and DataStore preferences are private to the app sandbox (`Context.MODE_PRIVATE`).

## 2. Privacy Audit
- **Notification Data:** Notifications received by `FluentNotificationListenerService` are held in in-memory `StateFlow` and are never serialized or transmitted off-device.
- **Analytics & Telemetry:** Zero analytics SDKs, trackers, or telemetry libraries are present.

## 3. Legal & Intellectual Property Audit
- **Trademarks:** The application is named **FluentShell** to avoid infringing on the "Windows" trademark.
- **Logos & Wallpapers:** No copyrighted Microsoft Windows 11 default wallpapers, start logos, or official brand assets are included. Built-in wallpapers and icons are custom vector designs or derived from MIT-licensed Fluent System Icons.
- **Fonts:** System typography uses standard system sans-serif fonts with Selawik metrics compatibility; Segoe UI font files are not distributed.
- **Disclaimers:** README and application About pages clearly display trademark notices and non-affiliation disclaimers.

## 4. Performance & Compatibility Audit
- **Target SDK:** Target SDK 35 (Android 15) with `minSdk` 29 (Android 10).
- **Blur Performance:** Uses `RenderEffect` on Android 12+ (API 31+) with automatic fallback to solid tinted translucent backgrounds on API 29-30 and budget Mali GPUs.
- **Memory Footprint:** Application uses memory-efficient vector graphics and lazy grid rendering in Jetpack Compose to maintain a low memory footprint (< 150MB).
