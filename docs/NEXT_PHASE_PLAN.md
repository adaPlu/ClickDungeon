# Next Phase Plan

Last updated: 2026-03-14

## Objective
Move ClickDungeon from "Phase 9 hardening is coded" to "connected services and late-game UX are release-ready." The next phase is a multi-track stabilization effort.

## Track 1 - Test Harness Stabilization
- Fix Android online unit-test wiring so online-only tests can compile against `app/src/online/` and run under `:app:testOnlineDebugUnitTest`.
- Triage the existing online backlog by failure cluster:
  - shared Robolectric environment/setup failures
  - adapter/view binding nullability failures
  - combat formatting failures
  - gameplay/activity state regressions
- Promote the focused online backend tests from scaffold-only files into the executing Gradle source set once the classpath issue is resolved.
- Exit criteria:
  - `:app:testDebugUnitTest` passes
  - targeted online backend tests execute under Gradle
  - the remaining red tests are product bugs, not harness-wide failures

## Track 2 - Connected Services Rollout Validation
- Provision the external dependencies that are still out of repo scope:
  - Firebase project setup
  - `google-services.json`
  - App Check configuration
  - Play Console API access for purchase validation
  - challenge-signing key material
- Execute the rollout-gate runbook with real devices:
  - anonymous auth
  - leaderboard submit/read
  - friends send/accept/remove
  - cloud save sync and restore
  - signed challenge fetch and single-claim acceptance
  - sandbox purchase validation
- Capture results in the device matrix and rollout docs.

## Track 3 - UX Polish and Content Completion
- Finish the documented but still-open UX/content items:
  - extended animation states
  - Ranger asset delivery/import
  - localization QA pass
  - tablet/readability cleanup
  - audio cue coverage follow-up
- Keep this work out of unstable debug lanes unless a bug requires coordination.

## Track 4 - Release Readiness and Operations
- Turn the current hardening state into a release checklist:
  - build/test matrix
  - backend deploy/test order
  - incident rollback drill
  - owner list for Firebase/Play Console credentials
- Sync README, roadmap, backend docs, and QA docs after each gate.

## Recommended Order
1. Track 1 first, because the online test harness is the biggest verification blind spot.
2. Start Track 2 provisioning in parallel once owners and secrets are available.
3. Pull Track 3 after Track 1 removes the broad harness uncertainty.
4. Close with Track 4 once validation evidence exists.
