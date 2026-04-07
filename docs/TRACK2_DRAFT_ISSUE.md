# Track 2 — Connected Services Provisioning (Draft Issue)

Summary
-------
This draft captures the concrete steps required to provision and validate ClickDungeon's connected services: Firebase (`google-services.json`, App Check) and Play Console access for purchase validation and sandbox testing. Use it as the opening comment when filing the Track 2 issue from the template.

Owners & Contacts
------------------
- Owner: @<OWNER_GH_HANDLE>  <!-- replace with actual GitHub handle -->
- Infra/Cloud: @<INFRA_HANDLE>
- Release/Play: @<RELEASE_HANDLE>

Required Artifacts
------------------
- `google-services.json` (Firebase config) — store in secrets manager or CI only
- Play Service Account JSON (for server-side purchase validation) — store securely
- App Check attestation config and provider notes
- Challenge-signing key material and retrieval instructions

Checklist
---------
- [ ] Confirm owner and required permissions
- [ ] Provision Firebase project and enable required APIs
- [ ] Export `google-services.json` and add to CI secrets (do not commit)
- [ ] Configure App Check and document providers
- [ ] Create Play Console service account and export key to CI secrets
- [ ] Document rollout-gate runbook and device matrix
- [ ] Execute device matrix tests (anonymous auth, leaderboard, cloud save, sandbox purchase)

Acceptance Criteria
-------------------
- Local debug builds succeed when `google-services.json` is provided locally
- Sandbox purchase flow validates against Play API using CI-provided service account
- Cloud save sync/restore verified on at least two devices
- Rollout results and logs attached to the issue

Security Notes
--------------
- Never commit `google-services.json`, Play service account keys, or private signing keys to git. Use the organization secrets manager or CI secrets.

Attachments
-----------
- Add device matrix, logs, and test reports as comments on the issue.
