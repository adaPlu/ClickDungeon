# ClickDungeon Persistence & Security Notes

- Saves, inventory, and achievements are stored in plain SharedPreferences/JSON with no schema versioning, integrity checks, or encryption. Tampering is possible.
- Planned hardening:
  - Add `schemaVersion` and checksum/HMAC per saved blob; verify on load and migrate or prompt to reset on mismatch. Keep a “last-known-good” backup to auto-restore on corruption.
  - Optionally adopt `EncryptedSharedPreferences` (AndroidX Security) or Room with encrypted columns for sensitive fields.
  - Centralize all preference access through the managers; log checksum/encryption failures (without PII) to aid QA.
- Testing to add:
  - Corrupt-save tests that assert validation/migration kicks in.
  - Old `schemaVersion` tests that confirm migration or user prompt paths.
