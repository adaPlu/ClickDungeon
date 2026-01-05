package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AnimatedPlayerTest {

    @Test
    public void setActionUpdatesCurrentAction() {
        Context context = ApplicationProvider.getApplicationContext();
        AnimatedPlayer player = new AnimatedPlayer(context, PlayerClass.KNIGHT, 64, 64, 4, 120);

        player.setAction("attack");

        assertEquals("attack", player.getCurrentAction());
    }

    @Test
    public void getCurrentFrameReturnsBitmap() {
        Context context = ApplicationProvider.getApplicationContext();
        AnimatedPlayer player = new AnimatedPlayer(context, PlayerClass.THIEF, 64, 64, 4, 120);

        assertNotNull(player.getCurrentFrame());
    }
}
