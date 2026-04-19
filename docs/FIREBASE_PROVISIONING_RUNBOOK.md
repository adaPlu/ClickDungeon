# Firebase Provisioning Runbook

Step-by-step guide for provisioning a new Firebase project for ClickDungeon and
verifying that Analytics and Crashlytics events reach the Firebase console.

---

## 1. Create the Firebase Project

1. Open [console.firebase.google.com](https://console.firebase.google.com) and sign in
   with the project Google account.
2. Click **Add project**.
3. Enter the project name **ClickDungeon** and click **Continue**.
4. Enable or disable Google Analytics as desired (recommended: enable it so the
   Analytics DebugView is available), then click **Create project**.

---

## 2. Register the Android App

1. In the project overview, click the **Android** icon to add an Android app.
2. Enter the package name exactly: `com.adaplu.clickdungeon`
3. Optionally enter the app nickname (e.g. "ClickDungeon Android") and the
   SHA-1 debug certificate fingerprint (required for Google Sign-In / Dynamic
   Links, optional for Analytics).
4. Click **Register app**.

---

## 3. Download and Place google-services.json

1. On the next screen click **Download google-services.json**.
2. Copy the downloaded file to the **app/** directory:
   ```
   app/google-services.json
   ```
3. **Never commit this file.** Verify that `app/google-services.json` is listed
   in `.gitignore`. If not, add it:
   ```
   app/google-services.json
   ```
4. Click **Next** through the remaining SDK setup screens (the Gradle plugin is
   already configured in this project).

---

## 4. Enable Analytics DebugView

1. In the Firebase console, open the **Analytics** section in the left sidebar.
2. Select **DebugView** (under Analytics > DebugView).
3. The view will show events in real time once step 5 is completed on a
   connected device.

---

## 5. Enable Analytics Debug Mode on Device

On an ADB-connected Android device or emulator, run:

```bash
adb shell setprop debug.firebase.analytics.app com.adaplu.clickdungeon
```

This puts the app into DebugView mode so events appear in the console within
seconds rather than being batched. To disable debug mode later:

```bash
adb shell setprop debug.firebase.analytics.app .none.
```

---

## 6. Build a Release APK

From the project root, run:

```bash
./gradlew :app:assembleRelease
```

The signed APK will be output to `app/build/outputs/apk/release/`. Ensure
signing is configured in `app/build.gradle` (keystore path, alias, passwords)
before running this step.

---

## 7. Verify Analytics Events in DebugView

1. Install the release (or debug) APK on the device connected in step 5.
2. Launch the app and play through a full run (start a run, fight at least one
   monster, level up, then die or reach victory).
3. In the Firebase console DebugView, confirm the following events appear:
   - `run_start` — fired when a new dungeon run begins.
   - `floor_reached` — fired each time the player descends to a new floor.
   - `combat_ended` — fired after every combat outcome (victory, defeat, fled).
   - `level_up` — fired when the player gains a character level.
   - `ability_used` — fired when the player activates a class ability.
4. Check event parameters by clicking each event row to expand its bundle.

---

## 8. Verify Crashlytics Non-Fatals

1. Trigger a `logSaveFailed` event. The easiest way is to temporarily force a
   save error in a debug build (e.g. by pointing SaveManager at an unwritable
   path) or by adding a one-off test button that calls:
   ```java
   TelemetryManager.logSaveFailed("TEST_ERROR", 0);
   ```
2. Wait up to 5 minutes for the non-fatal to propagate to Crashlytics.
3. In the Firebase console, open **Crashlytics** > **Non-fatals** and confirm
   an entry with the message `save_failed: TEST_ERROR slot=0` appears.
4. Remove the test code before releasing.

---

## Notes

- `google-services.json` contains API keys scoped to this Firebase project.
  Treat it as a secret and manage it via CI secrets (see `docs/CI_SECRETS.md`).
- All Analytics events are defined in `TelemetryManager`; see
  `docs/TELEMETRY_EVENTS.md` for the full event schema.
- DebugView data is not retained in production reports; only release builds
  running without the debug ADB property contribute to Analytics dashboards.
