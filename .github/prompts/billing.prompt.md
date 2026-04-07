---
name: billing-tests
description: "Prompt to generate BillingManager test doubles and expanded unit tests."
---

Generate a `BillingClientTestDouble` and a set of JUnit tests that:
- Simulate successful purchase flow and acknowledgment
- Simulate user-cancelled and failure flows
- Verify `BillingManager` calls `acknowledgePurchase` for unacknowledged purchases

Place generated files under `app/src/test/java/com/example/clickdungeon/util/` and include a README with run instructions.
