# Firebase Configuration Guide - ClickDungeon Online Flavor

This guide covers setting up Firebase for the **online flavor** of ClickDungeon. The offline flavor requires no Firebase setup and will build/run without these steps.

---

## Overview

The **online flavor** (`app/src/online/`) includes Firebase integration for:
- **Authentication** (optional user accounts)
- **Cloud Firestore** (save backup and sync)
- **Remote Config** (feature flags and A/B testing)
- **Cloud Functions** (backend endpoints)
- **Telemetry** (optional event logging)

The **offline flavor** (`app/src/offline/`) provides no-op implementations and requires no configuration.

---

## Prerequisites

1. **Firebase Project**
   - Visit [Firebase Console](https://console.firebase.google.com/)
   - Create a new project (or use existing)
   - Note your Firebase Project ID (e.g., `clickdungeon-abc123`)

2. **Google Account**
   - Required to manage Firebase project settings

3. **Android Studio or ClickDungeon Repository**
   - Already on your machine

---

## Step 1: Create Firebase Project

### 1.1 Access Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Create a project** or select an existing project
3. Enter **Project Name**: `ClickDungeon` (or preferred name)
4. Accept terms and click **Create project**
5. Wait for provisioning (1-2 minutes)

### 1.2 Register Android App

1. In Firebase Console, click **Add app** > **Android**
2. Fill in app details:
   - **Android package name**: `com.example.clickdungeon`
   - **App nickname** (optional): `ClickDungeon Android`
   - **Debug signing certificate SHA-1** (optional for now):
     - Run: `./gradlew signingReport` to get your debug key SHA-1
     - Copy value for `offline` variant `debugAndroidTest`
   - Click **Register app**

3. Download `google-services.json` file
   - Click **Download google-services.json**
   - Save file to: `app/google-services.json` (root of app module)

---

## Step 2: Add google-services.json to Project

### 2.1 Copy File

```powershell
# From your downloads folder, copy to app module root
Copy-Item "C:\Users\YourName\Downloads\google-services.json" `
          -Destination "ClickDungeon/app/google-services.json"
```

### 2.2 Verify Location

```
ClickDungeon/
├── app/
│   ├── google-services.json        ← Should be HERE
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   ├── offline/
│   │   └── online/
```

### 2.3 Verify Gradle Plugin

Check `app/build.gradle.kts` includes Google Services plugin:

```kotlin
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")  // ← Should be present
}
```

If missing, add:
```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services")
}
```

And in root `build.gradle.kts`:
```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

---

## Step 3: Initialize Firebase Services

### 3.1 Enable Firestore (Optional - for save sync)

1. In Firebase Console: **Firestore Database** > **Create database**
2. Select **Start in test mode** (development)
3. Choose region: `us-central1` or nearest to your users
4. Click **Create**

### 3.2 Enable Authentication (Optional - for user accounts)

1. **Authentication** > **Get started**
2. Select **Anonymous** or **Email/Password** as sign-in methods
3. Save configuration

### 3.3 Set Up Remote Config (Optional - for feature flags)

1. **Remote Config** > **Create config**
2. Add sample parameters (e.g., `enable_premium_store = true`)
3. Publish config

---

## Step 4: Build Online Flavor

### 4.1 Build with Firebase Dependencies

```powershell
# Online flavor (requires google-services.json)
./gradlew assembleOnlineDebug

# Test with Firebase integration
./gradlew testOnlineDebugUnitTest

# Run on emulator/device
./gradlew installOnlineDebug
```

### 4.2 If Build Fails

**Error**: `google-services.json not found`
- **Fix**: Ensure `app/google-services.json` exists (not in `app/src/main/`)
- **Verify**: Run `Get-ChildItem app/google-services.json`

**Error**: Firebase BOM version conflict
- **Fix**: Update Firebase BOM in `app/build.gradle.kts`:
  ```kotlin
  implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
  ```

**Error**: Google Services plugin not found
- **Fix**: Add plugin to `build.gradle.kts` as shown in Step 3.1

---

## Step 5: Implement Backend Endpoints

The online flavor's `app/src/online/java/...` package provides service stubs for:

- `AuthService.java` — User authentication
- `SaveService.java` — Cloud save sync
- `RemoteConfigService.java` — Feature flags
- `TelemetryManager.java` — Event logging
- `ChallengeService.java` — Challenge/leaderboard data

See [backend/functions/README.md](../backend/functions/README.md) for endpoint implementation details.

---

## Step 6: Deploy Cloud Functions (Optional)

For backend endpoints, deploy Cloud Functions:

```powershell
# Navigate to functions directory
cd backend/functions

# Deploy (requires Firebase CLI)
firebase deploy --only functions

# Verify deployment
firebase functions:list
```

See [backend/functions/README.md](../backend/functions/README.md) for detailed endpoint specs.

---

## Configuration Reference

### Firestore Rules (Optional)

Sample rules for read/write access (test mode):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write for authenticated users
    match /users/{userId}/saves/{saveId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Public challenge data (read-only)
    match /challenges/{document=**} {
      allow read: if true;
    }
  }
}
```

Apply via Firebase Console: **Firestore** > **Rules** > **Edit and publish**

### Remote Config Sample

Add these to Firebase Console > **Remote Config**:

| Parameter | Type | Value | Description |
|-----------|------|-------|-------------|
| `enable_premium_store` | Boolean | `true` | Show premium store |
| `event_logging_enabled` | Boolean | `true` | Enable telemetry |
| `cloud_save_enabled` | Boolean | `true` | Enable save sync |

---

## Testing Firebase Integration

### 4.1 Test Auth (if implemented)

```java
// In GameActivity or test
FirebaseAuth auth = FirebaseAuth.getInstance();
auth.signInAnonymously().addOnCompleteListener(task -> {
    if (task.isSuccessful()) {
        String uid = auth.getCurrentUser().getUid();
        Log.d("Firebase", "Auth OK: " + uid);
    }
});
```

### 4.2 Test Firestore (if implemented)

```java
// Test save backup read
FirebaseFirestore db = FirebaseFirestore.getInstance();
db.collection("users")
  .document(uid)
  .collection("saves")
  .get()
  .addOnSuccessListener(snapshot -> {
      Log.d("Firebase", "Saves: " + snapshot.size());
  });
```

### 4.3 Test Remote Config (if implemented)

```java
// Test feature flag fetch
FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
config.fetchAndActivate().addOnCompleteListener(task -> {
    boolean isPremiumEnabled = config.getBoolean("enable_premium_store");
    Log.d("Firebase", "Premium enabled: " + isPremiumEnabled);
});
```

---

## Security Best Practices

### 1. Restrict google-services.json

**Add to `.gitignore`** to prevent accidental commit:

```
# .gitignore
app/google-services.json
```

### 2. Use Service-Specific Rules

Firestore rules should restrict access by user/role:

```javascript
// Restrict saves to owner only
match /users/{userId}/saves/{saveId} {
  allow read, write: if request.auth.uid == userId;
}
```

### 3. Rotate Debug Keys Before Release

Before production, replace debug signing key SHA-1 with release key:

```powershell
# Get release key SHA-1 (after signing config is set)
./gradlew signingReport
```

Add release key SHA-1 in Firebase Console: **App settings** > **Add SHA-1**

### 4. Enable Firestore Security Rules

Before release, switch from **Test Mode** to **Production Mode**:
- Firebase Console > Firestore > **Rules** > **Edit and publish**
- Ensure rules restrict access properly

---

## Flavor Structure

### Offline Flavor (No Firebase)

```
app/src/offline/java/com/example/clickdungeon/
├── util/backend/
│   ├── AuthService.java        (no-op)
│   ├── SaveService.java        (no-op)
│   └── RemoteConfigService.java (no-op)
```

**Build**: `./gradlew assembleOfflineDebug`  
**No dependencies required**

### Online Flavor (With Firebase)

```
app/src/online/java/com/example/clickdungeon/
├── util/backend/
│   ├── AuthService.java        (Firebase Auth)
│   ├── SaveService.java        (Firestore)
│   └── RemoteConfigService.java (Remote Config)
```

**Build**: `./gradlew assembleOnlineDebug`  
**Requires**: `google-services.json`

---

## Troubleshooting

### Build Failures

**Plugin not found: `com.google.gms.google-services`**
- Ensure root `build.gradle.kts` includes plugin definition

**google-services.json conflicts**
- Check that only one `google-services.json` exists (not in `src/main/`, only in `app/`)

### Runtime Errors

**FirebaseAuth.getInstance() returns null**
- Ensure `google-services.json` is valid and app is registered in Firebase Console

**Firestore queries return empty**
- Check Firestore rules allow the authenticated user access
- Verify data is published to the correct collection/document path

### Emulator Issues

**Firebase emulator offline**
- Use online flavor with physical device or Firebase emulator suite
- For testing, use offline flavor which has no external dependencies

---

## Next Steps

1. ✅ Copy `google-services.json` to `app/` directory
2. ✅ Build with `./gradlew assembleOnlineDebug`
3. 📖 Read [backend/functions/README.md](../backend/functions/README.md) for endpoint details
4. 🔒 Configure Firestore rules for production
5. 🚀 Plan Cloud Functions deployment

---

## Additional Resources

- [Firebase Console](https://console.firebase.google.com)
- [Firebase Android Docs](https://firebase.google.com/docs/android/setup)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Remote Config Guide](https://firebase.google.com/docs/remote-config)

