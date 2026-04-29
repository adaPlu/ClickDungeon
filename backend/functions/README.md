# ClickDungeon Backend Functions

Backend Functions are provisioned for Node 20. From the repo root, install
dependencies before deploying:

```bash
cd backend/functions
npm install
```

For dependency verification, run:

```bash
npm audit --omit=dev
```

## Known Audit State

As of the current backend dependency review, `npm audit --omit=dev` reports 11
production advisories through Firebase Admin / Google Cloud transitive
dependencies: 2 low and 9 moderate. The affected packages are:

- `@google-cloud/firestore`
- `@google-cloud/storage`
- `@tootallnate/once`
- `firebase-admin`
- `firebase-functions`
- `gaxios`
- `google-gax`
- `http-proxy-agent`
- `retry-request`
- `teeny-request`
- `uuid`

Do not run `npm audit fix --force`. npm currently proposes a breaking downgrade
to `firebase-admin@10.1.0` / `firebase-functions@4.9.0`, which is not an
acceptable fix for this backend. The audit-clean override path also requires
major transitive versions outside Firebase Admin's published dependency ranges,
so it is not deploy-ready without Firebase validation.

During Firebase provisioning, accept this audit state only as a documented
temporary exception. Reevaluate before deployment by checking for a Firebase
Admin release that updates its Firestore and Storage dependency ranges to
patched Google Cloud packages, then rerun `npm install --package-lock-only` and
`npm audit --omit=dev`.
