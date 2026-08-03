# Disclaimer — no warranty, no liability

**Use this software entirely at your own risk.**

This project is provided **"as is"**, without warranty of any kind, express or implied,
including but not limited to the warranties of merchantability, fitness for a particular
purpose and non-infringement. In no event shall the authors or contributors be liable for
any claim, damages or other liability, whether in an action of contract, tort or otherwise,
arising from, out of or in connection with the software or its use.

It is an experimental, non-commercial project provided for **study and educational
purposes**.

## What that means concretely

- The app runs on a **vehicle head unit**, holds an **accessibility service** and **draws
  over other apps**. That combination is powerful by design: it is what lets a swipe from
  the bottom edge reach any foreground app. It is also the classic Android malware pairing,
  which is why the declarations are kept as narrow as the code —
  `typeWindowStateChanged` only, `canRetrieveWindowContent="false"` — and why widening them
  is a reviewed change, not a drive-by commit. See [`SECURITY.md`](SECURITY.md).
- **The swipe strips sit at the bottom edge of every screen.** A mistuned strip can
  intercept a touch meant for the app underneath, including a vehicle app. Set the strip
  size parked, and try it before you rely on it.
- **Do not configure the app while driving.** Choosing the target app, resizing the swipe
  areas or granting permissions is parked-only work.
- Installing it is your decision and your responsibility, including any effect on the head
  unit's stability, your warranty, your insurance, or your vehicle's roadworthiness.
- The app **holds no vehicle privileges**: no `android.car.*` permission, no `sharedUserId`,
  no bridge to MG4Control. It cannot read or change a vehicle setting. What it *can* do is
  start an app and consume touches at the edge of the screen the driver looks at.
- **Compatibility is inferred, not certified.** The panel geometry and Android version
  are project compatibility targets, not a vendor specification. A firmware update can
  change the system UI or how overlays and
  accessibility services are handled.
- The **stable channel is fully offline** — no `INTERNET` permission, no updater code in
  the APK. The **unstable channel self-updates** from GitHub pre-releases over https, with a
  host allowlist and a signature check, and is meant for testers, not for a car you depend
  on.
- Release builds are signed with the **MG4 suite platform key**. Installing them replaces
  any earlier build signed differently — you must uninstall first, and your settings are
  reset.

## Not affiliated

This project is **not affiliated with, endorsed by, or supported by** SAIC Motor, MG Motor,
Nova Launcher, or Google. All trademarks, brand names and graphic resources belong to their
respective owners and are used only to identify the vehicle the software targets.

## Contributors

Contributors provide their work on the same terms: no warranty, and no liability for how
anyone uses it.
