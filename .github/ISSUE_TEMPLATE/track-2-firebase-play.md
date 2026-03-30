---
name: "Track 2 — Firebase & Play Console provisioning"
about: "Provision Firebase, add `google-services.json`, configure App Check, and grant Play Console/API access for purchase validation."
title: "Track 2: Provision Firebase & Play Console (Connected Services)"
labels: "infra,track-2,blocking"
assignees: ""
---

## Summary

Provision the external connected-services artifacts required for Track 2 rollout validation and verify integration with the app. This issue captures the required artifacts, steps, owners, and acceptance criteria.

## Owner
- Owner: @<OWNER_GH_HANDLE>  <!-- replace with actual GitHub handle -->

## Required artifacts
- `app/google-services.json` (Firebase config file)
- App Check configuration and site key / token verification notes
- Play Console API access (service account or owner with permission to manage testing APIs)
- Challenge-signing key material (private key or secure location + instructions)

## Tasks
- [ ] Confirm owner and escalate permissions request to infra/ops
- [ ] Add `google-services.json` to `app/` (do NOT commit secrets to Git; see notes)
- [ ] Configure App Check and document accepted attestation providers and tokens
- [ ] Grant Play Console API access for purchase validation (create service account and JSON key)
- [ ] Add challenge-signing key to secure vault (document retrieval path for CI)
- [ ] Run rollout-gate checklist on a device matrix (see Validation section)
- [ ] Document all steps performed and add secure storage locations to team secrets doc

## Validation / Acceptance criteria
- [ ] `app/google-services.json` is present in the CI/CD secret store or `app/` (per repo policy) and local builds succeed (`./gradlew assembleDebug`)
- [ ] Sandbox purchases validate with the Play API (sandbox flow exercised)
- [ ] Cloud save sync & restore tested with at least 2 device profiles
- [ ] Leaderboard submit/read and friends send/accept flows tested and pass basic end-to-end checks
- [ ] Rollout results captured in the device matrix and attached to this issue

## How to run basic checks locally
1. Place `google-services.json` in `app/` (local dev only). Do not commit it.
2. Build debug to ensure config is accepted:

```bash
./gradlew :app:assembleDebug
```

3. For App Check and cloud flows, run device validation flows per `docs/NEXT_PHASE_PLAN.md` Track 2 runbook.

## Security notes
- Never commit `google-services.json`, Play service account keys, or challenge-signing private keys to source control. Store them in the organization's secrets manager and document retrieval steps here.

## Attachments
- Link rollout logs, device matrix, and test reports as comments on this issue.
