# ClickDungeon Documentation Index

Reference guide to the project documentation currently maintained in this repository.

**Last Updated**: 2026-04-26
**Current Version**: 1.0.0 | **Status**: launch hardening; Track 1 green; Gate 4 screenshots/hosting, Play Console, and Firebase provisioning pending

---

## Start Here

| Document | Purpose |
|----------|---------|
| [README.md](../README.md) | Project overview, features, and current status |
| [LAUNCH_PLAN.md](LAUNCH_PLAN.md) | Gate-by-gate v1 launch status and next steps |
| [GATE4_CHECKLIST.md](GATE4_CHECKLIST.md) | Store asset/legal checklist and remaining external blockers |
| [ROADMAP.md](ROADMAP.md) | Verified feature coverage, gaps, and dependency-ordered phases |
| [NEXT_PHASE_PLAN.md](NEXT_PHASE_PLAN.md) | Current execution order across Track 1-4 work |
| [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) | Local environment setup, build, and test commands |

---

## Architecture & Systems

| Document | Purpose |
|----------|---------|
| [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) | Sprite-sheet and animation-state notes |
| [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md) | Import workflow for art/audio assets |
| [PERFORMANCE_PLAN.md](PERFORMANCE_PLAN.md) | Performance targets and optimization plan |
| [TELEMETRY_EVENTS.md](TELEMETRY_EVENTS.md) | Event naming and telemetry schema |
| [SECURITY.md](../SECURITY.md) | Save security, encryption, and integrity notes |

---

## Testing & QA

| Document | Purpose |
|----------|---------|
| [TEST_GAP_BACKLOG.md](TEST_GAP_BACKLOG.md) | Remaining automated-test gaps |
| [TRACK1_TRIAGE.md](TRACK1_TRIAGE.md) | Track 1 harness triage checklist and commands |
| [`scripts/run_online_tests.ps1`](../scripts/run_online_tests.ps1) | PowerShell helper to run `:app:testDebugUnitTest` and collect reports |

> `app/src/online/java` is currently compiled into the standard unit-test source set, so online smoke tests run through `:app:testDebugUnitTest`.

---

## Connected Services & Release Prep

| Document | Purpose |
|----------|---------|
| [FIREBASE_SETUP.md](FIREBASE_SETUP.md) | Current Firebase / Play provisioning steps for Track 2 |
| [CI_SECRETS.md](CI_SECRETS.md) | Secure handling of `google-services.json` and Play service-account keys in CI |
| [`track-2-firebase-play.md`](../.github/ISSUE_TEMPLATE/track-2-firebase-play.md) | Actionable GitHub issue template for Track 2 rollout work |

---

## Archived Docs

| Document | Status | Note |
|----------|--------|------|
| [archives/BUGS_V0.2_OBSOLETE.md](archives/BUGS_V0.2_OBSOLETE.md) | Archived | Historical v0.2 bug list |

---

## Documentation Status

- This index references only documentation files that are present in the repository.
- The primary planning docs are `README.md`, `LAUNCH_PLAN.md`, `ROADMAP.md`, and `NEXT_PHASE_PLAN.md`.
- Track 1 is green as of 2026-04-26: debug APK builds, `:app:testDebugUnitTest` passes, and `:app:lintDebug` passes.
- Gate 4 is blocked only by external phone screenshots in `docs/screenshots/output/`, hosted privacy URL, and Play listing URL entry.
- Gate 5 and Gate 6 are now primarily external Console/Firebase provisioning work; in-repo scaffolding and docs are present.

When adding a new documentation file, update this index at the same time so links remain accurate.
