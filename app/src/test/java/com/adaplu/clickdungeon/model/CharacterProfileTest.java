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
        assertEquals(18, knight.getMaxHP());
        assertEquals(2, knight.getAttack());
        assertEquals(1, knight.getDefense());

        CharacterProfile thief = new CharacterProfile("T", PlayerClass.THIEF);
        assertEquals(13, thief.getMaxHP());
        assertEquals(2, thief.getAttack());
        assertEquals(1, thief.getDefense());

        CharacterProfile wizard = new CharacterProfile("W", PlayerClass.WIZARD);
        assertEquals(12, wizard.getMaxHP());
        assertEquals(2, wizard.getAttack());
        assertEquals(1, wizard.getDefense());

        CharacterProfile ranger = new CharacterProfile("R", PlayerClass.RANGER);
        assertEquals(14, ranger.getMaxHP());
        assertEquals(2, ranger.getAttack());
        assertEquals(1, ranger.getDefense());
    }

    @Test
    public void addExperienceAddsToCurrentClassXp() {
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.KNIGHT);
        profile.addExperience(250);

        assertEquals(1, profile.getLevel());
        assertEquals(250, profile.getCurrentClassXp());
        assertEquals(18, profile.getMaxHP());
        assertEquals(18, profile.getCurrentHP());
    }

    @Test
    public void abilityChargesRegenerateOverTime() {
        CharacterProfile profile = new CharacterProfile("Mage", PlayerClass.WIZARD);
        profile.addClassXp(PlayerClass.WIZARD, 200);
        assertTrue(profile.unlockAbility(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_METEOR));

        long now = 1_000L;
        assertEquals(3, profile.getAbilityCharges(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_METEOR, now));
        assertTrue(profile.consumeAbilityCharge(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_METEOR, now));
        assertEquals(2, profile.getAbilityCharges(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_METEOR, now));

        long restoredAt = now + PlayerClass.WIZARD.findAbility(PlayerClass.ABILITY_WIZARD_METEOR).getRechargeDurationMillis();
        assertEquals(3, profile.getAbilityCharges(PlayerClass.WIZARD, PlayerClass.ABILITY_WIZARD_METEOR, restoredAt));
    }

    @Test
    public void takeDamageClampsToZero() {
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.THIEF);
        profile.takeDamage(999);

        assertTrue(profile.isDead());
        assertEquals(0, profile.getCurrentHP());
    }

    @Test
    public void unlockingAbilitySpendsClassXp() {
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.RANGER);
        profile.addClassXp(PlayerClass.RANGER, 80);

        assertTrue(profile.unlockAbility(PlayerClass.RANGER, PlayerClass.ABILITY_RANGER_CAMOUFLAGE));
        assertEquals(24, profile.getClassXp(PlayerClass.RANGER));
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

    @Test
    public void rangerAttackScalesFromBoosts() {
        CharacterProfile profile = new CharacterProfile("Scout", PlayerClass.RANGER);
        assertEquals(2, profile.getAttack());

        profile.addAttackBoost(2);

        assertEquals(4, profile.getAttack());
    }
}
