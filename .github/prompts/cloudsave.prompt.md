---
name: cloudsave-scaffold
description: "Prompt to generate CloudSave mock test scaffolds and a small README explaining gservices setup."
---

Create a JUnit test `CloudSaveServiceMockTest` under `app/src/online/java/com/example/clickdungeon/` that:
- Mocks a cloud save client with successful sync and restore paths
- Verifies that `SaveManager` calls the cloud client and reconciles blobs
- Includes a small `README.md` explaining where to place `google-services.json` for device tests

Return the file list and brief instructions for running the tests.
