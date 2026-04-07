# ClickDungeon Persistence & Security Notes

## Current State
- Saves, inventory, achievements, and terrain metadata use schema + checksum validation with backup recovery; encrypted prefs use Tink + Android Keystore on API 23+ with fallback.
- Game state persistence happens during pause; instance-state bundles only track the active save slot to avoid redundant full-save serialization.
- Save serialization and HMAC computation run on a background executor, with debounced saves for non-critical events and immediate saves for pause/critical events.
- Background saves snapshot state before serialization to avoid mid-save mutation issues.
- Encrypted prefs failure policy defaults to fail-open (fallback to plain prefs) with a configurable fail-closed option.
- Schema mismatch review prompts are surfaced in the Continue screen, and integrity keys support rotation with a previous-key fallback.
- Restore/mismatch events are logged without PII and exposed through an in-memory QA event list.

## Phase 1: Validation + Recovery (implemented)
- Added `schemaVersion` and checksum/HMAC per saved blob; loads verify integrity and schema before use.
- Added a last-known-good backup and auto-restore on corruption.
- Centralized integrity handling through `PersistedBlobStore`; checksum failures are logged without PII.

## Phase 2: Encryption (implemented)
- Tink AES-GCM with Android Keystore is used on API 23+ with a fallback to plain prefs if unavailable.

## Phase 3: Testing (implemented)
- Added corrupt-save tests that assert validation + backup restore behavior.
- Added schema mismatch tests that ensure mismatch handling is exercised.
- Added PersistedBlobStore and SecurePreferences unit coverage for integrity and encrypted prefs access.
