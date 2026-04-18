//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.adaplu.clickdungeon;

import com.adaplu.clickdungeon.util.SecurePreferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adaplu.clickdungeon.model.AnimatedMonster;
import com.adaplu.clickdungeon.model.AnimatedPlayer;
import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.InventoryItem;
import com.adaplu.clickdungeon.model.ItemDefinition;
import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.MonsterAffinity;
import com.adaplu.clickdungeon.model.MonsterFamily;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.model.ShopItem;
import com.adaplu.clickdungeon.model.TerrainType;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;
import com.adaplu.clickdungeon.adapter.InventoryAdapter;
import com.adaplu.clickdungeon.ui.CombatDialogFragment;
import com.adaplu.clickdungeon.util.AchievementManager;
import com.adaplu.clickdungeon.util.BossCatalog;
import com.adaplu.clickdungeon.util.MonsterCatalog;
import com.adaplu.clickdungeon.util.MonsterCatalog.MonsterTemplate;
import com.adaplu.clickdungeon.util.DungeonGenerator;
import com.adaplu.clickdungeon.util.FeedbackManager;
import com.adaplu.clickdungeon.util.GameBalance;
import com.adaplu.clickdungeon.util.InventoryManager;
import com.adaplu.clickdungeon.util.ItemCatalog;
import com.adaplu.clickdungeon.util.MerchantManager;
import com.adaplu.clickdungeon.util.MonsterAnimationHelper;
import com.adaplu.clickdungeon.util.MonsterLootRoll;
import com.adaplu.clickdungeon.util.OnboardingManager;
import com.adaplu.clickdungeon.util.SaveManager;
import com.adaplu.clickdungeon.util.SettingsManager;
import com.adaplu.clickdungeon.util.SoundManager;
import com.adaplu.clickdungeon.util.TelemetryManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.EnumSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main gameplay Activity. Owns dungeon generation, grid rendering, combat entry,
 * class abilities, status effects, persistence, and HUD updates.
 */
public class GameActivity extends AppCompatActivity implements CombatDialogFragment.CombatCallbacks {

    /** Targeting modes used when the player is selecting an ability tile. */
    private enum AbilityTargetMode {
        NONE,
        WIZARD_FIREBALL,
        WIZARD_FROST_NOVA,
        WIZARD_CHAIN_LIGHTNING,
        WIZARD_METEOR,
        THIEF_SCAN,
        THIEF_SHADOWSTEP,
        THIEF_DISARM_EXPERT,
        THIEF_AMBUSH,
        KNIGHT_SHIELD,
        KNIGHT_TAUNT,
        KNIGHT_VALIANT_STRIKE
    }

    /** Reasons used to decide whether a save is immediate or debounced. */
    private enum SaveReason {
        PAUSE,
        START_NEW_RUN,
        FLOOR_TRANSITION,
        COMBAT_VICTORY,
        COMBAT_FLEE,
        COMBAT_DAMAGE,
        COMBAT_HEAL,
        MP_SPEND,
        INVENTORY_CHANGE,
        EQUIP_CHANGE,
        STAT_ALLOC,
        TILE_REVEAL,
        ABILITY_USED,
        COMBAT_STATE_UPDATE,
        OTHER
    }

    // --- Intent extras ---
    public static final String EXTRA_SLOT_INDEX = "com.adaplu.clickdungeon.extra.SLOT_INDEX";
    public static final String EXTRA_IS_NEW_GAME = "com.adaplu.clickdungeon.extra.IS_NEW_GAME";
    public static final String EXTRA_PROFILE_JSON = "com.adaplu.clickdungeon.extra.PROFILE_JSON";

    // --- Gameplay constants ---
    private static final int INVENTORY_STACK_LIMIT = 3;
    private static final int DUNGEON_EXPLORER_TARGET = 50;
    private static final int GOLD_HOARDER_TARGET = 1000;
    private static final String PROGRESS_PREFS = "player_progress";
    private static final String PROGRESS_KEY_TILES = "tiles_revealed_total";
    private static final String PROGRESS_KEY_GOLD = "gold_collected_total";
    private static final String PROGRESS_KEY_ENEMIES = "enemies_defeated_total";
    private static final int ABILITY_RANGE = 3;
    private static final int ABILITY_COOLDOWN_FLOORS = 2;
    private static final int RANGED_ATTACK_MIN_FLOOR = 6;
    private static final float RANGED_ATTACK_CHANCE_HARDCORE = 0.25f;
    private static final int MERCHANT_FLOOR_INTERVAL = 3;
    private static final float MERCHANT_VISIT_CHANCE = 0.5f;
    private static final String SMALL_KEY_NAME = "SMALL KEY";
    private static final int CHEST_MAGIC_ITEM_CHANCE = 10;
    private static final int CHEST_ITEM_CHANCE = 25;
    private static final String[] MAGIC_ITEM_POOL = new String[] {
            "Flame Relic",
            "Frost Sigil",
            "Storm Charm"
    };

    // --- Grid + run constants ---
    private static final int GRID_SIZE = 5;
    private static final int TOTAL_SAVE_SLOTS = 4;
    private static final int MAX_FLOOR = 15;
    private static final int DEFAULT_SLOT_INDEX = 0;
    private static final String TAG_COMBAT_DIALOG = "CombatDialog";
    private static final long GRID_ANIMATION_FRAME_DELAY_MS = 120L;
    private static final String DEFAULT_TILE_GLYPH = "[]";
    private static final long SAVE_DEBOUNCE_MS = 300L;
    // Critical save reasons are persisted immediately; other reasons are debounced.
    private static final EnumSet<SaveReason> CRITICAL_SAVE_REASONS = EnumSet.of(
            SaveReason.PAUSE,
            SaveReason.START_NEW_RUN,
            SaveReason.FLOOR_TRANSITION,
            SaveReason.COMBAT_VICTORY,
            SaveReason.COMBAT_FLEE,
            SaveReason.COMBAT_DAMAGE,
            SaveReason.COMBAT_HEAL,
            SaveReason.MP_SPEND,
            SaveReason.INVENTORY_CHANGE,
            SaveReason.EQUIP_CHANGE,
            SaveReason.STAT_ALLOC
    );

    // --- UI references ---
    private GridLayout gridLayout;
    private View gameRootLayout;
    private TextView goldCounterText, platinumCounterText, hpCounterText, statusEffectText, floorText;
    private TextView playerNameLevelText, xpCounterText;
    private ImageView playerHudIcon;
    private ProgressBar hpCounterBar;
    private ProgressBar mpCounterBar;
    private Button classAbilityButton;
    private Button levelUpButton;
    private Button inventoryButton;
    private Button usePotionButton;

    // --- Run state ---
    private Tile[][] dungeonGrid;
    private int currentGold = 0;
    private int currentPlatinum = 0;
    private int safeTilesToReveal = 0;
    private int revealedSafeTiles = 0;
    private int currentFloor = 1;
    private TerrainType currentTerrain = null;
    private int frozenTurnsLeft = 0;
    private int poisonTurnsLeft = 0;
    private final List<String> inventoryChangeLog = new ArrayList<>();

    // --- Player + combat state ---
    private CharacterProfile profile;
    private TileType placedLockedStair = null;
    private final Random random = new Random();

    // --- Services + managers ---
    private SaveManager saveManager;
    private SettingsManager.Difficulty difficultyMode;
    private int activeSlotIndex = -1;
    private Tile activeCombatTile = null;
    private View activeCombatTileView = null;
    private boolean colorBlindModeEnabled = false;
    private int pendingCombatGoldReward = 0;
    private int pendingCombatXpReward = 0;
    private int playerRow = GRID_SIZE / 2;
    private int playerCol = GRID_SIZE / 2;
    private int nextAbilityAvailableFloor = 1;
    private AbilityTargetMode pendingAbilityTargetMode = AbilityTargetMode.NONE;
    private boolean knightShieldActive = false;
    private int knightShieldStrength = 0;
    private int knightShieldRow = -1;
    private int knightShieldCol = -1;
    private int smokeVeilCharges = 0;
    private int lastFloorClearAwarded = -1;

    // --- Animation and save scheduling ---
    private final Map<String, AnimatedMonster> gridMonsterAnimations = new HashMap<>();
    private final Map<String, View> activeAnimatedTiles = new HashMap<>();
    private final Handler gridAnimationHandler = new Handler(Looper.getMainLooper());
    private final Handler saveHandler = new Handler(Looper.getMainLooper());
    private final Handler statusEffectHandler = new Handler(Looper.getMainLooper());
    private final Object saveQueueLock = new Object();
    private Executor saveExecutor = Executors.newSingleThreadExecutor();
    private SaveManager.SaveSnapshot pendingSaveSnapshot;
    private boolean saveQueued = false;
    private long saveDebounceMs = SAVE_DEBOUNCE_MS;
    private final Runnable debouncedSaveRunnable = () -> enqueueSave(SaveReason.OTHER);
    private boolean gridAnimationRunning = false;
    private final Runnable gridAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gridAnimationRunning) {
                return;
            }
            animateGridFrame();
            gridAnimationHandler.postDelayed(this, GRID_ANIMATION_FRAME_DELAY_MS);
        }
    };
    private View activePlayerTileView = null;
    private int lastPlayerRow = -1;
    private int lastPlayerCol = -1;
    private LruCache<String, Bitmap> monsterBitmapCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Load difficulty and accessibility preferences for this session.
        difficultyMode = SettingsManager.getDifficultyMode(this);
        colorBlindModeEnabled = SettingsManager.isColorBlindModeEnabled(this);

        // Bind UI references.
        gameRootLayout = findViewById(R.id.layoutGameRoot);
        gridLayout = findViewById(R.id.gridDungeon);
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = gridLayout.getWidth();
                int height = gridLayout.getHeight();
                if (height > width) {
                    ViewGroup.LayoutParams params = gridLayout.getLayoutParams();
                    params.height = width;
                    gridLayout.setLayoutParams(params);
                }
            }
        });
        goldCounterText = findViewById(R.id.textGoldCounter);
        platinumCounterText = findViewById(R.id.textPlatinumCounter);
        hpCounterText = findViewById(R.id.textHpCounter);
        playerNameLevelText = findViewById(R.id.textPlayerNameLevel);
        xpCounterText = findViewById(R.id.textXpCounter);
        playerHudIcon = findViewById(R.id.imagePlayerHudIcon);
        hpCounterBar = findViewById(R.id.progressHpCounter);
        mpCounterBar = findViewById(R.id.progressMpCounter);
        statusEffectText = findViewById(R.id.textStatus);
        floorText = findViewById(R.id.textFloor);
        classAbilityButton = findViewById(R.id.btnClassAbility);
        levelUpButton = findViewById(R.id.btnLevelUp);
        inventoryButton = findViewById(R.id.btnInventory);
        usePotionButton = findViewById(R.id.btnUsePotion);
        AchievementManager.loadAchievements(this);
        initBitmapCache();

        // Resolve save slot and decide whether to load or start a new run.
        saveManager = new SaveManager(this);
        Intent launchIntent = getIntent();
        activeSlotIndex = launchIntent.getIntExtra(EXTRA_SLOT_INDEX, -1);
        boolean launchingNewSlotGame = launchIntent.getBooleanExtra(EXTRA_IS_NEW_GAME, false);
        String profileJsonOverride = launchIntent.getStringExtra(EXTRA_PROFILE_JSON);

        if (savedInstanceState != null && activeSlotIndex < 0) {
            activeSlotIndex = savedInstanceState.getInt(EXTRA_SLOT_INDEX, -1);
        }

        if (activeSlotIndex < 0 || activeSlotIndex >= TOTAL_SAVE_SLOTS) {
            SharedPreferences prefs = SecurePreferences.get(this, "player_profile");
            int storedSlot = prefs.getInt("save_slot", DEFAULT_SLOT_INDEX);
            activeSlotIndex = Math.max(0, Math.min(TOTAL_SAVE_SLOTS - 1, storedSlot));
        }

        // Initialize either a fresh run or restore an existing save.
        if (launchingNewSlotGame) {
            if (loadProfileForSessionInvert(profileJsonOverride)) {
                return;
            }
            if (ensureProfileHasNameForSession()) {
                return;
            }
            ensureAnimatedPlayer();
            startNewRunForActiveSlot();
            if (profile != null && profile.getPlayerClass() != null) {
                TelemetryManager.logRunStart(
                        profile.getPlayerClass().name(),
                        difficultyMode != null ? difficultyMode.name() : "NORMAL",
                        activeSlotIndex);
            }
        } else {
            SaveManager.GameState gameState = saveManager.loadGame(activeSlotIndex);
            if (gameState != null) {
                profile = gameState.profile;
                if (ensureProfileHasNameForSession()) {
                    return;
                }
                currentFloor = gameState.currentFloor;
                currentGold = gameState.currentGold;
                currentPlatinum = gameState.currentPlatinum;
                dungeonGrid = gameState.dungeonGrid;
                gridMonsterAnimations.clear();
                activeAnimatedTiles.clear();
                recalculateSafeTileTargets(dungeonGrid);
                restoreLockedStairStateFromGrid(dungeonGrid);
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
                InventoryManager.syncPlatinumWithCurrentRun(this, currentPlatinum);
                ensureAnimatedPlayer();
                restoreRunMetadata(gameState.metadata);
            } else {
                Toast.makeText(this, R.string.continue_slot_load_failed, Toast.LENGTH_LONG).show();
                if (loadProfileForSessionInvert(profileJsonOverride)) {
                    return;
                }
                if (ensureProfileHasNameForSession()) {
                    return;
                }
                ensureAnimatedPlayer();
                startNewRunForActiveSlot();
            }
        }

        gridLayout.setColumnCount(GRID_SIZE);
        gridLayout.setRowCount(GRID_SIZE);

        // Initial render + HUD update.
        renderGrid();
        updateGoldCounter();
        updatePlatinumCounter();
        updateMpCounter();
        updateHpCounter();
        updateXpCounter();
        updatePlayerIdentityHud();
        updatePlayerHudIcon();
        updateStatusText();
        updateFloorDisplay();
        applyTerrainBackgroundForCurrentTerrain();
        setupClassAbilityButton();
        setupLevelUpButton();
        setupInventoryButton();
        OnboardingManager.showDungeonTutorialIfNeeded(this, difficultyMode, colorBlindModeEnabled);
    }

    // --- Dungeon generation / run lifecycle ---
    private void generateDungeon() {
        DungeonGenerator.Result result = DungeonGenerator.generateFloor(GRID_SIZE, currentFloor,
                this::createRandomMonsterForCurrentFloor);
        dungeonGrid = result.grid;
        placedLockedStair = result.lockedStair;
        safeTilesToReveal = result.safeTiles;
        maybePlaceBossEncounter();
        gridMonsterAnimations.clear();
        activeAnimatedTiles.clear();
        preWarmMonsterBitmaps(getMonsterPoolForFloor(currentFloor));
    }

    /** Replaces one enemy tile with a boss on boss floors. */
    private void maybePlaceBossEncounter() {
        if (dungeonGrid == null || !GameBalance.isBossFloor(currentFloor)) {
            return;
        }
        Monster boss = BossCatalog.createBossForFloor(currentFloor);
        if (boss == null) {
            return;
        }
        for (int row = 0; row < dungeonGrid.length; row++) {
            for (int col = 0; col < dungeonGrid[row].length; col++) {
                Tile tile = dungeonGrid[row][col];
                if (tile != null && tile.getType() == TileType.ENEMY) {
                    tile.setMonster(boss);
                    return;
                }
            }
        }
    }

    private void startNewRunForActiveSlot() {
        // Reset all transient run state first so nothing bleeds in from a previous run.
        resetRunState();

        currentFloor = 1;
        currentTerrain = selectTerrainForFloor(currentFloor);
        currentGold = GameBalance.STARTING_GOLD;
        currentPlatinum = GameBalance.STARTING_PLATINUM;
        lastFloorClearAwarded = -1;
        dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
        if (profile != null) {
            profile.setCurrentHP(profile.getMaxHP());
        }
        generateDungeon();
        resetPlayerPositionToCenter();
        nextAbilityAvailableFloor = currentFloor;
        smokeVeilCharges = 0;
        int potionCount = 3;
        if (profile != null && profile.getPlayerClass() == PlayerClass.WIZARD) {
            potionCount = 5;
        }
        InventoryManager.adjustItemQuantity(this, "Healing Potion", potionCount, 5);
        InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", 1, INVENTORY_STACK_LIMIT);
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        InventoryManager.syncPlatinumWithCurrentRun(this, currentPlatinum);

        // Re-render grid and refresh all HUD elements so in-place restarts are fully clean.
        renderGrid();
        updateGoldCounter();
        updatePlatinumCounter();
        updateHpCounter();
        updateXpCounter();
        updateMpCounter();
        updatePlayerIdentityHud();
        updatePlayerHudIcon();
        updateStatusText();
        updateFloorDisplay();
        applyTerrainBackgroundForCurrentTerrain();
        updateAbilityButtonState();
        setupClassAbilityButton();

        // Restart the grid animation loop after clearing it in resetRunState.
        startGridAnimationLoop();

        requestSave(SaveReason.START_NEW_RUN);
    }

    private void fallToNextFloor() {
        currentFloor++;
        if (currentFloor > MAX_FLOOR) {
            currentFloor = MAX_FLOOR;
            showVictoryDialog();
            return;
        }
        // Clear buyback items when moving to next floor
        MerchantManager.clearBuybackItems(this);
        currentTerrain = selectTerrainForFloor(currentFloor);
        generateDungeon();
        resetPlayerPositionToCenter();
        clearKnightShield(0);
        pendingAbilityTargetMode = AbilityTargetMode.NONE;
        smokeVeilCharges = 0;
        renderGrid();
        updateFloorDisplay();
        applyTerrainBackgroundForCurrentTerrain();
        frozenTurnsLeft = 0;
        poisonTurnsLeft = 0;
        updateStatusText();
        updateAbilityButtonState();
        requestSave(SaveReason.FLOOR_TRANSITION);
        maybeShowMerchant();
        Toast.makeText(this, getString(R.string.fell_to_floor, currentFloor), Toast.LENGTH_LONG).show();
    }

    // --- HUD buttons and ability flow ---
    private void setupClassAbilityButton() {
        if (classAbilityButton == null) {
            return;
        }
        classAbilityButton.setVisibility(View.VISIBLE);
        classAbilityButton.setOnClickListener(v -> triggerClassAbility());
        updateAbilityButtonState();
    }

    private void setupLevelUpButton() {
        if (levelUpButton == null) {
            return;
        }
        // Surface stat allocation when the player has unspent points.
        levelUpButton.setOnClickListener(v -> showInventoryDialog());
        updateLevelUpButtonState();
    }

    private void setupInventoryButton() {
        if (inventoryButton != null) {
            inventoryButton.setOnClickListener(v -> showInventoryDialog());
        }
        if (usePotionButton != null) {
            usePotionButton.setOnClickListener(v -> useHealingPotionFromHud());
        }
    }

    private void useHealingPotionFromHud() {
        if (profile == null) return;
        if (profile.getCurrentHP() >= profile.getMaxHP()) {
            Toast.makeText(this, R.string.potion_hp_full, Toast.LENGTH_SHORT).show();
            return;
        }
        int before = profile.getCurrentHP();
        boolean used = onUseHealingPotionRequested(CombatDialogFragment.HEALING_POTION_STRENGTH);
        if (used) {
            int healed = profile.getCurrentHP() - before;
            Toast.makeText(this, getString(R.string.potion_used, healed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.potion_none_available, Toast.LENGTH_SHORT).show();
        }
    }

    private void triggerClassAbility() {
        if (pendingAbilityTargetMode != AbilityTargetMode.NONE) {
            pendingAbilityTargetMode = AbilityTargetMode.NONE;
            Toast.makeText(this, R.string.ability_target_cancelled, Toast.LENGTH_SHORT).show();
            updateAbilityButtonState();
            return;
        }
        if (!isAbilityReady()) {
            Toast.makeText(this, getString(R.string.ability_on_cooldown, nextAbilityAvailableFloor), Toast.LENGTH_SHORT).show();
            return;
        }
        if (profile == null || profile.getPlayerClass() == null) {
            return;
        }
        showAbilityChooser();
    }

    private void showAbilityChooser() {
        if (profile == null || profile.getPlayerClass() == null) {
            return;
        }
        PlayerClass.AbilityDefinition[] abilities =
                profile.getPlayerClass().getAbilitiesUpToLevel(profile.getLevel());
        if (abilities.length == 0) {
            return;
        }
        // Present unlocked abilities and route to the current class effect mapping.
        String[] labels = new String[abilities.length];
        for (int i = 0; i < abilities.length; i++) {
            labels[i] = abilities[i].getName();
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.use_ability)
                .setItems(labels, (dialog, which) -> useAbility(abilities[which]))
                .show();
    }

    private void useAbility(PlayerClass.AbilityDefinition ability) {
        if (profile == null || profile.getPlayerClass() == null) {
            return;
        }
        // Ability effects are still tied to legacy class actions; names will map to distinct behavior later.
        PlayerClass playerClass = profile.getPlayerClass();
        String abilityName = ability != null ? ability.getName() : "";
        switch (playerClass) {
            case WIZARD:
                if (PlayerClass.ABILITY_WIZARD_ARCANE_SHIELD.equals(abilityName)) {
                    if (!consumeWizardMp(abilityName)) {
                        Toast.makeText(this, R.string.not_enough_mp, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (executeWizardArcaneShield()) {
                        consumeAbilityUse();
                        requestSave(SaveReason.ABILITY_USED);
                    }
                    break;
                }
                if (PlayerClass.ABILITY_WIZARD_FIREBALL.equals(abilityName)) {
                    if (!consumeWizardMp(abilityName)) {
                        Toast.makeText(this, R.string.not_enough_mp, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    beginAbilityTargeting(AbilityTargetMode.WIZARD_FIREBALL, R.plurals.ability_target_prompt_fireball);
                    break;
                }
                if (PlayerClass.ABILITY_WIZARD_FROST_NOVA.equals(abilityName)) {
                    if (!consumeWizardMp(abilityName)) {
                        Toast.makeText(this, R.string.not_enough_mp, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    beginAbilityTargeting(AbilityTargetMode.WIZARD_FROST_NOVA, R.plurals.ability_target_prompt_frost_nova);
                    break;
                }
                if (PlayerClass.ABILITY_WIZARD_CHAIN_LIGHTNING.equals(abilityName)) {
                    if (!consumeWizardMp(abilityName)) {
                        Toast.makeText(this, R.string.not_enough_mp, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    beginAbilityTargeting(AbilityTargetMode.WIZARD_CHAIN_LIGHTNING, R.plurals.ability_target_prompt_chain_lightning);
                    break;
                }
                if (PlayerClass.ABILITY_WIZARD_METEOR.equals(abilityName)) {
                    if (!consumeWizardMp(abilityName)) {
                        Toast.makeText(this, R.string.not_enough_mp, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    beginAbilityTargeting(AbilityTargetMode.WIZARD_METEOR, R.plurals.ability_target_prompt_meteor);
                }
                break;
            case THIEF:
                if (PlayerClass.ABILITY_THIEF_VEIL_OF_SMOKE.equals(abilityName)) {
                    if (executeThiefVeilOfSmoke()) {
                        consumeAbilityUse();
                        requestSave(SaveReason.ABILITY_USED);
                    }
                    break;
                }
                if (PlayerClass.ABILITY_THIEF_TRAP_SCAN.equals(abilityName)) {
                    beginAbilityTargeting(AbilityTargetMode.THIEF_SCAN, R.plurals.ability_target_prompt_scan);
                    break;
                }
                if (PlayerClass.ABILITY_THIEF_SHADOWSTEP.equals(abilityName)) {
                    beginAbilityTargeting(AbilityTargetMode.THIEF_SHADOWSTEP, R.plurals.ability_target_prompt_shadowstep);
                    break;
                }
                if (PlayerClass.ABILITY_THIEF_DISARM_EXPERT.equals(abilityName)) {
                    beginAbilityTargeting(AbilityTargetMode.THIEF_DISARM_EXPERT, R.plurals.ability_target_prompt_disarm);
                    break;
                }
                if (PlayerClass.ABILITY_THIEF_AMBUSH.equals(abilityName)) {
                    beginAbilityTargeting(AbilityTargetMode.THIEF_AMBUSH, R.plurals.ability_target_prompt_ambush);
                }
                break;
            case KNIGHT:
                if (PlayerClass.ABILITY_KNIGHT_FORTIFY.equals(abilityName)) {
                    if (executeKnightFortify()) {
                        consumeAbilityUse();
                        requestSave(SaveReason.ABILITY_USED);
                    }
                    break;
                }
                if (PlayerClass.ABILITY_KNIGHT_GUARDIANS_OATH.equals(abilityName)) {
                    if (executeKnightGuardiansOath()) {
                        consumeAbilityUse();
                        requestSave(SaveReason.ABILITY_USED);
                    }
                    break;
                }
                if (PlayerClass.ABILITY_KNIGHT_SHIELD_WALL.equals(abilityName)) {
                    beginAbilityTargeting(AbilityTargetMode.KNIGHT_SHIELD, R.plurals.ability_target_prompt_shield);
                    break;
                }
                if (PlayerClass.ABILITY_KNIGHT_TAUNT.equals(abilityName)) {
                    beginAbilityTargeting(AbilityTargetMode.KNIGHT_TAUNT, R.plurals.ability_target_prompt_taunt);
                    break;
                }
                if (PlayerClass.ABILITY_KNIGHT_VALIANT_STRIKE.equals(abilityName)) {
                    beginAbilityTargeting(AbilityTargetMode.KNIGHT_VALIANT_STRIKE, R.plurals.ability_target_prompt_valiant_strike);
                }
                break;
            default:
                break;
        }
    }

    private boolean consumeWizardMp(String abilityName) {
        if (profile == null || !profile.usesMp()) {
            return true;
        }
        // Wizard abilities share a flat MP cost until per-ability tuning is introduced.
        int cost = getWizardMpCost(abilityName);
        if (profile.getCurrentMP() < cost) {
            return false;
        }
        profile.setCurrentMP(profile.getCurrentMP() - cost);
        updateMpCounter();
        requestSave(SaveReason.MP_SPEND);
        return true;
    }

    private int getWizardMpCost(String abilityName) {
        if (PlayerClass.ABILITY_WIZARD_METEOR.equals(abilityName)) {
            return GameBalance.WIZARD_MP_COST_METEOR;
        }
        if (PlayerClass.ABILITY_WIZARD_CHAIN_LIGHTNING.equals(abilityName)) {
            return GameBalance.WIZARD_MP_COST_CHAIN_LIGHTNING;
        }
        if (PlayerClass.ABILITY_WIZARD_FROST_NOVA.equals(abilityName)
                || PlayerClass.ABILITY_WIZARD_ARCANE_SHIELD.equals(abilityName)) {
            return GameBalance.WIZARD_MP_COST_FROST_NOVA;
        }
        return GameBalance.WIZARD_MP_COST_DEFAULT;
    }

    private void beginAbilityTargeting(AbilityTargetMode mode, int promptPluralResId) {
        if (mode == null) {
            return;
        }
        pendingAbilityTargetMode = mode;
        String prompt = getResources().getQuantityString(promptPluralResId, ABILITY_RANGE, ABILITY_RANGE);
        Toast.makeText(this, prompt, Toast.LENGTH_SHORT).show();
        updateAbilityButtonState();
    }

    private void updateAbilityButtonState() {
        if (classAbilityButton == null || profile == null || profile.getPlayerClass() == null) {
            return;
        }
        boolean targeting = pendingAbilityTargetMode != AbilityTargetMode.NONE;
        classAbilityButton.setEnabled(!targeting && isAbilityReady());
        classAbilityButton.setText(getString(targeting
                ? R.string.ability_button_targeting
                : R.string.use_ability));
    }

    private int getAbilityButtonLabelRes(PlayerClass playerClass) {
        switch (playerClass) {
            case WIZARD:
                return R.string.ability_button_wizard;
            case THIEF:
                return R.string.ability_button_thief;
            case KNIGHT:
                return R.string.ability_button_knight;
            default:
                return R.string.use_ability;
        }
    }

    private boolean isAbilityReady() {
        return profile != null
                && currentFloor >= nextAbilityAvailableFloor
                && pendingAbilityTargetMode == AbilityTargetMode.NONE;
    }

    private void updateLevelUpButtonState() {
        if (levelUpButton == null || profile == null) {
            return;
        }
        // Hide the Level Up button unless stat points are available.
        levelUpButton.setVisibility(profile.getAvailableStatPoints() > 0 ? View.VISIBLE : View.GONE);
    }

    private void consumeAbilityUse() {
        nextAbilityAvailableFloor = currentFloor + ABILITY_COOLDOWN_FLOORS;
        pendingAbilityTargetMode = AbilityTargetMode.NONE;
        updateAbilityButtonState();
    }

    // --- Profile loading and run restore ---
    private boolean loadProfileForSessionInvert(String profileJsonOverride) {
        if (profileJsonOverride != null) {
            profile = new Gson().fromJson(profileJsonOverride, CharacterProfile.class);
        }
        if (profile == null) {
            SharedPreferences prefs = SecurePreferences.get(this, "player_profile");
            String json = prefs.getString("profile", null);
            if (json != null) {
                profile = new Gson().fromJson(json, CharacterProfile.class);
            }
        }
        if (profile == null) {
            Toast.makeText(this, R.string.profile_missing_message, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ClassSelectionActivity.class);
            intent.putExtra(ClassSelectionActivity.EXTRA_SAVE_SLOT_INDEX,
                    Math.max(0, activeSlotIndex));
            startActivity(intent);
            finish();
            return true;
        }
        ensureAnimatedPlayer();
        return false;
    }

    private boolean ensureProfileHasNameForSession() {
        if (profile != null && !TextUtils.isEmpty(profile.getName())
                && !TextUtils.isEmpty(profile.getName().trim())) {
            return false;
        }
        Toast.makeText(this, R.string.choose_name_and_class, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ClassSelectionActivity.class);
        intent.putExtra(ClassSelectionActivity.EXTRA_SAVE_SLOT_INDEX, Math.max(0, activeSlotIndex));
        startActivity(intent);
        finish();
        return true;
    }

    // --- Terrain + monster selection ---
    private void recalculateSafeTileTargets(Tile[][] grid) {
        if (grid == null) {
            safeTilesToReveal = 0;
            return;
        }
        int totalSafeTiles = 0;
        for (Tile[] row : grid) {
            for (Tile tile : row) {
                if (tile != null && tile.getType() != TileType.ENEMY) {
                    totalSafeTiles++;
                }
            }
        }
        safeTilesToReveal = totalSafeTiles;
    }

    private Monster createRandomMonsterForCurrentFloor() {
        ensureTerrainForFloor(currentFloor);
        MonsterTemplate[] pool = getMonsterPoolForFloor(currentFloor);
        MonsterTemplate template = pickTerrainWeightedMonster(pool);
        Monster monster = template.spawnForFloor(random, currentFloor, difficultyMode);
        if (shouldGrantRangedAttack(currentFloor, difficultyMode)
                && random.nextFloat() < RANGED_ATTACK_CHANCE_HARDCORE) {
            monster.setHasRangedAttack(true);
        }
        applyTerrainModifiers(monster);
        return monster;
    }

    private MonsterTemplate[] getMonsterPoolForFloor(int floor) {
        switch (floor) {
            case 1:
                return new MonsterTemplate[] { MonsterCatalog.SLIME, MonsterCatalog.GOBLIN, MonsterCatalog.RAT };
            case 2:
                return new MonsterTemplate[] { MonsterCatalog.SLIME, MonsterCatalog.GOBLIN, MonsterCatalog.BAT };
            case 3:
                return new MonsterTemplate[] { MonsterCatalog.GOBLIN, MonsterCatalog.SKELETON, MonsterCatalog.SPIDER };
            case 4:
                return new MonsterTemplate[] { MonsterCatalog.SKELETON, MonsterCatalog.ORC, MonsterCatalog.WOLF };
            case 5:
                return new MonsterTemplate[] { MonsterCatalog.ORC, MonsterCatalog.TROLL, MonsterCatalog.BANDIT };
            case 6:
                return new MonsterTemplate[] { MonsterCatalog.TROLL, MonsterCatalog.WITCH, MonsterCatalog.CULTIST };
            case 7:
                return new MonsterTemplate[] { MonsterCatalog.WITCH, MonsterCatalog.VAMPIRE, MonsterCatalog.WARLOCK };
            case 8:
                return new MonsterTemplate[] { MonsterCatalog.VAMPIRE, MonsterCatalog.DEMON, MonsterCatalog.WRAITH };
            case 9:
                return new MonsterTemplate[] { MonsterCatalog.DEMON, MonsterCatalog.DRAGON, MonsterCatalog.GOLEM };
            case 10:
                return new MonsterTemplate[] { MonsterCatalog.DRAGON, MonsterCatalog.LICH, MonsterCatalog.HELLHOUND };
            case 11:
                return new MonsterTemplate[] { MonsterCatalog.DRAGON, MonsterCatalog.LICH, MonsterCatalog.REVENANT };
            case 12:
                return new MonsterTemplate[] { MonsterCatalog.LICH, MonsterCatalog.DEMON, MonsterCatalog.WRAITH };
            case 13:
                return new MonsterTemplate[] { MonsterCatalog.DRAGON, MonsterCatalog.LICH, MonsterCatalog.DEMON };
            case 14:
                return new MonsterTemplate[] { MonsterCatalog.DRAGON, MonsterCatalog.LICH, MonsterCatalog.ARCHDEMON };
            case 15:
                return new MonsterTemplate[] { MonsterCatalog.DRAGON, MonsterCatalog.ARCHDEMON, MonsterCatalog.ANCIENT_WYRM };
            default:
                return new MonsterTemplate[] { MonsterCatalog.SLIME, MonsterCatalog.GOBLIN, MonsterCatalog.SKELETON };
        }
    }

    private void ensureTerrainForFloor(int floor) {
        if (currentTerrain != null) {
            return;
        }
        currentTerrain = selectTerrainForFloor(floor);
    }

    private TerrainType selectTerrainForFloor(int floor) {
        TerrainType[] types = TerrainType.values();
        if (types.length == 0) {
            return null;
        }
        int index = Math.abs(Math.max(1, floor) - 1) % types.length;
        return types[index];
    }

    private MonsterTemplate pickTerrainWeightedMonster(MonsterTemplate[] basePool) {
        if (basePool == null || basePool.length == 0) {
            return MonsterCatalog.SLIME;
        }
        List<MonsterTemplate> weighted = new ArrayList<>();
        for (MonsterTemplate template : basePool) {
            int weight = isFamilyFavored(currentTerrain, template.family) ? 2 : 1;
            for (int i = 0; i < weight; i++) {
                weighted.add(template);
            }
        }
        return weighted.get(random.nextInt(weighted.size()));
    }

    private boolean isFamilyFavored(@Nullable TerrainType terrain, @NonNull MonsterFamily family) {
        if (terrain == null) {
            return false;
        }
        switch (terrain) {
            case CAVERN:
                return family == MonsterFamily.BEAST || family == MonsterFamily.HUMANOID;
            case CRYPT:
                return family == MonsterFamily.UNDEAD;
            case LAVA_FIELD:
                return family == MonsterFamily.DEMONIC
                        || family == MonsterFamily.DRACONIC
                        || family == MonsterFamily.ELEMENTAL;
            case MIRE:
                return family == MonsterFamily.BEAST || family == MonsterFamily.ELEMENTAL;
            case FROZEN_RUINS:
                return family == MonsterFamily.UNDEAD || family == MonsterFamily.CONSTRUCT;
            case THORN_WILDS:
                return family == MonsterFamily.BEAST;
            case STORM_PLATEAU:
                return family == MonsterFamily.ARCANE || family == MonsterFamily.CONSTRUCT;
            case ARCANE_NEXUS:
                return family == MonsterFamily.ARCANE;
            case SUNKEN_TEMPLE:
                return family == MonsterFamily.BEAST || family == MonsterFamily.ELEMENTAL;
            case ASH_WASTES:
                return family == MonsterFamily.DEMONIC || family == MonsterFamily.ELEMENTAL;
            default:
                return false;
        }
    }

    private void applyTerrainModifiers(@NonNull Monster monster) {
        if (currentTerrain == null) {
            return;
        }
        int attack = monster.getAttack();
        int defense = monster.getDefense();
        MonsterFamily family = monster.getFamily();
        MonsterAffinity affinity = monster.getAffinity();
        switch (currentTerrain) {
            case CAVERN:
                defense += 1;
                if (monster.hasRangedAttack()) {
                    attack = Math.max(1, attack - 1);
                }
                break;
            case CRYPT:
                if (family == MonsterFamily.UNDEAD) {
                    attack = Math.max(1, Math.round(attack * 1.10f));
                }
                break;
            case LAVA_FIELD:
                if (affinity == MonsterAffinity.FIRE) {
                    defense = Math.max(0, Math.round(defense * 1.15f));
                }
                break;
            case MIRE:
                if (family == MonsterFamily.BEAST) {
                    attack += 1;
                }
                break;
            case FROZEN_RUINS:
                if (affinity == MonsterAffinity.ICE) {
                    defense = Math.max(0, Math.round(defense * 1.10f));
                }
                break;
            case THORN_WILDS:
                if (family == MonsterFamily.BEAST) {
                    defense += 1;
                }
                break;
            case STORM_PLATEAU:
                if (affinity == MonsterAffinity.LIGHTNING) {
                    attack += 1;
                }
                break;
            case ARCANE_NEXUS:
                if (affinity == MonsterAffinity.ARCANE) {
                    attack += 1;
                } else {
                    defense = Math.max(0, defense - 1);
                }
                break;
            case SUNKEN_TEMPLE:
                if (affinity == MonsterAffinity.FIRE) {
                    attack = Math.max(1, Math.round(attack * 0.90f));
                }
                break;
            case ASH_WASTES:
                if (family == MonsterFamily.ELEMENTAL) {
                    attack = Math.max(1, Math.round(attack * 1.05f));
                }
                break;
            default:
                break;
        }
        monster.setAttack(attack);
        monster.setDefense(defense);
    }

    private boolean shouldGrantRangedAttack(int floor, SettingsManager.Difficulty difficulty) {
        return difficulty == SettingsManager.Difficulty.HARDCORE
                && floor >= RANGED_ATTACK_MIN_FLOOR;
    }

    private void maybeShowMerchant() {
        if (currentFloor <= 0 || currentFloor % MERCHANT_FLOOR_INTERVAL != 0) {
            return;
        }
        if (random.nextFloat() > MERCHANT_VISIT_CHANCE) {
            return;
        }
        Intent intent = new Intent(this, ShopActivity.class);
        intent.putExtra(ShopActivity.EXTRA_MERCHANT_FLOOR, currentFloor);
        startActivity(intent);
    }

    private void restoreLockedStairStateFromGrid(Tile[][] grid) {
        placedLockedStair = null;
        if (grid == null) {
            return;
        }
        for (Tile[] row : grid) {
            for (Tile tile : row) {
                if (tile == null) continue;
                normalizeLegacyTile(tile);
                TileType type = tile.getType();
                if (type == TileType.STAIR_DOWN_LOCKED) {
                    placedLockedStair = TileType.STAIR_DOWN_LOCKED;
                    return;
                }
            }
        }
    }

    // --- Save pipeline ---
    // Route save requests to immediate or debounced pipelines.
    private void requestSave(SaveReason reason) {
        if (CRITICAL_SAVE_REASONS.contains(reason)) {
            requestCriticalSave(reason);
        } else {
            requestDeferredSave(reason);
        }
    }

    private void requestDeferredSave(SaveReason reason) {
        if (dungeonGrid == null || profile == null || saveManager == null) {
            return;
        }
        saveHandler.removeCallbacks(debouncedSaveRunnable);
        saveHandler.postDelayed(debouncedSaveRunnable, saveDebounceMs);
    }

    private void requestCriticalSave(SaveReason reason) {
        if (dungeonGrid == null || profile == null || saveManager == null) {
            return;
        }
        saveHandler.removeCallbacks(debouncedSaveRunnable);
        enqueueSave(reason);
    }

    private SaveManager.RunMetadata buildRunMetadata() {
        return new SaveManager.RunMetadata(
                playerRow,
                playerCol,
                knightShieldActive,
                knightShieldStrength,
                knightShieldRow,
                knightShieldCol,
                nextAbilityAvailableFloor,
                currentTerrain != null ? currentTerrain.name() : null);
    }

    private void enqueueSave(SaveReason reason) {
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        InventoryManager.syncPlatinumWithCurrentRun(this, currentPlatinum);
        int slotToPersist = activeSlotIndex >= 0 ? activeSlotIndex : DEFAULT_SLOT_INDEX;
        // Snapshot and queue the latest state; the worker coalesces rapid updates.
        SaveManager.SaveSnapshot snapshot = saveManager.buildSnapshot(
                profile,
                currentFloor,
                currentGold,
                currentPlatinum,
                dungeonGrid,
                buildRunMetadata());
        synchronized (saveQueueLock) {
            pendingSaveSnapshot = snapshot;
            if (saveQueued) {
                return;
            }
            saveQueued = true;
        }
        saveExecutor.execute(() -> {
            while (true) {
                SaveManager.SaveSnapshot toSave;
                synchronized (saveQueueLock) {
                    toSave = pendingSaveSnapshot;
                    pendingSaveSnapshot = null;
                }
                if (toSave == null) {
                    synchronized (saveQueueLock) {
                        saveQueued = false;
                    }
                    return;
                }
                saveManager.saveSnapshot(slotToPersist, toSave);
            }
        });
    }

    void setSaveExecutorForTest(Executor executor) {
        if (executor != null) {
            saveExecutor = executor;
        }
    }

    void setSaveDebounceMsForTest(long debounceMs) {
        saveDebounceMs = Math.max(0L, debounceMs);
    }

    private void clearPersistedState() {
        if (saveManager == null) {
            return;
        }
        int slotToClear = activeSlotIndex >= 0 ? activeSlotIndex : DEFAULT_SLOT_INDEX;
        saveManager.deleteSave(slotToClear);
    }

    // --- Grid rendering and tile glyphs ---
    private void updateFloorDisplay() {
        String label;
        if (currentTerrain != null) {
            label = getString(R.string.floor_display_dynamic_terrain,
                    currentFloor,
                    currentTerrain.getDisplayName());
        } else {
            label = getString(R.string.floor_display_dynamic, currentFloor);
        }
        if (placedLockedStair != null) {
            label += getString(R.string.floor_display_hard_suffix);
        }
        floorText.setText(label);
    }

    private void applyTerrainBackgroundForCurrentTerrain() {
        if (gameRootLayout == null) {
            return;
        }
        int terrainRes = getTerrainBackgroundResource(currentTerrain);
        if (terrainRes != 0) {
            gameRootLayout.setBackgroundResource(terrainRes);
        } else {
            gameRootLayout.setBackgroundResource(R.drawable.bg_screen_dungeon);
        }
    }

    private int getTerrainBackgroundResource(@Nullable TerrainType terrain) {
        if (terrain == null) {
            return 0;
        }
        String[] candidates;
        switch (terrain) {
            case CAVERN:
                candidates = new String[] { "Cavern", "cavern" };
                break;
            case CRYPT:
                candidates = new String[] { "Crypt", "crypt" };
                break;
            case LAVA_FIELD:
                candidates = new String[] { "LavaField", "lava_field", "lavafield" };
                break;
            case MIRE:
                candidates = new String[] { "Mire", "mire" };
                break;
            case FROZEN_RUINS:
                candidates = new String[] { "FrozenWastes", "frozen_ruins", "frozen_wastes" };
                break;
            case THORN_WILDS:
                candidates = new String[] { "ThornWilds", "thorn_wilds" };
                break;
            case STORM_PLATEAU:
                candidates = new String[] { "StormPlateau", "storm_plateau" };
                break;
            case ARCANE_NEXUS:
                candidates = new String[] { "ArcaneNexus", "arcane_nexus" };
                break;
            case SUNKEN_TEMPLE:
                candidates = new String[] { "SunkenTemple", "sunken_temple" };
                break;
            case ASH_WASTES:
                candidates = new String[] { "AshWastes", "ash_wastes" };
                break;
            default:
                candidates = new String[0];
                break;
        }
        for (String candidate : candidates) {
            int resId = getResources().getIdentifier(candidate, "drawable", getPackageName());
            if (resId != 0) {
                return resId;
            }
        }
        return 0;
    }

    private void renderGrid() {
        if (gridLayout == null || dungeonGrid == null) {
            return;
        }
        if (gridLayout.getChildCount() == 0) {
            LayoutInflater inflater = getLayoutInflater();
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    View tileView = inflater.inflate(R.layout.item_tile, gridLayout, false);
                    tileView.setTag(R.id.tag_row, row);
                    tileView.setTag(R.id.tag_col, col);
                    tileView.setOnClickListener(v -> {
                        Object tagRow = v.getTag(R.id.tag_row);
                        Object tagCol = v.getTag(R.id.tag_col);
                        if (tagRow instanceof Integer && tagCol instanceof Integer) {
                            handleTileClick((Integer) tagRow, (Integer) tagCol, v);
                        }
                    });
                    tileView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(View v) {
                            // No-op; rebinding will ensure frames are correct.
                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {
                            Object r = v.getTag(R.id.tag_row);
                            Object c = v.getTag(R.id.tag_col);
                            if (r instanceof Integer && c instanceof Integer) {
                                String key = coordinateKey((Integer) r, (Integer) c);
                                gridMonsterAnimations.remove(key);
                                activeAnimatedTiles.remove(key);
                            }
                        }
                    });
                    gridLayout.addView(tileView);
                }
            }
        }
        revealedSafeTiles = 0;
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View tileView = gridLayout.getChildAt(i);
            Object tagRow = tileView.getTag(R.id.tag_row);
            Object tagCol = tileView.getTag(R.id.tag_col);
            if (!(tagRow instanceof Integer) || !(tagCol instanceof Integer)) {
                continue;
            }
            int row = (Integer) tagRow;
            int col = (Integer) tagCol;
            Tile tile = dungeonGrid[row][col];
            if (tile != null && tile.isRevealed() && tile.getType() != TileType.ENEMY) {
                revealedSafeTiles++;
            }
            if (tile != null && !tile.isDirty()) {
                continue;
            }
            bindTileView(tileView, tile, row, col);
        }
        animateGridFrame();
    }

    private String getTileDisplay(Tile tile) {
        if (tile == null) {
            return DEFAULT_TILE_GLYPH;
        }
        String glyph = getTileGlyph(tile);
        if (!colorBlindModeEnabled) {
            return glyph;
        }
        String suffix = getColorBlindSuffix(tile);
        return suffix.isEmpty() ? glyph : glyph + " " + suffix;
    }

    private String getTileGlyph(Tile tile) {
        TileType type = tile.getType();
        switch (type) {
            case GOLD:
                return "G";
            case EMPTY:
                return ".";
            case ENEMY:
                if (!tile.hasMonster()) {
                    return getString(R.string.combat_tile_cleared);
                }
                String icon = tile.getMonster().getImage();
                return !TextUtils.isEmpty(icon) ? icon : "E";
            case STAIR_DOWN:
                return "DN";
            case STAIR_DOWN_LOCKED:
                return "LK";
            case STAIR_UP:
                return "UP";
            case SMALL_KEY:
                return "SK";
            case BIG_KEY:
                return "BK";
            case CHEST:
                return "CH";
            case TRAP_FIRE:
                return "TF";
            case TRAP_POISON:
                return "TP";
            case TRAP_ACID:
                return "TA";
            case TRAP_FREEZE:
                return "TI";
            case TRAP_PITFALL:
                return "TH";
            default:
                return type.toString();
        }
    }

    private String getColorBlindSuffix(Tile tile) {
        TileType type = tile.getType();
        switch (type) {
            case SMALL_KEY:
                return "SK";
            case BIG_KEY:
                return "BK";
            case CHEST:
                return "C";
            case TRAP_FIRE:
                return "F";
            case TRAP_POISON:
                return "P";
            case TRAP_ACID:
                return "A";
            case TRAP_FREEZE:
                return "I";
            case TRAP_PITFALL:
                return "H"; // Hole
            case ENEMY:
                if (tile.hasMonster()) {
                    String name = tile.getMonster().getMonsterType();
                    return !TextUtils.isEmpty(name) ? name.substring(0, 1).toUpperCase(Locale.ROOT) : "E";
                }
                return "";
            case STAIR_DOWN:
                return "DN";
            case STAIR_DOWN_LOCKED:
                return "LK";
            case STAIR_UP:
                return "UP";
            default:
                return "";
        }
    }

    private String getTileContentDescription(Tile tile) {
        if (tile == null) {
            return getString(R.string.tile_desc_hidden);
        }
        switch (tile.getType()) {
            case GOLD:
                return getString(R.string.tile_desc_gold);
            case ENEMY:
                return getString(R.string.tile_desc_enemy);
            case STAIR_DOWN:
                return getString(R.string.tile_desc_stairs_down);
            case STAIR_DOWN_LOCKED:
                return getString(R.string.tile_desc_locked_stair_big);
            case STAIR_UP:
                return getString(R.string.tile_desc_stairs_up);
            case SMALL_KEY:
                return getString(R.string.tile_desc_key_small);
            case BIG_KEY:
                return getString(R.string.tile_desc_key_big);
            case CHEST:
                return getString(R.string.tile_desc_chest);
            case TRAP_FIRE:
                return getString(R.string.tile_desc_trap_fire);
            case TRAP_POISON:
                return getString(R.string.tile_desc_trap_poison);
            case TRAP_ACID:
                return getString(R.string.tile_desc_trap_acid);
            case TRAP_FREEZE:
                return getString(R.string.tile_desc_trap_freeze);
            case TRAP_PITFALL:
                return getString(R.string.tile_desc_trap_pitfall);
            default:
                return tile.getType().name();
        }
    }

    private int getBoardTileImageResource(@Nullable Tile tile) {
        if (tile == null) {
            return 0;
        }
        switch (tile.getType()) {
            case EMPTY:
                return R.drawable.ic_tile_empty_128x128;
            case GOLD:
                return R.drawable.ic_tile_gold_128x128;
            case STAIR_DOWN:
                return R.drawable.ic_stairs_down_128x128;
            case STAIR_DOWN_LOCKED:
                return R.drawable.ic_stairs_locked_128x128;
            case STAIR_UP:
                return R.drawable.ic_stairs_up_128x128;
            case SMALL_KEY:
                return colorBlindModeEnabled
                        ? R.drawable.ic_small_key_cb_128x128
                        : R.drawable.ic_small_key_128x128;
            case BIG_KEY:
                return colorBlindModeEnabled
                        ? R.drawable.ic_big_key_cb_128x128
                        : R.drawable.ic_big_key_128x128;
            case CHEST:
                return R.drawable.ic_chest_closed_128x128;
            case TRAP_FIRE:
                return colorBlindModeEnabled
                        ? R.drawable.ic_trap_fire_cb_128x128
                        : R.drawable.ic_trap_fire_128x128;
            case TRAP_POISON:
                return colorBlindModeEnabled
                        ? R.drawable.ic_trap_poison_cb_128x128
                        : R.drawable.ic_trap_poison_128x128;
            case TRAP_ACID:
                return colorBlindModeEnabled
                        ? R.drawable.ic_trap_acid_cb_128x128
                        : R.drawable.ic_trap_acid_128x128;
            case TRAP_FREEZE:
                return colorBlindModeEnabled
                        ? R.drawable.ic_trap_freeze_cb_128x128
                        : R.drawable.ic_trap_freeze_128x128;
            case TRAP_PITFALL:
                return colorBlindModeEnabled
                        ? R.drawable.ic_trap_pitfall_cb_128x128
                        : R.drawable.ic_trap_pitfall_128x128;
            default:
                return 0;
        }
    }

    private void updateTileTextDisplay(TextView tileText, Tile tile) {
        if (tileText != null) {
            ImageView tileImage = null;
            View parent = (View) tileText.getParent();
            if (parent != null) {
                tileImage = parent.findViewById(R.id.imageTile);
            }

            int imageRes = getBoardTileImageResource(tile);
            if (tileImage != null && imageRes != 0) {
                tileImage.setVisibility(View.VISIBLE);
                tileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                tileImage.setImageResource(imageRes);
                tileText.setVisibility(View.GONE);
            } else {
                if (tileImage != null) {
                    tileImage.setVisibility(View.GONE);
                    tileImage.setImageDrawable(null);
                }
                tileText.setVisibility(View.VISIBLE);
                tileText.setText(getTileDisplay(tile));
            }
            tileText.setContentDescription(getTileContentDescription(tile));
        }
    }

    private void bindTileView(@NonNull View tileView,
                              @Nullable Tile tile,
                              int row,
                              int col) {
        normalizeLegacyTile(tile);
        TextView tileText = tileView.findViewById(R.id.textTile);
        ImageView tileImage = tileView.findViewById(R.id.imageTile);
        ProgressBar hpBar = tileView.findViewById(R.id.monsterHpBar);
        View overlay = tileView.findViewById(R.id.damageOverlay);

        tileImage.setVisibility(View.GONE);
        tileImage.setImageDrawable(null);
        hpBar.setVisibility(View.GONE);
        tileText.setVisibility(View.VISIBLE);

        boolean revealed = tile != null && tile.isRevealed();
        tileView.setBackgroundResource(revealed
                ? getRevealedTileBackgroundResource()
                : R.drawable.tile_bg);

        String description;
        String coordKey = coordinateKey(row, col);
        if (!revealed) {
            gridMonsterAnimations.remove(coordKey);
            tileImage.setVisibility(View.VISIBLE);
            tileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            tileImage.setImageResource(R.drawable.dungeon_door);
            tileText.setVisibility(View.GONE);
            description = getString(R.string.tile_desc_hidden);
        } else if (tile.getType() == TileType.ENEMY && tile.hasMonster()) {
            tileText.setVisibility(View.GONE);
            tileImage.setVisibility(View.VISIBLE);
            tileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            AnimatedMonster animator = ensureGridAnimatedMonster(row, col, tile);
            Bitmap sprite = animator != null ? animator.getCurrentFrame() : null;
            if (sprite != null) {
                tileImage.setImageBitmap(sprite);
            } else {
                Bitmap fallback = getMonsterBitmap(tile.getMonster());
                if (fallback != null) {
                    tileImage.setImageBitmap(fallback);
                } else {
                    tileImage.setImageResource(getMonsterSpriteResource(tile.getMonster()));
                }
            }
            hpBar.setVisibility(View.VISIBLE);
            hpBar.setMax(Math.max(1, tile.getMonster().getMaxHP()));
            hpBar.setProgress(Math.max(0, tile.getMonster().getCurrentHP()));
            tile.setMonsterSpriteKey(tile.getMonster().getMonsterType());
            tile.setCachedMonsterHp(tile.getMonster().getCurrentHP());
            tile.setCachedMonsterMaxHp(tile.getMonster().getMaxHP());
            description = getString(R.string.tile_desc_enemy_with_hp,
                    tile.getMonster().getMonsterType(),
                    tile.getMonster().getCurrentHP(),
                    tile.getMonster().getMaxHP());
        } else {
            gridMonsterAnimations.remove(coordKey);
            updateTileTextDisplay(tileText, tile);
            description = getTileContentDescription(tile);
        }

        boolean playerHere = tile != null && tile.hasPlayer();
        if (playerHere) {
            overlay.setVisibility(View.VISIBLE);
            overlay.setBackgroundColor(getColorCompat(R.color.player_highlight_overlay));
            overlay.setAlpha(0.5f);
            tileText.setVisibility(View.GONE);
            tileImage.setVisibility(View.VISIBLE);
            tileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Bitmap frame = null;
            if (profile != null && profile.getAnimatedPlayer() != null) {
                frame = profile.getAnimatedPlayer().getCurrentFrame();
            }
            if (frame != null) {
                tileImage.setImageBitmap(frame);
            } else {
                tileImage.setImageResource(getPlayerSpriteSheetResource());
            }
            activePlayerTileView = tileView;
            description = getString(R.string.tile_desc_player_here, description);
        } else {
            overlay.setVisibility(View.GONE);
        }

        tileView.setContentDescription(description);
        if (tile != null) {
            tile.setDirty(false);
        }
        updateAnimatedTileRegistry(row, col, tile, tileView);
    }

    private int getRevealedTileBackgroundResource() {
        int terrainRes = getTerrainBackgroundResource(currentTerrain);
        return terrainRes != 0 ? terrainRes : R.drawable.bg_screen_dungeon;
    }

    private String coordinateKey(int row, int col) {
        return row + "_" + col;
    }

    @Nullable
    private AnimatedMonster ensureGridAnimatedMonster(int row,
                                                      int col,
                                                      @NonNull Tile tile) {
        if (!tile.hasMonster()) {
            gridMonsterAnimations.remove(coordinateKey(row, col));
            return null;
        }
        String key = coordinateKey(row, col);
        AnimatedMonster animator = gridMonsterAnimations.get(key);
        if (animator == null) {
            animator = MonsterAnimationHelper.createAnimatedClone(this, tile.getMonster());
            if (animator != null) {
                gridMonsterAnimations.put(key, animator);
            }
        }
        return animator;
    }

    private void animateGridFrame() {
        if (gridLayout == null || dungeonGrid == null) {
            return;
        }
        AnimatedPlayer playerAnimator = profile != null ? profile.getAnimatedPlayer() : null;
        for (java.util.Iterator<Map.Entry<String, View>> it = activeAnimatedTiles.entrySet().iterator();
             it.hasNext(); ) {
            Map.Entry<String, View> entry = it.next();
            View tileView = entry.getValue();
            if (tileView == null || !tileView.isShown()) {
                continue;
            }
            Object tagRow = tileView.getTag(R.id.tag_row);
            Object tagCol = tileView.getTag(R.id.tag_col);
            if (!(tagRow instanceof Integer) || !(tagCol instanceof Integer)) {
                continue;
            }
            int row = (Integer) tagRow;
            int col = (Integer) tagCol;
            Tile tile = dungeonGrid[row][col];
            if (tile == null) {
                it.remove();
                continue;
            }
            ImageView tileImage = tileView.findViewById(R.id.imageTile);
            if (tile.hasPlayer() && playerAnimator != null && tileImage.getVisibility() == View.VISIBLE) {
                Bitmap frame = playerAnimator.getCurrentFrame();
                if (frame != null) {
                    tileImage.setImageBitmap(frame);
                    }
            } else if (tile.isRevealed() && tile.getType() == TileType.ENEMY && tile.hasMonster()) {
                AnimatedMonster animator = ensureGridAnimatedMonster(row, col, tile);
                if (animator != null && tileImage.getVisibility() == View.VISIBLE) {
                    Bitmap frame = animator.getCurrentFrame();
                    if (frame != null) {
                        tileImage.setImageBitmap(frame);
                    }
                }
            }
        }
    }

    private void updateAnimatedTileRegistry(int row, int col, @Nullable Tile tile, @NonNull View tileView) {
        String key = coordinateKey(row, col);
        boolean shouldAnimate = tile != null
                && (tile.hasPlayer()
                || (tile.isRevealed() && tile.getType() == TileType.ENEMY && tile.hasMonster()));
        if (shouldAnimate) {
            activeAnimatedTiles.put(key, tileView);
        } else {
            activeAnimatedTiles.remove(key);
        }
    }

    private void startGridAnimationLoop() {
        if (gridAnimationRunning) {
            return;
        }
        gridAnimationRunning = true;
        gridAnimationHandler.post(gridAnimationRunnable);
    }

    private void stopGridAnimationLoop() {
        if (!gridAnimationRunning) {
            return;
        }
        gridAnimationRunning = false;
        gridAnimationHandler.removeCallbacks(gridAnimationRunnable);
    }

    private void refreshTileViewAt(int row, int col) {
        if (!isValidGridPosition(row, col) || gridLayout == null || dungeonGrid == null) {
            return;
        }
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            Object tagRow = child.getTag(R.id.tag_row);
            Object tagCol = child.getTag(R.id.tag_col);
            if (tagRow instanceof Integer && tagCol instanceof Integer
                    && (Integer) tagRow == row
                    && (Integer) tagCol == col) {
                Tile tile = dungeonGrid[row][col];
                if (tile == null || !tile.isDirty()) {
                    return;
                }
                bindTileView(child, tile, row, col);
                break;
            }
        }
    }

    private void refreshTile(int row, int col) {
        if (!isValidGridPosition(row, col) || dungeonGrid == null) {
            return;
        }
        Tile tile = dungeonGrid[row][col];
        if (tile != null) {
            tile.setDirty(true);
        }
        refreshTileViewAt(row, col);
    }

    private void refreshActiveCombatTileView() {
        if (activeCombatTileView == null || activeCombatTile == null) {
            return;
        }
        Object tagRow = activeCombatTileView.getTag(R.id.tag_row);
        Object tagCol = activeCombatTileView.getTag(R.id.tag_col);
        if (tagRow instanceof Integer && tagCol instanceof Integer) {
            refreshTile((Integer) tagRow, (Integer) tagCol);
        }
    }

    private int getMonsterSpriteResource(@NonNull Monster monster) {
        String type = monster.getMonsterType();
        if (type == null) {
            return R.drawable.slime_sprite_sheet;
        }
        switch (type.toLowerCase(Locale.ROOT)) {
            case "goblin":
                return R.drawable.goblin_sprite_sheet;
            case "skeleton":
                return R.drawable.skeleton_sprite_sheet;
            case "orc":
                return R.drawable.orc_sprite_sheet;
            case "troll":
                return R.drawable.troll_sprite_sheet;
            case "witch":
                return R.drawable.witch_sprite_sheet;
            case "vampire":
                return R.drawable.vampire_sprite_sheet;
            case "demon":
                return R.drawable.demon_sprite_sheet;
            case "dragon":
                return R.drawable.dragon_sprite_sheet;
            case "rat":
                return R.drawable.rat_sprite_sheet;
            case "bat":
                return R.drawable.bat_sprite_sheet;
            case "spider":
                return R.drawable.spider_sprite_sheet;
            case "wolf":
                return R.drawable.wolf_sprite_sheet;
            case "bandit":
                return R.drawable.bandit_sprite_sheet;
            case "cultist":
                return R.drawable.cultist_sprite_sheet;
            case "warlock":
                return R.drawable.warlock_sprite_sheet;
            case "wraith":
                return R.drawable.wraith_sprite_sheet;
            case "golem":
                return R.drawable.golem_sprite_sheet;
            case "lich":
                return R.drawable.lich_sprite_sheet;
            case "hellhound":
                return R.drawable.hellhound_sprite_sheet;
            case "revenant":
                return R.drawable.revenant_sprite_sheet;
            case "archdemon":
                return R.drawable.archdemon_sprite_sheet;
            case "ancient wyrm":
            case "ancient_wyrm":
                return R.drawable.ancient_wyrm_sprite_sheet;
            default:
                return R.drawable.slime_sprite_sheet;
        }
    }

    @Nullable
    private Bitmap getMonsterBitmap(@NonNull Monster monster) {
        initBitmapCache();
        String key = monster.getMonsterType();
        if (TextUtils.isEmpty(key)) {
            key = "default";
        }
        Bitmap cached = monsterBitmapCache.get(key);
        if (cached == null) {
            int resId = getMonsterSpriteResource(monster);
            cached = BitmapFactory.decodeResource(getResources(), resId);
            if (cached != null) {
                monsterBitmapCache.put(key, cached);
            }
        }
        return cached;
    }

    private int getPlayerSpriteSheetResource() {
        if (profile == null || profile.getPlayerClass() == null) {
            return R.drawable.knight_sprite_sheet;
        }
        switch (profile.getPlayerClass()) {
            case THIEF:
                return R.drawable.thief_sprite_sheet;
            case WIZARD:
                return R.drawable.wizard_sprite_sheet;
            case RANGER:
                // Ranger sprite sheet not yet available; falls back to knight until v1.1.
            case KNIGHT:
            default:
                return R.drawable.knight_sprite_sheet;
        }
    }

    private int getPlayerIconResource() {
        PlayerClass playerClass = profile != null ? profile.getPlayerClass() : null;
        String className = playerClass != null ? playerClass.name().toLowerCase(Locale.ROOT) : "knight";
        int resId = getResources().getIdentifier("icon_" + className, "drawable", getPackageName());
        return resId != 0 ? resId : R.drawable.icon_knight;
    }

    private int getColorCompat(int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(colorRes);
        }
        return ContextCompat.getColor(this, colorRes);
    }

    // --- Combat entry and tile interaction ---
    /** Launches the combat dialog for the supplied monster tile and tracks rewards. */
    private void startCombat(Tile tile, View tileView) {
        if (tile == null || !tile.hasMonster() || profile == null) {
            return;
        }
        if (getSupportFragmentManager().findFragmentByTag(TAG_COMBAT_DIALOG) != null) {
            return;
        }
        Monster monster = tile.getMonster();
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster.getMonsterType());
        fragment.setCombatants(profile, monster);
        fragment.setCombatCallbacks(this);
        pendingCombatXpReward = GameBalance.calculateXpReward(monster, currentFloor, difficultyMode, random);
        pendingCombatGoldReward = GameBalance.calculateGoldReward(monster, currentFloor, difficultyMode, random);
        fragment.setRewardPreview(pendingCombatXpReward, pendingCombatGoldReward);
        activeCombatTile = tile;
        activeCombatTileView = tileView;
        fragment.show(getSupportFragmentManager(), TAG_COMBAT_DIALOG);
    }

    private void handleTileClick(int row, int col, View tileView) {
        if (pendingAbilityTargetMode != AbilityTargetMode.NONE) {
            handleAbilityTargetSelection(row, col);
            return;
        }
        Tile clickedTile = dungeonGrid[row][col];
        if (clickedTile.isRevealed() && clickedTile.getType() == TileType.ENEMY
                && clickedTile.hasMonster() && clickedTile.getMonster().getCurrentHP() > 0) {
            executeInlineCombatRound(row, col, clickedTile, tileView);
            return;
        }
        if (!clickedTile.isRevealed()) {
            if (isBlockedByMonster(row, col)) {
                Toast.makeText(this, R.string.blocked_by_monster, Toast.LENGTH_SHORT).show();
                return;
            }
            // Covered tiles use the current cover image; clicking one applies the floor terrain backdrop.
            applyTerrainBackgroundForCurrentTerrain();
            clickedTile.reveal();
            if (profile.getAnimatedPlayer() != null) {
                profile.getAnimatedPlayer().setAction("move");
            }
            TextView tileText = tileView.findViewById(R.id.textTile);
            revealTile(tileView, tileText, clickedTile);
            if (clickedTile.getType() != TileType.ENEMY) {
                revealedSafeTiles++;
                recordSafeTileReveal();
                updatePlayerPosition(row, col);
            }
            refreshTile(row, col);
            requestSave(SaveReason.TILE_REVEAL);
            checkVictoryCondition();
        }
    }

    private void updatePlayerPosition(int row, int col) {
        int clampedRow = clampGridIndex(row);
        int clampedCol = clampGridIndex(col);
        setPlayerFlag(clampedRow, clampedCol);
        playerRow = clampedRow;
        playerCol = clampedCol;
        verifyShieldAnchor();
    }

    private boolean isBlockedByMonster(int row, int col) {
        int[][] offsets = { {0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
        for (int[] offset : offsets) {
            int r = row + offset[0];
            int c = col + offset[1];
            if (!isValidGridPosition(r, c)) {
                continue;
            }
            Tile t = dungeonGrid[r][c];
            if (t != null && t.isRevealed() && t.getType() == TileType.ENEMY
                    && t.hasMonster() && t.getMonster().getCurrentHP() > 0) {
                return true;
            }
        }
        return false;
    }

    private void executeInlineCombatRound(int row, int col, Tile tile, View tileView) {
        if (profile == null || !tile.hasMonster()) {
            return;
        }
        Monster monster = tile.getMonster();
        int playerDamage = Math.max(1, profile.getTotalAttack() - monster.getDefense());
        monster.takeDamage(playerDamage);
        showFloatingDamage(tileView, "+" + playerDamage, 0xFFFFD700);
        if (monster.getCurrentHP() <= 0) {
            handleInlineCombatVictory(row, col, tile, tileView, monster);
            return;
        }
        int monsterDamage = Math.max(1, monster.getAttack() - profile.getTotalDefense());
        takeDamage(monsterDamage);
        showFloatingDamage(tileView, "-" + monsterDamage, 0xFFE53935);
        if (profile.isDead()) {
            return;
        }
        updateHpCounter();
        refreshTile(row, col);
    }

    private void handleInlineCombatVictory(int row, int col, Tile tile, View tileView, Monster monster) {
        boolean convertedEnemy = tile.getType() == TileType.ENEMY;
        tile.setMonster(null);
        tile.setType(TileType.EMPTY);
        int xpReward = pendingCombatXpReward > 0
                ? pendingCombatXpReward
                : GameBalance.calculateXpReward(monster, currentFloor, difficultyMode, random);
        awardExperience(xpReward);
        int goldReward = pendingCombatGoldReward > 0
                ? pendingCombatGoldReward
                : GameBalance.calculateGoldReward(monster, currentFloor, difficultyMode, random);
        currentGold += goldReward;
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        updatePlatinumCounter();
        updateGoldCounter();
        updateHpCounter();
        recordGoldEarned(goldReward);
        applyMonsterLoot(monster);
        recordEnemyDefeat();
        if (monster.isBoss()) {
            unlockAchievement(R.string.achievement_boss_slayer_title);
        }
        FeedbackManager.playSound(this, FeedbackManager.SoundEffect.COMBAT_VICTORY);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
        String goldFragment = getResources().getQuantityString(
                R.plurals.combat_gold_reward, goldReward, goldReward);
        Toast.makeText(this,
                getString(R.string.combat_result_victory, monster.getMonsterType(), xpReward, goldFragment),
                Toast.LENGTH_SHORT).show();
        if (convertedEnemy) {
            safeTilesToReveal++;
            revealedSafeTiles++;
        }
        updatePlayerPosition(row, col);
        refreshTile(row, col);
        clearCombatTracking();
        requestSave(SaveReason.COMBAT_VICTORY);
        if (convertedEnemy) {
            checkVictoryCondition();
        }
    }

    private void showFloatingDamage(View tileView, String text, int color) {
        if (tileView == null) {
            return;
        }
        TextView floater = tileView.findViewById(R.id.textFloatingDamage);
        if (floater == null) {
            return;
        }
        floater.setText(text);
        floater.setTextColor(color);
        floater.setVisibility(View.VISIBLE);
        floater.setAlpha(1f);
        floater.setTranslationY(0f);
        floater.animate()
                .translationY(-getResources().getDimension(R.dimen.floating_damage_offset))
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() -> floater.setVisibility(View.GONE))
                .start();
    }

    private int clampGridIndex(int value) {
        if (value < 0 || value >= GRID_SIZE) {
            return Math.max(0, Math.min(GRID_SIZE - 1, value));
        }
        return value;
    }

    private void setPlayerFlag(int row, int col) {
        if (dungeonGrid == null) {
            return;
        }
        int previousRow = lastPlayerRow;
        int previousCol = lastPlayerCol;
        if (isValidGridPosition(previousRow, previousCol)) {
            dungeonGrid[previousRow][previousCol].setHasPlayer(false);
            refreshTileViewAt(previousRow, previousCol);
        }
        if (isValidGridPosition(row, col)) {
            dungeonGrid[row][col].setHasPlayer(true);
            lastPlayerRow = row;
            lastPlayerCol = col;
            refreshTileViewAt(row, col);
        } else {
            lastPlayerRow = -1;
            lastPlayerCol = -1;
        }
    }

    private boolean isValidGridPosition(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    private void refreshPlayerTileFlags() {
        if (dungeonGrid == null) {
            return;
        }
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Tile tile = dungeonGrid[r][c];
                if (tile != null) {
                    tile.setHasPlayer(false);
                }
            }
        }
        if (isValidGridPosition(playerRow, playerCol)) {
            dungeonGrid[playerRow][playerCol].setHasPlayer(true);
            lastPlayerRow = playerRow;
            lastPlayerCol = playerCol;
            refreshTileViewAt(playerRow, playerCol);
        } else {
            lastPlayerRow = -1;
            lastPlayerCol = -1;
        }
        activePlayerTileView = null;
    }

    // --- Animation helpers ---
    private void ensureAnimatedPlayer() {
        if (profile == null || profile.getPlayerClass() == null) {
            return;
        }
        if (profile.getAnimatedPlayer() == null) {
            profile.setAnimatedPlayer(new AnimatedPlayer(
                    this,
                    profile.getPlayerClass(),
                    64,
                    64,
                    4,
                    120));
        }
    }

    private void clearAnimationResources() {
        gridMonsterAnimations.clear();
        activeAnimatedTiles.clear();
        if (monsterBitmapCache != null) {
            monsterBitmapCache.evictAll();
            monsterBitmapCache = null;
        }
        if (profile != null && profile.getAnimatedPlayer() != null) {
            profile.getAnimatedPlayer().reset();
            profile.setAnimatedPlayer(null);
        }
    }

    // --- Ability execution and targeting ---
    private void handleAbilityTargetSelection(int row, int col) {
        if (pendingAbilityTargetMode == AbilityTargetMode.NONE) {
            return;
        }
        if (isPlayerPositionKnownInvert()) {
            Toast.makeText(this, R.string.player_position_unknown, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isTargetWithinAbilityRangeInvert(row, col)) {
            String message = getResources().getQuantityString(
                    R.plurals.ability_range_error, ABILITY_RANGE, ABILITY_RANGE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean resolved = false;
        switch (pendingAbilityTargetMode) {
            case WIZARD_FIREBALL:
                resolved = executeWizardFireball(row, col);
                break;
            case WIZARD_FROST_NOVA:
                resolved = executeWizardFrostNova(row, col);
                break;
            case WIZARD_CHAIN_LIGHTNING:
                resolved = executeWizardChainLightning(row, col);
                break;
            case WIZARD_METEOR:
                resolved = executeWizardMeteor(row, col);
                break;
            case THIEF_SCAN:
                resolved = executeThiefScan(row, col);
                break;
            case THIEF_SHADOWSTEP:
                resolved = executeThiefShadowstep(row, col);
                break;
            case THIEF_DISARM_EXPERT:
                resolved = executeThiefDisarmExpert(row, col);
                break;
            case THIEF_AMBUSH:
                resolved = executeThiefAmbush(row, col);
                break;
            case KNIGHT_SHIELD:
                resolved = deployKnightShield(row, col);
                break;
            case KNIGHT_TAUNT:
                resolved = executeKnightTaunt(row, col);
                break;
            case KNIGHT_VALIANT_STRIKE:
                resolved = executeKnightValiantStrike(row, col);
                break;
            case NONE:
            default:
                break;
        }
        if (resolved) {
            consumeAbilityUse();
            requestSave(SaveReason.ABILITY_USED);
        }
    }

    private boolean isPlayerPositionKnownInvert() {
        return playerRow < 0 || playerCol < 0;
    }

    private boolean isTargetWithinAbilityRangeInvert(int row, int col) {
        if (isPlayerPositionKnownInvert()) {
            return true;
        }
        int distance = Math.abs(row - playerRow) + Math.abs(col - playerCol);
        return distance > ABILITY_RANGE;
    }

    private boolean executeWizardFireball(int row, int col) {
        Tile tile = dungeonGrid[row][col];
        if (tile == null) {
            return false;
        }
        TextView tileText = getTileTextView(row, col);
        if (!playClassSound("attack")) {
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        }
        tile.reveal();
        updateTileTextDisplay(tileText, tile);
        int damage = calculateFireballDamage();
        if (tile.hasMonster()) {
            Monster monster = tile.getMonster();
            monster.takeDamage(damage);
            if (monster.isDead()) {
                handleAbilityMonsterDefeat(monster, tile, tileText, row, col);
                Toast.makeText(this,
                        getString(R.string.fireball_result_kill, monster.getMonsterType()),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        getResources().getQuantityString(R.plurals.fireball_result_hit, damage, damage),
                        Toast.LENGTH_SHORT).show();
            }
            FeedbackManager.playSound(this, FeedbackManager.SoundEffect.POSITIVE);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
        } else {
            Toast.makeText(this, R.string.fireball_result_whiff, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean executeWizardFrostNova(int row, int col) {
        if (!playClassSound("attack")) {
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        }
        boolean affectedAny = false;
        int damage = 2 + getAbilityLevelScale();
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (!isValidGridPosition(r, c)) {
                    continue;
                }
                Tile tile = dungeonGrid[r][c];
                if (tile == null) {
                    continue;
                }
                tile.reveal();
                if (isTrapTile(tile)) {
                    affectedAny = true;
                }
                if (tile.hasMonster()) {
                    affectedAny = true;
                    applyAbilityDamageToTile(r, c, damage, false);
                }
                refreshTile(r, c);
            }
        }
        Toast.makeText(this,
                affectedAny ? R.string.frost_nova_hit : R.string.frost_nova_no_targets,
                Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeWizardChainLightning(int row, int col) {
        if (!playClassSound("attack")) {
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        }
        int damage = 3 + getAbilityLevelScale();
        boolean hitAny = false;
        hitAny |= applyAbilityDamageToTile(row, col, damage, true);
        int[][] offsets = { {1,0}, {-1,0}, {0,1}, {0,-1} };
        for (int[] offset : offsets) {
            int r = row + offset[0];
            int c = col + offset[1];
            if (!isValidGridPosition(r, c)) {
                continue;
            }
            if (applyAbilityDamageToTile(r, c, Math.max(1, damage - 1), false)) {
                hitAny = true;
            }
        }
        if (!hitAny) {
            Toast.makeText(this, R.string.chain_lightning_no_targets, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean executeWizardMeteor(int row, int col) {
        if (!playClassSound("attack")) {
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        }
        int damage = 6 + (getAbilityLevelScale() * 2);
        boolean affectedAny = false;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (!isValidGridPosition(r, c)) {
                    continue;
                }
                Tile tile = dungeonGrid[r][c];
                if (tile == null) {
                    continue;
                }
                tile.reveal();
                if (isTrapTile(tile)) {
                    tile.setType(TileType.EMPTY);
                    affectedAny = true;
                }
                if (tile.hasMonster()) {
                    affectedAny = true;
                    applyAbilityDamageToTile(r, c, damage, false);
                }
                refreshTile(r, c);
            }
        }
        Toast.makeText(this,
                affectedAny ? R.string.meteor_hit : R.string.meteor_no_targets,
                Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeThiefScan(int row, int col) {
        if (!playClassSound("move")) {
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        }
        boolean revealedAny = false;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r < 0 || c < 0 || r >= GRID_SIZE || c >= GRID_SIZE) {
                    continue;
                }
                Tile tile = dungeonGrid[r][c];
                if (!isTrapTile(tile)) {
                    continue;
                }
                boolean wasRevealed = tile.isRevealed();
                tile.reveal();
                refreshTile(r, c);
                if (!wasRevealed) {
                    revealedAny = true;
                }
            }
        }
        Toast.makeText(this,
                revealedAny ? R.string.thief_scan_result : R.string.thief_scan_no_traps,
                Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeThiefShadowstep(int row, int col) {
        Tile tile = dungeonGrid[row][col];
        if (tile == null) {
            return false;
        }
        if (!tile.isRevealed() || tile.getType() == TileType.ENEMY || isTrapTile(tile)) {
            Toast.makeText(this, R.string.shadowstep_invalid_target, Toast.LENGTH_SHORT).show();
            return false;
        }
        tile.reveal();
        updatePlayerPosition(row, col);
        if (profile != null && profile.getAnimatedPlayer() != null) {
            profile.getAnimatedPlayer().setAction("move");
        }
        playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        Toast.makeText(this, R.string.shadowstep_success, Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeThiefDisarmExpert(int row, int col) {
        boolean usedKit = InventoryManager.getItemQuantity(this, "Trap Disarm Kit") > 0;
        boolean affectedAny = false;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (!isValidGridPosition(r, c)) {
                    continue;
                }
                Tile tile = dungeonGrid[r][c];
                if (tile == null || !isTrapTile(tile)) {
                    continue;
                }
                affectedAny = true;
                tile.reveal();
                if (usedKit) {
                    tile.setType(TileType.EMPTY);
                }
                refreshTile(r, c);
            }
        }
        if (!affectedAny) {
            Toast.makeText(this, R.string.disarm_no_traps, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (usedKit) {
            InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", -1);
            addInventoryChange("Trap Disarm Kit", false);
            Toast.makeText(this, R.string.disarm_used_kit, Toast.LENGTH_SHORT).show();
            requestSave(SaveReason.INVENTORY_CHANGE);
        } else {
            Toast.makeText(this, R.string.disarm_revealed_traps, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean executeThiefAmbush(int row, int col) {
        Tile tile = dungeonGrid[row][col];
        if (tile == null || !tile.hasMonster()) {
            Toast.makeText(this, R.string.ambush_no_target, Toast.LENGTH_SHORT).show();
            return false;
        }
        tile.reveal();
        int damage = 4 + getAbilityLevelScale();
        Monster monster = tile.getMonster();
        monster.takeDamage(damage);
        if (monster.isDead()) {
            handleAbilityMonsterDefeat(monster, tile, getTileTextView(row, col), row, col);
            Toast.makeText(this, R.string.ambush_kill, Toast.LENGTH_SHORT).show();
            return true;
        }
        View tileView = getTileViewAt(row, col);
        executeInlineCombatRound(row, col, tile, tileView);
        Toast.makeText(this, R.string.ambush_engage, Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeThiefVeilOfSmoke() {
        int centerRow = playerRow;
        int centerCol = playerCol;
        for (int r = centerRow - 1; r <= centerRow + 1; r++) {
            for (int c = centerCol - 1; c <= centerCol + 1; c++) {
                if (!isValidGridPosition(r, c)) {
                    continue;
                }
                Tile tile = dungeonGrid[r][c];
                if (tile != null) {
                    tile.reveal();
                    refreshTile(r, c);
                }
            }
        }
        smokeVeilCharges = 1;
        Toast.makeText(this, R.string.smoke_veil_ready, Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean deployKnightShield(int row, int col) {
        if (isTargetWithinAbilityRangeInvert(row, col)) {
            String message = getResources().getQuantityString(
                    R.plurals.ability_range_error, ABILITY_RANGE, ABILITY_RANGE);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return false;
        }
        Tile tile = dungeonGrid[row][col];
        if (tile == null || !tile.isRevealed()) {
            Toast.makeText(this, R.string.knight_shield_requires_visible, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tile.getType() == TileType.ENEMY || isTrapTile(tile)) {
            Toast.makeText(this, R.string.knight_shield_invalid_tile, Toast.LENGTH_SHORT).show();
            return false;
        }
        knightShieldStrength = calculateKnightShieldStrength();
        knightShieldRow = row;
        knightShieldCol = col;
        knightShieldActive = true;
        playerRow = row;
        playerCol = col;
        if (!playClassSound("defend")) {
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        }
        String message = getResources().getQuantityString(
                R.plurals.knight_shield_activated, knightShieldStrength, knightShieldStrength);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        updateAbilityButtonState();
        return true;
    }

    private boolean executeKnightTaunt(int row, int col) {
        Tile tile = dungeonGrid[row][col];
        if (tile == null || !tile.hasMonster()) {
            Toast.makeText(this, R.string.taunt_no_target, Toast.LENGTH_SHORT).show();
            return false;
        }
        tile.reveal();
        playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        refreshTile(row, col);
        Toast.makeText(this, R.string.taunt_engage, Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeKnightValiantStrike(int row, int col) {
        Tile tile = dungeonGrid[row][col];
        if (tile == null || !tile.hasMonster()) {
            Toast.makeText(this, R.string.valiant_no_target, Toast.LENGTH_SHORT).show();
            return false;
        }
        tile.reveal();
        int damage = 6 + (getAbilityLevelScale() * 2);
        Monster monster = tile.getMonster();
        monster.takeDamage(damage);
        if (monster.isDead()) {
            handleAbilityMonsterDefeat(monster, tile, getTileTextView(row, col), row, col);
            Toast.makeText(this, R.string.valiant_kill, Toast.LENGTH_SHORT).show();
            return true;
        }
        View tileView = getTileViewAt(row, col);
        executeInlineCombatRound(row, col, tile, tileView);
        Toast.makeText(this, R.string.valiant_engage, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void clearStatusEffects() {
        frozenTurnsLeft = 0;
        poisonTurnsLeft = 0;
        updateStatusText();
    }

    /**
     * Fully resets all transient run state before starting a new game or restarting.
     * Safe to call on both in-place restarts (same Activity) and before finish().
     */
    private void resetRunState() {
        // Cancel any pending poison tick — it self-schedules and will survive across restarts
        // if not explicitly removed.
        statusEffectHandler.removeCallbacksAndMessages(null);
        frozenTurnsLeft = 0;
        poisonTurnsLeft = 0;

        // Clear animation tracking so stale entries from the previous dungeon don't bleed in.
        stopGridAnimationLoop();
        gridMonsterAnimations.clear();
        activeAnimatedTiles.clear();

        // Clear any in-flight combat state.
        clearCombatTracking();

        // Reset ability targeting mode.
        pendingAbilityTargetMode = AbilityTargetMode.NONE;

        // Knight shield must be off at the start of every run.
        clearKnightShield(0);
    }

    private void healPlayer(int amount) {
        if (profile != null) {
            profile.setCurrentHP(Math.min(profile.getMaxHP(), profile.getCurrentHP() + amount));
            updateHpCounter();
        }
    }

    private boolean executeKnightFortify() {
        clearStatusEffects();
        int heal = 4 + getAbilityLevelScale();
        healPlayer(heal);
        playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        Toast.makeText(this, R.string.fortify_ready, Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeKnightGuardiansOath() {
        clearStatusEffects();
        int heal = 6 + (getAbilityLevelScale() * 2);
        healPlayer(heal);
        knightShieldStrength = calculateKnightShieldStrength();
        knightShieldRow = playerRow;
        knightShieldCol = playerCol;
        knightShieldActive = true;
        playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        Toast.makeText(this, R.string.guardians_oath_ready, Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean executeWizardArcaneShield() {
        if (profile == null || !profile.usesMp()) {
            return false;
        }
        int restore = 4 + getAbilityLevelScale();
        profile.setCurrentMP(Math.min(profile.getMaxMP(), profile.getCurrentMP() + restore));
        updateMpCounter();
        playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
        Toast.makeText(this, R.string.arcane_shield_ready, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void handleAbilityMonsterDefeat(Monster monster,
                                           Tile tile,
                                           TextView tileText,
                                           int row,
                                           int col) {
        tile.setMonster(null);
        tile.setType(TileType.EMPTY);
        if (tileText != null) {
            tileText.setText(getString(R.string.combat_tile_cleared));
            tileText.setContentDescription(getTileContentDescription(tile));
        }
        int xpReward = GameBalance.calculateXpReward(monster, currentFloor, difficultyMode, random);
        awardExperience(xpReward);
        int goldReward = GameBalance.calculateGoldReward(monster, currentFloor, difficultyMode, random);
        currentGold += goldReward;
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        updateGoldCounter();
        recordGoldEarned(goldReward);
        applyMonsterLoot(monster);
        recordEnemyDefeat();
        if (monster.isBoss()) {
            int bossXp = GameBalance.calculateBossXpReward(currentFloor, difficultyMode);
            int bossGold = GameBalance.calculateBossGoldReward(currentFloor, difficultyMode);
            awardExperience(bossXp);
            currentGold += bossGold;
            InventoryManager.syncGoldWithCurrentRun(this, currentGold);
            updateGoldCounter();
            unlockAchievement(R.string.achievement_boss_slayer_title);
            Toast.makeText(this,
                    getString(R.string.boss_reward_message, bossXp, bossGold),
                    Toast.LENGTH_SHORT).show();
        }
        if (revealedSafeTiles < safeTilesToReveal) {
            revealedSafeTiles++;
            recordSafeTileReveal();
        }
        checkVictoryCondition();
    }

    private boolean isTrapTile(Tile tile) {
        if (tile == null) {
            return false;
        }
        TileType type = tile.getType();
        return type == TileType.TRAP_FIRE
                || type == TileType.TRAP_ACID
                || type == TileType.TRAP_POISON
                || type == TileType.TRAP_FREEZE
                || type == TileType.TRAP_PITFALL;
    }

    private int calculateFireballDamage() {
        int level = profile != null ? Math.max(1, profile.getLevel()) : 1;
        return 4 + (level * 2);
    }

    private int calculateKnightShieldStrength() {
        int level = profile != null ? Math.max(1, profile.getLevel()) : 1;
        return 6 + (level * 3);
    }

    private boolean applyAbilityDamageToTile(int row, int col, int damage, boolean showToast) {
        if (!isValidGridPosition(row, col)) {
            return false;
        }
        Tile tile = dungeonGrid[row][col];
        if (tile == null || !tile.hasMonster()) {
            return false;
        }
        tile.reveal();
        Monster monster = tile.getMonster();
        monster.takeDamage(damage);
        if (monster.isDead()) {
            handleAbilityMonsterDefeat(monster, tile, getTileTextView(row, col), row, col);
            if (showToast) {
                Toast.makeText(this, R.string.ability_kill_confirmed, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        tile.setDirty(true);
        refreshTile(row, col);
        return true;
    }

    private int getAbilityLevelScale() {
        return profile != null ? Math.max(1, profile.getLevel()) : 1;
    }

    @Nullable
    private TextView getTileTextView(int row, int col) {
        int index = (row * GRID_SIZE) + col;
        if (index < 0 || index >= gridLayout.getChildCount()) {
            return null;
        }
        View child = gridLayout.getChildAt(index);
        if (child == null) {
            return null;
        }
        return child.findViewById(R.id.textTile);
    }

    @Nullable
    private View getTileViewAt(int row, int col) {
        int index = (row * GRID_SIZE) + col;
        if (index < 0 || index >= gridLayout.getChildCount()) {
            return null;
        }
        return gridLayout.getChildAt(index);
    }

    private void verifyShieldAnchor() {
        if (!knightShieldActive) {
            return;
        }
        if (playerRow != knightShieldRow || playerCol != knightShieldCol) {
            clearKnightShield(R.string.knight_shield_moved);
        }
    }

    private void clearKnightShield(@StringRes int messageResId) {
        if (knightShieldActive && messageResId != 0) {
            Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        }
        knightShieldActive = false;
        knightShieldStrength = 0;
        knightShieldRow = -1;
        knightShieldCol = -1;
    }

    private int applyKnightShield(int incomingDamage) {
        if (!knightShieldActive || incomingDamage <= 0) {
            return incomingDamage;
        }
        if (playerRow != knightShieldRow || playerCol != knightShieldCol) {
            // The shield only applies while the player stays on the placement tile.
            clearKnightShield(0);
            return incomingDamage;
        }
        int absorbed = Math.min(knightShieldStrength, incomingDamage);
        knightShieldStrength -= absorbed;
        incomingDamage -= absorbed;
        if (absorbed > 0) {
            Toast.makeText(this,
                    getResources().getQuantityString(R.plurals.knight_shield_absorb, absorbed, absorbed),
                    Toast.LENGTH_SHORT).show();
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
        }
        if (knightShieldStrength <= 0) {
            clearKnightShield(R.string.knight_shield_broken);
        }
        return incomingDamage;
    }

    private void resetPlayerPositionToCenter() {
        playerRow = GRID_SIZE / 2;
        playerCol = GRID_SIZE / 2;
        refreshPlayerTileFlags();
    }

    private void initBitmapCache() {
        if (monsterBitmapCache != null) {
            return;
        }
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = Math.max(4096, maxMemory / 24);
        cacheSize = Math.min(cacheSize, 8192);
        monsterBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value == null ? 0 : value.getByteCount() / 1024;
            }
        };
    }

    private void preWarmMonsterBitmaps(@Nullable MonsterTemplate[] pool) {
        if (pool == null || pool.length == 0) {
            return;
        }
        initBitmapCache();
        for (MonsterTemplate template : pool) {
            if (template == null) {
                continue;
            }
            Monster stub = new Monster(template.name, 1, 1, 0, "");
            getMonsterBitmap(stub);
        }
    }


    private void restoreRunMetadata(@Nullable SaveManager.RunMetadata metadata) {
        if (metadata == null) {
            resetPlayerPositionToCenter();
            clearKnightShield(0);
            nextAbilityAvailableFloor = currentFloor;
            currentTerrain = selectTerrainForFloor(currentFloor);
            return;
        }
        playerRow = metadata.playerRow >= 0 ? clampGridIndex(metadata.playerRow) : GRID_SIZE / 2;
        playerCol = metadata.playerCol >= 0 ? clampGridIndex(metadata.playerCol) : GRID_SIZE / 2;
        nextAbilityAvailableFloor = Math.max(currentFloor, metadata.nextAbilityAvailableFloor);
        currentTerrain = TerrainType.fromName(metadata.currentTerrain);
        if (currentTerrain == null) {
            currentTerrain = selectTerrainForFloor(currentFloor);
        }
        if (metadata.knightShieldActive && metadata.knightShieldStrength > 0) {
            knightShieldActive = true;
            knightShieldStrength = metadata.knightShieldStrength;
            knightShieldRow = metadata.knightShieldRow >= 0
                    ? clampGridIndex(metadata.knightShieldRow) : -1;
            knightShieldCol = metadata.knightShieldCol >= 0
                    ? clampGridIndex(metadata.knightShieldCol) : -1;
        } else {
            clearKnightShield(0);
        }
        refreshPlayerTileFlags();
    }

    private void handleGameOver() {
        if (profile != null) {
            TelemetryManager.logRunFailed(
                    currentFloor,
                    profile.getPlayerClass() != null ? profile.getPlayerClass().name() : "UNKNOWN",
                    profile.getLevel(),
                    currentGold);
            profile.setCurrentHP(profile.getMaxHP());
        }
        clearPersistedState();
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over_title)
                .setMessage(R.string.game_over_message)
                .setPositiveButton(R.string.restart, (dialog, which) -> restartGame())
                .setNegativeButton(R.string.main_menu, (dialog, which) -> goToMainMenu())
                .setCancelable(false)
                .show();
    }

    private void restartGame() {
        if (profile != null) {
            profile.setCurrentHP(profile.getMaxHP());
        }
        Intent intent = new Intent(this, GameActivity.class);
        if (activeSlotIndex >= 0) {
            intent.putExtra(EXTRA_SLOT_INDEX, activeSlotIndex);
            intent.putExtra(EXTRA_IS_NEW_GAME, true);
            intent.putExtra(EXTRA_PROFILE_JSON, new Gson().toJson(profile));
        }
        startActivity(intent);
        finish();
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        // Cancel poison tick and any other delayed status-effect callbacks.
        statusEffectHandler.removeCallbacksAndMessages(null);
        stopGridAnimationLoop();
        // Clean up save scheduling/executor to avoid background work after destroy.
        saveHandler.removeCallbacks(debouncedSaveRunnable);
        if (saveExecutor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) saveExecutor).shutdown();
        }
        clearAnimationResources();
        super.onDestroy();
    }

    private void playCueWithFallback(@Nullable String soundKey,
                                     @Nullable FeedbackManager.SoundEffect fallback) {
        boolean played = !TextUtils.isEmpty(soundKey) && SoundManager.playAndReport(soundKey);
        if (!played && fallback != null) {
            FeedbackManager.playSound(this, fallback);
        }
    }

    private boolean playClassSound(@NonNull String action) {
        if (profile == null || profile.getPlayerClass() == null) {
            return false;
        }
        String key = profile.getPlayerClass().name().toLowerCase(Locale.ROOT) + "_" + action.toLowerCase(Locale.ROOT);
        return SoundManager.playAndReport(key);
    }

    // --- Tile reveal, loot, keys, and traps ---
    private void revealTile(View tileView, TextView tileText, Tile tile) {
        normalizeLegacyTile(tile);
        updateTileTextDisplay(tileText, tile);
        switch (tile.getType()) {
            case GOLD:
                int goldFound = GameBalance.calculateGoldPile(currentFloor, difficultyMode, random);
                currentGold += goldFound;
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
                updateGoldCounter();
                playCueWithFallback(SoundManager.KEY_EFFECT_TREASURE, FeedbackManager.SoundEffect.TREASURE);
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
                recordGoldEarned(goldFound);
                String message = getResources().getQuantityString(
                        R.plurals.gold_found_message, goldFound, goldFound);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                requestSave(SaveReason.INVENTORY_CHANGE);
                break;

            case ENEMY:
                if (tile.hasMonster()) {
                    playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
                }
                break;

            case STAIR_DOWN:
                awardFloorClearXp(currentFloor);
                playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
                fallToNextFloor();
                break;

            case STAIR_DOWN_LOCKED:
                handleLockedStair(tileText, tile);
                break;

            case CHEST:
                handleChest(tileText, tile);
                break;

            case SMALL_KEY:
            case BIG_KEY:
                collectKey(tile, tileText);
                break;

            case STAIR_UP:
                // Visual update already applied above.
                playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
                break;

            case EMPTY:
                playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
                break;

            default:
                handleTrap(tileText, tile);
                break;
        }
    }

    private void collectKey(Tile tile, TextView tileText) {
        TileType originalType = tile.getType();
        String inventoryKeyName = getInventoryKeyNameForTile(tile);
        if (TextUtils.isEmpty(inventoryKeyName)) {
            return;
        }
        String displayName = getKeyDisplayNameForTile(tile);
        InventoryManager.adjustItemQuantity(this, inventoryKeyName, 1);
        playCueWithFallback(SoundManager.KEY_EFFECT_KEY_PICKUP, FeedbackManager.SoundEffect.POSITIVE);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
        Toast.makeText(this, getString(R.string.key_found_message, displayName), Toast.LENGTH_SHORT).show();
        awardItemFoundXp();
        addInventoryChange(displayName, true);
        tile.setType(TileType.EMPTY);
        tile.setMonster(null);
        updateTileTextDisplay(tileText, tile);
        requestSave(SaveReason.INVENTORY_CHANGE);
    }

    private void handleLockedStair(TextView tileText, Tile tile) {
        String neededKey;
        String neededKeyDisplay;
        if (tile.getType() != TileType.STAIR_DOWN_LOCKED) {
            return;
        }
        neededKey = getBigKeyNameForFloor(currentFloor);
        neededKeyDisplay = getBigKeyDisplayName(currentFloor);
        if (TextUtils.isEmpty(neededKey) || TextUtils.isEmpty(neededKeyDisplay)) {
            return;
        }

        if (InventoryManager.getItemQuantity(this, neededKey) > 0) {
            InventoryManager.adjustItemQuantity(this, neededKey, -1);
            Toast.makeText(this,
                    getString(R.string.locked_stair_unlocked, neededKeyDisplay),
                    Toast.LENGTH_SHORT).show();
            addInventoryChange(neededKeyDisplay, false);
            playCueWithFallback(SoundManager.KEY_EFFECT_KEY_PICKUP, FeedbackManager.SoundEffect.POSITIVE);
            awardFloorClearXp(currentFloor);
            fallToNextFloor();
        } else {
            Toast.makeText(this,
                    getString(R.string.locked_stair_missing, neededKeyDisplay),
                    Toast.LENGTH_SHORT).show();
        }
        updateTileTextDisplay(tileText, tile);
    }

    private void handleChest(TextView tileText, Tile tile) {
        if (InventoryManager.getItemQuantity(this, SMALL_KEY_NAME) <= 0) {
            Toast.makeText(this, R.string.chest_requires_key, Toast.LENGTH_SHORT).show();
            return;
        }
        InventoryManager.adjustItemQuantity(this, SMALL_KEY_NAME, -1);
        addInventoryChange(SMALL_KEY_NAME, false);
        playCueWithFallback(SoundManager.KEY_EFFECT_CHEST, FeedbackManager.SoundEffect.TREASURE);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);

        int roll = random.nextInt(100);
        if (roll < CHEST_MAGIC_ITEM_CHANCE) {
            String magicItem = MAGIC_ITEM_POOL[random.nextInt(MAGIC_ITEM_POOL.length)];
            InventoryManager.adjustItemQuantity(this, magicItem, 1);
            awardItemFoundXp();
            addInventoryChange(magicItem, true);
            Toast.makeText(this, getString(R.string.chest_found_magic, magicItem), Toast.LENGTH_SHORT).show();
        } else if (roll < CHEST_MAGIC_ITEM_CHANCE + CHEST_ITEM_CHANCE) {
            List<ShopItem> items = GameBalance.loadShopItems(this);
            if (!items.isEmpty()) {
                ShopItem item = items.get(random.nextInt(items.size()));
                InventoryManager.adjustItemQuantity(this, item.getName(), 1);
                awardItemFoundXp();
                addInventoryChange(item.getName(), true);
                Toast.makeText(this, getString(R.string.chest_found_item, item.getName()), Toast.LENGTH_SHORT).show();
            } else {
                roll = 100; // fall back to gold if no items exist.
            }
        }

        if (roll >= CHEST_MAGIC_ITEM_CHANCE + CHEST_ITEM_CHANCE) {
            int goldFound = GameBalance.calculateGoldPile(currentFloor, difficultyMode, random);
            currentGold += goldFound;
            InventoryManager.syncGoldWithCurrentRun(this, currentGold);
            updateGoldCounter();
            recordGoldEarned(goldFound);
            String message = getResources().getQuantityString(
                    R.plurals.chest_found_gold, goldFound, goldFound);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        tile.setType(TileType.EMPTY);
        tile.setMonster(null);
        updateTileTextDisplay(tileText, tile);
        requestSave(SaveReason.INVENTORY_CHANGE);
    }

    private String getInventoryKeyNameForTile(Tile tile) {
        if (tile == null) {
            return null;
        }
        TileType type = tile.getType();
        switch (type) {
            case SMALL_KEY:
                return SMALL_KEY_NAME;
            case BIG_KEY:
                if (!TextUtils.isEmpty(tile.getCustomName())) {
                    return tile.getCustomName();
                }
                return getBigKeyNameForFloor(currentFloor);
            default:
                return getInventoryKeyNameForType(type);
        }
    }

    private String getKeyDisplayNameForTile(Tile tile) {
        if (tile == null) {
            return null;
        }
        if (!TextUtils.isEmpty(tile.getCustomName())) {
            return tile.getCustomName();
        }
        TileType type = tile.getType();
        switch (type) {
            case SMALL_KEY:
                return getString(R.string.tile_desc_key_small);
            case BIG_KEY:
                return getBigKeyDisplayName(currentFloor);
            default:
                return getKeyDisplayName(type, null);
        }
    }

    private String getBigKeyNameForFloor(int floor) {
        return DungeonGenerator.getBigKeyNameForFloor(floor);
    }

    private String getBigKeyDisplayName(int floor) {
        return DungeonGenerator.getBigKeyNameForFloor(floor);
    }

    private void normalizeLegacyTile(@Nullable Tile tile) {
        if (tile == null) {
            return;
        }
        TileType type = tile.getType();
        if (type == TileType.BIG_KEY && TextUtils.isEmpty(tile.getCustomName())) {
            tile.setCustomName(DungeonGenerator.getBigKeyNameForFloor(currentFloor));
        }
    }

    private String getInventoryKeyNameForType(TileType type) {
        switch (type) {
            case SMALL_KEY:
                return SMALL_KEY_NAME;
            default:
                return null;
        }
    }

    private String getKeyDisplayName(TileType type, String customName) {
        if (!TextUtils.isEmpty(customName)) {
            return customName;
        }
        switch (type) {
            case SMALL_KEY:
                return getString(R.string.tile_desc_key_small);
            case BIG_KEY:
                return getBigKeyDisplayName(currentFloor);
            default:
                return "";
        }
    }

    private void handleTrap(TextView tileText, Tile tile) {
        updateTileTextDisplay(tileText, tile);
        if (smokeVeilCharges > 0) {
            smokeVeilCharges = Math.max(0, smokeVeilCharges - 1);
            tile.setType(TileType.EMPTY);
            tile.setMonster(null);
            updateTileTextDisplay(tileText, tile);
            Toast.makeText(this, R.string.smoke_veil_triggered, Toast.LENGTH_SHORT).show();
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
            return;
        }
        if (InventoryManager.getItemQuantity(this, "Trap Disarm Kit") > 0) {
            Toast.makeText(this, R.string.trap_disarmed, Toast.LENGTH_SHORT).show();
            InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", -1);
            addInventoryChange("Trap Disarm Kit", false);
            awardTrapDisabledXp();
            tile.setType(TileType.EMPTY);
            tile.setMonster(null);
            updateTileTextDisplay(tileText, tile);
            unlockAchievement(R.string.achievement_trap_dodger_title);
            playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
            return;
        }

        switch (tile.getType()) {
            case TRAP_FIRE:
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
                applyTrapDamage();
                break;
            case TRAP_ACID:
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
                applyTrapDamage();
                break;
            case TRAP_POISON:
                poisonTurnsLeft = 3;
                poisonTick();
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
                playCueWithFallback(SoundManager.KEY_EFFECT_TRAP, FeedbackManager.SoundEffect.TRAP);
                break;
            case TRAP_FREEZE:
                frozenTurnsLeft = 2 + random.nextInt(2);
                updateStatusText();
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
                playCueWithFallback(SoundManager.KEY_EFFECT_TRAP, FeedbackManager.SoundEffect.TRAP);
                break;
            case TRAP_PITFALL:
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.HEAVY);
                playCueWithFallback(SoundManager.KEY_EFFECT_TRAP, FeedbackManager.SoundEffect.TRAP);
                fallToNextFloor();
                break;
            default:
                break;
        }
    }

    // --- Status effects and trap damage ---
    private void poisonTick() {
        if (poisonTurnsLeft > 0) {
            poisonTurnsLeft--;
            applyTrapDamage();
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
            updateStatusText();
            statusEffectHandler.postDelayed(this::poisonTick, 1500);
        }
    }

    private void applyTerrainStatusOnMonsterAttack(@NonNull Monster monster) {
        if (currentTerrain == null || profile == null) {
            return;
        }
        switch (currentTerrain) {
            case LAVA_FIELD:
                if (monster.getAffinity() != MonsterAffinity.FIRE) {
                    maybeApplyPoison(2, 0.15f);
                }
                break;
            case MIRE:
                maybeApplyPoison(2, 0.15f);
                break;
            case FROZEN_RUINS:
                if (monster.getAffinity() != MonsterAffinity.ICE) {
                    maybeApplyFreeze(2, 0.15f);
                }
                break;
            case THORN_WILDS:
                maybeApplyPoison(2, 0.10f);
                break;
            case STORM_PLATEAU:
                maybeApplyFreeze(1, 0.10f);
                break;
            case ASH_WASTES:
                maybeApplyPoison(1, 0.10f);
                break;
            default:
                break;
        }
    }

    private void maybeApplyPoison(int turns, float chance) {
        if (turns <= 0 || chance <= 0f || poisonTurnsLeft > 0) {
            return;
        }
        if (random.nextFloat() < chance) {
            poisonTurnsLeft = turns;
            poisonTick();
        }
    }

    private void maybeApplyFreeze(int turns, float chance) {
        if (turns <= 0 || chance <= 0f || frozenTurnsLeft > 0) {
            return;
        }
        if (random.nextFloat() < chance) {
            frozenTurnsLeft = turns;
            updateStatusText();
        }
    }

    private void applyTrapDamage() {
        int finalDamage = 1;
        if (difficultyMode != null) {
            finalDamage = difficultyMode.scaleTrapDamage(1);
        }
        if (finalDamage <= 0) {
            return;
        }
        takeDamage(finalDamage);
        playCueWithFallback(SoundManager.KEY_EFFECT_TRAP, FeedbackManager.SoundEffect.TRAP);
    }

    private void revealAllTraps() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Tile tile = dungeonGrid[r][c];
                if (!tile.isRevealed() && tile.getType().name().startsWith("TRAP")) {
                    tile.reveal();
                }
            }
        }
        renderGrid();
    }

    private void checkVictoryCondition() {
        if (revealedSafeTiles >= safeTilesToReveal) {
            awardFloorClearXp(currentFloor);
            if (profile != null) {
                TelemetryManager.logRunCompleted(
                        currentFloor,
                        profile.getPlayerClass() != null ? profile.getPlayerClass().name() : "UNKNOWN",
                        profile.getLevel(),
                        currentGold);
            }
            showVictoryDialog();
            unlockAchievement(R.string.achievement_victory_title);
        }
    }

    // --- Rewards, progression, and HUD updates ---
    private void updateGoldCounter() {
        goldCounterText.setText(getString(R.string.gold_display_dynamic, currentGold));
    }

    private void updatePlatinumCounter() {
        if (platinumCounterText != null) {
            platinumCounterText.setText(getString(R.string.platinum_display_dynamic, currentPlatinum));
        }
    }

    private void awardFloorClearXp(int floorCleared) {
        if (profile == null || floorCleared <= 0 || floorCleared == lastFloorClearAwarded) {
            return;
        }
        int xpReward = GameBalance.calculateFloorClearXp(floorCleared, difficultyMode);
        awardExperience(xpReward);
        lastFloorClearAwarded = floorCleared;
    }

    private void awardItemFoundXp() {
        if (profile == null) {
            return;
        }
        int xpReward = GameBalance.calculateItemFoundXp(currentFloor, difficultyMode);
        awardExperience(xpReward);
    }

    private void awardTrapDisabledXp() {
        if (profile == null) {
            return;
        }
        int xpReward = GameBalance.calculateTrapDisabledXp(currentFloor, difficultyMode);
        awardExperience(xpReward);
    }

    private void applyMonsterLoot(@NonNull Monster monster) {
        MonsterLootRoll loot = GameBalance.rollMonsterLoot(currentFloor, difficultyMode, random);
        if (loot == null || !loot.hasLoot()) {
            return;
        }
        if (loot.getBonusGold() > 0) {
            currentGold += loot.getBonusGold();
            InventoryManager.syncGoldWithCurrentRun(this, currentGold);
            updateGoldCounter();
            recordGoldEarned(loot.getBonusGold());
            String message = getResources().getQuantityString(
                    R.plurals.monster_loot_gold, loot.getBonusGold(), loot.getBonusGold());
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        if (loot.getSmallKeyCount() > 0) {
            InventoryManager.adjustItemQuantity(this, SMALL_KEY_NAME, loot.getSmallKeyCount());
            awardItemFoundXp();
            addInventoryChange(SMALL_KEY_NAME, true);
            Toast.makeText(this, R.string.monster_loot_small_key, Toast.LENGTH_SHORT).show();
        }
        for (String name : loot.getItemNames()) {
            InventoryManager.adjustItemQuantity(this, name, 1);
            awardItemFoundXp();
            addInventoryChange(name, true);
            Toast.makeText(this, getString(R.string.monster_loot_item, name), Toast.LENGTH_SHORT).show();
        }
        for (String name : loot.getMagicItemNames()) {
            InventoryManager.adjustItemQuantity(this, name, 1);
            awardItemFoundXp();
            addInventoryChange(name, true);
            Toast.makeText(this, getString(R.string.monster_loot_magic, name), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateHpCounter() {
        if (profile != null) {
            hpCounterText.setText(getString(R.string.hp_display_dynamic, profile.getCurrentHP(), profile.getMaxHP()));
            if (hpCounterBar != null) {
                hpCounterBar.setMax(Math.max(1, profile.getMaxHP()));
                hpCounterBar.setProgress(Math.max(0, profile.getCurrentHP()));
            }
        } else {
            hpCounterText.setText(getString(R.string.hp_display_dynamic, 0, 0));
            if (hpCounterBar != null) {
                hpCounterBar.setMax(1);
                hpCounterBar.setProgress(0);
            }
        }
    }

    private void updateMpCounter() {
        if (mpCounterBar == null) {
            return;
        }
        if (profile != null && profile.getMaxMP() > 0) {
            mpCounterBar.setVisibility(View.VISIBLE);
            mpCounterBar.setMax(Math.max(1, profile.getMaxMP()));
            mpCounterBar.setProgress(Math.max(0, profile.getCurrentMP()));
        } else {
            mpCounterBar.setVisibility(View.GONE);
            mpCounterBar.setMax(1);
            mpCounterBar.setProgress(0);
        }
    }

    private void awardExperience(int xpReward) {
        if (profile == null || xpReward <= 0) {
            return;
        }
        // Keep HUD state in sync when XP triggers a level-up.
        int beforeLevel = profile.getLevel();
        profile.addExperience(xpReward);
        updateXpCounter();
        if (profile.getLevel() != beforeLevel) {
            updatePlayerIdentityHud();
            updateHpCounter();
            updateMpCounter();
            updateLevelUpButtonState();
        }
    }

    private void updateXpCounter() {
        if (xpCounterText == null) {
            return;
        }
        if (profile == null) {
            xpCounterText.setText(getString(R.string.xp_display_dynamic, 0, 100));
            return;
        }
        int level = Math.max(1, profile.getLevel());
        int xpNeeded = Math.max(100, level * 100);
        xpCounterText.setText(getString(R.string.xp_display_dynamic, profile.getXp(), xpNeeded));
    }

    private void updatePlayerIdentityHud() {
        if (playerNameLevelText == null) {
            return;
        }
        if (profile == null) {
            playerNameLevelText.setText(getString(R.string.player_hud_name_level_placeholder));
            return;
        }
        String playerName = TextUtils.isEmpty(profile.getName())
                ? getString(R.string.player_name_fallback)
                : profile.getName().trim();
        playerNameLevelText.setText(getString(
                R.string.player_hud_name_level,
                playerName,
                Math.max(1, profile.getLevel())));
    }

    private void updatePlayerHudIcon() {
        if (playerHudIcon == null) {
            return;
        }
        playerHudIcon.setImageResource(getPlayerIconResource());
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        if (frozenTurnsLeft > 0) {
            status.append(getResources().getQuantityString(
                    R.plurals.frozen_status, frozenTurnsLeft, frozenTurnsLeft)).append("\n");
        }
        if (poisonTurnsLeft > 0) {
            status.append(getResources().getQuantityString(
                    R.plurals.poison_status, poisonTurnsLeft, poisonTurnsLeft)).append("\n");
        }
        statusEffectText.setText(status.toString().trim());
    }

    private void addInventoryChange(String label, boolean gained) {
        String entry = getString(gained ? R.string.inventory_gain_message : R.string.inventory_use_message, label);
        inventoryChangeLog.add(0, entry);
        while (inventoryChangeLog.size() > 10) {
            inventoryChangeLog.remove(inventoryChangeLog.size() - 1);
        }
        InventoryManager.recordInventoryChange(this, entry);
        Toast.makeText(this, entry, Toast.LENGTH_SHORT).show();
        playCueWithFallback(SoundManager.KEY_EFFECT_INVENTORY, FeedbackManager.SoundEffect.POSITIVE);
    }

    private void takeDamage(int amount) {
        int remaining = applyKnightShield(amount);
        if (remaining <= 0) {
            requestSave(SaveReason.COMBAT_DAMAGE);
            return;
        }
        profile.setCurrentHP(profile.getCurrentHP() - remaining);
        updateHpCounter();
        if (profile.getCurrentHP() <= 0) {
            showGameOverDialog();
        } else if (profile.getCurrentHP() == 1) {
            unlockAchievement(R.string.achievement_low_hp_survivor_title);
        }
        requestSave(SaveReason.COMBAT_DAMAGE);
    }

    // --- Combat callbacks from CombatDialogFragment ---
    @Override
    /** Applies rewards, clears the tile, and persists state after a combat win. */
    public void onCombatVictory(@NonNull Monster monster) {
        TelemetryManager.logCombatEnded("VICTORY", monster.getMonsterType(), currentFloor);
        boolean convertedEnemy = activeCombatTile != null && activeCombatTile.getType() == TileType.ENEMY;
        if (activeCombatTile != null) {
            activeCombatTile.setMonster(null);
            activeCombatTile.setType(TileType.EMPTY);
        }
        refreshActiveCombatTileView();

        int xpReward = pendingCombatXpReward > 0
                ? pendingCombatXpReward
                : GameBalance.calculateXpReward(monster, currentFloor, difficultyMode, random);
        awardExperience(xpReward);
        int goldReward = pendingCombatGoldReward > 0
                ? pendingCombatGoldReward
                : GameBalance.calculateGoldReward(monster, currentFloor, difficultyMode, random);
        currentGold += goldReward;
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        updatePlatinumCounter();
        updateGoldCounter();
        updateHpCounter();
        recordGoldEarned(goldReward);
        applyMonsterLoot(monster);
        recordEnemyDefeat();
        if (monster.isBoss()) {
            unlockAchievement(R.string.achievement_boss_slayer_title);
        }
        FeedbackManager.playSound(this, FeedbackManager.SoundEffect.COMBAT_VICTORY);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
        String goldFragment = getResources().getQuantityString(
                R.plurals.combat_gold_reward, goldReward, goldReward);
        Toast.makeText(this,
                getString(R.string.combat_result_victory, monster.getMonsterType(), xpReward, goldFragment),
                Toast.LENGTH_SHORT).show();

        if (convertedEnemy) {
            safeTilesToReveal++;
            revealedSafeTiles++;
        }
        requestSave(SaveReason.COMBAT_VICTORY);
        clearCombatTracking();
        if (convertedEnemy) {
            checkVictoryCondition();
        }
    }

    @Override
    /** Handles combat loss feedback and triggers the game over flow. */
    public void onCombatDefeat() {
        TelemetryManager.logCombatEnded("DEFEAT",
                activeCombatTile != null && activeCombatTile.getMonster() != null
                        ? activeCombatTile.getMonster().getMonsterType() : "UNKNOWN",
                currentFloor);
        FeedbackManager.playSound(this, FeedbackManager.SoundEffect.COMBAT_DEFEAT);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.HEAVY);
        clearCombatTracking();
        handleGameOver();
    }

    @Override
    /** Applies the flee penalty and cleans up the combat session. */
    public void onCombatFled(int penaltyDamage) {
        TelemetryManager.logCombatEnded("FLED",
                activeCombatTile != null && activeCombatTile.getMonster() != null
                        ? activeCombatTile.getMonster().getMonsterType() : "UNKNOWN",
                currentFloor);
        if (penaltyDamage > 0) {
            takeDamage(penaltyDamage);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
            String message = getResources().getQuantityString(
                    R.plurals.combat_result_flee, penaltyDamage, penaltyDamage);
            Toast.makeText(this,
                    message,
                    Toast.LENGTH_SHORT).show();
            if (profile.isDead()) {
                clearCombatTracking();
                handleGameOver();
                return;
            }
        }

        refreshActiveCombatTileView();

        clearCombatTracking();
        requestSave(SaveReason.COMBAT_FLEE);
    }

    @Override
    /** Refreshes UI and persists interim combat state changes. */
    public void onCombatStateUpdated() {
        updateHpCounter();
        requestSave(SaveReason.COMBAT_STATE_UPDATE);
    }

    @Override
    /** Applies environmental effects when the monster attacks. */
    public void onMonsterAttack(@NonNull Monster monster) {
        applyTerrainStatusOnMonsterAttack(monster);
    }

    @Override
    /** Consumes a healing potion if available and returns true when used. */
    public boolean onUseHealingPotionRequested(int healAmount) {
        if (InventoryManager.getItemQuantity(this, "Healing Potion") > 0) {
            InventoryManager.adjustItemQuantity(this, "Healing Potion", -1);
            addInventoryChange("Healing Potion", false);
            int healedHp = Math.min(profile.getCurrentHP() + healAmount, profile.getMaxHP());
            profile.setCurrentHP(healedHp);
            updateHpCounter();
            requestSave(SaveReason.COMBAT_HEAL);
            FeedbackManager.playSound(this, FeedbackManager.SoundEffect.POSITIVE);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
            return true;
        }
        return false;
    }

    private SharedPreferences getProgressPreferences() {
        return getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE);
    }

    private void recordSafeTileReveal() {
        SharedPreferences prefs = getProgressPreferences();
        int total = prefs.getInt(PROGRESS_KEY_TILES, 0) + 1;
        prefs.edit().putInt(PROGRESS_KEY_TILES, total).apply();
        if (total >= DUNGEON_EXPLORER_TARGET) {
            unlockAchievement(R.string.achievement_dungeon_explorer_title);
        }
    }

    private void recordGoldEarned(int amount) {
        if (amount <= 0) {
            return;
        }
        SharedPreferences prefs = getProgressPreferences();
        int total = prefs.getInt(PROGRESS_KEY_GOLD, 0) + amount;
        prefs.edit().putInt(PROGRESS_KEY_GOLD, total).apply();
        if (total >= GOLD_HOARDER_TARGET) {
            unlockAchievement(R.string.achievement_gold_hoarder_title);
        }
    }

    private void recordEnemyDefeat() {
        SharedPreferences prefs = getProgressPreferences();
        int total = prefs.getInt(PROGRESS_KEY_ENEMIES, 0) + 1;
        prefs.edit().putInt(PROGRESS_KEY_ENEMIES, total).apply();
        if (total == 1) {
            unlockAchievement(R.string.achievement_first_blood_title);
        }
    }

    private void unlockAchievement(int titleResId) {
        AchievementManager.unlock(this, getString(titleResId));
    }

    /** Resets transient combat tracking state after encounters. */
    private void clearCombatTracking() {
        activeCombatTile = null;
        activeCombatTileView = null;
        pendingCombatGoldReward = 0;
        pendingCombatXpReward = 0;
    }

    private void showVictoryDialog() {
        clearPersistedState();
        new AlertDialog.Builder(this)
                .setTitle(R.string.victory_title)
                .setMessage(R.string.victory_message)
                .setCancelable(false)
                .setPositiveButton(R.string.play_again, (dialog, which) ->
                    startNewRunForActiveSlot())
                .setNegativeButton(R.string.main_menu, (dialog, which) -> goToMainMenu())
                .show();
    }

    private void showGameOverDialog() {
        clearPersistedState();
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over_title)
                .setMessage(R.string.game_over_message)
                .setCancelable(false)
                .setPositiveButton(R.string.restart, (dialog, which) -> startNewRunForActiveSlot())
                .setNegativeButton(R.string.main_menu, (dialog, which) -> goToMainMenu())
                .show();
    }

    // --- Inventory + equipment UI ---
    /** Opens the in-game inventory dialog with equipment and stat allocation controls. */
    private void showInventoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_inventory, null, false);
        RecyclerView recycler = dialogView.findViewById(R.id.recyclerInventory);
        TextView goldView = dialogView.findViewById(R.id.textInventoryGold);
        TextView platinumView = dialogView.findViewById(R.id.textInventoryPlatinum);
        TextView countView = dialogView.findViewById(R.id.textInventoryCount);
        TextView weaponView = dialogView.findViewById(R.id.textEquippedWeapon);
        TextView armorView = dialogView.findViewById(R.id.textEquippedArmor);
        TextView statView = dialogView.findViewById(R.id.textStatDelta);
        TextView emptyView = dialogView.findViewById(R.id.textEmptyInventory);
        TextView changeLogTitle = dialogView.findViewById(R.id.textInventoryChangeLogTitle);
        TextView changeLogView = dialogView.findViewById(R.id.textInventoryChangeLog);
        View statAllocation = dialogView.findViewById(R.id.layoutStatAllocation);
        TextView statPointsView = dialogView.findViewById(R.id.textStatPoints);
        TextView statStrengthView = dialogView.findViewById(R.id.textStatStrength);
        TextView statDexterityView = dialogView.findViewById(R.id.textStatDexterity);
        TextView statConstitutionView = dialogView.findViewById(R.id.textStatConstitution);
        TextView statIntelligenceView = dialogView.findViewById(R.id.textStatIntelligence);
        View statStrengthButton = dialogView.findViewById(R.id.buttonStatStrength);
        View statDexterityButton = dialogView.findViewById(R.id.buttonStatDexterity);
        View statConstitutionButton = dialogView.findViewById(R.id.buttonStatConstitution);
        View statIntelligenceButton = dialogView.findViewById(R.id.buttonStatIntelligence);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        java.util.List<com.adaplu.clickdungeon.model.InventoryItem> items = InventoryManager.loadInventory(this);
        final android.app.Dialog[] dialogRef = {null};
        InventoryAdapter adapter = new InventoryAdapter(items, item -> {
            if (profile == null) return;
            ItemDefinition def = ItemCatalog.getItemDefinition(item.getName());
            if (def != null && def.getType() == ItemDefinition.ItemType.CONSUMABLE) {
                // Consumables are used, not equipped.
                if ("Healing Potion".equals(item.getName())) {
                    if (profile.getCurrentHP() >= profile.getMaxHP()) {
                        Toast.makeText(this, R.string.potion_hp_full, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int before = profile.getCurrentHP();
                    boolean used = onUseHealingPotionRequested(CombatDialogFragment.HEALING_POTION_STRENGTH);
                    if (used) {
                        int healed = profile.getCurrentHP() - before;
                        Toast.makeText(this, getString(R.string.potion_used, healed), Toast.LENGTH_SHORT).show();
                        if (dialogRef[0] != null) dialogRef[0].dismiss();
                    } else {
                        Toast.makeText(this, R.string.potion_none_available, Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }
            if (!toggleEquip(profile, item.getName())) {
                Toast.makeText(this, R.string.equip_not_allowed, Toast.LENGTH_SHORT).show();
                return;
            }
            updateEquippedSummary(profile, weaponView, armorView, statView);
            requestSave(SaveReason.EQUIP_CHANGE);
        });
        recycler.setAdapter(adapter);
        goldView.setText(getString(R.string.gold_display_dynamic, InventoryManager.getGold(this)));
        platinumView.setText(getString(R.string.platinum_display_dynamic, InventoryManager.getPlatinum(this)));
        if (countView != null) {
            countView.setText(getString(R.string.inventory_item_count, items.size()));
        }
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        if (profile != null) {
            updateEquippedSummary(profile, weaponView, armorView, statView);
            TextView mpView = dialogView.findViewById(R.id.textInventoryMp);
            if (mpView != null) {
                mpView.setText(getString(R.string.mp_display_dynamic, profile.getCurrentMP(), profile.getMaxMP()));
            }
            bindStatAllocationInDialog(profile,
                    statAllocation,
                    statPointsView,
                    statStrengthView,
                    statDexterityView,
                    statConstitutionView,
                    statIntelligenceView,
                    statStrengthButton,
                    statDexterityButton,
                    statConstitutionButton,
                    statIntelligenceButton,
                    weaponView,
                    armorView,
                    statView,
                    mpView);
        } else if (statAllocation != null) {
            statAllocation.setVisibility(View.GONE);
        }
        bindChangeLogViews(changeLogTitle, changeLogView,
                InventoryManager.getInventoryChangeLog(this));

        dialogRef[0] = new AlertDialog.Builder(this)
                .setTitle(R.string.inventory_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /** Equips or unequips an item based on its slot, returning true on success. */
    private boolean toggleEquip(CharacterProfile profile, String itemName) {
        ItemDefinition definition = ItemCatalog.getItemDefinition(itemName);
        if (definition == null || definition.getEquipSlot() == ItemDefinition.EquipSlot.NONE) {
            return false;
        }
        switch (definition.getEquipSlot()) {
            case WEAPON:
                if (itemName.equals(profile.getEquippedWeaponName())) {
                    profile.unequipWeapon();
                    Toast.makeText(this, getString(R.string.unequip_success, itemName), Toast.LENGTH_SHORT).show();
                } else {
                    profile.equipWeapon(itemName);
                    Toast.makeText(this, getString(R.string.equip_success, itemName), Toast.LENGTH_SHORT).show();
                }
                playCueWithFallback(SoundManager.KEY_EFFECT_EQUIP, FeedbackManager.SoundEffect.POSITIVE);
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
                return true;
            case ARMOR:
                if (itemName.equals(profile.getEquippedArmorName())) {
                    profile.unequipArmor();
                    Toast.makeText(this, getString(R.string.unequip_success, itemName), Toast.LENGTH_SHORT).show();
                } else {
                    profile.equipArmor(itemName);
                    Toast.makeText(this, getString(R.string.equip_success, itemName), Toast.LENGTH_SHORT).show();
                }
                playCueWithFallback(SoundManager.KEY_EFFECT_EQUIP, FeedbackManager.SoundEffect.POSITIVE);
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
                return true;
            default:
                return false;
        }
    }

    /** Updates inventory dialog labels for equipped gear and derived stats. */
    private void updateEquippedSummary(CharacterProfile profile,
                                       TextView weaponView,
                                       TextView armorView,
                                       TextView statView) {
        String weapon = profile.getEquippedWeaponName() != null ? profile.getEquippedWeaponName() : "None";
        String armor = profile.getEquippedArmorName() != null ? profile.getEquippedArmorName() : "None";
        weaponView.setText(getString(R.string.equipped_weapon_label, weapon));
        armorView.setText(getString(R.string.equipped_armor_label, armor));
        statView.setText(getString(R.string.equipped_stat_delta,
                profile.getBaseAttack(),
                profile.getAttackBonus(),
                profile.getBaseDefense(),
                profile.getDefenseBonus()));
    }

    /** Wires stat allocation buttons to profile updates inside the dialog. */
    private void bindStatAllocationInDialog(CharacterProfile profile,
                                            View allocationView,
                                            TextView pointsView,
                                            TextView strengthView,
                                            TextView dexterityView,
                                            TextView constitutionView,
                                            TextView intelligenceView,
                                            View strengthButton,
                                            View dexterityButton,
                                            View constitutionButton,
                                            View intelligenceButton,
                                            TextView weaponView,
                                            TextView armorView,
                                            TextView statView,
                                            TextView mpView) {
        if (allocationView == null) {
            return;
        }
        allocationView.setVisibility(View.VISIBLE);
        Runnable refresh = () -> {
            updateStatViews(profile, pointsView, strengthView,
                    dexterityView, constitutionView, intelligenceView);
            if (mpView != null) {
                mpView.setText(getString(R.string.mp_display_dynamic, profile.getCurrentMP(), profile.getMaxMP()));
            }
        };
        refresh.run();

        strengthButton.setOnClickListener(v -> {
            if (profile.increaseStrength(1)) {
                refresh.run();
                updateEquippedSummary(profile, weaponView, armorView, statView);
                updateHpCounter();
                updateLevelUpButtonState();
                requestSave(SaveReason.STAT_ALLOC);
            }
        });
        dexterityButton.setOnClickListener(v -> {
            if (profile.increaseDexterity(1)) {
                refresh.run();
                updateEquippedSummary(profile, weaponView, armorView, statView);
                updateLevelUpButtonState();
                requestSave(SaveReason.STAT_ALLOC);
            }
        });
        constitutionButton.setOnClickListener(v -> {
            if (profile.increaseConstitution(1)) {
                refresh.run();
                updateHpCounter();
                updateLevelUpButtonState();
                requestSave(SaveReason.STAT_ALLOC);
            }
        });
        intelligenceButton.setOnClickListener(v -> {
            if (profile.increaseIntelligence(1)) {
                refresh.run();
                updateMpCounter();
                updateLevelUpButtonState();
                requestSave(SaveReason.STAT_ALLOC);
            }
        });
    }

    /** Refreshes stat point and attribute text fields in the dialog. */
    private void updateStatViews(CharacterProfile profile,
                                 TextView pointsView,
                                 TextView strengthView,
                                 TextView dexterityView,
                                 TextView constitutionView,
                                 TextView intelligenceView) {
        pointsView.setText(getString(R.string.stat_points_available, profile.getAvailableStatPoints()));
        strengthView.setText(getString(R.string.stat_label_strength, profile.getStrength()));
        dexterityView.setText(getString(R.string.stat_label_dexterity, profile.getDexterity()));
        constitutionView.setText(getString(R.string.stat_label_constitution, profile.getConstitution()));
        intelligenceView.setText(getString(R.string.stat_label_intelligence, profile.getIntelligence()));
    }

    private void bindChangeLogViews(TextView titleView, TextView logView, java.util.List<String> entries) {
        if (titleView == null || logView == null) {
            return;
        }
        if (entries == null || entries.isEmpty()) {
            titleView.setVisibility(View.GONE);
            logView.setVisibility(View.GONE);
            return;
        }
        titleView.setVisibility(View.VISIBLE);
        logView.setVisibility(View.VISIBLE);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(entries.get(i));
        }
        logView.setText(builder.toString());
    }

    // --- Activity lifecycle ---
    @Override
    protected void onResume() {
        super.onResume();
        startGridAnimationLoop();
    }

    @Override
    protected void onPause() {
        stopGridAnimationLoop();
        super.onPause();
        requestSave(SaveReason.PAUSE);
    }

    /**
     * Force-saves synchronously when the OS signals the process is critically low on memory
     * or is about to be killed. The normal save path enqueues work on a background executor,
     * which may not finish if the OS terminates the process immediately after onPause() returns.
     * A direct synchronous write here ensures progress is not lost.
     *
     * Triggered at TRIM_MEMORY_RUNNING_CRITICAL (device critically low, app still running)
     * and TRIM_MEMORY_COMPLETE (app is first candidate for process death).
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            forceSaveNow();
        }
    }

    /**
     * Writes the current game state synchronously on the calling thread.
     * Cancels any pending debounced save first so we don't double-write.
     * Safe to call from onTrimMemory or any lifecycle callback.
     */
    private void forceSaveNow() {
        if (dungeonGrid == null || profile == null || saveManager == null) {
            return;
        }
        // Cancel any pending debounced save — we're about to write directly.
        saveHandler.removeCallbacks(debouncedSaveRunnable);

        int slot = activeSlotIndex >= 0 ? activeSlotIndex : DEFAULT_SLOT_INDEX;
        SaveManager.SaveSnapshot snapshot = saveManager.buildSnapshot(
                profile,
                currentFloor,
                currentGold,
                currentPlatinum,
                dungeonGrid,
                buildRunMetadata());
        // Write synchronously — blocks briefly but prevents data loss on process death.
        saveManager.saveSnapshot(slot, snapshot);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SLOT_INDEX, activeSlotIndex);
    }

}

