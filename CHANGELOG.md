# Changelog

All notable changes to **EVSwipe** are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/),
and this project roughly follows semantic versioning.

## [Unreleased]

## [2.0.0] - 2026-08-10 — EVSuite alignment

### ⚠️ Breaking — existing users must reinstall once
- Application id changed from `com.tommasov.evswipenovalauncher` to
  **`com.evsuite.swipe`**, and the app is now signed with the **EVSuite platform
  key** (the same key as EVProfile and EVTasker). Either change alone forces a fresh
  install: uninstall the previous version, then install the new one. Settings are reset.

### Added
- **Two release channels.** `stable` has no self-update path at all — the updater class is
  not in the APK and the manifest declares no `INTERNET` permission, so a stable build is
  fully offline by construction. `unstable` is a rolling pre-release published on every
  push to `master`, with OTA, installed alongside stable as `.unstable`.
- **OTA updater** for the unstable channel, shared with the rest of the suite: https only,
  exact-match GitHub host allowlist, and the downloaded APK must be signed with the same
  certificate as the running app or it is deleted. Both gates fail closed and are
  unit-tested. Install stays a manual tap — the app does not request
  `REQUEST_INSTALL_PACKAGES`.
- **CI/CD**: `tests.yml`, `security.yml`, `unstable.yml`, `release.yml`. A blocking
  permission-drift gate fails the build on any `uses-permission` not justified in
  `.github/security/permission-allowlist.txt`; gitleaks is blocking; mobsfscan, semgrep and
  OWASP Dependency-Check upload informational SARIF.
- [SECURITY.md](SECURITY.md) with the reporting process and the design decisions behind the
  accessibility and overlay surfaces.
- `mise.toml` pinning JDK 17, with build/test/lint/permission tasks.

### Changed
- **UI rebuilt on the EVSuite design system**: Material 3 dark on the shared `ev_*`
  colour and spacing tokens, with the suite's 64 dp touch target. Dark is now imposed
  rather than following the system — the screen faces the driver at night. The day/night
  PNG artwork and `values-night/` are gone.
- Default swipe target updated to EVLauncher's new id, `com.evsuite.launcher`.

### Security
- **Accessibility declaration narrowed to what the code actually uses**:
  `typeWindowStateChanged` only, with `canRetrieveWindowContent="false"`. It previously
  declared `typeAllMask` + `canRetrieveWindowContent="true"`. `AccService` never read
  window content, but the declaration granted the ability to.

## [1.4.1] - 2026-06-22

### Changed
- The "Opening…" loader overlay is now **enabled by default** for new installs.

## [1.4] - 2026-06-22

### ⚠️ Breaking — existing users must reinstall once
- The app is now signed with a **new, stable release key**. Android does not allow
  installing an update over an app signed with a different key, so anyone on
  **v1.2 / v1.3 must uninstall the old app once** before installing v1.4. This is a
  one-time migration — every future update installs normally, without uninstalling.
  App settings are reset on reinstall.

### Added
- Stable release signing configuration: credentials read from a git-ignored
  `keystore.properties`, so every build is signed with the same key and users can
  always update in place going forward.

### Changed
- Default swipe target is now **EVLauncher**
  (`com.evsuite.launcher`) instead of Nova Launcher. As the home launcher
  it stays warm in memory, so the swipe re-opens it almost instantly.
- App display name simplified to "EVSwipe"; repository renamed to
  `EV_Swipe_Launcher` (the old URL redirects automatically).
- Added missing Italian translations (loader/option strings).

### Fixed
- The "Opening…" loader is now genuinely **event-driven**: it disappears the moment
  the target app's window is actually on screen, instead of after a fixed timer. The
  root cause was the accessibility-service config filtering out *all* window events
  (empty `packageNames`); the timeout now only acts as a safety cap.

## [1.3] - 2026-06-22

### Added
- **Swap swipe areas (left ↔ right)** option.
- "Opening…" loader overlay (initial fixed-timer version).

### Changed
- New app icon; README updates.

### Fixed
- The swap help labels now swap both their text **and** their background colour to
  match the active layout.

## [1.2] - 2024-11-03

### Added
- On-screen help labels for the two swipe areas.
- Option to hide the floating back button (useful during car servicing).

### Changed
- UI, strings and layout refinements; service-stop intent handling; back button
  visibility preference.

## [1.1] - 2024-11-02

### Added
- Custom target-app selection.
- Floating back button via the Accessibility Service (simulated physical Back).
- Italian translation; dedicated night back button.

### Fixed
- Permissions flow, floating-button drag handling, preferences manager.

## [1.0] - 2024-10-29

### Added
- Initial release: swipe up from the bottom edge of the screen to launch a chosen app.
