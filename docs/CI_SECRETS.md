# CI Secrets and Local Development Guidelines

This document explains how to store and inject Firebase config, Play service-account keys, and release-signing material in CI without committing secrets.

1) Encode secrets for GitHub Actions

- Linux / macOS:
  ```bash
  base64 -w0 google-services.json > google-services.json.b64
  base64 -w0 play-service-account.json > play-service-account.json.b64
  base64 -w0 clickdungeon-upload.jks > clickdungeon-upload.jks.b64
  ```

- Windows (PowerShell):
  ```powershell
  $b = [Convert]::ToBase64String([IO.File]::ReadAllBytes('google-services.json'))
  $b | Out-File -Encoding ascii google-services.json.b64
  $k = [Convert]::ToBase64String([IO.File]::ReadAllBytes('clickdungeon-upload.jks'))
  $k | Out-File -Encoding ascii clickdungeon-upload.jks.b64
  ```

2) Add secrets to GitHub (Organization/Repo secrets)

- `GOOGLE_SERVICES_JSON` = contents of `google-services.json.b64`
- `PLAY_SERVICE_ACCOUNT_JSON` = contents of `play-service-account.json.b64`
- `CLICKDUNGEON_KEYSTORE_BASE64` = contents of `clickdungeon-upload.jks.b64`
- `CLICKDUNGEON_STORE_PASSWORD` = keystore password
- `CLICKDUNGEON_KEY_ALIAS` = release key alias
- `CLICKDUNGEON_KEY_PASSWORD` = release key password

3) CI workflow placeholder

- See `.github/workflows/ci-secrets-placeholder.yml` for an example that decodes the secrets into `app/google-services.json`, `/tmp/play-service-account.json`, and a temporary keystore path during the workflow.

4) Local dev

- For local testing, place `google-services.json` in the `app/` folder (do not commit).
- For Play service account tests or server-side validation, keep keys in a secure vault and reference them in CI only.
- For local release signing, put these values in `~/.gradle/gradle.properties`, not the tracked project `gradle.properties`:
  ```properties
  CLICKDUNGEON_STORE_FILE=/absolute/path/to/clickdungeon-upload.jks
  CLICKDUNGEON_STORE_PASSWORD=<keystore password>
  CLICKDUNGEON_KEY_ALIAS=clickdungeon
  CLICKDUNGEON_KEY_PASSWORD=<key password>
  ```

Security note: never commit private keys, signing passwords, local JDK paths, or service account JSON to the repository.
