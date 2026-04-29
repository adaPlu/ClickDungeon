# Next Phase Plan

Last updated: 2026-04-26

## Objective
Move ClickDungeon from its current hardening state to a release-ready state for store submission, connected-services provisioning, and late-game UX polish. The next phase is organized as a multi-track stabilization effort.

## Track 1 - Test Harness Stabilization
- **Status update (2026-04-26):** the current local verification lane is green: debug APK builds, `:app:testDebugUnitTest` passes, and `:app:lintDebug` passes. `app/src/online/java` remains wired into the standard unit-test source set for smoke coverage.
- Keep the existing unit suite green while adding any follow-up coverage for late launch fixes.
- Promote focused online backend tests only after the external Firebase/Play dependencies they exercise are provisioned.
- Exit criteria:
  - [x] Debug APK builds (verified 2026-04-26)
  - [x] `:app:testDebugUnitTest` passes (verified 2026-04-26)
  - [x] `:app:lintDebug` passes (verified 2026-04-26)
  - [ ] Targeted connected-services tests run after external provisioning is available

## Track 2 - Connected Services Rollout Validation
- **Status update (2026-04-19):** in-repo prep is present, signing secrets and local JDK paths have been removed from tracked `gradle.properties`, and secret-bearing files are expected to stay local or in CI/console configuration.
- **Security audit note (2026-04-26):** `npm audit fix` was applied to the backend functions lockfile and restored the missing `backend/functions/package.json`. The remaining backend audit findings are transitive Firebase Admin / Google Cloud dependency advisories where npm only offers `npm audit fix --force`, which would install an older breaking `firebase-admin@10.1.0`; resolve during Firebase provisioning with a tested dependency upgrade path.
- In-repo pieces already present:
  - Track 2 issue templates/drafts
  - `docs/CI_SECRETS.md`
  - CI secrets placeholder workflow
  - `BillingManager` scaffolding
- Provision the external dependencies that are out of repo scope:
  - Firebase project setup
  - local-only `app/google-services.json`
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
- **Status update (2026-04-19):** Ranger class enablement is live with gameplay support. English-only is the adopted v1 locale policy; translation folders/workflow are post-launch work. The current Ranger icon/sprite fallback is acceptable for launch; dedicated Ranger art is a post-launch polish item.
- Finish the documented but still-open UX/content items:
  - extended animation states
  - Ranger icon + sprite-sheet replacement
  - post-launch localization workflow
  - tablet/readability cleanup
  - audio cue coverage follow-up
- Keep this work out of unstable debug lanes unless a bug requires coordination.

## Track 4 - Release Readiness and Operations
- Turn the current hardening state into a release checklist that separates in-repo evidence from external-console work:
  - build/test matrix, including debug APK, unit, and lint verification
  - backend deploy/test order
  - incident rollback drill
  - owner list for Firebase/Play Console credentials
- Sync README, roadmap, backend docs, and QA docs after each gate.

## Recommended Order
1. Keep Track 1 green while release-facing docs/assets are finalized.
2. Finish Gate 4 external assets and hosting, because this is the only current store-listing blocker.
3. Start Track 2 / Gate 5 / Gate 6 external provisioning once console owners and secrets are available.
4. Pull Track 3 polish only when it does not destabilize the green verification lane.
5. Close with Track 4 once console validation evidence exists.
