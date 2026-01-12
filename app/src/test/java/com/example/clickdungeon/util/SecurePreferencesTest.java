package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;

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
}
