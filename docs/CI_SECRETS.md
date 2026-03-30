CI Secrets & local dev guidelines

This document explains how to store and inject `google-services.json` and Play service account keys into CI without committing secrets.

1) Encode secrets for GitHub Actions

- Linux / macOS:
  ```bash
  base64 -w0 google-services.json > google-services.json.b64
  base64 -w0 play-service-account.json > play-service-account.json.b64
  ```

- Windows (PowerShell):
  ```powershell
  $b = [Convert]::ToBase64String([IO.File]::ReadAllBytes('google-services.json'))
  $b | Out-File -Encoding ascii google-services.json.b64
  ```

2) Add secrets to GitHub (Organization/Repo secrets)

- `GOOGLE_SERVICES_JSON` = contents of `google-services.json.b64`
- `PLAY_SERVICE_ACCOUNT_JSON` = contents of `play-service-account.json.b64`

3) CI workflow placeholder

- See `.github/workflows/ci-secrets-placeholder.yml` for an example that decodes the secrets into `app/google-services.json` and `/tmp/play-service-account.json` during the workflow.

4) Local dev

- For local testing, place `google-services.json` in the `app/` folder (do not commit).
- For Play service account tests or server-side validation, keep keys in a secure vault and reference them in CI only.

Security note: never commit private keys or service account JSON to the repository.
