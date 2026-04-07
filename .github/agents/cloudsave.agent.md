---
name: cloudsave-agent
description: "Agent to scaffold and test cloud save sync/restore flows for ClickDungeon. Use when adding or testing cloud save logic, mock harnesses, or device-matrix validations."
applyTo: "app/src/online/**"
---

This agent helps create test scaffolds, mock services, and run-local test harness code for cloud save flows.

Usage examples:
- Create `CloudSaveServiceMockTest` under `app/src/online/java` that mocks sync/restore API calls.
- Produce a short README with instructions to provision `google-services.json` for local testing.

Invoke via the `runSubagent` tool with `agentName: "cloudsave-agent"` to perform multi-step operations.
