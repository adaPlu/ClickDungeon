package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.AnimatedMonster;
import com.example.clickdungeon.model.MonsterAffinity;
import com.example.clickdungeon.model.MonsterFamily;
import com.example.clickdungeon.model.Monster;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MonsterAnimationHelperTest {

    @Test
    public void createAnimatedCloneCopiesStatsAndCurrentHp() {
        Context context = ApplicationProvider.getApplicationContext();
        Monster source = new Monster("Goblin", 6, 3, 1, "G");
        source.setFamily(MonsterFamily.HUMANOID);
        source.setAffinity(MonsterAffinity.ARCANE);
        source.takeDamage(2);

        AnimatedMonster clone = MonsterAnimationHelper.createAnimatedClone(context, source);

        assertNotNull(clone);
        assertEquals(source.getMonsterType(), clone.getMonsterType());
        assertEquals(source.getMaxHP(), clone.getMaxHP());
        assertEquals(source.getAttack(), clone.getAttack());
        assertEquals(source.getDefense(), clone.getDefense());
        assertEquals(source.getCurrentHP(), clone.getCurrentHP());
        assertEquals(source.getFamily(), clone.getFamily());
        assertEquals(source.getAffinity(), clone.getAffinity());
    }
}
