package com.adaplu.clickdungeon.ui;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;

import com.adaplu.clickdungeon.R;
import com.adaplu.clickdungeon.model.AnimatedMonster;
import com.adaplu.clickdungeon.model.AnimatedPlayer;
import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.MonsterAffinity;
import com.adaplu.clickdungeon.model.MonsterFamily;
import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.util.MonsterAnimationHelper;

/**
 * CombatDialogFragment renders the interactive battle screen.
 * Refactored to minimize Bundle size by passing only primitive identifiers.
 */
public class CombatDialogFragment extends DialogFragment {

    public interface CombatCallbacks {
        void onCombatVictory(@NonNull Monster monster);
        void onCombatDefeat();
        void onCombatFled(int penaltyDamage);
        void onCombatStateUpdated();
        void onMonsterAttack(@NonNull Monster monster);
        boolean onUseHealingPotionRequested(int healAmount);
    }

    // Argument and state keys.
    private static final String ARG_MONSTER_NAME = "arg_monster_name";
    private static final String STATE_MONSTER_NAME = "state_monster_name";
    private static final String STATE_MONSTER_ATTACK = "state_monster_attack";
    private static final String STATE_MONSTER_DEFENSE = "state_monster_defense";
    private static final String STATE_MONSTER_MAX_HP = "state_monster_max_hp";
    private static final String STATE_MONSTER_CURRENT_HP = "state_monster_current_hp";
    private static final String STATE_MONSTER_IMAGE = "state_monster_image";
    private static final String STATE_MONSTER_FAMILY = "state_monster_family";
    private static final String STATE_MONSTER_AFFINITY = "state_monster_affinity";
    private static final String STATE_MONSTER_RANGED = "state_monster_ranged";
    private static final String STATE_MONSTER_BOSS = "state_monster_boss";
    private static final String STATE_MONSTER_BOSS_PHASES = "state_monster_boss_phases";

    public static final int HEALING_POTION_STRENGTH = 6;
    private static final long ANIMATION_FRAME_DELAY_MS = 80L;

    @Nullable private CombatCallbacks callbacks;
    @Nullable private CharacterProfile profile;
    @Nullable private Monster monster;

    // View references.
    private TextView playerStatsView, monsterStatsView, monsterIntentView, combatLogView, combatSummaryView;
    private Button attackButton, potionButton, fleeButton, closeButton;
    private ProgressBar monsterIntentBar, playerHpBar, monsterHpBar;
    private ImageView playerAnimationView, monsterAnimationView;

    private final StringBuilder logBuilder = new StringBuilder();
    private final java.util.Random random = new java.util.Random();
    private MonsterIntent currentIntent;
    private int previewXpReward, previewGoldReward;
    private int totalDamageTaken, totalDamageDealt, turnsTaken, potionsUsed;
    private boolean summaryVisible;

    @Nullable private AnimatedPlayer animatedPlayer;
    @Nullable private AnimatedMonster animatedMonster;
    @Nullable private Handler animationHandler;

    private final Runnable animationTick = new Runnable() {
        @Override
        public void run() {
            updateAnimationFrames();
            if (animationHandler != null) {
                animationHandler.postDelayed(this, ANIMATION_FRAME_DELAY_MS);
            }
        }
    };

    /** Factory method using just the type string to keep arguments lightweight. */
    public static CombatDialogFragment newInstance(@NonNull String monsterType) {
        CombatDialogFragment fragment = new CombatDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MONSTER_NAME, monsterType);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCombatants(@NonNull CharacterProfile profile, @NonNull Monster monster) {
        this.profile = profile;
        this.monster = monster;
    }

    public void setCombatCallbacks(@Nullable CombatCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setRewardPreview(int xpReward, int goldReward) {
        this.previewXpReward = xpReward;
        this.previewGoldReward = goldReward;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (monster == null) return;
        outState.putString(STATE_MONSTER_NAME, monster.getMonsterType());
        outState.putInt(STATE_MONSTER_ATTACK, monster.getAttack());
        outState.putInt(STATE_MONSTER_DEFENSE, monster.getDefense());
        outState.putInt(STATE_MONSTER_MAX_HP, monster.getMaxHP());
        outState.putInt(STATE_MONSTER_CURRENT_HP, monster.getCurrentHP());
        outState.putString(STATE_MONSTER_IMAGE, monster.getImage());
        outState.putString(STATE_MONSTER_FAMILY, monster.getFamily().name());
        outState.putString(STATE_MONSTER_AFFINITY, monster.getAffinity().name());
        outState.putBoolean(STATE_MONSTER_RANGED, monster.hasRangedAttack());
        outState.putBoolean(STATE_MONSTER_BOSS, monster.isBoss());
        outState.putInt(STATE_MONSTER_BOSS_PHASES, monster.getBossPhaseCount());
    }

    @Nullable
    private Monster restoreMonsterFromState(@Nullable Bundle state) {
        if (state == null || !state.containsKey(STATE_MONSTER_NAME)) return null;
        int maxHp = Math.max(1, state.getInt(STATE_MONSTER_MAX_HP, 1));
        Monster restored = new Monster(
                state.getString(STATE_MONSTER_NAME, ""),
                maxHp,
                state.getInt(STATE_MONSTER_ATTACK, 1),
                state.getInt(STATE_MONSTER_DEFENSE, 0),
                state.getString(STATE_MONSTER_IMAGE, ""));
        int currentHp = Math.max(0, Math.min(maxHp, state.getInt(STATE_MONSTER_CURRENT_HP, maxHp)));
        if (currentHp < maxHp) restored.takeDamage(maxHp - currentHp);
        restored.setFamily(parseMonsterFamily(state.getString(STATE_MONSTER_FAMILY)));
        restored.setAffinity(parseMonsterAffinity(state.getString(STATE_MONSTER_AFFINITY)));
        restored.setHasRangedAttack(state.getBoolean(STATE_MONSTER_RANGED, false));
        restored.setBoss(state.getBoolean(STATE_MONSTER_BOSS, false));
        restored.setBossPhaseCount(state.getInt(STATE_MONSTER_BOSS_PHASES, 1));
        return restored;
    }

    private MonsterFamily parseMonsterFamily(@Nullable String value) {
        if (TextUtils.isEmpty(value)) return MonsterFamily.UNKNOWN;
        try {
            return MonsterFamily.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return MonsterFamily.UNKNOWN;
        }
    }

    private MonsterAffinity parseMonsterAffinity(@Nullable String value) {
        if (TextUtils.isEmpty(value)) return MonsterAffinity.NONE;
        try {
            return MonsterAffinity.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return MonsterAffinity.NONE;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View root = getLayoutInflater().inflate(R.layout.dialog_combat, null, false);

        playerStatsView = root.findViewById(R.id.textPlayerStats);
        monsterStatsView = root.findViewById(R.id.textMonsterStats);
        monsterIntentView = root.findViewById(R.id.textMonsterIntent);
        monsterIntentBar = root.findViewById(R.id.progressMonsterIntent);
        combatSummaryView = root.findViewById(R.id.textCombatSummary);
        combatLogView = root.findViewById(R.id.textCombatLog);
        attackButton = root.findViewById(R.id.buttonAttack);
        potionButton = root.findViewById(R.id.buttonUsePotion);
        fleeButton = root.findViewById(R.id.buttonFlee);
        closeButton = root.findViewById(R.id.buttonCloseSummary);
        playerHpBar = root.findViewById(R.id.playerHpBar);
        monsterHpBar = root.findViewById(R.id.monsterHpBar);
        playerAnimationView = root.findViewById(R.id.imagePlayerAnimation);
        monsterAnimationView = root.findViewById(R.id.imageMonsterAnimation);

        if (profile == null || monster == null) {
            dismissAllowingStateLoss();
            return new AlertDialog.Builder(requireContext()).create();
        }

        prepareAnimatedCombatants(profile, monster);
        totalDamageTaken = 0;
        totalDamageDealt = 0;
        turnsTaken = 0;
        potionsUsed = 0;
        summaryVisible = false;
        
        if (combatSummaryView != null) {
            combatSummaryView.setText(getString(R.string.combat_summary_ready));
        }
        if (closeButton != null) {
            closeButton.setVisibility(View.GONE);
            closeButton.setOnClickListener(v -> dismissAllowingStateLoss());
        }
        if (monsterIntentBar != null) {
            monsterIntentBar.setProgress(0);
            monsterIntentBar.setMax(Math.max(1, profile.getMaxHP()));
        }

        appendLog(getString(R.string.combat_log_intro, monster.getMonsterType()));
        refreshStatBlocks();
        rollNextMonsterIntent(true);
        startAnimationLoop();

        attackButton.setOnClickListener(v -> handleAttack());
        potionButton.setOnClickListener(v -> handleUsePotion());
        fleeButton.setOnClickListener(v -> handleFlee());

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.combat_dialog_title)
                .setView(root)
                .create();
    }

    private void handleAttack() {
        if (profile == null || monster == null) return;
        resetSummary();
        turnsTaken++;
        int damageToMonster = Math.max(1, profile.getTotalAttack() - monster.getDefense());
        monster.takeDamage(damageToMonster);
        totalDamageDealt += damageToMonster;
        appendLog(getResources().getQuantityString(R.plurals.combat_log_player_attack, damageToMonster, damageToMonster, monster.getMonsterType()));
        animatePulse(monsterStatsView);
        triggerMonsterAction("defend", false);
        triggerPlayerAction("attack");
        notifyStateChanged();
        refreshStatBlocks();

        if (monster.isDead()) {
            appendLog(getString(R.string.combat_log_monster_defeated, monster.getMonsterType()));
            disableActions();
            showVictorySummary();
            if (callbacks != null) callbacks.onCombatVictory(monster);
            triggerMonsterAction("defend", true);
            return;
        }
        executeMonsterTurn();
    }

    private void handleUsePotion() {
        if (profile == null) return;
        turnsTaken++;
        if (profile.getCurrentHP() >= profile.getMaxHP()) {
            appendLog(getString(R.string.combat_log_potion_full_hp));
            return;
        }
        boolean consumed = callbacks != null && callbacks.onUseHealingPotionRequested(HEALING_POTION_STRENGTH);
        if (consumed) {
            potionsUsed++;
            appendLog(getString(R.string.combat_log_use_potion, HEALING_POTION_STRENGTH));
            notifyStateChanged();
            refreshStatBlocks();
            showTransientSummary(getString(R.string.combat_summary_potion_used, HEALING_POTION_STRENGTH));
            animatePulse(playerStatsView);
            triggerPlayerAction("defend");
        } else {
            appendLog(getString(R.string.combat_log_no_potions));
        }
    }

    private void handleFlee() {
        turnsTaken++;
        int penalty = monster != null ? Math.max(1, monster.getAttack() / 2) : 0;
        if (callbacks != null) callbacks.onCombatFled(penalty);
        if (penalty > 0) totalDamageTaken += penalty;
        disableActions();
        int turns = Math.max(1, turnsTaken);
        String turnsText = getResources().getQuantityString(R.plurals.combat_summary_turns, turns, turns);
        String dealtText = getResources().getQuantityString(R.plurals.combat_summary_damage, totalDamageDealt, totalDamageDealt);
        String penaltyText = getResources().getQuantityString(R.plurals.combat_summary_damage, penalty, penalty);
        showFinalSummary(getString(R.string.combat_summary_flee, turnsText, dealtText, penaltyText));
        triggerPlayerAction("move");
        refreshStatBlocks();
    }

    private void executeMonsterTurn() {
        if (profile == null || monster == null) return;
        if (currentIntent == null) rollNextMonsterIntent(false);
        int damageToPlayer = currentIntent.estimatedDamage;

        if (damageToPlayer > 0) {
            profile.takeDamage(damageToPlayer);
            totalDamageTaken += damageToPlayer;
            appendLog(getResources().getQuantityString(R.plurals.combat_log_monster_attack, damageToPlayer, monster.getMonsterType(), damageToPlayer));
            animatePulse(playerStatsView);
        } else {
            appendLog(getString(R.string.combat_log_monster_glancing, monster.getMonsterType()));
        }
        
        triggerMonsterAction("attack", true);
        if (callbacks != null) callbacks.onMonsterAttack(monster);
        notifyStateChanged();
        refreshStatBlocks();

        if (profile.isDead()) {
            disableActions();
            appendLog(getString(R.string.combat_log_player_defeated));
            showDefeatSummary();
            if (callbacks != null) callbacks.onCombatDefeat();
            return;
        }
        rollNextMonsterIntent(false);
    }

    private void rollNextMonsterIntent(boolean firstTurn) {
        if (monsterIntentView == null || monsterIntentBar == null || monster == null || profile == null) return;
        MonsterIntentType type = MonsterIntentType.randomType(random, monster.hasRangedAttack());
        int baseEstimate = type.estimateDamage(monster, profile);
        int adjustedEstimate = applyBossPhaseScaling(monster, baseEstimate);
        currentIntent = new MonsterIntent(type, adjustedEstimate);
        
        String intentLabel = getString(type.labelRes);
        if (monster.isBoss()) intentLabel = getString(R.string.combat_intent_boss_phase, intentLabel, monster.getBossPhase());
        monsterIntentView.setText(getString(R.string.combat_intent_display, intentLabel, currentIntent.estimatedDamage));
        
        int maxHp = Math.max(1, profile.getMaxHP());
        monsterIntentBar.setMax(maxHp);
        int progress = Math.min(maxHp, Math.max(0, currentIntent.estimatedDamage));
        monsterIntentBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), type.colorRes)));
        
        if (firstTurn) monsterIntentBar.setProgress(progress);
        else {
            ObjectAnimator animator = ObjectAnimator.ofInt(monsterIntentBar, "progress", 0, progress);
            animator.setDuration(400); animator.start();
        }
        if (!summaryVisible) showTransientSummary(getString(R.string.combat_summary_ready));
    }

    private void resetSummary() {
        summaryVisible = false;
        if (closeButton != null) closeButton.setVisibility(View.GONE);
        if (combatSummaryView != null) combatSummaryView.setText(getString(R.string.combat_summary_ready));
    }

    private void showTransientSummary(String text) {
        summaryVisible = false;
        if (closeButton != null) closeButton.setVisibility(View.GONE);
        if (combatSummaryView != null) combatSummaryView.setText(text);
    }

    private void showFinalSummary(String text) {
        summaryVisible = true;
        if (combatSummaryView != null) combatSummaryView.setText(text);
        if (closeButton != null) closeButton.setVisibility(View.VISIBLE);
        if (monsterIntentBar != null) monsterIntentBar.setProgress(0);
    }

    private void showVictorySummary() {
        int xp = Math.max(1, previewXpReward);
        int gold = Math.max(0, previewGoldReward);
        int turns = Math.max(1, turnsTaken);
        String xpText = getResources().getQuantityString(R.plurals.combat_summary_xp, xp, xp);
        String goldText = getResources().getQuantityString(R.plurals.combat_summary_gold, gold, gold);
        String turnsText = getResources().getQuantityString(R.plurals.combat_summary_turns, turns, turns);
        String dealtText = getResources().getQuantityString(R.plurals.combat_summary_damage, totalDamageDealt, totalDamageDealt);
        String takenText = getResources().getQuantityString(R.plurals.combat_summary_damage, totalDamageTaken, totalDamageTaken);
        String potionsText = getResources().getQuantityString(R.plurals.combat_summary_potions, potionsUsed, potionsUsed);
        showFinalSummary(getString(R.string.combat_summary_victory, xpText, goldText, turnsText, dealtText, takenText, potionsText));
    }

    private void showDefeatSummary() {
        int turns = Math.max(1, turnsTaken);
        String turnsText = getResources().getQuantityString(R.plurals.combat_summary_turns, turns, turns);
        String dealtText = getResources().getQuantityString(R.plurals.combat_summary_damage, totalDamageDealt, totalDamageDealt);
        String takenText = getResources().getQuantityString(R.plurals.combat_summary_damage, totalDamageTaken, totalDamageTaken);
        String potionsText = getResources().getQuantityString(R.plurals.combat_summary_potions, potionsUsed, potionsUsed);
        showFinalSummary(getString(R.string.combat_summary_defeat, turnsText, dealtText, takenText, potionsText));
    }

    private void animatePulse(View target) {
        if (target == null) return;
        AlphaAnimation animation = new AlphaAnimation(0.4f, 1f);
        animation.setDuration(220); animation.setRepeatMode(AlphaAnimation.REVERSE); animation.setRepeatCount(1);
        target.startAnimation(animation);
    }

    private static final class MonsterIntent {
        final MonsterIntentType type;
        final int estimatedDamage;
        MonsterIntent(MonsterIntentType type, int estimatedDamage) { this.type = type; this.estimatedDamage = Math.max(0, estimatedDamage); }
    }

    private enum MonsterIntentType {
        QUICK(R.string.combat_intent_quick, 0.75f, 0.0f, R.color.intent_quick),
        GUARD_BREAK(R.string.combat_intent_guard_break, 1.05f, 0.5f, R.color.intent_guard),
        HEAVY(R.string.combat_intent_heavy, 1.35f, 0.15f, R.color.intent_heavy),
        RANGED(R.string.combat_intent_ranged, 0.9f, 0.65f, R.color.intent_ranged);

        final int labelRes, colorRes;
        final float attackMultiplier, defenseBypass;

        MonsterIntentType(int labelRes, float attackMultiplier, float defenseBypass, int colorRes) {
            this.labelRes = labelRes; this.attackMultiplier = attackMultiplier; this.defenseBypass = defenseBypass; this.colorRes = colorRes;
        }

        int estimateDamage(@NonNull Monster monster, @NonNull CharacterProfile profile) {
            int baseAttack = Math.max(1, Math.round(monster.getAttack() * attackMultiplier));
            int defense = profile.getTotalDefense();
            int mitigatedDefense = defense - Math.round(defense * defenseBypass);
            return Math.max(0, baseAttack - Math.max(0, mitigatedDefense));
        }

        static MonsterIntentType randomType(java.util.Random random, boolean allowRanged) {
            int roll = random.nextInt(100);
            if (allowRanged && roll < 20) return RANGED;
            else if (roll < 45) return QUICK;
            else if (roll < 80) return GUARD_BREAK;
            return HEAVY;
        }
    }

    private int applyBossPhaseScaling(Monster monster, int damage) {
        if (monster == null || !monster.isBoss()) return damage;
        int phase = monster.getBossPhase();
        if (phase <= 1) return damage;
        return Math.max(0, Math.round(damage * (1f + (0.15f * (phase - 1)))));
    }

    private void refreshStatBlocks() {
        if (profile == null || monster == null) return;
        playerStatsView.setText(getString(R.string.combat_player_stats, profile.getName(), profile.getCurrentHP(), profile.getMaxHP(), profile.getTotalAttack(), profile.getTotalDefense()));
        String baseName = monster.getMonsterType();
        if (monster.isBoss()) baseName = getString(R.string.combat_boss_label, baseName);
        monsterStatsView.setText(getString(R.string.combat_monster_stats, (TextUtils.isEmpty(monster.getImage()) ? "" : monster.getImage() + " ") + baseName, monster.getCurrentHP(), monster.getMaxHP(), monster.getAttack(), monster.getDefense()));
        combatLogView.setText(logBuilder.toString());
        updateHpMeters();
    }

    private void appendLog(String line) {
        if (logBuilder.length() > 0) logBuilder.append('\n');
        logBuilder.append(line);
        if (combatLogView != null) combatLogView.setText(logBuilder.toString());
    }

    private void disableActions() { attackButton.setEnabled(false); potionButton.setEnabled(false); fleeButton.setEnabled(false); }
    private void notifyStateChanged() { if (callbacks != null) callbacks.onCombatStateUpdated(); }

    @Override public void onAttach(@NonNull Context context) { super.onAttach(context); if (context instanceof CombatCallbacks) callbacks = (CombatCallbacks) context; }
    @Override public void onDetach() { super.onDetach(); callbacks = null; }
    @Override public void onDestroyView() { stopAnimationLoop(); animationHandler = null; animatedPlayer = null; animatedMonster = null; super.onDestroyView(); }

    private void prepareAnimatedCombatants(@NonNull CharacterProfile profileData, @NonNull Monster monsterData) {
        if (profileData.getAnimatedPlayer() == null) {
            profileData.setAnimatedPlayer(new AnimatedPlayer(requireContext().getApplicationContext(), profileData.getPlayerClass(), 64, 64, 4, 120L));
        } else profileData.getAnimatedPlayer().reset();
        animatedPlayer = profileData.getAnimatedPlayer();
        animatedMonster = (monsterData instanceof AnimatedMonster) ? (AnimatedMonster) monsterData : MonsterAnimationHelper.createAnimatedClone(requireContext(), monsterData);
    }

    private void startAnimationLoop() {
        if (animationHandler == null) animationHandler = new Handler(Looper.getMainLooper());
        animationHandler.removeCallbacks(animationTick); updateAnimationFrames(); animationHandler.post(animationTick);
    }

    private void stopAnimationLoop() { if (animationHandler != null) animationHandler.removeCallbacks(animationTick); }

    private void updateAnimationFrames() {
        if (playerAnimationView != null && animatedPlayer != null) playerAnimationView.setImageBitmap(animatedPlayer.getCurrentFrame());
        if (monsterAnimationView != null && animatedMonster != null) monsterAnimationView.setImageBitmap(animatedMonster.getCurrentFrame());
    }

    private void updateHpMeters() {
        if (playerHpBar != null && profile != null) { playerHpBar.setMax(profile.getMaxHP()); playerHpBar.setProgress(profile.getCurrentHP()); }
        if (monsterHpBar != null && monster != null) { monsterHpBar.setMax(monster.getMaxHP()); monsterHpBar.setProgress(monster.getCurrentHP()); }
    }

    private void triggerPlayerAction(@NonNull String action) { if (animatedPlayer != null) animatedPlayer.setAction(action); }
    private void triggerMonsterAction(@NonNull String action, boolean playSound) { if (animatedMonster != null) animatedMonster.setAction(action, getContext(), playSound); }
}
