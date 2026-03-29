# ClickDungeon Documentation Index

Complete reference guide for all ClickDungeon documentation. Use this index to quickly find the guide you need.

**Last Updated**: 2026-03-03 (Phase G Complete)  
**Current Version**: 0.06 | **Status**: Phase G Complete, Phase 9 Pending

---

## 📚 Getting Started (For New Developers)

Start here if you're new to ClickDungeon:

| Document | Purpose | Audience |
|----------|---------|----------|
| [README.md](../README.md) | Feature overview, version, and quick links | Everyone |
| [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) | Configure JDK 17, Gradle, Android Studio, run tests | Developers |
| [FIREBASE_SETUP.md](FIREBASE_SETUP.md) | Setup Firebase for online flavor | Backend developers |
| [ROADMAP.md](ROADMAP.md) | Detailed feature phases, gaps, technical debt | Project managers, Leads |

---

## 🎮 Architecture & Design Guides

Reference guides for implementing features:

| Document | Purpose | Use When... |
|----------|---------|-----------|
| [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) | Sprite sheets, animation states, frame timing | Adding animations, implementing HIT/DEFEAT/CAST/LOOT/LEVEL_UP states |
| [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md) | Import and validate sprites, audio, icons, terrains | Adding new art assets or sounds |
| [ASSET_PIPELINE.md](ASSET_PIPELINE.md) | Asset delivery workflow and asset gaps | Planning art/audio deliverables |
| [SECURITY.md](../SECURITY.md) | Encryption, persistence, and data validation | Working with user data or saves |
| [TELEMETRY_EVENTS.md](TELEMETRY_EVENTS.md) | Event tracking schema and analytics | Adding new gameplay events or analyzing telemetry |

---

## 📋 Project Status & Planning

Phase tracking, roadmap, and release notes:

| Document | Purpose | Content |
|----------|---------|---------|
| [PHASE_STATUS_2026-03-01.md](PHASE_STATUS_2026-03-01.md) | Latest phase completion status | Phase G complete, Phase 9 in progress |
| [PHASE_STATUS_2026-02-27.md](PHASE_STATUS_2026-02-27.md) | Previous phase status for reference | Historical comparison |
| [PHASE_COMPARISON_2026-02-27.md](PHASE_COMPARISON_2026-02-27.md) | Comparison across phases | Tracking feature additions |
| [RELEASE_NOTES_2026-02-24.md](RELEASE_NOTES_2026-02-24.md) | v0.06 release notes | What's new in current version |
| [ROADMAP.md](ROADMAP.md) | Detailed feature roadmap and technical debt | Overall project planning |

---

## 🧪 Testing & Quality Assurance

QA procedures and test coverage:

| Document | Purpose | Use When... |
|----------|---------|-----------|
| [DEVICE_MATRIX_RUNBOOK.md](DEVICE_MATRIX_RUNBOOK.md) | Device testing matrix and execution steps | Running QA validation across devices |
| [device_matrix_log.csv](device_matrix_log.csv) | Device test results log | Tracking historical test runs |
| [TABLET_REGRESSION_CHECKLIST.md](TABLET_REGRESSION_CHECKLIST.md) | Tablet-specific regression tests | Testing tablet layouts and responsive UI |
| [LOCALIZATION_QA_CHECKLIST.md](LOCALIZATION_QA_CHECKLIST.md) | Localization validation (Spanish locale) | Testing i18n support |
| [TEST_GAP_BACKLOG.md](TEST_GAP_BACKLOG.md) | Remaining test coverage gaps | Identifying test priorities |

---

## 🚀 Deployment & Operations

Release, rollout, and operational guides:

| Document | Purpose | Content |
|----------|---------|---------|
| [BACKEND_ARCHITECTURE.md](BACKEND_ARCHITECTURE.md) | Backend services, Firebase setup, endpoints | Cloud infrastructure and API design |
| [ROLLOUT_GATES.md](ROLLOUT_GATES.md) | Rollout progression and metrics gates | Production release phases |
| [INCIDENT_PLAYBOOK.md](INCIDENT_PLAYBOOK.md) | Incident response and escalation procedures | What to do when things break |
| [PERFORMANCE_PLAN.md](PERFORMANCE_PLAN.md) | Performance monitoring and optimization strategy | Memory, CPU, frame rate targets |
| [REMOTE_CONFIG_FALLBACK_MATRIX.md](REMOTE_CONFIG_FALLBACK_MATRIX.md) | Feature flag fallback values and rules | Remote Config configuration |

---

## 📦 Archived Documentation

Historical or deprecated documents:

| Document | Status | Note |
|----------|--------|------|
| [archives/BUGS_V0.2_OBSOLETE.md](archives/BUGS_V0.2_OBSOLETE.md) | ❌ Obsolete | v0.02 bug list (see ROADMAP for current gaps) |

---

## 🔍 Quick Reference by Topic

### For "How do I...?" Questions

**Add a new class/animation state?**
→ [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) + [ROADMAP.md - Current Gaps](ROADMAP.md#current-gaps--technical-debt)

**Import new art assets?**
→ [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md)

**Add a new telemetry event?**
→ [TELEMETRY_EVENTS.md](TELEMETRY_EVENTS.md) + code TODO comments

**Setup Firebase/backend?**
→ [FIREBASE_SETUP.md](FIREBASE_SETUP.md) + [BACKEND_ARCHITECTURE.md](BACKEND_ARCHITECTURE.md)

**Setup local development environment?**
→ [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md)

**Understand encryption and data safety?**
→ [SECURITY.md](../SECURITY.md)

**Plan a release?**
→ [ROLLOUT_GATES.md](ROLLOUT_GATES.md) + [ROADMAP.md](ROADMAP.md)

---

## 📊 Documentation Status

| Area | Status | Notes |
|------|--------|-------|
| **Getting Started** | ✅ Complete | DEVELOPER_SETUP.md, FIREBASE_SETUP.md added |
| **Architecture** | ✅ Complete | ANIMATION_ARCHITECTURE.md, ASSET_IMPORT_GUIDE.md added, TELEMETRY_EVENTS.md added |
| **Phase Tracking** | ✅ Current | Phase G completed, Phase comparisons available |
| **QA & Testing** | ✅ In Progress | Checklists created, execution results pending |
| **Deployment** | ✅ Available | Rollout gates, incident playbook, performance plan defined |
| **Code TODO Comments** | ✅ In Progress | Key gaps marked in AnimatedPlayer.java, AnimationState.java, etc. |

---

## 🎯 Implementation Gaps (Marked with TODO)

See [ROADMAP.md - Current Gaps](ROADMAP.md#current-gaps--technical-debt) for full details.

**Critical items:**
- [ ] Extended animation states (HIT, DEFEAT, CAST, LOOT, LEVEL_UP) — See [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) for spec
- [ ] Ranger sprite assets (icon + sheet) — Pending external delivery; see [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md) for import steps
- [ ] Cloud Functions endpoint hardening — Scaffolding in place, integration pending
- [ ] Firebase google-services.json setup — Instructions in [FIREBASE_SETUP.md](FIREBASE_SETUP.md)

**High Priority:**
- [ ] JDK version alignment (gradle.properties)
- [ ] Device matrix QA execution
- [ ] Locale QA validation (Spanish)

---

## 🔗 Cross-References

### By Code File

**AnimatedPlayer.java** → [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) + [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md)  
**SoundManager.java** → [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md#2-icon-assets-ui-elements) + [TELEMETRY_EVENTS.md](TELEMETRY_EVENTS.md)  
**SaveManager.java** → [SECURITY.md](../SECURITY.md) + [ROADMAP.md](ROADMAP.md#current-gaps--technical-debt)  
**GameActivity.java** → [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) + [ROADMAP.md](ROADMAP.md)  
**CombatDialogFragment.java** → [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) + [TELEMETRY_EVENTS.md](TELEMETRY_EVENTS.md)  

### By Feature

**Authentication** → [FIREBASE_SETUP.md](FIREBASE_SETUP.md) + [BACKEND_ARCHITECTURE.md](BACKEND_ARCHITECTURE.md)  
**Cloud Sync** → [FIREBASE_SETUP.md](FIREBASE_SETUP.md) + [SECURITY.md](../SECURITY.md)  
**Achievements** → [README.md](../README.md) + [ROADMAP.md](ROADMAP.md)  
**Shop/Inventory** → [ROADMAP.md - Inventory UX](ROADMAP.md#current-gaps--technical-debt)  
**Abilities** → [README.md - Class Selection & Abilities](../README.md#key-features) + [ROADMAP.md](ROADMAP.md)  

---

## 📞 Support & Feedback

- **Build issues**: See [DEVELOPER_SETUP.md — Troubleshooting](DEVELOPER_SETUP.md#troubleshooting)
- **Firebase questions**: See [FIREBASE_SETUP.md](FIREBASE_SETUP.md)
- **Animation/sprite issues**: See [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) + [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md)
- **Test failures**: See [TEST_GAP_BACKLOG.md](TEST_GAP_BACKLOG.md) + [ROADMAP.md - Testing Gaps](ROADMAP.md#current-gaps--technical-debt)
- **Design/performance**: See [PERFORMANCE_PLAN.md](PERFORMANCE_PLAN.md) + code TODO comments

---

## 📝 Documentation Conventions

- **TODO comments in code** link to specific documentation sections
- **Phase Status** files track completion of feature phases (A through G complete, Phase 9 pending)
- **Gaps & Technical Debt** are documented in [ROADMAP.md](ROADMAP.md) with links to implementation guides
- **Archived documentation** moved to `docs/archives/` retains links for historical reference

---

## 🔄 Version History

| Version | Date | Phase | Key Notes |
|---------|------|-------|-----------|
| 0.06 | 2026-02-24 | Phase F-G | Current release; Phase G docs completed 2026-03-03 |
| 0.05 | 2026-02-XX | Phase F | Animation/FX polish phase |
| 0.04 | 2026-01-XX | Phase E | Security/encryption phase |
| ... | ... | ... | See RELEASE_NOTES_2026-02-24.md |
| 0.02 | Early 2025 | Alpha | Initial development (obsolete; see archives) |

---

## 📋 Checklist for New Developers

Use this checklist when onboarding:

- [ ] Read [README.md](../README.md) for feature overview
- [ ] Follow [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) to setup local environment
- [ ] Run `./gradlew testDebugUnitTest` to verify build works
- [ ] Read [ROADMAP.md](ROADMAP.md) to understand phases and gaps
- [ ] Review [ANIMATION_ARCHITECTURE.md](ANIMATION_ARCHITECTURE.md) if working on animations
- [ ] Review [ASSET_IMPORT_GUIDE.md](ASSET_IMPORT_GUIDE.md) if working on assets
- [ ] Check code TODO comments for implementation gaps
- [ ] Review [SECURITY.md](../SECURITY.md) if working with user data
- [ ] Review [TELEMETRY_EVENTS.md](TELEMETRY_EVENTS.md) if adding new gameplay events

---

**Need help?** Start with [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) or [ROADMAP.md](ROADMAP.md) depending on your role.

