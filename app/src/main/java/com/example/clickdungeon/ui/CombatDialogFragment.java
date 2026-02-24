package com.example.clickdungeon.ui;

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

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.AnimatedMonster;
import com.example.clickdungeon.model.AnimatedPlayer;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.MonsterAffinity;
import com.example.clickdungeon.model.MonsterFamily;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.util.MonsterAnimationHelper;

/**
 * CombatDialogFragment renders the interactive battle screen.
 * It manages turn-based logic, enemy intent telegraphs, combat animations, and logging.
 * Communication with the game state is handled via the {@link CombatCallbacks} interface.
 */
public class CombatDialogFragment extends DialogFragment {

    /** Callbacks to notify the host Activity of combat results and player actions. */
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
    private static final String STATE_MONSTER_RANGED = "state_monster_ranged";
    private static final String STATE_MONSTER_FAMILY = "state_monster_family";
    private static final String STATE_MONSTER_AFFINITY = "state_monster_affinity";
    private static final String STATE_MONSTER_BOSS = "state_monster_boss";
    private static final String STATE_MONSTER_BOSS_PHASES = "state_monster_boss_phases";
    private static final String ARG_XP_REWARD = "arg_xp_reward";
    private static final String ARG_GOLD_REWARD = "arg_gold_reward";

    private static final int HEALING_POTION_STRENGTH = 6;
    private static final long ANIMATION_FRAME_DELAY_MS = 80L;
    private static final int PLAYER_FRAME_WIDTH = 64;
    private static final int PLAYER_FRAME_HEIGHT = 64;
    private static final int PLAYER_FRAME_COUNT = 4;
    private static final long PLAYER_FRAME_DURATION_MS = 120L;

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

    /** Animation tick runnable to update sprite frames. */
    private final Runnable animationTick = new Runnable() {
        @Override
        public void run() {
            updateAnimationFrames();
            if (animationHandler != null) {
                animationHandler.postDelayed(this, ANIMATION_FRAME_DELAY_MS);
            }
        }
    };

    /** Factory method to create a new instance using the monster type string. */
    public static CombatDialogFragment newInstance(@NonNull String monsterType) {
        CombatDialogFragment fragment = new CombatDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MONSTER_NAME, monsterType);
        fragment.setArguments(args);
        return fragment;
    }

    /** Sets the combatants for the session. */
    public void setCombatants(@NonNull CharacterProfile profile, @NonNull Monster monster) {
        this.profile = profile;
        this.monster = monster;
    }

    /** Sets the callbacks for reporting combat events. */
    public void setCombatCallbacks(@Nullable CombatCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    /** Previews rewards displayed in the victory summary. */
    public void setRewardPreview(int xpReward, int goldReward) {
        this.previewXpReward = xpReward;
        this.previewGoldReward = goldReward;
        Bundle args = getArguments();
        if (args != null) {
            args.putInt(ARG_XP_REWARD, xpReward);
            args.putInt(ARG_GOLD_REWARD, goldReward);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false); // Force player to resolve the encounter.
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View root = getLayoutInflater().inflate(R.layout.dialog_combat, null, false);

        // Bind all UI components.
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

        Monster monsterData = monster;
        CharacterProfile profileData = profile;

        // Restore monster data if the fragment was recreated (e.g., rotation).
        Bundle args = getArguments();
        if (monsterData == null) {
            if (savedInstanceState != null && savedInstanceState.containsKey(STATE_MONSTER_NAME)) {
                monsterData = restoreMonsterFromState(savedInstanceState);
            } else if (args != null) {
                String name = args.getString(ARG_MONSTER_NAME, "");
                monsterData = buildFallbackMonster(name);
            }
            this.monster = monsterData;
        }

        if (profileData == null || monsterData == null) {
            dismissAllowingStateLoss();
            return new AlertDialog.Builder(requireContext()).create();
        }

        // Initialize animation sprite controllers.
        prepareAnimatedCombatants(profileData, monsterData);

        // Restore reward metadata.
        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            if (previewXpReward == 0) {
                previewXpReward = argsBundle.getInt(ARG_XP_REWARD, previewXpReward);
            }
            if (previewGoldReward == 0) {
                previewGoldReward = argsBundle.getInt(ARG_GOLD_REWARD, previewGoldReward);
            }
        }

        // Initialize session counters.
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
            monsterIntentBar.setMax(Math.max(1, profileData.getMaxHP()));
        }

        // Initial setup of stats and first monster move.
        appendLog(getString(R.string.combat_log_intro, monsterData.getMonsterType()));
        refreshStatBlocks();
        rollNextMonsterIntent(true);
        startAnimationLoop();

        // Wire button listeners.
        attackButton.setOnClickListener(v -> handleAttack());
        potionButton.setOnClickListener(v -> handleUsePotion());
        fleeButton.setOnClickListener(v -> handleFlee());

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.combat_dialog_title)
                .setView(root)
                .create();
    }

    /** Executes a player attack turn. */
    private void handleAttack() {
        if (profile == null || monster == null) {
            return;
        }

        resetSummary();
        turnsTaken++;
        
        // Calculate and apply damage to monster.
        int damageToMonster = Math.max(1, profile.getTotalAttack() - monster.getDefense());
        monster.takeDamage(damageToMonster);
        totalDamageDealt += damageToMonster;
        
        appendLog(getResources().getQuantityString(
                R.plurals.combat_log_player_attack, damageToMonster, damageToMonster, monster.getMonsterType()));
        
        animatePulse(monsterStatsView);
        triggerMonsterAction("defend", false);
        triggerPlayerAction("attack");

        notifyStateChanged();
        refreshStatBlocks();

        // Check for victory.
        if (monster.isDead()) {
            appendLog(getString(R.string.combat_log_monster_defeated, monster.getMonsterType()));
            disableActions();
            showVictorySummary();
            if (callbacks != null) {
                callbacks.onCombatVictory(monster);
            }
            triggerMonsterAction("defend");
            return;
        }

        // Monster takes its turn if it survived.
        executeMonsterTurn();
    }

    /** Uses a healing potion if available. */
    private void handleUsePotion() {
        if (profile == null) {
            return;
        }

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

    /** Attempts to escape the battle with a damage penalty. */
    private void handleFlee() {
        turnsTaken++;
        int penalty = monster != null ? Math.max(1, monster.getAttack() / 2) : 0;
        if (callbacks != null) {
            callbacks.onCombatFled(penalty);
        }
        if (penalty > 0) {
            totalDamageTaken += penalty;
        }
        disableActions();
        
        int turns = Math.max(1, turnsTaken);
        int dealt = Math.max(0, totalDamageDealt);
        String turnsText = getResources().getQuantityString(R.plurals.combat_summary_turns, turns, turns);
        String dealtText = getResources().getQuantityString(R.plurals.combat_summary_damage, dealt, dealt);
        String takenText = getResources().getQuantityString(R.plurals.combat_summary_damage, penalty, penalty);
        showFinalSummary(getString(R.string.combat_summary_flee, turnsText, dealtText, takenText));
        
        triggerPlayerAction("move");
        refreshStatBlocks();
    }

    /** Executes the monster's attack based on telegraphed intent. */
    private void executeMonsterTurn() {
        if (profile == null || monster == null) {
            return;
        }

        if (currentIntent == null) {
            rollNextMonsterIntent(false);
        }

        int damageToPlayer = currentIntent != null
                ? currentIntent.estimatedDamage
                : Math.max(0, monster.getAttack() - profile.getTotalDefense());

        if (damageToPlayer > 0) {
            profile.takeDamage(damageToPlayer);
            totalDamageTaken += damageToPlayer;
            appendLog(getResources().getQuantityString(
                    R.plurals.combat_log_monster_attack, damageToPlayer, monster.getMonsterType(), damageToPlayer));
            animatePulse(playerStatsView);
        } else {
            appendLog(getString(R.string.combat_log_monster_glancing, monster.getMonsterType()));
        }
        
        triggerMonsterAction("attack");
        if (callbacks != null) {
            callbacks.onMonsterAttack(monster);
        }

        notifyStateChanged();
        refreshStatBlocks();

        // Check for player defeat.
        if (profile.isDead()) {
            disableActions();
            appendLog(getString(R.string.combat_log_player_defeated));
            showDefeatSummary();
            if (callbacks != null) {
                callbacks.onCombatDefeat();
            }
            return;
        }

        // Telegraph the next monster move.
        rollNextMonsterIntent(false);
    }

    /** Randomly selects and telegraphs the monster's next move. */
    private void rollNextMonsterIntent(boolean firstTurn) {
        if (monsterIntentView == null || monsterIntentBar == null || monster == null || profile == null) {
            return;
        }
        MonsterIntentType type = MonsterIntentType.randomType(random,
                monster != null && monster.hasRangedAttack());
        int baseEstimate = type.estimateDamage(monster, profile);
        int adjustedEstimate = applyBossPhaseScaling(monster, baseEstimate);
        currentIntent = new MonsterIntent(type, adjustedEstimate);
        
        String intentLabel = getString(type.labelRes);
        if (monster.isBoss()) {
            intentLabel = getString(R.string.combat_intent_boss_phase, intentLabel, monster.getBossPhase());
        }
        monsterIntentView.setText(getString(R.string.combat_intent_display,
                intentLabel, currentIntent.estimatedDamage));
        
        int maxHp = Math.max(1, profile.getMaxHP());
        monsterIntentBar.setMax(maxHp);
        int progress = Math.min(maxHp, Math.max(0, currentIntent.estimatedDamage));
        
        monsterIntentBar.setProgressTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), type.colorRes)));
        
        if (firstTurn) {
            monsterIntentBar.setProgress(progress);
        } else {
            // Animate the bar change for better UX.
            ObjectAnimator animator = ObjectAnimator.ofInt(monsterIntentBar, "progress", 0, progress);
            animator.setDuration(400);
            animator.start();
        }
        
        if (!summaryVisible) {
            showTransientSummary(getString(R.string.combat_summary_ready));
        }
    }

    /** Clears final summary state. */
    private void resetSummary() {
        summaryVisible = false;
        if (closeButton != null) {
            closeButton.setVisibility(View.GONE);
        }
        if (combatSummaryView != null) {
            combatSummaryView.setText(getString(R.string.combat_summary_ready));
        }
    }

    /** Shows a non-final combat status message. */
    private void showTransientSummary(String text) {
        summaryVisible = false;
        if (closeButton != null) {
            closeButton.setVisibility(View.GONE);
        }
        if (combatSummaryView != null) {
            combatSummaryView.setText(text);
        }
    }

    /** Shows the final battle results and enables the close button. */
    private void showFinalSummary(String text) {
        summaryVisible = true;
        if (combatSummaryView != null) {
            combatSummaryView.setText(text);
        }
        if (closeButton != null) {
            closeButton.setVisibility(View.VISIBLE);
        }
        if (monsterIntentBar != null) {
            monsterIntentBar.setProgress(0);
        }
    }

    /** Builds the victory data block. */
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
        
        showFinalSummary(getString(R.string.combat_summary_victory,
                xpText, goldText, turnsText, dealtText, takenText, potionsText));
    }

    /** Builds the defeat data block. */
    private void showDefeatSummary() {
        int turns = Math.max(1, turnsTaken);
        int dealt = Math.max(0, totalDamageDealt);
        String turnsText = getResources().getQuantityString(R.plurals.combat_summary_turns, turns, turns);
        String dealtText = getResources().getQuantityString(R.plurals.combat_summary_damage, dealt, dealt);
        String takenText = getResources().getQuantityString(R.plurals.combat_summary_damage, totalDamageTaken, totalDamageTaken);
        String potionsText = getResources().getQuantityString(R.plurals.combat_summary_potions, potionsUsed, potionsUsed);
        
        showFinalSummary(getString(R.string.combat_summary_defeat,
                turnsText, dealtText, takenText, potionsText));
    }

    /** Pulsing effect for taken damage or healing. */
    private void animatePulse(View target) {
        if (target == null) {
            return;
        }
        AlphaAnimation animation = new AlphaAnimation(0.4f, 1f);
        animation.setDuration(220);
        animation.setRepeatMode(AlphaAnimation.REVERSE);
        animation.setRepeatCount(1);
        target.startAnimation(animation);
    }

    /** Data model for telegraphed monster moves. */
    private static final class MonsterIntent {
        final MonsterIntentType type;
        final int estimatedDamage;

        MonsterIntent(MonsterIntentType type, int estimatedDamage) {
            this.type = type;
            this.estimatedDamage = Math.max(0, estimatedDamage);
        }
    }

    /** Enumeration of possible monster move archetypes. */
    private enum MonsterIntentType {
        QUICK(R.string.combat_intent_quick, 0.75f, 0.0f, R.color.intent_quick),
        GUARD_BREAK(R.string.combat_intent_guard_break, 1.05f, 0.5f, R.color.intent_guard),
        HEAVY(R.string.combat_intent_heavy, 1.35f, 0.15f, R.color.intent_heavy),
        RANGED(R.string.combat_intent_ranged, 0.9f, 0.65f, R.color.intent_ranged);

        final int labelRes;
        final float attackMultiplier;
        final float defenseBypass;
        final int colorRes;

        MonsterIntentType(int labelRes, float attackMultiplier, float defenseBypass, int colorRes) {
            this.labelRes = labelRes;
            this.attackMultiplier = attackMultiplier;
            this.defenseBypass = defenseBypass;
            this.colorRes = colorRes;
        }

        int estimateDamage(@NonNull Monster monster, @NonNull CharacterProfile profile) {
            int baseAttack = Math.max(1, Math.round(monster.getAttack() * attackMultiplier));
            int defense = profile.getTotalDefense();
            int mitigatedDefense = defense - Math.round(defense * defenseBypass);
            int damage = baseAttack - Math.max(0, mitigatedDefense);
            return Math.max(0, damage);
        }

        static MonsterIntentType randomType(java.util.Random random, boolean allowRanged) {
            int roll = random.nextInt(100);
            if (allowRanged && roll < 20) {
                return RANGED;
            } else if (roll < 45) {
                return QUICK;
            } else if (roll < 80) {
                return GUARD_BREAK;
            }
            return HEAVY;
        }
    }

    private int applyBossPhaseScaling(Monster monster, int damage) {
        if (monster == null || !monster.isBoss()) {
            return damage;
        }
        int phase = monster.getBossPhase();
        if (phase <= 1) {
            return damage;
        }
        float scale = 1f + (0.15f * (phase - 1));
        return Math.max(0, Math.round(damage * scale));
    }

    /** Refreshes the text blocks showing combatant status. */
    private void refreshStatBlocks() {
        if (profile == null || monster == null) {
            return;
        }
        String playerStats = getString(R.string.combat_player_stats,
                profile.getName(),
                profile.getCurrentHP(),
                profile.getMaxHP(),
                profile.getTotalAttack(),
                profile.getTotalDefense());

        String monsterLabel;
        String image = monster.getImage();
        String baseName = monster.getMonsterType();
        if (monster.isBoss()) {
            baseName = getString(R.string.combat_boss_label, baseName);
        }
        if (!TextUtils.isEmpty(image)) {
            monsterLabel = image + " " + baseName;
        } else {
            monsterLabel = baseName;
        }

        String monsterStats = getString(R.string.combat_monster_stats,
                monsterLabel,
                monster.getCurrentHP(),
                monster.getMaxHP(),
                monster.getAttack(),
                monster.getDefense());

        playerStatsView.setText(playerStats);
        monsterStatsView.setText(monsterStats);
        combatLogView.setText(logBuilder.toString());
        updateHpMeters();
    }

    /** Adds a line to the scrolling battle log. */
    private void appendLog(String line) {
        if (logBuilder.length() > 0) {
            logBuilder.append('\n');
        }
        logBuilder.append(line);
        if (combatLogView != null) {
            combatLogView.setText(logBuilder.toString());
        }
    }

    /** Disables interaction buttons when combat ends. */
    private void disableActions() {
        attackButton.setEnabled(false);
        potionButton.setEnabled(false);
        fleeButton.setEnabled(false);
    }

    /** Notifies the activity that vitals have changed. */
    private void notifyStateChanged() {
        if (callbacks != null) {
            callbacks.onCombatStateUpdated();
        }
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof CombatCallbacks) {
            callbacks = (CombatCallbacks) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onDestroyView() {
        stopAnimationLoop();
        animationHandler = null;
        animatedPlayer = null;
        animatedMonster = null;
        playerAnimationView = null;
        monsterAnimationView = null;
        playerHpBar = null;
        monsterHpBar = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (monster == null) {
            return;
        }
        // Save monster state to survive configuration changes.
        outState.putString(STATE_MONSTER_NAME, monster.getMonsterType());
        outState.putInt(STATE_MONSTER_ATTACK, monster.getAttack());
        outState.putInt(STATE_MONSTER_DEFENSE, monster.getDefense());
        outState.putInt(STATE_MONSTER_MAX_HP, monster.getMaxHP());
        outState.putInt(STATE_MONSTER_CURRENT_HP, monster.getCurrentHP());
        outState.putString(STATE_MONSTER_IMAGE, monster.getImage());
        outState.putBoolean(STATE_MONSTER_RANGED, monster.hasRangedAttack());
        outState.putString(STATE_MONSTER_FAMILY, monster.getFamily().name());
        outState.putString(STATE_MONSTER_AFFINITY, monster.getAffinity().name());
        outState.putBoolean(STATE_MONSTER_BOSS, monster.isBoss());
        outState.putInt(STATE_MONSTER_BOSS_PHASES, monster.getBossPhaseCount());
    }

    /** Restores monster state from a saved bundle. */
    @Nullable
    private Monster restoreMonsterFromState(@NonNull Bundle state) {
        String name = state.getString(STATE_MONSTER_NAME, "");
        int maxHp = state.getInt(STATE_MONSTER_MAX_HP, 1);
        int attack = state.getInt(STATE_MONSTER_ATTACK, 1);
        int defense = state.getInt(STATE_MONSTER_DEFENSE, 0);
        String image = state.getString(STATE_MONSTER_IMAGE, "");
        boolean ranged = state.getBoolean(STATE_MONSTER_RANGED, false);
        boolean isBoss = state.getBoolean(STATE_MONSTER_BOSS, false);
        int bossPhases = state.getInt(STATE_MONSTER_BOSS_PHASES, 1);
        
        Monster restored = new Monster(name, maxHp, attack, defense, image);
        restored.setHasRangedAttack(ranged);
        restored.setFamily(MonsterFamily.fromName(state.getString(STATE_MONSTER_FAMILY, MonsterFamily.UNKNOWN.name())));
        restored.setAffinity(MonsterAffinity.fromName(state.getString(STATE_MONSTER_AFFINITY, MonsterAffinity.NONE.name())));
        restored.setBoss(isBoss);
        restored.setBossPhaseCount(bossPhases);
        
        int currentHp = state.getInt(STATE_MONSTER_CURRENT_HP, maxHp);
        while (restored.getCurrentHP() > currentHp) {
            restored.takeDamage(1);
        }
        return restored;
    }

    /** Recreates a monster from the type name if full state is missing. */
    @Nullable
    private Monster buildFallbackMonster(@Nullable String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        try {
            return com.example.clickdungeon.model.MonsterFactory.create(requireContext(), name);
        } catch (Exception ignored) {
            return new Monster(name, 1, 1, 0, "");
        }
    }

    /** Configures the sprite controllers for battle animations. */
    private void prepareAnimatedCombatants(@NonNull CharacterProfile profileData, @NonNull Monster monsterData) {
        ensureAnimatedPlayer(profileData);
        animatedPlayer = profileData.getAnimatedPlayer();
        if (playerAnimationView != null && animatedPlayer == null) {
            playerAnimationView.setImageResource(getPlayerPlaceholderSpriteSheet(profileData.getPlayerClass()));
        }

        if (monsterData instanceof AnimatedMonster) {
            animatedMonster = (AnimatedMonster) monsterData;
        } else {
            animatedMonster = MonsterAnimationHelper.createAnimatedClone(requireContext(), monsterData);
        }

        if (animatedMonster != null) {
            this.monster = animatedMonster;
        }
    }

    /** Ensures the player sprite controller is ready. */
    private void ensureAnimatedPlayer(@NonNull CharacterProfile profileData) {
        if (profileData.getAnimatedPlayer() == null) {
            profileData.setAnimatedPlayer(new AnimatedPlayer(
                    requireContext().getApplicationContext(),
                    profileData.getPlayerClass(),
                    PLAYER_FRAME_WIDTH,
                    PLAYER_FRAME_HEIGHT,
                    PLAYER_FRAME_COUNT,
                    PLAYER_FRAME_DURATION_MS));
        } else {
            profileData.getAnimatedPlayer().reset();
        }
    }

    private void startAnimationLoop() {
        if (animationHandler == null) {
            animationHandler = new Handler(Looper.getMainLooper());
        }
        animationHandler.removeCallbacks(animationTick);
        updateAnimationFrames();
        animationHandler.post(animationTick);
    }

    private void stopAnimationLoop() {
        if (animationHandler != null) {
            animationHandler.removeCallbacks(animationTick);
        }
    }

    /** Refreshes sprite bitmaps on the animation views. */
    private void updateAnimationFrames() {
        if (playerAnimationView != null && animatedPlayer != null) {
            Bitmap frame = animatedPlayer.getCurrentFrame();
            if (frame != null) {
                playerAnimationView.setImageBitmap(frame);
            }
        }
        if (monsterAnimationView != null && animatedMonster != null) {
            Bitmap frame = animatedMonster.getCurrentFrame();
            if (frame != null) {
                monsterAnimationView.setImageBitmap(frame);
            }
        }
    }

    /** Updates the HP progress bars. */
    private void updateHpMeters() {
        if (playerHpBar != null && profile != null) {
            playerHpBar.setMax(Math.max(1, profile.getMaxHP()));
            playerHpBar.setProgress(Math.max(0, profile.getCurrentHP()));
        }
        if (monsterHpBar != null && monster != null) {
            monsterHpBar.setMax(Math.max(1, monster.getMaxHP()));
            monsterHpBar.setProgress(Math.max(0, monster.getCurrentHP()));
        }
    }

    private void triggerPlayerAction(@NonNull String action) {
        if (animatedPlayer != null) {
            animatedPlayer.setAction(action);
        }
    }

    private void triggerMonsterAction(@NonNull String action) {
        triggerMonsterAction(action, true);
    }

    private void triggerMonsterAction(@NonNull String action, boolean playSound) {
        if (animatedMonster == null) {
            return;
        }
        Context context = getContext();
        if (context != null) {
            animatedMonster.setAction(action, context, playSound);
        }
    }

    /** Returns a sprite-sheet placeholder if animations are unavailable. */
    private int getPlayerPlaceholderSpriteSheet(@Nullable PlayerClass playerClass) {
        if (playerClass == null) {
            return R.drawable.knight_sprite_sheet;
        }
        switch (playerClass) {
            case WIZARD: return R.drawable.wizard_sprite_sheet;
            case THIEF: return R.drawable.thief_sprite_sheet;
            case KNIGHT:
            default: return R.drawable.knight_sprite_sheet;
        }
    }
}
