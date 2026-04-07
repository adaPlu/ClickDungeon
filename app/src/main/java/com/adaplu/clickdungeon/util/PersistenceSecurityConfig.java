package com.adaplu.clickdungeon.util;

/**
 * Central toggle for persistence hardening options.
 */
public final class PersistenceSecurityConfig {
    /** Enables encrypted preferences when available. */
    public static final boolean USE_ENCRYPTED_PREFS = true;
    /** Policy to apply when encrypted preferences cannot be initialized. */
        public static final EncryptedPrefsFailurePolicy ENCRYPTED_PREFS_FAILURE_POLICY =
            EncryptedPrefsFailurePolicy.FAIL_OPEN;
    /** Version used to rotate the integrity HMAC key. */
    public static final int INTEGRITY_KEY_VERSION = 1;

    private PersistenceSecurityConfig() {
    }

    public enum EncryptedPrefsFailurePolicy {
        FAIL_OPEN,
        FAIL_CLOSED
    }
}
