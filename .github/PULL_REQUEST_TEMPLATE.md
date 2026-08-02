# Pull Request: MG4SwipeLauncher

## 📝 What and Why

**What changes:**
<!-- List the files changed and the functional changes -->

**Why:**
<!-- Explain the problem this solves and how it solves it. "The diff already says what" — your job is to say WHY. -->

---

## 🔍 Verification

**Testing performed:**
- [ ] `mise run check` passes (permission gate + lint + unit tests)
- [ ] New behavior covered by a unit test
- [ ] Tested on emulator (`mise run run`)
- [ ] Tested on real MG4 head unit — Firmware version: ____________

**Gesture testing (if swipe detection changed):**
- [ ] Swipes from each edge (left, right, top, bottom) work correctly
- [ ] Strip width configured correctly in tests
- [ ] Gesture detection sensitivity matches expected behavior
- [ ] No false positives (accidental activation)
- [ ] No missed gestures (intended swipes detected)
- [ ] Overlay responsiveness acceptable (< 100ms)

**Overlay/rendering (if overlay display changed):**
- [ ] Overlay draws at correct screen edges
- [ ] Visual appearance consistent with configured strip size
- [ ] Passes touches through correctly outside the strip
- [ ] No overlay flicker or rendering artifacts
- [ ] Works with different foreground apps (navigation, media, settings)

---

## 🔐 Security Checklist

**Overlay Integrity:**
- [ ] Accessibility service config unchanged — still `typeWindowStateChanged` only, still `canRetrieveWindowContent="false"`
- [ ] `AccService` does NOT read window content
- [ ] Overlay is minimal (only the gesture strip) — passes touches through outside it
- [ ] Overlay does NOT interact with safety-critical apps (navigation, phone, cruise control)

**Permissions & Privileges:**
- [ ] No new `uses-permission` added (or added to `.github/security/permission-allowlist.txt` with justification)
- [ ] No vehicle privileges (no `android.car.*`, no `sharedUserId`, no MG4Control IPC)
- [ ] App does NOT attempt to self-grant overlay or accessibility at runtime
- [ ] No network code in `src/main/` (OTA stays in `src/unstable/` behind `BuildConfig.OTA_ENABLED`)

**Input Validation:**
- [ ] All user input validated (gesture coordinates, strip width, etc.)
- [ ] No prompt injection risk — all configuration strings sanitized
- [ ] Gesture coordinates checked for bounds (screen dimensions)

---

## 🤖 Optional: Claude AI Assistance

If you'd like Claude AI to help review this PR, include this checklist:
- [ ] I request automated code review from Claude AI
- [ ] I understand Claude may suggest improvements to clarity, efficiency, or safety
- [ ] I grant permission to use my PR content for training (per GitHub's terms)

**Claude Refinement Prompt** (optional — paste if requesting AI review):
```
Please review this swipe launcher PR for:
1. Gesture detection correctness
2. Overlay safety (doesn't hijack foreground app input)
3. Security checklist compliance
4. Prompt injection resistance
5. Performance impact on gesture detection
```

---

## 📋 Notes for Reviewer

<!-- Any context, gotchas, or decisions for the reviewer -->


<!-- Anything you are unsure about, or deliberately left out of scope. -->
