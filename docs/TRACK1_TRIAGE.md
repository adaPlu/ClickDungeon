# Track 1 - Test Harness Stabilization: Triage Checklist

Purpose: provide a repeatable triage flow for the online test backlog and a
small set of commands for collecting failures and diagnostics.

1. Verify test wiring
   - Ensure `app/src/online/java` exists and contains online-only tests.
   - Confirm sample test:
     `app/src/online/java/com/adaplu/clickdungeon/OnlineSampleTest.java`.

2. Run unit tests and capture results
   - Windows PowerShell:
     ```powershell
     .\gradlew.bat :app:testDebugUnitTest --no-daemon | Tee-Object test-run.log
     ```
   - Linux/macOS:
     ```bash
     ./gradlew :app:testDebugUnitTest --no-daemon | tee test-run.log
     ```

3. Collect failing tests and stack traces
   - Test reports: `app/build/test-results/testDebugUnitTest/` and
     `app/build/reports/tests/testDebugUnitTest/`
   - For failed tests, open the matching XML file and capture the stack trace

4. Classify failure clusters
   - Shared Robolectric/setup
   - Adapter/view binding nullability
   - Combat formatting
   - Gameplay/activity state

5. Fix and re-run
   - Confirm `testOptions.unitTests.isIncludeAndroidResources = true`
   - Confirm Robolectric is pinned in `app/build.gradle.kts`
   - Isolate view-binding failures only if the harness is otherwise blocked

6. Promote tests
   - Move verified online tests into `app/src/online/java`
   - Remove scaffold-only placeholders or add TODO references

## Exit Criteria
- `:app:testDebugUnitTest` passes locally, or failures are classified with an
  owner
- Focused online backend tests execute under Gradle and are moved into
  `app/src/online`

## Notes
- `app/src/online` is compiled as part of the standard `test` source set
- `:app:testOnlineDebugUnitTest` remains available as a legacy alias

## Recent Additions
- `app/src/online/java/com/adaplu/clickdungeon/CloudSaveServiceMockTest.java`
- `app/src/online/java/com/adaplu/clickdungeon/LeaderboardServiceMockTest.java`
