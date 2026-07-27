## What and why

<!-- What changes, and what problem it solves. The diff already says what; explain why. -->

## Verification

<!-- Be specific about what you actually ran. "Not verified on a head unit" is a fine and
     expected answer — most of this can only be confirmed on the car or an emulator. -->

- [ ] `mise run check` passes (permission gate + lint + unit tests)
- [ ] New behaviour is covered by a unit test
- [ ] Tried on the emulator (`mise run run`, which grants overlay + accessibility)
- [ ] Tried on a real MG4 head unit — if yes, which firmware: <!-- e.g. SWI68 -->

## Security checklist

- [ ] `res/xml/accessibility_service_config.xml` unchanged — still `typeWindowStateChanged`
      only, still `canRetrieveWindowContent="false"` (if changed, justify it here and update
      `SECURITY.md`)
- [ ] `AccService` still never reads window content
- [ ] The overlay stays as small as the gesture needs and passes touches through outside it
- [ ] The app still holds no vehicle privileges (no `android.car.*`, no `sharedUserId`, no
      MG4Control IPC)
- [ ] No new `uses-permission` — or it is added to
      `.github/security/permission-allowlist.txt` with a justification
- [ ] Nothing network-shaped added to `src/main/`; OTA code stays in `src/unstable/` behind
      `BuildConfig.OTA_ENABLED`
- [ ] Nothing in the app tries to self-grant overlay or accessibility at runtime

## Notes for the reviewer

<!-- Anything you are unsure about, or deliberately left out of scope. -->
