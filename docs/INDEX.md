# ClickDungeon Documentation Index

Reference guide to the project documentation currently maintained in this repository.

**Last Updated**: 2026-03-30  
**Current Version**: 0.06 | **Status**: late-phase hardening and Track 2 connected-services preparation in progress

---

## 📚 Start Here

| Document | Purpose |
|----------|---------|
| [README.md](../README.md) | Project overview, features, and current status |
| [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) | Local environment setup, build, and test commands |
| [ROADMAP.md](ROADMAP.md) | Verified feature coverage, gaps, and dependency-ordered phases |
| [NEXT_PHASE_PLAN.md](NEXT_PHASE_PLAN.md) | Current execution order across Track 1-4 work |

---

## 🎮 Architecture & Systems

| Document | Purpose |
|----------|---------|
| [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) | Sprite-sheet and animation-state notes |
| [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md) | Import workflow for art/audio assets |
| [PERFORMANCE_PLAN.md](PERFORMANCE_PLAN.md) | Performance targets and optimization plan |
| [TELEMETRY_EVENTS.md](TELEMETRY_EVENTS.md) | Event naming and telemetry schema |
| [SECURITY.md](../SECURITY.md) | Save security, encryption, and integrity notes |

---

## 🧪 Testing & QA

| Document | Purpose |
|----------|---------|
| [TEST_GAP_BACKLOG.md](TEST_GAP_BACKLOG.md) | Remaining automated-test gaps |
| [TRACK1_TRIAGE.md](TRACK1_TRIAGE.md) | Track 1 harness triage checklist and commands |
| [`scripts/run_online_tests.ps1`](../scripts/run_online_tests.ps1) | PowerShell helper to run `:app:testDebugUnitTest` and collect reports |

> `app/src/online/java` is currently compiled into the standard unit-test source set, so online smoke tests run through `:app:testDebugUnitTest`.

---

## ☁️ Connected Services & Release Prep

| Document | Purpose |
|----------|---------|
| [FIREBASE_SETUP.md](FIREBASE_SETUP.md) | Current Firebase / Play provisioning steps for Track 2 |
| [CI_SECRETS.md](CI_SECRETS.md) | Secure handling of `google-services.json` and Play service-account keys in CI |
| [`track-2-firebase-play.md`](../.github/ISSUE_TEMPLATE/track-2-firebase-play.md) | Actionable GitHub issue template for Track 2 rollout work |

---

## 📦 Archived Docs

| Document | Status | Note |
|----------|--------|------|
| [archives/BUGS_V0.2_OBSOLETE.md](archives/BUGS_V0.2_OBSOLETE.md) | Archived | Historical v0.2 bug list |

---

## 📌 Documentation Status

- This index now references only documentation files that are present in the repository.
- The primary planning docs are `README.md`, `ROADMAP.md`, and `NEXT_PHASE_PLAN.md`.
- Track 1 support docs (`TRACK1_TRIAGE.md`, `run_online_tests.ps1`) and Track 2 support docs (`FIREBASE_SETUP.md`, `CI_SECRETS.md`) are current as of 2026-03-30.

When adding a new documentation file, update this index at the same time so links remain accurate.

