# Security policy

This app holds an accessibility service and draws over other apps — the pairing most
Android malware uses. It deserves scrutiny, and security reports are welcome and taken
seriously.

## Reporting a vulnerability

Please **do not** open a public issue for a vulnerability. Use GitHub's
[private vulnerability reporting](../../security/advisories/new) instead.

Include what you were able to do and on which firmware generation. A proof of concept
helps; a working exploit is not required.

## What is in scope

- Anything that makes `AccService` read, log, or forward **window content** — text typed by
  the driver, notification contents, anything beyond the foreground package name.
- Tapjacking: anything that lets the floating button or the swipe strips sit over another
  app's UI in a way that causes an unintended tap to be delivered to that app.
- Anything that lets another app on the head unit drive `AccService`'s back action or
  `SwipeService` — for example by sending the internal `ACTION_BACK` broadcast.
- Anything that gets the unstable updater to accept an APK that is **not** served over https
  from an allowlisted GitHub host, or that is **not** signed with the running app's
  certificate.
- Path traversal or overwrite through the OTA download file name, which is derived from a
  remote asset name.

## What is not in scope

- Requiring physical access to an unlocked head unit with developer mode enabled.
- The app requiring accessibility and overlay permissions at all. Those are the feature: a
  virtual back button and edge swipe areas cannot exist without them, and both are granted
  explicitly by the user through system screens.
- The unstable channel self-updating. That is the channel's stated trade-off; use stable if
  you do not want it.
- Vulnerabilities in the OEM firmware itself.

## Design decisions you should know about

- **The accessibility declaration is as narrow as the code.** `accessibility_service_config`
  requests `typeWindowStateChanged` only, with `canRetrieveWindowContent="false"`. Earlier
  versions declared `typeAllMask` + `canRetrieveWindowContent="true"`; the code never used
  it, but the declaration granted the ability to, and that gap was the point. `AccService`
  handles exactly one event — it forwards the *foreground package name* to `SwipeService`
  so the "opening" loader can be dismissed at the right moment — and performs exactly one
  action, `performGlobalAction(GLOBAL_ACTION_BACK)`. It never calls
  `getRootInActiveWindow()` and never reads node text.
- **The internal broadcast is not exported.** `ACTION_BACK` is registered with
  `RECEIVER_NOT_EXPORTED` and sent with an explicit package, so another app cannot trigger
  the back action. `SwipeService` and `AccService` are both `exported="false"`.
- **Two channels, and stable is fully offline.** `stable` contains no updater class and its
  manifest declares no `INTERNET` permission — the app cannot reach the network by
  construction. It is not a disabled feature; the code is not in the APK. Stable is
  installed from a USB stick.
- **The unstable updater fails closed, twice.** The APK URL comes from a remote JSON
  document and is never trusted: https only, **exact-match** host allowlist (never a suffix
  test, so `github.com.attacker.net` is rejected), re-checked immediately before the URL
  reaches the system downloader. The downloaded APK must then be signed by the same
  certificate as the running app, or it is deleted rather than offered for install. An
  unreadable archive or a failed API call counts as a mismatch. Both gates are unit-tested.
- **No `REQUEST_INSTALL_PACKAGES`.** The updater downloads to public Downloads and stops
  there; the user taps the file and the *system* installer prompts. The permission-drift
  gate blocks the permission from appearing without a review.
- **The remote version string never reaches a path raw.** It is reduced to `[a-z0-9._-]`
  before becoming a file name, and there is a unit test for `../../etc/passwd`.
- **Permission drift is a blocking CI gate.** Every `uses-permission` in every manifest must
  appear in `.github/security/permission-allowlist.txt`, which carries the justification
  for each one. Adding a permission without editing that file fails the build.
- **Signed with the EVSuite platform key.** This app claims no privileged permission of
  its own; the shared key is what makes the suite one installable set, and it is what the
  OTA signature check compares an incoming APK against.
- **The APK is not minified.** R8 is off on release, so a published APK stays verifiable
  line-by-line against this source — which matters more here than for most apps.
