# Next Phase Plan

Last updated: 2026-04-18

## Objective
Move ClickDungeon from its current hardening state to a release-ready state for connected services and late-game UX. The next phase is organized as a multi-track stabilization effort.

## Track 1 - Test Harness Stabilization
- **Status update (2026-03-30):** `app/src/online/java` is now wired into the unit-test source set, `OnlineSampleTest` is present, `scripts/run_online_tests.ps1` was added, and `:app:testDebugUnitTest` passes locally.
- Triage the existing online backlog by failure cluster:
  - shared Robolectric environment/setup failures
  - adapter/view binding nullability failures
  - combat formatting failures
  - gameplay/activity state regressions
- Promote the focused online backend tests from scaffold-only files into `app/src/online/java` once each dependency path is verified.
- Exit criteria:
  - ✅ `:app:testDebugUnitTest` passes (verified 2026-03-30)
  - ⏳ targeted online backend tests execute under Gradle
  - ⏳ the remaining red tests are product bugs, not harness-wide failures

## Track 2 - Connected Services Rollout Validation
- **Status update (2026-03-30):** in-repo prep has started: Track 2 issue templates/drafts are present, `docs/CI_SECRETS.md` is added, a CI secrets placeholder workflow exists, and `BillingManager` scaffolding is in place.
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
- **Status update (2026-04-18):** Ranger class enablement is now live in the app with gameplay support. The current icon/sprite fallback is acceptable for launch; dedicated Ranger art is a post-launch polish item.
- Finish the documented but still-open UX/content items:
  - extended animation states
  - Ranger icon + sprite-sheet replacement
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
