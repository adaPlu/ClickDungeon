package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.util.PersistedBlobStore;
import com.adaplu.clickdungeon.util.SecurePreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class PersistedBlobStoreFailureTest {

    private Context context;
    private static final String PREFS = "blob_store_test_fail";
    private static final String KEY = "payload";

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SecurePreferences.get(context, PREFS).edit().clear().commit();
        PersistedBlobStore.setTelemetryListener(null);
    }

    @After
    public void tearDown() {
        PersistedBlobStore.setForceHmacUnavailableForTests(false);
        PersistedBlobStore.setTelemetryListener(null);
    }

    @Test
    public void saveReturnsFalseAndRecordsEventWhenHmacUnavailable() {
        PersistedBlobStore.setForceHmacUnavailableForTests(true);

        boolean ok = PersistedBlobStore.save(context, PREFS, KEY, 1, "{\"value\":1}");

        assertFalse(ok);

        List<String> events = PersistedBlobStore.getRecentEvents();
        boolean found = false;
        for (String e : events) {
            if (e != null && e.startsWith("save_failed:")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}
