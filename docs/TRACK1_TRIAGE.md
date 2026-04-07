# Track 1 — Test Harness Stabilization: Triage Checklist

Purpose: provide a repeatable triage flow for the online test backlog and a small set of commands for collecting failures and diagnostics.

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

5) Fix and re-run
   - For classpath or Robolectric issues: confirm `testOptions.unitTests.isIncludeAndroidResources = true` and that the Robolectric SDK is pinned in `app/build.gradle.kts`.
   - For view-binding issues: add lightweight Robolectric-compatible mocks or temporarily isolate the affected tests until the harness is stable.

6) Promote tests
   - Move verified online tests into `app/src/online/java` (this repo now compiles that directory automatically).
   - Remove scaffold-only placeholders or add TODOs referencing the corresponding issue.

Exit criteria
   - `:app:testDebugUnitTest` passes locally (green) or failing tests are categorized as product bugs with owner assigned.
   - Focused online backend tests execute under Gradle and are moved into `app/src/online`.

Notes
   - The repo currently compiles `app/src/online` as part of the standard `test` source set, so `:app:testDebugUnitTest` runs them together.
   - For backward compatibility, `:app:testOnlineDebugUnitTest` is available as an alias to the same task.
