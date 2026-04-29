# Developer Setup Guide - ClickDungeon Android

This guide walks through the recommended local setup for building and testing ClickDungeon.

---

## Prerequisites

### Required Software

1. **JDK 17** (or later)
   - Download from [Oracle](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html) or [OpenJDK](https://adoptopenjdk.net/)
   - Windows: Recommended installation path: `C:\Program Files\Java\jdk-17.x.x`
   - Verify installation: Open PowerShell and run `java -version` (should show 17.x.x)

2. **Android Studio** (2024.1 or later)
   - Download from [Google Android Studio](https://developer.android.com/studio)
   - Follow installation wizard; accept default SDK paths
   - Launch Android Studio at least once to initialize build tools

3. **Android SDK** (via Android Studio)
   - Target SDK: **36** (Android 16)
   - Min SDK: **21**
   - Compile SDK: **36**
   - Install via Android Studio > SDK Manager > SDK Platforms:
     - ✅ Android 16 (API 36)
     - ✅ Android 7.0 (API 24) - for testing
     - ✅ Build Tools 36.0.0

4. **Gradle Wrapper** (included in repo)
   - Already bundled in `gradlew.bat` (Windows)
   - Uses version specified in `gradle/wrapper/gradle-wrapper.properties`

---

## Initial Setup

### 1. Clone and Configure

```powershell
# Clone the repository
git clone <repository-url>
cd ClickDungeon

# Copy local.properties.example to local.properties
Copy-Item local.properties.example -Destination local.properties

# Update local.properties with your Android SDK path
# Example: C:\Users\YourUsername\AppData\Local\Android\sdk
```

### 2. Verify JDK Configuration

```powershell
# Prefer Android Studio's Gradle JDK selector or JAVA_HOME.
# If you must pin Gradle's JDK path, put it in your user Gradle properties:
#   $env:USERPROFILE\.gradle\gradle.properties
# Never add machine-specific paths to the tracked project gradle.properties file.

# Verify in PowerShell:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.x.x"
java -version
```

### 3. Build the Project

```powershell
# Run Gradle build
./gradlew build

# Or for faster testing without full build:
./gradlew testDebugUnitTest

# If you encounter Gradle init script errors:
# - Delete the .gradle folder and retry
# - Run: ./gradlew clean
```

---

## Running Tests

### Unit Tests (Robolectric + JUnit)

```powershell
# Run all unit tests
./gradlew test

# Run the main local smoke target (verified 2026-03-30)
./gradlew :app:testDebugUnitTest

# Windows helper script for the same task + report collection
./scripts/run_online_tests.ps1
```

**Note**: Tests pin SDK 34 via `robolectric.properties`. `app/src/online/java` is currently included in the standard `test` source set, so online smoke tests run under `:app:testDebugUnitTest`. For compatibility with older scripts/docs, the alias task `:app:testOnlineDebugUnitTest` also works and redirects to the same suite.

### Instrumentation Tests (Device/Emulator)

```powershell
# Requires a running emulator or physical device
# Run instrumentation tests
./gradlew connectedAndroidTest

# Or target the current debug variant explicitly
./gradlew :app:connectedDebugAndroidTest
```

### Troubleshooting Test Failures

- **Missing audio files**: Check `app/src/main/res/raw/` for missing `.ogg` files. `SoundManager` logs missing keys.
- **SDK version mismatch**: Ensure `robolectric.properties` references SDK 34 and JDK 17 is active.
- **Gradle cache**: Clean with `./gradlew clean` if seeing stale build artifacts.

---

## Building the App

```powershell
# Build the shared debug APK
./gradlew :app:assembleDebug

# Install on an emulator or connected device
./gradlew :app:installDebug
```

> The repository does **not** currently define separate `online` and `offline` product flavors. Connected-services work is being staged through Track 2 documentation and scaffolding rather than through a distinct app variant.

---

## IDE Configuration (Android Studio)

### JDK Configuration

1. **File > Project Structure > SDK Location**
   - Verify **Android SDK location** points to your installed SDK (e.g., `C:\Users\YourName\AppData\Local\Android\sdk`)
   - Set **JDK location** to JDK 17 path

2. **File > Project Structure > Project**
   - **Gradle JDK**: Select "17" or navigate to your JDK 17 installation
   - **Compile SDK**: Select "Android 16 (API 36)"

### Gradle Configuration

1. **File > Settings > Build, Execution, Deployment > Gradle**
   - **Gradle JDK**: Select version 17
   - **Gradle home path**: Leave blank (uses embedded Gradle wrapper)

### Running App in Emulator

1. Create or start an Android emulator:
   - **Recommended**: API 34 (Android 14) for testing
   - Min spec: 2GB RAM, 4GB storage

2. Run from IDE:
   - Select **Run > Run 'app'** or press `Shift+F10`
   - Select emulator or device from launcher dialog
   - App starts with default save slot

---

## Gradle Tasks Reference

### Common Tasks

| Task | Purpose |
|------|---------|
| `./gradlew build` | Full build (compile + test + package) |
| `./gradlew test` | Run all unit tests |
| `./gradlew testDebugUnitTest` | Quick smoke test (shortcut) |
| `./gradlew clean` | Remove all build artifacts |
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew check` | Run lint + tests (CI-equivalent) |

### Current App Tasks

| Task | Purpose |
|------|---------|
| `./gradlew :app:assembleDebug` | Build the current debug APK |
| `./gradlew :app:installDebug` | Install the current debug APK on a device/emulator |
| `./gradlew :app:testDebugUnitTest` | Run the verified local Robolectric/JUnit smoke target |
| `./scripts/run_online_tests.ps1` | Windows helper for the same unit-test run and report collection |

---

## Troubleshooting

### Gradle Build Errors

**Error**: `'...init.gradle' does not exist`
- **Solution**: Delete `~/.gradle` folder and rebuild
- Command: `Remove-Item -Recurse $env:USERPROFILE\.gradle`

**Error**: JDK version mismatch (Java 11 vs 20)
- **Solution**: Set `JAVA_HOME`, update your user Gradle `org.gradle.java.home`, or use Android Studio's Gradle JDK selector
- Check: `java -version` should show JDK 17

**Error**: Android SDK not found
- **Solution**: Verify `local.properties` has correct SDK path
- Run: `Get-Content local.properties | grep sdk.dir`

### Test Failures

**Robolectric SDK mismatch**: Ensure JDK 17 is active in `JAVA_HOME`
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.x.x"
./gradlew testDebugUnitTest
```

**Missing sound assets**: Check console for `SoundManager` missing-key logs
- Verify files in `app/src/main/res/raw/`
- Add missing `.ogg` files or update `SoundManager.java` registrations

### Emulator Issues

**Emulator too slow**: Use Android 14 (API 34) and allocate 2-4GB RAM
**App crashes on launch**: Check `Logcat` output; search for `ClickDungeonApp` or `GameActivity`

---

## Next Steps

1. ✅ Complete setup by building and running tests
2. 📖 Read [ROADMAP.md](ROADMAP.md) to understand feature phases
3. 🔐 For Cloud Backend features, follow [FIREBASE_SETUP.md](FIREBASE_SETUP.md)
4. 📝 For code contributions, check [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) and test guidelines in [ROADMAP.md](ROADMAP.md#current-gaps--technical-debt)

---

## Support

- **Build issues**: Check `app/build.gradle.kts`, tracked `gradle.properties`, and user Gradle properties for version alignment
- **Test issues**: Verify JDK 17 in `JAVA_HOME` and check `robolectric.properties`
- **IDE issues**: Invalidate Android Studio cache: **File > Invalidate Caches**

For additional help, see [ROADMAP.md - Build/Test Friction](ROADMAP.md#current-gaps--technical-debt).
