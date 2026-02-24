package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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

    @Test
    public void configForType_nullUsesDefaultSlimeConfig() throws Exception {
        Object nullConfig = invokeConfigForType(null);
        Object slimeConfig = invokeConfigForType("slime");

        assertNotNull(nullConfig);
        assertEquals(getIntField(slimeConfig, "spriteSheetRes"), getIntField(nullConfig, "spriteSheetRes"));
        assertEquals(getIntField(slimeConfig, "moveSoundRes"), getIntField(nullConfig, "moveSoundRes"));
        assertEquals(getIntField(slimeConfig, "attackSoundRes"), getIntField(nullConfig, "attackSoundRes"));
        assertEquals(getIntField(slimeConfig, "defendSoundRes"), getIntField(nullConfig, "defendSoundRes"));
    }

    @Test
    public void configForType_unknownFallsBackToDefaultConfig() throws Exception {
        Object unknownConfig = invokeConfigForType("not_a_monster");
        Object slimeConfig = invokeConfigForType("slime");
        Object goblinConfig = invokeConfigForType("goblin");

        assertNotNull(unknownConfig);
        assertEquals(getIntField(slimeConfig, "spriteSheetRes"), getIntField(unknownConfig, "spriteSheetRes"));
        assertNotEquals(getIntField(goblinConfig, "spriteSheetRes"), getIntField(unknownConfig, "spriteSheetRes"));
    }

    private Object invokeConfigForType(String type) throws Exception {
        Class<?> configClass = Class.forName("com.example.clickdungeon.util.MonsterAnimationHelper$Config");
        java.lang.reflect.Method method = configClass.getDeclaredMethod("forType", String.class);
        method.setAccessible(true);
        return method.invoke(null, type);
    }

    private int getIntField(Object target, String fieldName) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(target);
    }
}
