# Contributing to EVSwipe

Thank you for contributing! This guide ensures your work aligns with the project's safety, security, and quality standards.

## 🚨 Critical Context: Swipe Overlay Safety

**This app runs as an overlay on top of every other app on the infotainment screen.** Incorrectly capturing, blocking, or misinterpreting swipe gestures can:
- Block the driver's access to navigation, phone, or safety-critical functions
- Cause the app in the foreground to miss intended input
- Create security vulnerabilities where malicious apps hijack the overlay

**Every PR touching gesture detection or overlay rendering must be tested on the actual vehicle.**

---

## 🎯 Ground Rules

1. **The accessibility service stays minimal.** `AccService` reads `TYPE_WINDOW_STATE_CHANGED` only (learns foreground package), never window content. `canRetrieveWindowContent="false"` in `res/xml/accessibility_service_config.xml` is non-negotiable.
2. **The app holds no vehicle privileges.** No `android.car.*`, no `sharedUserId`, no IPC to EVProfile. Vehicle work → EVProfile. Automation → EVTasker.
3. **The overlay must not eat touches it doesn't own.** Swipe strips are minimal and pass touches through outside the gesture area. Never block input the driver intended for the foreground app.
4. **Stable stays offline.** No `INTERNET` permission in stable flavor. All network code in `src/unstable/` behind `BuildConfig.OTA_ENABLED`.
5. **Be transparent about testing.** Say what you verified and what you didn't. "Passes CI, emulator OK, not tested on vehicle" is a good PR note.

---

## 🔧 Setup & Testing

### Build & Test Locally

```bash
# Check before opening a PR
mise run check      # permission gate + lint + unit tests

# Or without mise:
bash .github/security/check-permissions.sh
./gradlew testStableDebugUnitTest testUnstableDebugUnitTest lintStableDebug

# Deploy to emulator (auto-grants overlay + accessibility)
mise run run
```

### Testing Checklist

- **Unit Tests:** Gesture coordinates, config persistence, permissions
- **Emulator Tests:** Swipes from all edges, overlay responsiveness, no false positives
- **Vehicle Tests:** Real touch input, concurrent apps, different firmware versions (SWI68, SWI133, etc.)

### Gesture Testing Protocol

```bash
# On emulator or device:
# 1. Enable overlay (app notification → "Start" or settings)
# 2. Swipe from left edge → should open launcher
# 3. Swipe from right edge → should trigger configured action
# 4. Swipe from top/bottom edges → test if configured
# 5. Open maps + swipe → verify maps receives touches outside strip
# 6. No false positives while using other apps
```

---

## 💻 Coding Standards

### Gesture Detection & Overlay

```kotlin
// ✅ GOOD: Minimal accessibility service, gesture detection only
class AccService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            handleWindowChange(packageName)
        }
    }
    // No window content reading, no hierarchy access
}

// ✅ GOOD: Overlay passes touches through
val params = WindowManager.LayoutParams(
    TYPE_ACCESSIBILITY_OVERLAY,  // Minimal overlay type
    FLAG_NOT_TOUCHABLE or FLAG_NOT_FOCUSABLE  // Pass touches through
)

// ❌ BAD: Reading window content (PROHIBITED)
override fun onAccessibilityEvent(event: AccessibilityEvent) {
    val windowContent = event.source?.text  // NO! violates security model
}

// ❌ BAD: Blocking touches
val params = WindowManager.LayoutParams(
    TYPE_ACCESSIBILITY_OVERLAY,
    0  // Not pass-through → blocks driver input!
)
```

### Input Validation

```kotlin
// ✅ GOOD: Validate configuration with bounds
fun setStripWidth(width: Int): Boolean {
    if (width < MIN_STRIP_PX || width > MAX_STRIP_PX) return false
    preferences.edit().putInt("strip_width", width).apply()
    return true
}

// ✅ GOOD: Sanitize user strings
fun setGestureLabel(label: String) {
    val sanitized = label
        .take(MAX_LABEL_LEN)
        .filter { it.isLetterOrDigit() || it.isWhitespace() }
    preferences.edit().putString("label", sanitized).apply()
}

// ❌ BAD: No bounds checking (can crash or set invalid state)
fun setStripWidth(width: String) {
    preferences.edit().putInt("strip_width", width.toInt()).apply()
}

// ❌ BAD: Trusting user input (prompt injection)
fun setGestureLabel(label: String) {
    preferences.edit().putString("label", label).apply()  // Injection risk!
}
```

### Thread Safety

- Use `Handler` or `Coroutines` for background gesture processing
- Never block UI thread with gesture detection
- Synchronize shared gesture state access

---

## 📋 Submitting a Pull Request

1. **Before opening:** Run `mise run check` — must pass
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/gesture-sensitivity
   git commit -m "Add: gesture sensitivity slider"
   ```
3. **Push and open PR** using the [PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md)
4. **Address feedback** — push additional commits, no force-push
5. **Merge** when approved — squash if appropriate

### PR Template Sections

- **What and Why:** Clear problem statement and solution rationale
- **Verification:** Honest list of what you tested (emulator, vehicle, CI)
- **Gesture Testing:** If gesture detection changed, detail all edge cases tested
- **Overlay/Rendering:** If overlay display changed, verify all screens
- **Security Checklist:** Accessibility config, permissions, input validation
- **Claude AI Review:** Optional — request AI assistance for code review

---

## 🔐 Security Guidelines

### Overlay Integrity (Non-Negotiable)

**The overlay MUST NOT:**
- Capture input intended for the foreground app
- Read window content or app hierarchy
- Intercept navigation or phone calls
- Persist across uninstall (data in `SharedPreferences` is cleared by Android)

**Verify before PR:**
```bash
adb shell dumpsys accessibility | grep -i "EVSwipe"
# Expected: canRetrieveWindowContent: false
#           EVSwipe accessibility enabled
```

### Permissions

Every new `uses-permission` fails CI until added to `.github/security/permission-allowlist.txt` with justification:

```
SYSTEM_ALERT_WINDOW       → "Draw swipe strip overlay over other apps"
BIND_ACCESSIBILITY_SERVICE → "Detect swipe gestures (TYPE_WINDOW_STATE_CHANGED only)"
```

**Request install packages:** Deliberately absent. Unstable updater downloads APK; user taps to install.

### Prompt Injection Prevention

All user input must be:
1. **Type-checked** — integer, string length, enum values
2. **Bounds-checked** — screen dimensions, valid ranges
3. **Sanitized** — no special characters, no escape sequences
4. **Tested** — edge cases: 0, negative, max int, empty strings, Unicode

---

## 🤖 Using Claude AI to Improve Your Contribution

If you want Claude AI to help refine your issue or PR:

### For Issues
- **Add a comment:** `@Claude review-clarity`
- Claude will clarify and improve issue wording
- You approve before closure

### For Pull Requests
- **In PR template:** Check "I request automated code review from Claude AI"
- **Provide a refinement prompt:**
  ```
  Claude, please review this gesture detection change for:
  - Correctness of coordinate bounds checking
  - Overlay pass-through logic (doesn't block input outside strip)
  - Security checklist compliance
  - Input validation edge cases
  ```

### What Claude Can Help With
- ✅ Clarify issue descriptions
- ✅ Review code for security patterns
- ✅ Catch prompt injection and input validation gaps
- ✅ Suggest efficiency improvements
- ✅ Help write tests

### What Claude Cannot Do
- ❌ Test on actual vehicle hardware (humans only)
- ❌ Approve merges (maintainers only)
- ❌ Override safety requirements

**Data Usage:** Claude may learn from your contributions. By using Claude AI assistance, you agree to GitHub's terms.

---

## 📚 Project Structure

```
EVSwipe/
├── app/
│   ├── src/main/          # Core overlay & gesture detection (stable)
│   ├── src/unstable/      # OTA updater (unstable flavor only)
│   └── res/xml/
│       └── accessibility_service_config.xml  # Do NOT widen this!
├── .github/
│   ├── ISSUE_TEMPLATE/    # Bug, feature, security templates
│   ├── security/          # Permission allowlist, security gate
│   └── workflows/         # CI/CD (security.yml, tests.yml, release.yml)
├── SECURITY.md            # Vulnerability reporting (private)
├── DISCLAIMER.md          # Legal disclaimer
└── CONTRIBUTING.md        # This file
```

---

## 📖 Resources

- **[SECURITY.md](SECURITY.md)** — Vulnerability reporting & security policy
- **[DISCLAIMER.md](DISCLAIMER.md)** — Legal disclaimer (not affiliated with SAIC/MG Motor)
- **[README.md](README.md)** — Project overview
- **[LICENSE.md](LICENSE.md)** — License terms

---

## ❓ Questions?

- **General questions?** Open a Discussion or ask in an issue
- **Security issues?** See [SECURITY.md](SECURITY.md) for private reporting
- **Build problems?** Check the README or open an issue

---

**Thank you for contributing safely and securely!** 🎉

