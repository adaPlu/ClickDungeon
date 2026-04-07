---
name: billing-agent
description: "Agent to expand and scaffold BillingManager unit tests and billing-related mocks. Use when adding tests that mock `BillingClient` behaviors or verifying purchase reconciliation logic."
applyTo: "app/**"
---

This agent helps generate unit tests, mock wrappers for `BillingClient`, and example test doubles to validate purchase handling without requiring real Play services.

Typical outputs:
- `BillingClientTestDouble` under `app/src/test/java` that simulates responses
- Expanded `BillingManagerTest` methods with mocked billing results
- Test README describing how to run the tests locally

Invoke via `runSubagent` with `agentName: "billing-agent"`.
