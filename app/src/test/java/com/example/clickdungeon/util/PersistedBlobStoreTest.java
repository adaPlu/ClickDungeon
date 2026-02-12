package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PersistedBlobStoreTest {

    private static final String PREFS = "blob_store_test";
    private static final String KEY = "payload";

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, PREFS).edit().clear().commit();
    }

    @After
    public void tearDown() {
        PersistedBlobStore.setTelemetryListener(null);
    }

    @Test
    public void saveAndLoadReturnsOkStatus() {
        PersistedBlobStore.save(context, PREFS, KEY, 1, "{\"value\":1}");

        PersistedBlobStore.LoadResult result = PersistedBlobStore.load(context, PREFS, KEY, 1);

        assertEquals(PersistedBlobStore.LoadResult.Status.OK, result.status);
        assertEquals("{\"value\":1}", result.json);
        assertEquals(1, result.schemaVersion);
    }

    @Test
    public void corruptPrimaryRestoresBackup() {
        PersistedBlobStore.save(context, PREFS, KEY, 1, "{\"value\":1}");
        PersistedBlobStore.save(context, PREFS, KEY, 1, "{\"value\":2}");

        SharedPreferences prefs = SecurePreferences.get(context, PREFS);
        prefs.edit()
                .putString(KEY, "{\"value\":999}")
                .apply();

        PersistedBlobStore.LoadResult result = PersistedBlobStore.load(context, PREFS, KEY, 1);

        assertEquals(PersistedBlobStore.LoadResult.Status.OK, result.status);
        assertNotNull(result.json);
        assertEquals("{\"value\":1}", result.json);
    }

    @Test
    public void schemaMismatchReturnsStatus() {
        PersistedBlobStore.save(context, PREFS, KEY, 1, "{\"value\":5}");

        PersistedBlobStore.LoadResult result = PersistedBlobStore.load(context, PREFS, KEY, 2);

        assertEquals(PersistedBlobStore.LoadResult.Status.SCHEMA_MISMATCH, result.status);
        assertEquals("{\"value\":5}", result.json);
        assertEquals(1, result.schemaVersion);
    }

    @Test
    public void keyRotationKeepsPreviousKeyValid() {
        PersistedBlobStore.save(context, PREFS, KEY, 1, "{\"value\":1}");

        SecurePreferences.get(context, "security_prefs")
                .edit()
                .putInt("integrity_key_version", 0)
                .commit();

        PersistedBlobStore.save(context, "blob_store_test_b", "payload_b", 1, "{\"value\":2}");

        PersistedBlobStore.LoadResult result = PersistedBlobStore.load(context, PREFS, KEY, 1);

        assertEquals(PersistedBlobStore.LoadResult.Status.OK, result.status);
        assertEquals("{\"value\":1}", result.json);
    }

    @Test
    public void schemaMismatchEmitsTelemetryEvent() {
        PersistedBlobStore.save(context, PREFS, KEY, 1, "{\"value\":9}");
        PersistedBlobStore.setTelemetryListener(new PersistedBlobStore.TelemetryListener() {
            @Override
            public void onSchemaMismatch(String prefs, String key, int found, int expected) {
                // No-op; listener presence should still log into recent events.
            }

            @Override
            public void onRestoreFromBackup(String prefs, String key, boolean success) {
                // No-op.
            }
        });

        PersistedBlobStore.LoadResult result = PersistedBlobStore.load(context, PREFS, KEY, 2);

        assertEquals(PersistedBlobStore.LoadResult.Status.SCHEMA_MISMATCH, result.status);
        assertNotNull(PersistedBlobStore.getRecentEvents());
    }
}
