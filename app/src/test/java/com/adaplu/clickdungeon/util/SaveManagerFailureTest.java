package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(RobolectricTestRunner.class)
public class SaveManagerFailureTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Ensure slot prefs are empty and test hooks cleared
        SecurePreferences.get(context, "SaveSlot0").edit().clear().commit();
        PersistedBlobStore.setTelemetryListener(null);
    }

    @After
    public void tearDown() {
        PersistedBlobStore.setForceHmacUnavailableForTests(false);
        SaveManager.setTestSaveListener(null);
    }

    @Test
    public void saveGame_doesNotClearLegacyKeysOrNotifyOnSaveFailure() {
        // Populate legacy keys so we can detect whether they were cleared
        SharedPreferences prefs = SecurePreferences.get(context, "SaveSlot0");
        prefs.edit().putString("CharacterProfile", "{\"name\":\"hero\"}")
                .putString("DungeonGrid", "[]")
                .commit();

        final AtomicBoolean notified = new AtomicBoolean(false);
        SaveManager.setTestSaveListener(slotIndex -> notified.set(true));

        // Force HMAC unavailable so PersistedBlobStore.save returns false
        PersistedBlobStore.setForceHmacUnavailableForTests(true);

        SaveManager manager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Hero", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[][] {{ new Tile(TileType.EMPTY) }};

        manager.saveGame(0, profile, 1, 0, 0, grid, null);

        // listener should NOT have been invoked
        assertFalse(notified.get());

        // legacy key should still be present because save failed
        assertTrue(prefs.contains("CharacterProfile"));
    }
}
