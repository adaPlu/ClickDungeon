# Firebase / Connected Services Setup - ClickDungeon

This guide reflects the **current repository state as of 2026-04-08**. Firebase/Crashlytics is fully wired in Gradle and initialized in code. The only remaining step is provisioning the Firebase project and placing `google-services.json` locally.

---

## Current repo state

- The project builds as a **single Android app module** with `debug` / `release` variants.
- `app/src/online/java` is used for **online-only test and service scaffolding** and is compiled into `:app:testDebugUnitTest`.
- **Firebase plugins and dependencies are already enabled** in `app/build.gradle.kts` — see below.
- `FirebaseCrashlytics` is initialized in `ClickDungeonApp.java` with collection disabled in debug and enabled in release.
- `google-services.json` is gitignored and must be placed locally before a release build will succeed.

---

## Use this guide when you need

- a Firebase project for ClickDungeon
- `app/google-services.json` for local validation
- App Check configuration
- Play Console API or service-account access for purchase validation
- challenge-signing key material for connected-services rollout

---

## Step 1: Create and register the Firebase project

1. Open [Firebase Console](https://console.firebase.google.com/).
2. Create or select the `ClickDungeon` project.
3. Register the Android app with package name `com.adaplu.clickdungeon`.
4. Download `google-services.json`.

For local-only testing, place the file at:

```text
app/google-services.json
```

> Do **not** commit this file. Use CI secrets for shared automation; see `docs/CI_SECRETS.md`.

---

## Step 2: Store secrets safely

### Local development

```powershell
Copy-Item "C:\Users\YourName\Downloads\google-services.json" `
          -Destination "ClickDungeon/app/google-services.json"
```

### CI / GitHub Actions

Use the instructions in `docs/CI_SECRETS.md` and the placeholder workflow in `.github/workflows/ci-secrets-placeholder.yml`.

Recommended secret names:
- `GOOGLE_SERVICES_JSON`
- `PLAY_SERVICE_ACCOUNT_JSON`

---

## Step 3: Firebase is already wired — no Gradle changes needed

The following are **already in place** as of 2026-04-08:

**Root `build.gradle.kts`:**
```kotlin
alias(libs.plugins.google.services) apply false
alias(libs.plugins.firebase.crashlytics.plugin) apply false
```

**`app/build.gradle.kts` plugins block:**
```kotlin
alias(libs.plugins.google.services)
alias(libs.plugins.firebase.crashlytics.plugin)
```

**`app/build.gradle.kts` dependencies:**
```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.crashlytics)
implementation(libs.firebase.analytics)
```

**`ClickDungeonApp.java`:**
```java
FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
```

Once `google-services.json` is placed at `app/google-services.json`, the build will connect to Firebase automatically.

---

## Step 4: Verify the current local build path

These commands are valid **today** and match the current project setup:

```powershell
# Build the current debug variant
.\gradlew.bat :app:assembleDebug

# Run the verified local unit-test target
.\gradlew.bat :app:testDebugUnitTest --no-daemon
```

Verified locally on **2026-03-30**: `:app:testDebugUnitTest` passes.

> Tests under `app/src/online/java` are included in `:app:testDebugUnitTest`. The legacy alias `:app:testOnlineDebugUnitTest` is also available for older scripts and docs, but it currently maps to the same standard unit-test run.

---

## Step 5: External rollout checklist (Track 2)

Before connected services can be considered validated, the following external items still need owners and credentials:

- [ ] Firebase project setup complete
- [ ] `google-services.json` issued and stored securely
- [ ] App Check configured
- [ ] Play Console API access granted for purchase validation
- [ ] challenge-signing key material created and stored in a secure vault
- [ ] device-matrix validation run for auth, leaderboard, friends, cloud save, challenges, and sandbox purchases

---

## Troubleshooting

### `google-services.json not found`
- Ensure the file is placed at `app/google-services.json`
- Do not place it under `app/src/main/`

### CI cannot decode secrets
- Re-encode the JSON with base64 and update the matching GitHub secret
- Check `.github/workflows/ci-secrets-placeholder.yml` for the expected secret names

### Build works but Firebase is still inactive
- That is expected until the Google Services plugin and Firebase SDKs are enabled in the app module

---

## Security notes

- Never commit `google-services.json`, Play service-account JSON, or private signing keys.
- Store them in your organization secrets manager or CI secret store.
- Keep production Firebase rules and Play access limited to authorized owners.

---

## Additional resources

- [Firebase Console](https://console.firebase.google.com)
- [Firebase Android setup](https://firebase.google.com/docs/android/setup)
- [Google Play Billing overview](https://developer.android.com/google/play/billing)
- `docs/CI_SECRETS.md`
- `docs/NEXT_PHASE_PLAN.md`

