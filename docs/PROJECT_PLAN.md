# FluentShell — Windows 11–style Shell for Android Tablets
### Project Plan, Build Steps, Test Plan & Audits

> Working name: **FluentShell** (avoid "Windows" in the app name/icon — see Legal audit).
> Build flow: **Antigravity → GitHub → GitHub Actions → APK**.
> Last updated: 2026-10-26

---

## 1. Goal

An Android **tablet launcher/shell** that looks and behaves like Windows 11:

- Desktop with icons and wallpaper, taskbar, Start menu, system tray, action center.
- A Control Panel–style **Settings app** that changes real device settings where Android allows it.
- Windows-style notifications and popups (toasts, action center, flyouts).
- Themes (light/dark, accent, wallpaper, Mica-style blur) and a Refresh option.
- Phase 2: a **Linux terminal** (Termux-style).
- Phase 3: **Windows .exe support** via Wine + Box64 (experimental).
- Runs on **Snapdragon and MediaTek** (all ARM64).

## 2. Locked decisions

| Topic | Decision |
|---|---|
| .exe apps | Wine + Box64 layer (Phase 3, experimental) |
| Terminal | Linux shell, Termux-style (Phase 2) |
| Build order | MVP first: launcher, settings, notifications, themes |
| Language / UI | Kotlin + Jetpack Compose |
| ABI | arm64-v8a for release (x86_64 added to debug only, for emulators) |
| Distribution | Sideloaded APK via GitHub Releases (not Google Play) |

## 3. Reality checks (read before building)

1. **Android cannot be fully replaced.** The real status bar, navigation, lock screen and system notification shade stay. FluentShell is the Home app and mirrors notifications inside itself.
2. **Settings are limited by Android.** Apps can directly change brightness, volume, screen timeout, rotation lock, flashlight and wallpaper. Wi-Fi, Bluetooth, airplane mode, hotspot, and similar settings must open the system panel (Android 10+ blocks direct toggles).
3. **Windows-style notifications** appear inside FluentShell. They need the user to grant **Notification access**.
4. **Running binaries from app storage** is blocked for apps targeting Android 10+ (targetSdk 29+). Terminal and Wine need either targetSdk 28, or binaries shipped as `lib*.so` inside the APK. Recommended: a **separate companion "Runtime" APK** (targetSdk 28) for Phase 2/3, keeping the main launcher modern.
5. **Wine on MediaTek (Mali GPU) will be much weaker** than on Snapdragon (Adreno/Turnip). Chrome/Firefox as .exe will be slow on both. Android Chrome/Firefox in Windows-style windows should remain the default.
6. **Freeform windows** (resizable app windows) only work if the device supports it. Treat as optional.
7. **Branding:** do not use Microsoft logos, the Windows wallpaper, or Segoe UI. Use Fluent UI System Icons (MIT) and the Selawik font (open, Segoe-compatible metrics). Verify each license before bundling.

## 4. Tech stack

| Area | Choice |
|---|---|
| IDE / tooling | Antigravity + Android SDK (cmdline-tools or Android Studio), JDK 17, adb |
| Build | Gradle (Kotlin DSL), AGP, version catalog (`libs.versions.toml`) — use latest stable |
| SDK levels | minSdk 29, compileSdk/targetSdk = latest stable (35/36) |
| UI | Jetpack Compose, Material 3 base with custom Fluent theme |
| State | ViewModel + StateFlow, Kotlin coroutines |
| Storage | DataStore (settings), Room (pinned apps, desktop layout) |
| DI | Hilt (or manual DI if you want fewer moving parts) |
| Apps list | `LauncherApps` API + `<queries>` for the launcher intent (avoid `QUERY_ALL_PACKAGES`) |
| Blur | `RenderEffect` / `setBackgroundBlurRadius` on API 31+, translucent fallback below |
| Logging | Timber (debug only) |
| Quality | ktlint/detekt, Android Lint, JUnit, Compose UI tests, Macrobenchmark |

## 5. Architecture

Single-activity Compose app, multi-module recommended.

```
:app                 – manifest, Home activity, DI wiring
:core:design         – Fluent theme, colors, typography, icons, Mica/acrylic helpers
:core:data           – DataStore, Room, repositories
:core:system         – wrappers for Settings, Audio, Brightness, Wallpaper, Display
:feature:desktop     – desktop surface, icons, context menu, refresh
:feature:taskbar     – taskbar, tray, clock, quick settings flyout
:feature:start       – Start menu, search, all apps, power menu
:feature:settings    – Control Panel–style Settings app (separate launcher entry)
:feature:notifications – NotificationListenerService, action center, toasts
:feature:terminal    – (Phase 2) UI + bridge to Runtime APK
:runtime (separate APK) – (Phase 2/3) Linux rootfs, shell, Wine/Box64
```

Key components:

- `HomeActivity` — manifest filters `MAIN` + `HOME` + `DEFAULT`, `launchMode="singleTask"`. Request default via `RoleManager.ROLE_HOME`.
- `AppRepository` — loads apps through `LauncherApps`, listens for install/uninstall callbacks, caches icons.
- `SystemSettingsGateway` — one class for every setting, each method returns `Direct`, `NeedsPermission`, or `DeepLink`.
- `FluentNotificationService` — `NotificationListenerService` feeding a `NotificationStore` (StateFlow).
- `ThemeManager` — light/dark/auto, accent, wallpaper, blur on/off, persisted in DataStore.
- `RuntimeBridge` (Phase 2/3) — bound service or ContentProvider protected by a signature permission.

## 6. Permissions & Android constraints

| Permission / API | Used for | Notes |
|---|---|---|
| `ROLE_HOME` request | Become default Home | Normal flow, user confirms |
| `WRITE_SETTINGS` | Brightness, screen timeout, rotation lock | Special access screen; deep-link `ACTION_MANAGE_WRITE_SETTINGS` |
| Notification listener | Action center, toasts | Special access; user enables manually |
| Notification policy access | Do Not Disturb toggle | Special access |
| `SET_WALLPAPER` | Wallpaper | Normal permission |
| `BLUETOOTH_CONNECT` | Bluetooth state/status | Runtime permission (Android 12+) |
| `ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE` | Tray status icons | Normal |
| `SYSTEM_ALERT_WINDOW` (optional) | Toasts over other apps | Special; keep optional |
| `PACKAGE_USAGE_STATS` (optional) | Taskbar "recent/running" apps | Special; optional |
| AccessibilityService (optional) | Open recents/notification shade, back/home | Sensitive, off by default, explicit consent screen |
| **No `INTERNET` in MVP** | — | Add only when a feature truly needs it |

## 7. Feature specification (MVP — Phase 1)

### 7.1 Desktop
- Wallpaper (static image; user picks from gallery or built-ins).
- Icon grid with drag to move, long-press menu, snap to grid; icons persisted in Room.
- Desktop context menu: **View, Sort by, Refresh, Display settings, Personalize**.
- **Refresh**: rescan apps, reload wallpaper/theme, rebuild icon cache. Also bound to a pull gesture and keyboard `F5` when a hardware keyboard is attached.
- Optional: Android widgets panel via `AppWidgetHost`.

### 7.2 Taskbar
- Centered (default) or left-aligned icons; Start button, Search, Task View, pinned apps.
- Tray: clock/date, Wi-Fi, volume, battery, notification bell.
- Clicking tray groups opens flyouts (quick settings, calendar, action center).
- Taskbar settings: alignment, size, auto-hide.

### 7.3 Start menu
- Pinned grid, Recommended (recent), All apps A–Z, search box, user/power section (power menu opens system power dialog or lock via accessibility if enabled).
- Right-click/long-press: Pin to Start, Pin to taskbar, App info, Uninstall.
- Search: installed apps first, then Settings pages.

### 7.4 Settings app (Control Panel style)
Separate launcher icon, Windows 11 sidebar layout.

| Page | Direct change | Opens system screen |
|---|---|---|
| System → Display | Brightness, screen timeout, rotation lock, refresh rate (own window) | Resolution/font size |
| System → Sound | Media/ring/alarm volume | Sound profiles |
| System → Notifications | Notification access check, DND (with policy access) | Per-app notification settings |
| System → Battery / Storage / About | Read-only info | Battery saver, storage manager |
| Network & internet | Status display | Wi-Fi, mobile, hotspot, airplane (system panels) |
| Bluetooth & devices | Status display | Pairing |
| Personalization | **All** FluentShell options: background, colors, theme, taskbar, start, blur | — |
| Apps | Installed apps list, launch, app info | Default apps |
| Accounts / Time & language / Privacy | — | Matching system screens |
| About FluentShell | Version, licenses | — |

Every row shows a small badge: **Applied** (direct) or **Opens Android settings** (deep-link), so users are never misled.

### 7.5 Notifications & popups
- `NotificationListenerService` mirrors posted notifications.
- Toast popup (bottom-right) with app icon, title, text, actions, auto-dismiss, swipe to dismiss (also cancels the notification).
- Action center: grouped by app, clear all, DND quick tile, media controls if available.
- System dialogs (permission prompts etc.) stay Android-native; in-app dialogs use Fluent style.

### 7.6 Themes
- Light / Dark / Auto, 8+ accent colors, custom accent picker.
- Mica/acrylic surfaces (blur on API 31+, tinted translucent fallback).
- Wallpaper packs, rounded corner radius setting (Win11 default 8dp), icon size.
- Theme export/import as JSON (optional).

### 7.7 Quick settings flyout
Brightness slider, volume slider, Wi-Fi/Bluetooth/airplane (open system panels), flashlight (direct, no permission), rotation lock, DND, battery saver (opens settings), night light (opens settings).

## 8. Later phases

### Phase 2 — Terminal (Runtime APK)
- Embed terminal emulator + view components (verify each license before bundling).
- Bootstrap a Linux userland (apt/pkg), shell (bash/zsh), common tools.
- Windows-style aliases (`dir`, `cls`, `copy`, `type`) as a convenience layer — **not** real CMD/PowerShell.
- Fluent-styled terminal window, tabs, font and color schemes.
- Executes only from the Runtime APK's allowed locations (targetSdk 28 or `lib*.so`).

### Phase 3 — Wine + Box64 (experimental)
- glibc rootfs, Box64 for x86_64 → ARM64, Wine for PE loading.
- Graphics: Turnip/DXVK path on Adreno; VirGL/ANGLE fallback on Mali (slow).
- Per-app container profiles, controller/keyboard/mouse input mapping.
- Launch .exe from File picker or Start menu entry.
- Default for browsers stays Android Chrome/Firefox; .exe version offered as "experimental".
- Never bundle Windows OS files. Download installers from vendors' official sites on demand.

### Phase 4 — Extras (optional)
File Explorer (via SAF), Widgets panel polish, snap layouts for split-screen, freeform windows, multi-monitor/external display, lock screen.

## 9. Repository layout

```
fluentshell/
├─ .github/workflows/ci.yml
├─ .github/workflows/release.yml
├─ app/
├─ core/{design,data,system}/
├─ feature/{desktop,taskbar,start,settings,notifications,terminal}/
├─ runtime/                # Phase 2/3, separate APK
├─ docs/
│  ├─ PROJECT_PLAN.md      # this file
│  ├─ ARCHITECTURE.md
│  ├─ TEST_REPORT.md
│  └─ AUDIT_REPORT.md
├─ gradle/libs.versions.toml
├─ .gitignore              # local.properties, *.jks, *.keystore, build/
├─ LICENSE
├─ THIRD_PARTY_LICENSES.md
└─ README.md
```

## 10. Build steps (milestones)

Each milestone = one Antigravity task → one branch/PR → CI green → merge. After every milestone run `./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug` and test on a real tablet.

### M0 — Setup
- [ ] Create GitHub repo, add `.gitignore`, LICENSE, README.
- [ ] Create Gradle multi-module project, version catalog, minSdk 29.
- [ ] Add CI workflow (Section 14), confirm a debug APK builds in Actions.
- [ ] Add ktlint/detekt, Dependabot config.
- **Done when:** empty app installs and CI uploads an APK artifact.

### M1 — Design system
- [ ] `:core:design`: colors (light/dark), Fluent typography (Selawik), shapes (8dp), elevation, icon set (Fluent System Icons).
- [ ] Acrylic/Mica composables with blur + fallback.
- [ ] Preview gallery screen (debug only).
- **Done when:** previews render correctly in light/dark on phone and tablet sizes.

### M2 — Home + Desktop
- [ ] `HomeActivity` with HOME filter, role request flow.
- [ ] `AppRepository` (LauncherApps), icon cache, package-change callbacks.
- [ ] Desktop grid, wallpaper, drag/drop, context menu, Refresh.
- **Done when:** pressing the Home button opens FluentShell, apps launch, install/uninstall updates live.

### M3 — Taskbar + Start menu
- [ ] Taskbar layout (center/left), pinned apps, tray, clock.
- [ ] Start menu: pinned, recommended, all apps, search, power menu.
- [ ] Pin/unpin, uninstall, app info.
- **Done when:** full navigation works with touch, mouse, and hardware keyboard.

### M4 — Settings app
- [ ] `SystemSettingsGateway` with result types (`Direct` / `NeedsPermission` / `DeepLink`).
- [ ] Settings shell (sidebar + pages) and all MVP pages from §7.4.
- [ ] Permission onboarding screens (Write Settings, Notification access, DND).
- **Done when:** brightness/volume/timeout/rotation really change; every other row opens the correct system screen without crashes.

### M5 — Notifications + Quick Settings
- [ ] `NotificationListenerService`, `NotificationStore`, rebinding on boot/process death.
- [ ] Toasts, action center, swipe dismiss, clear all.
- [ ] Quick settings flyout.
- **Done when:** messages from other apps show as Windows-style toasts and appear in the action center.

### M6 — Themes, polish, onboarding
- [ ] Theme manager, accent picker, wallpaper packs, blur toggle.
- [ ] First-run wizard: set as default Home, grant permissions, choose theme.
- [ ] Animations (Start menu open/close, flyouts), haptics, reduced-motion support.
- [ ] Performance pass (baseline profile, Macrobenchmark).
- **Done when:** MVP acceptance checklist (§12.5) passes on both devices.

### M7 — Release v0.1.0 (MVP)
- [ ] Signed release APK via CI, GitHub Release, checksums, changelog.
- [ ] Run all audits (§13), fix blockers.

### M8 — Phase 2 Terminal
- [ ] Runtime APK skeleton (targetSdk 28), signature-protected bridge.
- [ ] Terminal UI, bootstrap install, package manager works offline-safe.
- [ ] Tests on both SoCs. Release v0.2.0.

### M9 — Phase 3 Wine/Box64
- [ ] rootfs + Box64 + Wine bootstrap, container manager.
- [ ] Run a simple console .exe, then a GUI .exe, then a browser .exe.
- [ ] GPU driver matrix (Adreno vs Mali), per-device profiles.
- [ ] Release v0.3.0-experimental.

## 11. Antigravity workflow

1. Install Android SDK + JDK 17 locally and confirm `./gradlew --version` and `adb devices` work in the Antigravity terminal.
2. Put this file at `docs/PROJECT_PLAN.md` and tell the agent to treat it as the project brief.
3. Give the agent **one milestone at a time**, with this standing instruction:

```
Read docs/PROJECT_PLAN.md. Implement milestone <Mx> only.
Rules: Kotlin + Compose, no INTERNET permission, no Microsoft assets, minimal permissions,
write unit tests for every non-UI class, keep modules as specified.
Before finishing: run ./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug and fix all failures.
Summarize what changed, what is untested on a real device, and any permission/behavior limits found.
```

4. Review the diff, install on a real tablet via `adb install -r`, then commit and push.
5. Never let the agent commit keystores, `local.properties`, or secrets.

## 12. Test plan

### 12.1 Device matrix (minimum)
| Device | Why |
|---|---|
| 1× Snapdragon tablet (Adreno) | Baseline for UI, blur, and later Wine/Turnip |
| 1× MediaTek tablet (Mali) | Catches Mali rendering/perf issues and OEM background limits |
| Emulator (tablet, API 29 and latest) | Fast UI regression only; x86_64 debug build |
| Screen sizes | 8", 10–11", 12–13"; landscape primary, portrait supported |
| Android versions | 10, 12, 13, 14, latest |

### 12.2 Automated
- **Unit tests:** repositories, `SystemSettingsGateway` result mapping, theme persistence, notification store grouping/dismiss, search ranking.
- **Compose UI tests:** Start menu open/close, search, pin/unpin, context menus, settings navigation, theme switch.
- **Instrumented tests:** `LauncherApps` loading, DataStore/Room migrations, permission states (using test rules).
- **Lint + static analysis:** Android Lint (warnings as errors for release), ktlint, detekt.
- **Macrobenchmark:** cold start, Start menu open, scrolling the All apps list.
- **CI gates:** build, unit tests, lint, ktlint; release build on tags only.

### 12.3 Manual functional checklist
- [ ] Becomes default Home; Home button returns to FluentShell; Back does nothing destructive.
- [ ] Survives rotation, split-screen, process death, device reboot.
- [ ] Install/uninstall an app → desktop/Start update within 2 seconds.
- [ ] Refresh (menu, gesture, F5) reloads apps, wallpaper, theme.
- [ ] Brightness/volume/timeout/rotation change the real device setting.
- [ ] Each non-direct setting opens the right Android screen; Back returns to FluentShell.
- [ ] Revoking Write Settings / Notification access shows a clear prompt, no crash.
- [ ] Notification from 3 different apps: toast shown, grouped in action center, swipe dismiss cancels it.
- [ ] Light/dark/accent/blur toggles apply instantly and persist after reboot.
- [ ] Mouse, touch, stylus and hardware keyboard (Win key / Ctrl+Esc opens Start, Alt+Tab where available).
- [ ] Uninstalling FluentShell restores the previous launcher cleanly.

### 12.4 Non-functional tests
- **Performance budgets:** cold start < 1.5 s, Start menu opens < 200 ms, 60 fps scrolling with 200+ apps, idle RAM < 150 MB, no jank on Mali with blur enabled (otherwise auto-disable blur).
- **Battery:** 1 hour idle with notification service active < 2% extra drain; no wake-lock leaks.
- **Stress:** 500 notifications posted in a loop, rapid install/uninstall, 100 app icons rearranged.
- **Compatibility:** OEM background-kill behavior (check on MediaTek tablet) — notification listener must rebind.
- **Accessibility:** TalkBack navigation, 48dp touch targets, font scale 200%, contrast ≥ 4.5:1, keyboard focus order.

### 12.5 MVP acceptance checklist (gate for v0.1.0)
- [ ] All M0–M6 "Done when" criteria pass on Snapdragon **and** MediaTek.
- [ ] Zero crashes in a 30-minute monkey run (`adb shell monkey -p <pkg> 5000`).
- [ ] No ANRs, no StrictMode violations on main thread in debug.
- [ ] Release APK installs on a clean device and onboarding completes.

### 12.6 Phase 2/3 additional tests
- Terminal: shell starts, `pkg install`, large output (100k lines) stays responsive, background session survives screen off.
- Wine: console .exe, GUI .exe, installer .exe, a browser .exe; record FPS/RAM on each SoC; crash logs captured; rootfs integrity check after update.
- Storage: rootfs size (expect GBs), low-storage behavior, uninstall cleanup.

## 13. Audits

### 13.1 Security
- [ ] Every manifest component reviewed: `exported` only where needed; intent extras validated.
- [ ] Runtime bridge protected by signature-level permission; no open sockets without authentication.
- [ ] No secrets/keystores in the repo (scan with gitleaks in CI).
- [ ] `allowBackup=false` (or explicit backup rules); R8/minify and resource shrinking on release.
- [ ] Dependencies pinned; Dependabot + OWASP dependency check / `osv-scanner`.
- [ ] Accessibility service (if shipped) is off by default and never reads text content beyond what is needed.
- [ ] Terminal/Wine: sandboxed to its own storage; no root assumptions; downloads over HTTPS with checksum/signature verification.

### 13.2 Privacy
- [ ] Notification content stays on device, never logged in release, never sent anywhere.
- [ ] No analytics, no ads, no network in MVP. If crash reporting is added later, make it opt-in.
- [ ] Clear permission explanations on the onboarding screens.

### 13.3 Permissions
- [ ] Final manifest matches §6; remove anything unused.
- [ ] No `QUERY_ALL_PACKAGES`; `<queries>` limited to launcher intents.
- [ ] Every special permission has a graceful "not granted" state.

### 13.4 Performance & battery
- [ ] Macrobenchmark results recorded in `docs/TEST_REPORT.md` for both SoCs.
- [ ] Baseline Profile generated and included.
- [ ] Memory leak check (LeakCanary in debug) on Start menu, desktop, settings.

### 13.5 Accessibility
- [ ] TalkBack labels on all icons/buttons; logical focus order; reduced-motion respected.

### 13.6 Legal, licensing & branding
- [ ] No Microsoft trademarks, logos, wallpapers, or Segoe UI. App name/icon differ clearly from Windows.
- [ ] Fluent System Icons, Selawik, and every library license listed in `THIRD_PARTY_LICENSES.md`.
- [ ] Verify licenses of any Termux, Box64, Wine, DXVK, Turnip components before bundling (GPL/LGPL obligations: source offer, relinking rights).
- [ ] State clearly that Windows software run via Wine requires the user's own legal copies/licenses; no Windows OS files shipped.
- [ ] README disclaimer: "Not affiliated with or endorsed by Microsoft."

### 13.7 Release readiness
- [ ] Version name/code bumped, changelog written, signed APK, SHA-256 checksum published.
- [ ] Install notes: enable unknown sources, Play Protect warning explanation, how to set default Home, how to revert.

## 14. CI/CD — GitHub Actions

`.github/workflows/ci.yml`

```yaml
name: CI
on:
  push: { branches: [main] }
  pull_request:
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug
      - uses: actions/upload-artifact@v4
        with:
          name: fluentshell-debug-apk
          path: app/build/outputs/apk/debug/*.apk
```

`.github/workflows/release.yml` (runs on tags like `v0.1.0`)

```yaml
name: Release
on:
  push:
    tags: ['v*']
permissions:
  contents: write
jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > "$RUNNER_TEMP/release.jks"
      - name: Build signed release
        env:
          KEYSTORE_PATH: ${{ runner.temp }}/release.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew testReleaseUnitTest assembleRelease
      - name: Checksums
        run: cd app/build/outputs/apk/release && sha256sum *.apk > SHA256SUMS.txt
      - uses: softprops/action-gh-release@v2
        with:
          files: |
            app/build/outputs/apk/release/*.apk
            app/build/outputs/apk/release/SHA256SUMS.txt
          generate_release_notes: true
```

Setup once: generate a keystore locally, base64-encode it, and add `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` as GitHub repository secrets. `app/build.gradle.kts` signing config must read these from environment variables.

Quick path to get a first APK: push to `main` → open the Actions tab → download `fluentshell-debug-apk`.

## 15. Risks & mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Android blocks direct toggles (Wi-Fi, Bluetooth, airplane) | Settings can't fully replace system | System panels + clear "Opens Android settings" badge |
| OEM kills notification service (esp. aggressive battery managers) | Missing toasts | Rebind logic, battery-optimization exemption prompt, per-OEM guide |
| Blur/acrylic slow on Mali | Jank on MediaTek | Auto-detect frame drops → fall back to flat translucent |
| Exec-from-storage restrictions | Terminal/Wine won't run | Separate Runtime APK (targetSdk 28) or `lib*.so` packaging |
| Wine performance/compatibility | Poor .exe experience | Mark experimental, per-app profiles, keep Android browsers as default |
| Large rootfs (GBs) | Storage/time issues | Optional download, storage check, clear uninstall |
| Trademark/licensing | Takedown risk | Own branding, open assets, license audit |
| Scope creep | Never ships | Strict milestones; MVP released before Phase 2 |

## 16. Rough timeline (solo + AI agent)

| Milestone | Estimate |
|---|---|
| M0–M1 setup + design system | 3–5 days |
| M2–M3 desktop, taskbar, Start | 1–2 weeks |
| M4 settings app | 1 week |
| M5 notifications + quick settings | 1 week |
| M6–M7 polish, audits, v0.1.0 | 1 week |
| M8 terminal | 2–3 weeks |
| M9 Wine/Box64 | 4+ weeks, open-ended |

## 17. Definition of done (per release)

- CI green; unit, UI, and lint checks pass.
- Manual checklist (§12.3) passed on one Snapdragon and one MediaTek tablet.
- Audits (§13) completed, blockers fixed, results saved in `docs/AUDIT_REPORT.md`.
- Signed APK + checksum attached to a GitHub Release with changelog and install notes.
