package com.example.clickdungeon.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.ui.CombatDialogFragment.CombatCallbacks;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class CombatDialogFragmentTest {

    @Test
    public void usePotion_consumesHealingWhenBelowMax() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Lena", PlayerClass.KNIGHT);
        profile.setCurrentHP(4);
        Monster monster = new Monster("Bat", 5, 3, 0, "🦇");

        TestCallbacks callbacks = new TestCallbacks(profile);
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster);
        fragment.setCombatants(profile, monster);
        fragment.setCombatCallbacks(callbacks);
        fragment.setRewardPreview(7, 3);
        fragment.show(activity.getSupportFragmentManager(), "combat_potion");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        dialog.findViewById(R.id.buttonUsePotion).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertTrue(callbacks.potionConsumed);
        assertEquals(10, profile.getCurrentHP());
        CharSequence summary = ((android.widget.TextView) dialog.findViewById(R.id.textCombatSummary)).getText();
        assertTrue(summary.toString().contains(
                activity.getString(R.string.combat_summary_potion_used, 6)));
    }

    @Test
    public void usePotion_atFullHealth_appendsLogAndDoesNotConsume() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Rin", PlayerClass.WIZARD);
        Monster monster = new Monster("Slime", 3, 2, 0, "🫧");

        TestCallbacks callbacks = new TestCallbacks(profile);
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster);
        fragment.setCombatants(profile, monster);
        fragment.setCombatCallbacks(callbacks);
        fragment.show(activity.getSupportFragmentManager(), "combat_full_hp_potion");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        dialog.findViewById(R.id.buttonUsePotion).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        CharSequence log = ((android.widget.TextView) dialog.findViewById(R.id.textCombatLog)).getText();
        assertTrue(log.toString().contains(activity.getString(R.string.combat_log_potion_full_hp)));
        assertFalse(callbacks.potionConsumed);
        assertEquals(profile.getMaxHP(), profile.getCurrentHP());
    }

    @Test
    public void flee_invokesPenaltyAndDisablesActions() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Mira", PlayerClass.THIEF);
        Monster monster = new Monster("Orc", 8, 9, 1, "👹");

        TestCallbacks callbacks = new TestCallbacks(profile);
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster);
        fragment.setCombatants(profile, monster);
        fragment.setCombatCallbacks(callbacks);
        fragment.show(activity.getSupportFragmentManager(), "combat_flee");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        dialog.findViewById(R.id.buttonFlee).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertEquals(4, callbacks.fleePenalty);
        assertFalse(dialog.findViewById(R.id.buttonAttack).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonUsePotion).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonFlee).isEnabled());
    }

    @Test
    public void attackVictory_triggersCallbackAndSummary() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Vale", PlayerClass.KNIGHT);
        Monster monster = new Monster("Imp", 1, 1, 0, "😈");

        TestCallbacks callbacks = new TestCallbacks(profile);
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster);
        fragment.setCombatants(profile, monster);
        fragment.setCombatCallbacks(callbacks);
        fragment.setRewardPreview(9, 5);
        fragment.show(activity.getSupportFragmentManager(), "combat_attack_victory");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        dialog.findViewById(R.id.buttonAttack).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertTrue(callbacks.victoryAchieved);
        CharSequence summary = ((android.widget.TextView) dialog.findViewById(R.id.textCombatSummary)).getText();
        assertTrue(summary.toString().contains(
                activity.getString(R.string.combat_summary_victory, 9, 5, 1, 2, 0, 0)));
        assertFalse(dialog.findViewById(R.id.buttonAttack).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonUsePotion).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonFlee).isEnabled());
    }

    @Test
    public void monsterDefeat_showsDetailedSummary() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Kira", PlayerClass.THIEF);
        profile.setCurrentHP(3);
        Monster monster = new Monster("Ogre", 8, 6, 1, "👺");

        TestCallbacks callbacks = new TestCallbacks(profile);
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster);
        fragment.setCombatants(profile, monster);
        fragment.setCombatCallbacks(callbacks);
        ReflectionHelpers.setField(fragment, "random", new Random(0));
        fragment.show(activity.getSupportFragmentManager(), "combat_defeat");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        dialog.findViewById(R.id.buttonAttack).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        CharSequence summary = ((android.widget.TextView) dialog.findViewById(R.id.textCombatSummary)).getText();
        assertTrue(summary.toString().contains(
                activity.getString(R.string.combat_summary_defeat, 1, 3, 5, 0)));
        assertFalse(dialog.findViewById(R.id.buttonAttack).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonUsePotion).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonFlee).isEnabled());
        assertTrue(callbacks.defeatCalled);
    }

    private FragmentActivity buildThemedActivity() {
        ActivityController<FragmentActivity> controller = Robolectric.buildActivity(FragmentActivity.class);
        FragmentActivity activity = controller.setup().get();
        activity.setTheme(R.style.Theme_ClickDungeon);
        return activity;
    }

    private static class TestCallbacks implements CombatCallbacks {
        final CharacterProfile profile;
        boolean potionConsumed;
        boolean victoryAchieved;
        boolean defeatCalled;
        int fleePenalty;

        TestCallbacks(CharacterProfile profile) {
            this.profile = profile;
        }

        @Override
        public void onCombatVictory(@NonNull Monster monster) {
            victoryAchieved = true;
        }

        @Override
        public void onCombatDefeat() {
            defeatCalled = true;
        }

        @Override
        public void onCombatFled(int penaltyDamage) {
            this.fleePenalty = penaltyDamage;
            profile.takeDamage(penaltyDamage);
        }

        @Override
        public void onCombatStateUpdated() {
        }

        @Override
        public boolean onUseHealingPotionRequested(int healAmount) {
            profile.setCurrentHP(profile.getCurrentHP() + healAmount);
            potionConsumed = true;
            return true;
        }
    }
}
