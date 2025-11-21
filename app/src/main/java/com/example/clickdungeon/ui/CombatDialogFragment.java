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
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.util.MonsterAnimationHelper;

/**
 * Dialog fragment that renders an interactive combat encounter between the player's character
 * and a monster. The fragment delegates persistence and inventory changes back to the hosting
 * activity via the {@link CombatCallbacks} interface.
 */
public class CombatDialogFragment extends DialogFragment {

    public interface CombatCallbacks {
        void onCombatVictory(@NonNull Monster monster);

        void onCombatDefeat();

        void onCombatFled(int penaltyDamage);

        void onCombatStateUpdated();

        boolean onUseHealingPotionRequested(int healAmount);
    }

    private static final String ARG_MONSTER_NAME = "arg_monster_name";
    private static final String ARG_MONSTER_ATTACK = "arg_monster_attack";
    private static final String ARG_MONSTER_DEFENSE = "arg_monster_defense";
    private static final String ARG_MONSTER_MAX_HP = "arg_monster_max_hp";
    private static final String ARG_MONSTER_CURRENT_HP = "arg_monster_current_hp";
    private static final String ARG_MONSTER_IMAGE = "arg_monster_image";
    private static final String ARG_XP_REWARD = "arg_xp_reward";
    private static final String ARG_GOLD_REWARD = "arg_gold_reward";

    private static final int HEALING_POTION_STRENGTH = 6;
    private static final long ANIMATION_FRAME_DELAY_MS = 80L;
    private static final int PLAYER_FRAME_WIDTH = 64;
    private static final int PLAYER_FRAME_HEIGHT = 64;
    private static final int PLAYER_FRAME_COUNT = 4;
    private static final long PLAYER_FRAME_DURATION_MS = 120L;

    @Nullable
    private CombatCallbacks callbacks;
    @Nullable
    private CharacterProfile profile;
    @Nullable
    private Monster monster;

    private TextView playerStatsView;
    private TextView monsterStatsView;
    private TextView monsterIntentView;
    private TextView combatLogView;
    private TextView combatSummaryView;
    private Button attackButton;
    private Button potionButton;
    private Button fleeButton;
    private Button closeButton;
    private ProgressBar monsterIntentBar;
    private ProgressBar playerHpBar;
    private ProgressBar monsterHpBar;
    private ImageView playerAnimationView;
    private ImageView monsterAnimationView;

    private final StringBuilder logBuilder = new StringBuilder();
    private final java.util.Random random = new java.util.Random();
    private MonsterIntent currentIntent;
    private int previewXpReward;
    private int previewGoldReward;
    private int totalDamageTaken;
    private int totalDamageDealt;
    private int turnsTaken;
    private int potionsUsed;
    private boolean summaryVisible;
    @Nullable
    private AnimatedPlayer animatedPlayer;
    @Nullable
    private AnimatedMonster animatedMonster;
    @Nullable
    private Handler animationHandler;
    private final Runnable animationTick = new Runnable() {
        @Override
        public void run() {
            updateAnimationFrames();
            if (animationHandler != null) {
                animationHandler.postDelayed(this, ANIMATION_FRAME_DELAY_MS);
            }
        }
    };

    public static CombatDialogFragment newInstance(@NonNull Monster monster) {
        CombatDialogFragment fragment = new CombatDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MONSTER_NAME, monster.getMonsterType());
        args.putInt(ARG_MONSTER_ATTACK, monster.getAttack());
        args.putInt(ARG_MONSTER_DEFENSE, monster.getDefense());
        args.putInt(ARG_MONSTER_MAX_HP, monster.getMaxHP());
        args.putInt(ARG_MONSTER_CURRENT_HP, monster.getCurrentHP());
        args.putString(ARG_MONSTER_IMAGE, monster.getImage());
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
        Bundle args = getArguments();
        if (args != null) {
            args.putInt(ARG_XP_REWARD, xpReward);
            args.putInt(ARG_GOLD_REWARD, goldReward);
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
        View root = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_combat, null, false);

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

        Bundle args = getArguments();
        if (monsterData == null && args != null) {
            String name = args.getString(ARG_MONSTER_NAME, "");
            int maxHp = args.getInt(ARG_MONSTER_MAX_HP, 1);
            int attack = args.getInt(ARG_MONSTER_ATTACK, 1);
            int defense = args.getInt(ARG_MONSTER_DEFENSE, 0);
            String image = args.getString(ARG_MONSTER_IMAGE, "");
            monsterData = new Monster(name, maxHp, attack, defense, image);
            int currentHp = args.getInt(ARG_MONSTER_CURRENT_HP, maxHp);
            while (monsterData.getCurrentHP() > currentHp) {
                monsterData.takeDamage(1);
            }
            this.monster = monsterData;
        }

        if (profileData == null || monsterData == null) {
            dismissAllowingStateLoss();
            return new AlertDialog.Builder(requireContext()).create();
        }

        prepareAnimatedCombatants(profileData, monsterData);

        Bundle argsBundle = getArguments();
        if (argsBundle != null) {
            if (previewXpReward == 0) {
                previewXpReward = argsBundle.getInt(ARG_XP_REWARD, previewXpReward);
            }
            if (previewGoldReward == 0) {
                previewGoldReward = argsBundle.getInt(ARG_GOLD_REWARD, previewGoldReward);
            }
        }

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

        appendLog(getString(R.string.combat_log_intro, monsterData.getMonsterType()));
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
        if (profile == null || monster == null) {
            return;
        }

        resetSummary();

        turnsTaken++;
        int damageToMonster = Math.max(1, profile.getAttack() - monster.getDefense());
        monster.takeDamage(damageToMonster);
        totalDamageDealt += damageToMonster;
        appendLog(getString(R.string.combat_log_player_attack, damageToMonster, monster.getMonsterType()));
        animatePulse(monsterStatsView);
        triggerMonsterAction("defend", false);
        triggerPlayerAction("attack");

        notifyStateChanged();
        refreshStatBlocks();

        if (monster.isDead()) {
            appendLog(getString(R.string.combat_log_monster_defeated, monster.getMonsterType()));
            disableActions();
            showFinalSummary(getString(R.string.combat_summary_victory,
                    Math.max(1, previewXpReward),
                    Math.max(0, previewGoldReward),
                    Math.max(1, turnsTaken),
                    totalDamageDealt,
                    totalDamageTaken,
                    potionsUsed));
            if (callbacks != null) {
                callbacks.onCombatVictory(monster);
            }
            triggerMonsterAction("defend");
            return;
        }

        executeMonsterTurn();
    }

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
        showFinalSummary(getString(R.string.combat_summary_flee,
                Math.max(1, turnsTaken),
                Math.max(0, totalDamageDealt),
                penalty));
        triggerPlayerAction("move");
        refreshStatBlocks();
    }

    private void executeMonsterTurn() {
        if (profile == null || monster == null) {
            return;
        }

        if (currentIntent == null) {
            rollNextMonsterIntent(false);
        }

        int damageToPlayer = currentIntent != null
                ? currentIntent.estimatedDamage
                : Math.max(0, monster.getAttack() - profile.getDefense());

        if (damageToPlayer > 0) {
            profile.takeDamage(damageToPlayer);
            totalDamageTaken += damageToPlayer;
            appendLog(getString(R.string.combat_log_monster_attack, monster.getMonsterType(), damageToPlayer));
            animatePulse(playerStatsView);
        } else {
            appendLog(getString(R.string.combat_log_monster_glancing, monster.getMonsterType()));
        }
        triggerMonsterAction("attack");

        notifyStateChanged();
        refreshStatBlocks();

        if (profile.isDead()) {
            disableActions();
            appendLog(getString(R.string.combat_log_player_defeated));
            showFinalSummary(getString(R.string.combat_summary_defeat,
                    Math.max(1, turnsTaken),
                    Math.max(0, totalDamageDealt),
                    totalDamageTaken,
                    potionsUsed));
            if (callbacks != null) {
                callbacks.onCombatDefeat();
            }
            return;
        }

        rollNextMonsterIntent(false);
    }

    private void rollNextMonsterIntent(boolean firstTurn) {
        if (monsterIntentView == null || monsterIntentBar == null || monster == null || profile == null) {
            return;
        }
        MonsterIntentType type = MonsterIntentType.randomType(random);
        currentIntent = new MonsterIntent(type, type.estimateDamage(monster, profile));
        monsterIntentView.setText(getString(R.string.combat_intent_display,
                getString(type.labelRes), currentIntent.estimatedDamage));
        int maxHp = Math.max(1, profile.getMaxHP());
        monsterIntentBar.setMax(maxHp);
        int progress = Math.min(maxHp, Math.max(0, currentIntent.estimatedDamage));
        monsterIntentBar.setProgress(0);
        monsterIntentBar.setProgressTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), type.colorRes)));
        if (firstTurn) {
            monsterIntentBar.setProgress(progress);
        } else {
            ObjectAnimator animator = ObjectAnimator.ofInt(monsterIntentBar, "progress", 0, progress);
            animator.setDuration(400);
            animator.start();
        }
        if (!summaryVisible) {
            showTransientSummary(getString(R.string.combat_summary_ready));
        }
    }

    private void resetSummary() {
        summaryVisible = false;
        if (closeButton != null) {
            closeButton.setVisibility(View.GONE);
        }
        if (combatSummaryView != null) {
            combatSummaryView.setText(getString(R.string.combat_summary_ready));
        }
    }

    private void showTransientSummary(String text) {
        summaryVisible = false;
        if (closeButton != null) {
            closeButton.setVisibility(View.GONE);
        }
        if (combatSummaryView != null) {
            combatSummaryView.setText(text);
        }
    }

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

    private static final class MonsterIntent {
        final MonsterIntentType type;
        final int estimatedDamage;

        MonsterIntent(MonsterIntentType type, int estimatedDamage) {
            this.type = type;
            this.estimatedDamage = Math.max(0, estimatedDamage);
        }
    }

    private enum MonsterIntentType {
        QUICK(R.string.combat_intent_quick, 0.75f, 0.0f, R.color.intent_quick),
        GUARD_BREAK(R.string.combat_intent_guard_break, 1.05f, 0.5f, R.color.intent_guard),
        HEAVY(R.string.combat_intent_heavy, 1.35f, 0.15f, R.color.intent_heavy);

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
            int defense = profile.getDefense();
            int mitigatedDefense = defense - Math.round(defense * defenseBypass);
            int damage = baseAttack - Math.max(0, mitigatedDefense);
            return Math.max(0, damage);
        }

        static MonsterIntentType randomType(java.util.Random random) {
            int roll = random.nextInt(100);
            if (roll < 45) {
                return QUICK;
            } else if (roll < 80) {
                return GUARD_BREAK;
            }
            return HEAVY;
        }
    }

    private void refreshStatBlocks() {
        if (profile == null || monster == null) {
            return;
        }
        String playerStats = getString(R.string.combat_player_stats,
                profile.getName(),
                profile.getCurrentHP(),
                profile.getMaxHP(),
                profile.getAttack(),
                profile.getDefense());

        String monsterLabel;
        Bundle args = getArguments();
        if (args != null) {
            String image = args.getString(ARG_MONSTER_IMAGE, "");
            if (!TextUtils.isEmpty(image)) {
                assert monster != null;
                monsterLabel = image + " " + monster.getMonsterType();
            } else {
                assert monster != null;
                monsterLabel = monster.getMonsterType();
            }
        } else {
            assert monster != null;
            monsterLabel = monster.getMonsterType();
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

    private void appendLog(String line) {
        if (logBuilder.length() > 0) {
            logBuilder.append('\n');
        }
        logBuilder.append(line);
        if (combatLogView != null) {
            combatLogView.setText(logBuilder.toString());
        }
    }

    private void disableActions() {
        attackButton.setEnabled(false);
        potionButton.setEnabled(false);
        fleeButton.setEnabled(false);
    }

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

    private void prepareAnimatedCombatants(@NonNull CharacterProfile profileData,
                                           @NonNull Monster monsterData) {
        ensureAnimatedPlayer(profileData);
        animatedPlayer = profileData.getAnimatedPlayer();
        if (playerAnimationView != null && animatedPlayer == null) {
            playerAnimationView.setImageResource(getPlayerPlaceholderIcon(profileData.getPlayerClass()));
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

    private int getPlayerPlaceholderIcon(@Nullable PlayerClass playerClass) {
        if (playerClass == null) {
            return R.drawable.icon_knight;
        }
        switch (playerClass) {
            case WIZARD:
                return R.drawable.icon_wizard;
            case THIEF:
                return R.drawable.icon_thief;
            case KNIGHT:
            default:
                return R.drawable.icon_knight;
        }
    }

}
