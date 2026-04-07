package com.adaplu.clickdungeon.util;

import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SoundManagerSmokeTest {

    private Context context;
    private final List<String> playedKeys = new ArrayList<>();

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SoundManager.init(context);
        SoundManager.setTestPlaybackListener(playedKeys::add);
    }

    @After
    public void tearDown() {
        SoundManager.setTestPlaybackListener(null);
        SoundManager.release();
    }

    @Test
    public void playsAllRegisteredKeys() {
        Set<String> registered = SoundManager.getRegisteredKeys();
        for (String key : registered) {
            SoundManager.playAndReport(key);
        }
        Set<String> played = new HashSet<>(playedKeys);
        for (String key : registered) {
            assertTrue("Expected playback request for " + key, played.contains(key));
        }
    }
}
