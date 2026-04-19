package com.adaplu.clickdungeon.model;

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
        assertEquals(0, abilities[0].getUnlockXpCost());
        assertEquals(24, abilities[1].getUnlockXpCost());
        assertEquals(56, abilities[2].getUnlockXpCost());
        assertEquals(96, abilities[3].getUnlockXpCost());
        assertEquals(150, abilities[4].getUnlockXpCost());
    }

    @Test
    public void baseAbilityMatchesProgression() {
        PlayerClass.AbilityDefinition[] abilities = PlayerClass.WIZARD.getAbilityProgression();
        assertTrue(abilities[0].getName().contains(PlayerClass.WIZARD.getBaseAbilityName()));
    }

    @Test
    public void rangerHasFiveAbilities() {
        PlayerClass.AbilityDefinition[] abilities = PlayerClass.RANGER.getAbilityProgression();
        assertEquals(5, abilities.length);
    }

    @Test
    public void rangerAbilityUnlockCostsAreCorrect() {
        PlayerClass.AbilityDefinition[] abilities = PlayerClass.RANGER.getAbilityProgression();
        assertEquals(0,   abilities[0].getUnlockXpCost());
        assertEquals(24,  abilities[1].getUnlockXpCost());
        assertEquals(56,  abilities[2].getUnlockXpCost());
        assertEquals(96,  abilities[3].getUnlockXpCost());
        assertEquals(150, abilities[4].getUnlockXpCost());
    }

    @Test
    public void rangerAbilityNamesMatchExpected() {
        PlayerClass.AbilityDefinition[] abilities = PlayerClass.RANGER.getAbilityProgression();
        assertEquals("Piercing Shot", abilities[0].getName());
        assertEquals("Rapid Volley",  abilities[1].getName());
        assertEquals("Camouflage",    abilities[2].getName());
        assertEquals("Net Trap",      abilities[3].getName());
        assertEquals("Eagle Eye",     abilities[4].getName());
    }

    @Test
    public void rangerStartsWithExpectedHealthAndNoMp() {
        assertEquals(14, PlayerClass.RANGER.getStartingHealth());
        assertEquals(false, PlayerClass.RANGER.usesMp());
    }

    @Test
    public void rangerBaseAbilityIsPiercingShot() {
        assertEquals("Piercing Shot", PlayerClass.RANGER.getBaseAbilityName());
    }

    @Test
    public void rangerAbilitiesUpToLevel4OnlyContainsFirstAbility() {
        PlayerClass.AbilityDefinition[] unlocked = PlayerClass.RANGER.getAbilitiesUpToLevel(4);
        assertEquals(1, unlocked.length);
        assertEquals("Piercing Shot", unlocked[0].getName());
    }

    @Test
    public void rangerAbilitiesUpToLevel10ContainsThreeAbilities() {
        PlayerClass.AbilityDefinition[] unlocked = PlayerClass.RANGER.getAbilitiesUpToLevel(10);
        assertEquals(3, unlocked.length);
    }

    @Test
    public void rangerAbilitiesUpToMaxLevelContainsAllFive() {
        PlayerClass.AbilityDefinition[] unlocked = PlayerClass.RANGER.getAbilitiesUpToLevel(20);
        assertEquals(5, unlocked.length);
    }
}
