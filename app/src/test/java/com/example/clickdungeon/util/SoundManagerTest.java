package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SoundManagerTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SoundManager.release();
        SoundManager.init(context);
    }

    @After
    public void tearDown() {
        SoundManager.release();
    }

    @Test
    public void playAndReport_unknownKey_returnsFalse() {
        boolean played = SoundManager.playAndReport("missing_key_for_test");
        assertFalse(played);
    }

    @Test
    public void playAndReport_registeredKey_returnsTrue() {
        boolean played = SoundManager.playAndReport(SoundManager.KEY_EFFECT_POSITIVE);
        assertTrue(played);
    }

    @Test
    public void playForMonster_buildsKeyAndReports() {
        final String[] lastKey = new String[1];
        SoundManager.setTestPlaybackListener(key -> lastKey[0] = key);

        SoundManager.playForMonster("Slime", "Attack");

        assertEquals("slime_attack", lastKey[0]);
    }

    @Test
    public void playForClass_buildsKeyAndReports() {
        final String[] lastKey = new String[1];
        SoundManager.setTestPlaybackListener(key -> lastKey[0] = key);

        SoundManager.playForClass("Knight", "Move");

        assertEquals("knight_move", lastKey[0]);
    }

    @Test
    public void toggleMuteFlipsState() {
        SoundManager.setMuted(false);
        SoundManager.toggleMute();
        assertTrue(SoundManager.isMuted());

        SoundManager.toggleMute();
        assertFalse(SoundManager.isMuted());
    }
}
