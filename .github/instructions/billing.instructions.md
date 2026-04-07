---
name: billing-instructions
description: "When adding billing tests, prefer test doubles and keep real credentials out of the repo."
applyTo: "app/**"
---

Steps:
1. Use `billing-agent` to scaffold `BillingClientTestDouble` and expanded `BillingManagerTest`.
2. Avoid real Play credentials; use mocked `BillingClient` behavior for unit tests.
3. Run `./gradlew :app:testDebugUnitTest` to validate.
4. Document any test doubles in `docs/TEST_GAP_BACKLOG.md`.
