package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashSet;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class SoundManagerKeysTest {

    private final Context context = ApplicationProvider.getApplicationContext();
    private final Set<String> playedKeys = new HashSet<>();

    @Before
    public void setUp() {
        SoundManager.release();
        SoundManager.init(context);
        SoundManager.setMuted(false);
        SoundManager.setTestPlaybackListener(playedKeys::add);
    }

    @After
    public void tearDown() {
        SoundManager.setTestPlaybackListener(null);
        SoundManager.release();
        playedKeys.clear();
    }

    @Test
    public void allRegisteredKeys_reportPlayback() {
        Set<String> keys = SoundManager.getRegisteredKeys();
        assertFalse("Expected registered sound keys", keys.isEmpty());
        for (String key : keys) {
            SoundManager.play(key);
        }
        assertTrue("Playback listener should observe all registered keys", playedKeys.containsAll(keys));
    }
}
