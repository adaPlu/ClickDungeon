package com.adaplu.clickdungeon.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CharacterProfileTest {

    @Test
    public void constructorSetsStatsPerClass() {
        CharacterProfile knight = new CharacterProfile("K", PlayerClass.KNIGHT);
        assertEquals(10, knight.getMaxHP());
        assertEquals(2, knight.getAttack());
        assertEquals(3, knight.getDefense());

        CharacterProfile thief = new CharacterProfile("T", PlayerClass.THIEF);
        assertEquals(6, thief.getMaxHP());
        assertEquals(4, thief.getAttack());
        assertEquals(5, thief.getDefense());

        CharacterProfile wizard = new CharacterProfile("W", PlayerClass.WIZARD);
        assertEquals(4, wizard.getMaxHP());
        assertEquals(3, wizard.getAttack());
        assertEquals(2, wizard.getDefense());
    }

    @Test
    public void addExperienceLevelsUpAndCarriesRemainder() {
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.KNIGHT);
        profile.addExperience(250);

        assertEquals(2, profile.getLevel());
        assertEquals(150, profile.getXp());
        assertEquals(12, profile.getMaxHP());
        assertEquals(12, profile.getCurrentHP());
        assertEquals(2, profile.getAttack());
        assertEquals(4, profile.getDefense());
        assertEquals(2, profile.getAvailableStatPoints());
    }

    @Test
    public void wizardUsesMpAndGainsManaWithIntelligence() {
        CharacterProfile profile = new CharacterProfile("Mage", PlayerClass.WIZARD);
        assertTrue(profile.usesMp());
        int initialMp = profile.getMaxMP();
        profile.addExperience(120);
        profile.increaseIntelligence(1);
        assertTrue(profile.getMaxMP() > initialMp);
    }

    @Test
    public void takeDamageClampsToZero() {
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.THIEF);
        profile.takeDamage(999);

        assertTrue(profile.isDead());
        assertEquals(0, profile.getCurrentHP());
    }

    @Test
    public void setAvailableStatPoints_clampsToNonNegative() {
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.KNIGHT);
        profile.setAvailableStatPoints(3);
        assertEquals(3, profile.getAvailableStatPoints());
        profile.setAvailableStatPoints(-2);
        assertEquals(0, profile.getAvailableStatPoints());
    }

    @Test
    public void animatedPlayerCanBeStored() {
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.KNIGHT);
        assertNull(profile.getAnimatedPlayer());

        Context context = ApplicationProvider.getApplicationContext();
        AnimatedPlayer animatedPlayer = new AnimatedPlayer(
                context,
                PlayerClass.KNIGHT,
                64,
                64,
                4,
                120);
        profile.setAnimatedPlayer(animatedPlayer);

        assertEquals(animatedPlayer, profile.getAnimatedPlayer());
    }
}
