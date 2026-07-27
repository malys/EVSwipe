# Contributing

This app holds an **accessibility service** and **draws over every other app** on a car's
head unit. That single fact drives everything below.

## Ground rules

1. **The accessibility service stays minimal.** `AccService` exists to learn the foreground
   package name (`typeWindowStateChanged`) and to perform the global back action. It does
   **not** read window content — `canRetrieveWindowContent="false"` in
   `res/xml/accessibility_service_config.xml`. Widening that config, or adding an event
   type, is a security change: it needs a justification in the PR and an update to
   [`SECURITY.md`](SECURITY.md).
2. **The app holds no vehicle privileges.** No `android.car.*` permission, no
   `sharedUserId`, no IPC to MG4Control. A patch that reaches the vehicle from here will be
   rejected — vehicle work belongs in MG4Control, and automation in MG4Tasker.
3. **The overlay must not eat touches it does not own.** The swipe strips sit on top of
   whatever the driver is using. Keep them as small as the gesture needs, keep them
   pass-through outside the gesture, and never grow the default size without saying why.
4. **Stable stays offline.** The `stable` flavor declares no `INTERNET` permission and
   contains no updater code, by construction. Anything network-shaped goes in
   `src/unstable/` behind `BuildConfig.OTA_ENABLED`, or it does not go in.
5. **Say what you did not verify.** Most of this can only be confirmed on a head unit.
   "Builds, unit tests pass, tried on the emulator, not on the car" is a good PR note.
   Silence implying it ran in a vehicle is not.

## Before opening a PR

```bash
mise run check      # permission gate + lint + unit tests
```

or, without mise:

```bash
bash .github/security/check-permissions.sh
./gradlew testStableDebugUnitTest testUnstableDebugUnitTest lintStableDebug
```

New behaviour needs a unit test. Logic that can be tested without Android — update-gate
decisions, version comparison, host allowlisting — belongs in a plain class, not in a
service or an activity.

## Permissions

Any new `uses-permission` fails CI until it is added to
`.github/security/permission-allowlist.txt` **with a justification comment**. This app
already holds the two permissions attackers want most (`SYSTEM_ALERT_WINDOW` plus an
accessibility service); it should not gain a third quietly. `REQUEST_INSTALL_PACKAGES` is
deliberately absent — the unstable updater downloads and stops, the user taps the file.

## The two channels

| | stable | unstable |
|---|---|---|
| Published by | `v*` tag | every push to `master` |
| Application id | `com.mg4.launcher.swipe` | `com.mg4.launcher.swipe.unstable` |
| `INTERNET` | absent | declared in `src/unstable/` |
| Updater code | not in the APK | https + host allowlist + signature check |

A change that blurs that separation — an updater class reachable from `src/main/`, a
permission moved up out of `src/unstable/` — will be rejected.

## Emulator note

Overlay and accessibility are "special" permissions: `adb install -g` does not grant them.
`mise run grant-permissions` sets the overlay appop and appends the service to
`enabled_accessibility_services`. That is a throwaway-emulator shortcut. Do not add
anything that tries the same trick at runtime on a real device — granting an accessibility
service is a deliberate, informed act by the user.

## Language and licence

English by default — code, comments, commit messages, docs. Read
[`LICENSE.md`](LICENSE.md) before contributing: this is a fork of an unlicensed upstream
project, and the licence situation is genuinely unusual.

## Commit messages

Explain **why**, not what — the diff already says what. If you fixed something subtle, say
what the failure looked like on the head unit, so the next person recognises it.
