# Track 1 — Test Harness Stabilization: Triage Checklist

Purpose: provide a reproducible triage flow for the online test backlog and a set of commands to collect test failures and diagnostics.

1) Verify test wiring
   - Ensure `app/src/online/java` exists and contains online-only tests.
   - Confirm sample test: `app/src/online/java/com/example/clickdungeon/OnlineSampleTest.java`.

2) Run unit tests and capture results
   - Windows PowerShell:
     ```powershell
     .\gradlew.bat :app:testDebugUnitTest --no-daemon | Tee-Object test-run.log
     ```
   - Linux/macOS:
     ```bash
     ./gradlew :app:testDebugUnitTest --no-daemon | tee test-run.log
     ```

3) Collect failing tests and stack traces
   - Test reports: `app/build/test-results/testDebugUnitTest/` (xml) and `app/build/reports/tests/testDebugUnitTest/` (HTML)
   - For failed tests, open the matching XML under `app/build/test-results/testDebugUnitTest/` and capture the stacktrace element.

4) Classify failure clusters
   - Shared Robolectric/setup: failures that show Robolectric errors, missing resources, or classpath exceptions.
   - Adapter/view binding nullability: NPEs originating from layout inflation or `findViewById`/ViewBinding usage.
   - Combat formatting: assertion/formatting differences in combat logs.
   - Gameplay/activity state: lifecycle/state mismatch failures.

5) Fix & re-run
   - For classpath/Robolectric issues: confirm `testOptions.unitTests.isIncludeAndroidResources = true` and Robolectric SDK pinned in `app/build.gradle.kts`.
   - For view-binding issues: add lightweight Robolectric-compatible mocks or move problematic online-only tests behind an integration tag until harness is stable.

6) Promote tests
   - Move verified online tests into `app/src/online/java` (this repo now compiles that directory automatically).
   - Remove scaffold-only placeholders or add TODOs referencing the corresponding issue.

Exit criteria
   - `:app:testDebugUnitTest` passes locally (green) or failing tests are categorized as product bugs with owner assigned.
   - Focused online backend tests execute under Gradle and are moved into `app/src/online`.

Notes
   - If you want a dedicated Gradle task for online tests only, add a custom task that filters by package or test pattern. This repo currently compiles `app/src/online` as part of `test` source set so `:app:testDebugUnitTest` runs them together.
