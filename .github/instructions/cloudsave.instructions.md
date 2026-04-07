---
name: cloudsave-instructions
description: "When working on cloud save features, run the cloudsave-agent to scaffold mocks and tests."
applyTo: "app/src/online/**"
---

Steps:
1. Run the `cloudsave-agent` to scaffold `CloudSaveServiceMockTest`.
2. Add a `google-services.json` to `app/` for device runs (do not commit secrets).
3. Run `.\scripts\run_online_tests.ps1` to verify tests.
4. Update `docs/FIREBASE_SETUP.md` with any new steps.
