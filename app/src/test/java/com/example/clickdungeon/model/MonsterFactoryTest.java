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
public class MonsterFactoryTest {

    @Test
    public void createReturnsTypedMonster() {
        Context context = ApplicationProvider.getApplicationContext();
        AnimatedMonster monster = MonsterFactory.create(context, "dragon");

        assertNotNull(monster);
        assertEquals("Dragon", monster.getMonsterType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUnknownTypeThrows() {
        Context context = ApplicationProvider.getApplicationContext();
        MonsterFactory.create(context, "unknown_type");
    }
}
