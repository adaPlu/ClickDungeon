package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SecurePreferencesTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, "secure_prefs_test")
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void getReturnsWritablePreferences() {
        SharedPreferences prefs = SecurePreferences.get(context, "secure_prefs_test");
        assertNotNull(prefs);
        prefs.edit().putString("value", "ok").commit();
        assertEquals("ok", prefs.getString("value", null));

        SharedPreferences raw = context.getSharedPreferences("secure_prefs_test", Context.MODE_PRIVATE);
        String stored = raw.getString("value", null);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
                && PersistenceSecurityConfig.USE_ENCRYPTED_PREFS) {
            assertNotEquals("ok", stored);
        }
    }

    @Test
    public void typedRoundTrip_andGetAllReturnsDecodedValues() {
        SharedPreferences prefs = SecurePreferences.get(context, "secure_prefs_test");
        prefs.edit()
                .putString("name", "Kai")
                .putInt("level", 7)
                .putBoolean("alive", true)
                .putLong("gold", 123L)
                .putFloat("ratio", 1.5f)
                .commit();

        assertEquals("Kai", prefs.getString("name", null));
        assertEquals(7, prefs.getInt("level", 0));
        assertTrue(prefs.getBoolean("alive", false));
        assertEquals(123L, prefs.getLong("gold", 0L));
        assertEquals(1.5f, prefs.getFloat("ratio", 0f), 0.0001f);

        java.util.Map<String, ?> all = prefs.getAll();
        assertEquals("Kai", all.get("name"));
        assertEquals(7, all.get("level"));
        assertEquals(true, all.get("alive"));
    }

    @Test
    public void malformedEncryptedPayloadFallsBackToDefault() {
        SharedPreferences raw = context.getSharedPreferences("secure_prefs_test", Context.MODE_PRIVATE);
        raw.edit().putString("broken", "enc:not-valid-base64").commit();

        SharedPreferences prefs = SecurePreferences.get(context, "secure_prefs_test");
        assertEquals("fallback", prefs.getString("broken", "fallback"));
        assertEquals(9, prefs.getInt("broken", 9));
        assertEquals(false, prefs.getBoolean("broken", false));
    }

    @Test
    public void stringSetBehavior_isExplicitForEncryptedOrPlainFallback() {
        SharedPreferences prefs = SecurePreferences.get(context, "secure_prefs_test");
        java.util.Set<String> values = new java.util.HashSet<>();
        values.add("a");
        values.add("b");

        try {
            prefs.edit().putStringSet("letters", values).commit();
            java.util.Set<String> loaded = prefs.getStringSet("letters", java.util.Collections.emptySet());
            assertNotNull(loaded);
        } catch (UnsupportedOperationException ex) {
            java.util.Set<String> fallback = prefs.getStringSet("letters", values);
            assertEquals(values, fallback);
        }
    }
}
