package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PersistenceSecurityConfigTest {

    @Test
    public void defaultsToEncryptedPrefsWithFailOpenPolicy() {
        assertTrue(PersistenceSecurityConfig.USE_ENCRYPTED_PREFS);
        assertEquals(
                PersistenceSecurityConfig.EncryptedPrefsFailurePolicy.FAIL_OPEN,
                PersistenceSecurityConfig.ENCRYPTED_PREFS_FAILURE_POLICY
        );
    }

    @Test
    public void integrityKeyVersionIsPositive() {
        assertTrue(PersistenceSecurityConfig.INTEGRITY_KEY_VERSION > 0);
    }
}
