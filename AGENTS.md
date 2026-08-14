# AGENTS.md — EVSwipe

Swipe-up-from-the-bottom app launcher for the SAIC MG4 head unit. Part of **EVSuite**
alongside [EVProfile](../EVProfile), [EVTasker](../EVTasker),
[EVABRPUploader](../EVABRPUploader) and [EVLauncher](../EVLauncher).

The workspace `AGENTS.md` and normative workspace `DESIGN.md` apply; this file contains
only launcher-specific additions.

Fork of [Tommasov/EV_Swipe_Launcher](https://github.com/Tommasov/EV_Swipe_Launcher) —
see [`LICENSE.md`](LICENSE.md), the licence situation is not the usual one.

Commit author: malys.training@gmail.com

## The one rule that shapes everything

**This app holds the two capabilities attackers want most** — an accessibility service and
`SYSTEM_ALERT_WINDOW` — so both are declared as narrowly as the code allows, and narrowing
is the default answer to any change:

- `res/xml/accessibility_service_config.xml`: `typeWindowStateChanged` only,
  `canRetrieveWindowContent="false"`. `AccService` forwards the foreground package name to
  `SwipeService` and performs the global back action. It never reads window content.
- The overlay is the invisible swipe strips at the bottom edge plus the optional floating
  back button. It sits on top of whatever app the driver is using, so it must stay as small
  as the gesture needs and pass touches through outside it.

Widening either declaration is a security change: justify it in the PR and update
[`SECURITY.md`](SECURITY.md).

**It never touches the vehicle.** No `android.car.*` permission, no `sharedUserId`, no IPC
to EVProfile. Vehicle reads and writes belong in EVProfile; automation in EVTasker.

## Two channels, separated by source set

| | stable | unstable |
|---|---|---|
| Application id | `com.evsuite.swipe` | `com.evsuite.swipe.unstable` |
| Published by | `v*` tag → `release.yml` | push to `master` → `unstable.yml`, rolling `unstable` tag |
| `INTERNET` | absent from the manifest | declared in `src/unstable/AndroidManifest.xml` |
| `BuildConfig.OTA_ENABLED` | `false` | `true` (hook audit-suspended) |
| Updater | `src/stable/.../UpdateHook.kt` — a no-op | policy code retained; `UpdateHook` inert during audit |

`UpdateHook` is the flavour-aware seam: `MainActivity` calls it without knowing which
channel it was built into, and the stable variant does nothing. The stable APK is fully
offline — the updater code is not in it. Keep it that way: nothing network-shaped in
`src/main/`.

The unstable OTA trigger is suspended while the suite safety and legal audit is open. Its
retained policy downloads into app-private `cacheDir`, validates
`https` plus the GitHub allowlist at every redirect, verifies the APK against the running
app certificate, then runs `/system/bin/pm install -r`. Success requires both exit code 0
and `Success` in the command output; the APK is always deleted afterwards. The unstable
manifest alone opts into `android.uid.system`; stable remains an ordinary offline app.
`OtaUpdaterTest` covers the pure policy gates and runs in CI.

## Permission allowlist is enforced, not documented

`.github/security/permission-allowlist.txt` lists every allowed `uses-permission` with the
reason it exists. `check-permissions.sh` fails the build on anything else, and it runs in
`security.yml`, in `release.yml` before publishing, and locally via `mise run permissions`.
Adding a permission means editing the allowlist **with a justification** in the same PR.

Current surface: `SYSTEM_ALERT_WINDOW` (the swipe strips), `FOREGROUND_SERVICE` +
`RECEIVE_BOOT_COMPLETED` (the overlay must exist from boot without opening settings), and
`INTERNET` in the unstable flavour only. `BIND_ACCESSIBILITY_SERVICE` is not a
`uses-permission` — it is what the service component requires *of the system*, which is how
an accessibility service is declared.

## Layout of the code

`SwipeService` is the foreground service that owns the overlay windows and the gesture
detection; `AccService` supplies the foreground package name and the back action;
`BootReceiver` starts the service at boot. `MainActivity` is the settings screen (target
app, strip geometry, options, version), `PermissionActivity` the gate shown when overlay or
accessibility is missing, `PreferencesManager` the persistence, `AppListAdapter` the target
picker.

## Reference patterns (shared with the suite)

- **Signing**: the EVSuite platform key, path + passwords from env vars (CI) or
  `gradle.properties` (local); the `signingConfig` is created only if the keystore file
  exists. Never a literal secret in a build file.
- **Security CI**: `.github/workflows/security.yml` — blocking permission-drift gate +
  gitleaks, plus informational SARIF from mobsfscan / semgrep / dependency-check.
- **Theme**: dark Material 3 on the shared `ev_*` colour and spacing tokens, 64 dp touch
  targets. Dark is imposed, not system-following — glare on a windscreen at night.
- **Language**: English by default (code, comments, commits, docs).

## Build

`mise run build | build-unstable | test | check | permissions | run`. JDK 17, AGP 8.6,
Gradle 8.7, `minSdk 28` / `targetSdk 34`. Emulator: `mise run emulator-setup` then
`emulator-screen` (API 28 at panel geometry) or `emulator-car` (API 33 Automotive); AVDs
are named `evswipe-*`, per-repo like the sibling projects. Overlay and accessibility are
special permissions that `adb install -g` does not grant — `mise run grant-permissions`
sets them on a throwaway emulator, and nothing in the app should ever attempt the same at
runtime.
