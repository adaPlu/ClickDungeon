# ClickDungeon Persistence & Security Notes

## Current State
- Saves, inventory, achievements, and terrain metadata use schema + checksum validation with backup recovery; encrypted prefs are used on API 23+ with fallback.
- Game state persistence happens during pause; instance-state bundles only track the active save slot to avoid redundant full-save serialization.

## Phase 1: Validation + Recovery (implemented)
- Added `schemaVersion` and checksum/HMAC per saved blob; loads verify integrity and schema before use.
- Added a last-known-good backup and auto-restore on corruption.
- Centralized integrity handling through `PersistedBlobStore`; checksum failures are logged without PII.

## Phase 2: Encryption (implemented)
- `EncryptedSharedPreferences` is used on API 23+ with a fallback to plain prefs if unavailable.

## Phase 3: Testing (implemented)
- Added corrupt-save tests that assert validation + backup restore behavior.
- Added schema mismatch tests that ensure mismatch handling is exercised.
