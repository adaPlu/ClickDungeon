package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PlayerClassTest {

    @Test
    public void abilityProgressionIncludesFiveUnlocks() {
        PlayerClass.AbilityDefinition[] abilities = PlayerClass.KNIGHT.getAbilityProgression();
        assertEquals(5, abilities.length);
        assertEquals(1, abilities[0].getUnlockLevel());
        assertEquals(5, abilities[1].getUnlockLevel());
        assertEquals(10, abilities[2].getUnlockLevel());
        assertEquals(15, abilities[3].getUnlockLevel());
        assertEquals(20, abilities[4].getUnlockLevel());
    }

    @Test
    public void baseAbilityMatchesProgression() {
        PlayerClass.AbilityDefinition[] abilities = PlayerClass.WIZARD.getAbilityProgression();
        assertTrue(abilities[0].getName().contains(PlayerClass.WIZARD.getBaseAbilityName()));
    }
}
