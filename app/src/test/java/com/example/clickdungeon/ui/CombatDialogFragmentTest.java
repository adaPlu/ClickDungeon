package com.example.clickdungeon.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Dialog;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.AnimatedPlayer;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.MonsterAffinity;
import com.example.clickdungeon.model.MonsterFamily;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.ui.CombatDialogFragment.CombatCallbacks;
import com.example.clickdungeon.util.SoundManager;

import java.util.Random;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class CombatDialogFragmentTest {

    @After
    public void clearSoundListener() {
        SoundManager.setTestPlaybackListener(null);
    }

    @Test
    public void usePotion_consumesHealingWhenBelowMax() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Lena", PlayerClass.KNIGHT);
        profile.setCurrentHP(4);
        Monster monster = new Monster("Bat", 5, 3, 0, "🦇");

        TestCallbacks callbacks = new TestCallbacks(profile);
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
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
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
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
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
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
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
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
        String xpText = activity.getResources().getQuantityString(R.plurals.combat_summary_xp, 9, 9);
        String goldText = activity.getResources().getQuantityString(R.plurals.combat_summary_gold, 5, 5);
        String turnsText = activity.getResources().getQuantityString(R.plurals.combat_summary_turns, 1, 1);
        String dealtText = activity.getResources().getQuantityString(R.plurals.combat_summary_damage, 2, 2);
        String takenText = activity.getResources().getQuantityString(R.plurals.combat_summary_damage, 0, 0);
        String potionsText = activity.getResources().getQuantityString(R.plurals.combat_summary_potions, 0, 0);
        assertTrue(summary.toString().contains(
                activity.getString(R.string.combat_summary_victory,
                        xpText,
                        goldText,
                        turnsText,
                        dealtText,
                        takenText,
                        potionsText)));
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
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
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
        String turnsText = activity.getResources().getQuantityString(R.plurals.combat_summary_turns, 1, 1);
        String dealtText = activity.getResources().getQuantityString(R.plurals.combat_summary_damage, 3, 3);
        String takenText = activity.getResources().getQuantityString(R.plurals.combat_summary_damage, 4, 4);
        String potionsText = activity.getResources().getQuantityString(R.plurals.combat_summary_potions, 0, 0);
        assertTrue(summary.toString().contains(
                activity.getString(R.string.combat_summary_defeat,
                        turnsText,
                        dealtText,
                        takenText,
                        potionsText)));
        assertFalse(dialog.findViewById(R.id.buttonAttack).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonUsePotion).isEnabled());
        assertFalse(dialog.findViewById(R.id.buttonFlee).isEnabled());
        assertTrue(callbacks.defeatCalled);
    }

    @Test
    public void attack_updatesHpBarAndAnimationState() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Ida", PlayerClass.KNIGHT);
        Monster monster = new Monster("Slime", 3, 1, 0, "🟢");

        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
        fragment.setCombatants(profile, monster);
        fragment.show(activity.getSupportFragmentManager(), "combat_animation");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        ProgressBar hpBar = dialog.findViewById(R.id.monsterHpBar);
        assertNotNull(hpBar);
        assertEquals(3, hpBar.getMax());

        dialog.findViewById(R.id.buttonAttack).performClick();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        AnimatedPlayer animatedPlayer = profile.getAnimatedPlayer();
        assertNotNull(animatedPlayer);
        assertEquals("attack", animatedPlayer.getCurrentAction());

        Monster trackedMonster = ReflectionHelpers.getField(fragment, "monster");
        assertNotNull(trackedMonster);
        assertEquals(trackedMonster.getCurrentHP(), hpBar.getProgress());
    }

    @Test
    public void show_initializesAnimationFrames() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Ezra", PlayerClass.THIEF);
        Monster monster = new Monster("Goblin", 5, 3, 1, "👺");

        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
        fragment.setCombatants(profile, monster);
        fragment.show(activity.getSupportFragmentManager(), "combat_animation_init");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        ImageView playerImage = dialog.findViewById(R.id.imagePlayerAnimation);
        ImageView monsterImage = dialog.findViewById(R.id.imageMonsterAnimation);
        assertNotNull(playerImage);
        assertNotNull(monsterImage);

        ReflectionHelpers.callInstanceMethod(fragment, "updateAnimationFrames");
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertNotNull(playerImage.getDrawable());
        assertNotNull(monsterImage.getDrawable());
    }

    @Test
    public void handleAttack_triggersClassSound() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("Bran", PlayerClass.KNIGHT);
        Monster monster = new Monster("Imp", 2, 1, 0, "😈");

        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
        fragment.setCombatants(profile, monster);
        fragment.show(activity.getSupportFragmentManager(), "combat_sound");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        TestPlaybackListener listener = new TestPlaybackListener();
        SoundManager.setTestPlaybackListener(listener);

        ReflectionHelpers.callInstanceMethod(fragment, "handleAttack");
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertEquals("knight_attack", listener.lastKey);
    }

    @Test
    public void restoreFromSavedState_preservesMonsterFamilyAndAffinity() {
        CharacterProfile profile = new CharacterProfile("Nora", PlayerClass.KNIGHT);
        Monster monster = new Monster("Goblin", 5, 3, 1, "dY`A");
        monster.setFamily(MonsterFamily.HUMANOID);
        monster.setAffinity(MonsterAffinity.ARCANE);

        CombatDialogFragment original = CombatDialogFragment.newInstance(monster.getMonsterType());
        original.setCombatants(profile, monster);
        android.os.Bundle savedState = new android.os.Bundle();
        original.onSaveInstanceState(savedState);

        CombatDialogFragment restoredFragment = CombatDialogFragment.newInstance(monster.getMonsterType());
        Monster restored = ReflectionHelpers.callInstanceMethod(restoredFragment, "restoreMonsterFromState",
                ReflectionHelpers.ClassParameter.from(android.os.Bundle.class, savedState));
        assertNotNull(restored);
        assertEquals(MonsterFamily.HUMANOID, restored.getFamily());
        assertEquals(MonsterAffinity.ARCANE, restored.getAffinity());
    }

    @Test
    public void bossIntentIncludesPhaseLabel() {
        FragmentActivity activity = buildThemedActivity();

        CharacterProfile profile = new CharacterProfile("BossTester", PlayerClass.KNIGHT);
        Monster monster = new Monster("Lich", 12, 6, 3, "");
        monster.setBoss(true);
        monster.setBossPhaseCount(3);
        monster.takeDamage(6); // move into later phase

        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
        fragment.setCombatants(profile, monster);
        fragment.show(activity.getSupportFragmentManager(), "combat_boss_phase");

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);

        CharSequence intentText = ((android.widget.TextView) dialog.findViewById(R.id.textMonsterIntent)).getText();
        assertTrue(intentText.toString().contains("Phase"));
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
        public void onMonsterAttack(@NonNull Monster monster) {
        }

        @Override
        public boolean onUseHealingPotionRequested(int healAmount) {
            profile.setCurrentHP(profile.getCurrentHP() + healAmount);
            potionConsumed = true;
            return true;
        }
    }

    private static final class TestPlaybackListener implements SoundManager.PlaybackListener {
        String lastKey;

        @Override
        public void onPlayRequest(String key) {
            lastKey = key;
        }
    }
}
