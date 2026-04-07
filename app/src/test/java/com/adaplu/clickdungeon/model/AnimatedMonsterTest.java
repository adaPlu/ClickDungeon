package com.adaplu.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AnimatedMonsterTest {

    @Test
    public void setActionUpdatesCurrentAction() {
        Context context = ApplicationProvider.getApplicationContext();
        AnimatedMonster monster = new AnimatedMonster(context, "Slime", 3, 1, 0, 6, 100,
                R.drawable.slime_sprite_sheet,
                R.raw.slime_move,
                R.raw.slime_attack,
                R.raw.slime_defend);

        monster.setAction("attack", context, false);

        assertEquals("attack", monster.getCurrentAction());
    }

    @Test
    public void getCurrentFrameReturnsBitmap() {
        Context context = ApplicationProvider.getApplicationContext();
        AnimatedMonster monster = new AnimatedMonster(context, "Slime", 3, 1, 0, 6, 100,
                R.drawable.slime_sprite_sheet,
                R.raw.slime_move,
                R.raw.slime_attack,
                R.raw.slime_defend);

        assertNotNull(monster.getCurrentFrame());
    }
}
