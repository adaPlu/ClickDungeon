//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.example.clickdungeon;

import android.content.Context;
//import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
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

import com.example.clickdungeon.model.AnimatedMonster;
import com.example.clickdungeon.model.AnimatedPlayer;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.adapter.InventoryAdapter;
import com.example.clickdungeon.ui.CombatDialogFragment;
import com.example.clickdungeon.util.AchievementManager;
import com.example.clickdungeon.util.DungeonGenerator;
import com.example.clickdungeon.util.FeedbackManager;
import com.example.clickdungeon.util.GameBalance;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.util.MonsterAnimationHelper;
import com.example.clickdungeon.util.OnboardingManager;
import com.example.clickdungeon.util.SaveManager;
import com.example.clickdungeon.util.SettingsManager;
import com.example.clickdungeon.util.SoundManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements CombatDialogFragment.CombatCallbacks {

    private enum AbilityTargetMode {
        NONE,
        WIZARD_FIREBALL,
        THIEF_SCAN,
        KNIGHT_SHIELD
    }

    public static final String EXTRA_SLOT_INDEX = "com.example.clickdungeon.extra.SLOT_INDEX";
    public static final String EXTRA_IS_NEW_GAME = "com.example.clickdungeon.extra.IS_NEW_GAME";
    public static final String EXTRA_PROFILE_JSON = "com.example.clickdungeon.extra.PROFILE_JSON";

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

    private static final int GRID_SIZE = 5;
    private static final int TOTAL_SAVE_SLOTS = 4;
    private static final int MAX_FLOOR = 15;
    private static final int DEFAULT_SLOT_INDEX = 0;
    private static final String TAG_COMBAT_DIALOG = "CombatDialog";
    private static final long GRID_ANIMATION_FRAME_DELAY_MS = 120L;
    private static final long OFFSCREEN_ANIMATION_THROTTLE_MS = 400L;
    private static final String DEFAULT_TILE_GLYPH = "[]";
    private GridLayout gridLayout;
    private TextView goldCounterText, hpCounterText, statusEffectText, floorText;
    private Button classAbilityButton;
    private Button inventoryButton;
    private Tile[][] dungeonGrid;
    private int currentGold = 0;
    private int safeTilesToReveal = 0;
    private int revealedSafeTiles = 0;
    private int currentFloor = 1;
    private int frozenTurnsLeft = 0;
    private int poisonTurnsLeft = 0;
    private final List<String> inventoryChangeLog = new ArrayList<>();

    private CharacterProfile profile;
    private TileType placedLockedStair = null;
    private final Random random = new Random();
    private final MonsterTemplate slime = new MonsterTemplate("Slime", "S", 3, 1, 0);
    private final MonsterTemplate goblin = new MonsterTemplate("Goblin", "G", 4, 2, 1);
    private final MonsterTemplate skeleton = new MonsterTemplate("Skeleton", "K", 5, 2, 2);
    private final MonsterTemplate orc = new MonsterTemplate("Orc", "O", 6, 3, 2);
    private final MonsterTemplate troll = new MonsterTemplate("Troll", "T", 7, 3, 3);
    private final MonsterTemplate witch = new MonsterTemplate("Witch", "W", 6, 4, 2);
    private final MonsterTemplate vampire = new MonsterTemplate("Vampire", "V", 6, 4, 3);
    private final MonsterTemplate demon = new MonsterTemplate("Demon", "D", 8, 5, 3);
    private final MonsterTemplate dragon = new MonsterTemplate("Dragon", "R", 10, 6, 4);
    private final MonsterTemplate rat = new MonsterTemplate("Rat", "r", 3, 1, 0);
    private final MonsterTemplate bat = new MonsterTemplate("Bat", "b", 3, 2, 0);
    private final MonsterTemplate spider = new MonsterTemplate("Spider", "s", 4, 2, 1);
    private final MonsterTemplate wolf = new MonsterTemplate("Wolf", "w", 5, 3, 1);
    private final MonsterTemplate bandit = new MonsterTemplate("Bandit", "B", 6, 3, 2);
    private final MonsterTemplate cultist = new MonsterTemplate("Cultist", "C", 6, 3, 2);
    private final MonsterTemplate warlock = new MonsterTemplate("Warlock", "L", 6, 4, 2);
    private final MonsterTemplate wraith = new MonsterTemplate("Wraith", "H", 7, 4, 3);
    private final MonsterTemplate golem = new MonsterTemplate("Golem", "M", 8, 4, 4);
    private final MonsterTemplate lich = new MonsterTemplate("Lich", "I", 9, 5, 4);
    private final MonsterTemplate hellhound = new MonsterTemplate("Hellhound", "h", 9, 6, 3);
    private final MonsterTemplate revenant = new MonsterTemplate("Revenant", "N", 8, 5, 3);
    private final MonsterTemplate archdemon = new MonsterTemplate("Archdemon", "A", 11, 7, 5);
    private final MonsterTemplate ancientWyrm = new MonsterTemplate("Ancient Wyrm", "Y", 12, 7, 5);

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
    private final Map<String, AnimatedMonster> gridMonsterAnimations = new HashMap<>();
    private final Map<String, Long> gridAnimationLastFrameMs = new HashMap<>();
    private final Handler gridAnimationHandler = new Handler(Looper.getMainLooper());
    private final Rect tempVisibleRect = new Rect();
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

        difficultyMode = SettingsManager.getDifficultyMode(this);
        colorBlindModeEnabled = SettingsManager.isColorBlindModeEnabled(this);

        gridLayout = findViewById(R.id.gridDungeon);
        goldCounterText = findViewById(R.id.textGoldCounter);
        hpCounterText = findViewById(R.id.textHpCounter);
        statusEffectText = findViewById(R.id.textStatus);
        floorText = findViewById(R.id.textFloor);
        classAbilityButton = findViewById(R.id.btnClassAbility);
        inventoryButton = findViewById(R.id.btnInventory);
        Button inventoryButton = findViewById(R.id.btnInventory);
        AchievementManager.loadAchievements(this);
        initBitmapCache();

        saveManager = new SaveManager(this);
        Intent launchIntent = getIntent();
        activeSlotIndex = launchIntent.getIntExtra(EXTRA_SLOT_INDEX, -1);
        boolean launchingNewSlotGame = launchIntent.getBooleanExtra(EXTRA_IS_NEW_GAME, false);
        String profileJsonOverride = launchIntent.getStringExtra(EXTRA_PROFILE_JSON);

        if (activeSlotIndex < 0 || activeSlotIndex >= TOTAL_SAVE_SLOTS) {
            SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
            int storedSlot = prefs.getInt("save_slot", DEFAULT_SLOT_INDEX);
            activeSlotIndex = Math.max(0, Math.min(TOTAL_SAVE_SLOTS - 1, storedSlot));
        }

        if (launchingNewSlotGame) {
            if (loadProfileForSessionInvert(profileJsonOverride)) {
                return;
            }
            ensureAnimatedPlayer();
            startNewRunForActiveSlot();
        } else {
            SaveManager.GameState gameState = saveManager.loadGame(activeSlotIndex);
            if (gameState != null) {
                profile = gameState.profile;
                currentFloor = gameState.currentFloor;
                currentGold = gameState.currentGold;
                dungeonGrid = gameState.dungeonGrid;
                gridMonsterAnimations.clear();
                recalculateSafeTileTargets(dungeonGrid);
                restoreLockedStairStateFromGrid(dungeonGrid);
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
                ensureAnimatedPlayer();
                restoreRunMetadata(gameState.metadata);
            } else {
                Toast.makeText(this, R.string.continue_slot_load_failed, Toast.LENGTH_LONG).show();
                if (loadProfileForSessionInvert(profileJsonOverride)) {
                    return;
                }
                ensureAnimatedPlayer();
                startNewRunForActiveSlot();
            }
        }

        gridLayout.setColumnCount(GRID_SIZE);
        gridLayout.setRowCount(GRID_SIZE);

        renderGrid();
        updateGoldCounter();
        updateHpCounter();
        updateStatusText();
        updateFloorDisplay();
        setupClassAbilityButton();
        setupInventoryButton();
        OnboardingManager.showDungeonTutorialIfNeeded(this, difficultyMode, colorBlindModeEnabled);
    }

    private void generateDungeon() {
        DungeonGenerator.Result result = DungeonGenerator.generateFloor(GRID_SIZE, currentFloor,
                this::createRandomMonsterForCurrentFloor);
        dungeonGrid = result.grid;
        placedLockedStair = result.lockedStair;
        safeTilesToReveal = result.safeTiles;
        gridMonsterAnimations.clear();
    }

    private void startNewRunForActiveSlot() {
        currentFloor = 1;
        currentGold = 100;
        dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
        if (profile != null) {
            profile.setCurrentHP(profile.getMaxHP());
        }
        generateDungeon();
        resetPlayerPositionToCenter();
        clearKnightShield(0);
        pendingAbilityTargetMode = AbilityTargetMode.NONE;
        nextAbilityAvailableFloor = currentFloor;
        int potionCount = 3;
        if (profile != null && profile.getPlayerClass() == PlayerClass.WIZARD) {
            potionCount = 5;
        }
        InventoryManager.adjustItemQuantity(this, "Healing Potion", potionCount, 5);
        InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", 1, INVENTORY_STACK_LIMIT);
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        updateAbilityButtonState();
        persistGameState();
    }

    private void fallToNextFloor() {
        currentFloor++;
        if (currentFloor > MAX_FLOOR) {
            currentFloor = MAX_FLOOR;
            showVictoryDialog();
            return;
        }
        generateDungeon();
        resetPlayerPositionToCenter();
        clearKnightShield(0);
        pendingAbilityTargetMode = AbilityTargetMode.NONE;
        renderGrid();
        updateFloorDisplay();
        frozenTurnsLeft = 0;
        poisonTurnsLeft = 0;
        updateStatusText();
        updateAbilityButtonState();
        persistGameState();
        Toast.makeText(this, "You fell to Floor " + currentFloor + "!", Toast.LENGTH_LONG).show();
    }

    private void setupClassAbilityButton() {
        if (profile == null || profile.getPlayerClass() == null || classAbilityButton == null) {
            if (classAbilityButton != null) {
                classAbilityButton.setVisibility(View.GONE);
            }
            return;
        }
        classAbilityButton.setVisibility(View.VISIBLE);
        classAbilityButton.setOnClickListener(v -> triggerClassAbility());
        updateAbilityButtonState();
    }

    private void setupInventoryButton() {
        if (inventoryButton != null) {
            inventoryButton.setOnClickListener(v -> showInventoryDialog());
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
        PlayerClass playerClass = profile.getPlayerClass();
        if (playerClass == null) {
            return;
        }
        switch (playerClass) {
            case WIZARD:
                beginAbilityTargeting(AbilityTargetMode.WIZARD_FIREBALL, R.string.ability_target_prompt_fireball);
                break;
            case THIEF:
                beginAbilityTargeting(AbilityTargetMode.THIEF_SCAN, R.string.ability_target_prompt_scan);
                break;
            case KNIGHT:
                beginAbilityTargeting(AbilityTargetMode.KNIGHT_SHIELD, R.string.ability_target_prompt_shield);
                break;
            default:
                break;
        }
    }

    private void beginAbilityTargeting(AbilityTargetMode mode, int promptResId) {
        if (mode == null) {
            return;
        }
        pendingAbilityTargetMode = mode;
        Toast.makeText(this, getString(promptResId, ABILITY_RANGE), Toast.LENGTH_SHORT).show();
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
                : getAbilityButtonLabelRes(profile.getPlayerClass())));
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

    private void consumeAbilityUse() {
        nextAbilityAvailableFloor = currentFloor + ABILITY_COOLDOWN_FLOORS;
        pendingAbilityTargetMode = AbilityTargetMode.NONE;
        updateAbilityButtonState();
    }

    private boolean loadProfileForSessionInvert(String profileJsonOverride) {
        if (profileJsonOverride != null) {
            profile = new Gson().fromJson(profileJsonOverride, CharacterProfile.class);
        }
        if (profile == null) {
            SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
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
        MonsterTemplate[] pool = getMonsterPoolForFloor(currentFloor);
        MonsterTemplate template = pool[random.nextInt(pool.length)];
        Monster monster = template.spawnForFloor(random, currentFloor, difficultyMode);
        if (shouldGrantRangedAttack(currentFloor, difficultyMode)
                && random.nextFloat() < RANGED_ATTACK_CHANCE_HARDCORE) {
            monster.setHasRangedAttack(true);
        }
        return monster;
    }

    private MonsterTemplate[] getMonsterPoolForFloor(int floor) {
        switch (floor) {
            case 1:
                return new MonsterTemplate[] { slime, goblin, rat };
            case 2:
                return new MonsterTemplate[] { slime, goblin, bat };
            case 3:
                return new MonsterTemplate[] { goblin, skeleton, spider };
            case 4:
                return new MonsterTemplate[] { skeleton, orc, wolf };
            case 5:
                return new MonsterTemplate[] { orc, troll, bandit };
            case 6:
                return new MonsterTemplate[] { troll, witch, cultist };
            case 7:
                return new MonsterTemplate[] { witch, vampire, warlock };
            case 8:
                return new MonsterTemplate[] { vampire, demon, wraith };
            case 9:
                return new MonsterTemplate[] { demon, dragon, golem };
            case 10:
                return new MonsterTemplate[] { dragon, lich, hellhound };
            case 11:
                return new MonsterTemplate[] { dragon, lich, revenant };
            case 12:
                return new MonsterTemplate[] { lich, demon, wraith };
            case 13:
                return new MonsterTemplate[] { dragon, lich, demon };
            case 14:
                return new MonsterTemplate[] { dragon, lich, archdemon };
            case 15:
                return new MonsterTemplate[] { dragon, archdemon, ancientWyrm };
            default:
                return new MonsterTemplate[] { slime, goblin, skeleton };
        }
    }

    private boolean shouldGrantRangedAttack(int floor, SettingsManager.Difficulty difficulty) {
        return difficulty == SettingsManager.Difficulty.HARDCORE
                && floor >= RANGED_ATTACK_MIN_FLOOR;
    }

    private void restoreLockedStairStateFromGrid(Tile[][] grid) {
        placedLockedStair = null;
        if (grid == null) {
            return;
        }
        for (Tile[] row : grid) {
            for (Tile tile : row) {
                if (tile == null) continue;
                TileType type = tile.getType();
                if (type == TileType.STAIR_DOWN_LOCKED_RED
                        || type == TileType.STAIR_DOWN_LOCKED_BLUE
                        || type == TileType.STAIR_DOWN_LOCKED_GREEN) {
                    placedLockedStair = type;
                    return;
                }
            }
        }
    }

    private void persistGameState() {
        if (dungeonGrid == null || profile == null || saveManager == null) {
            return;
        }
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        int slotToPersist = activeSlotIndex >= 0 ? activeSlotIndex : DEFAULT_SLOT_INDEX;
        SaveManager.RunMetadata metadata = new SaveManager.RunMetadata(
                playerRow,
                playerCol,
                knightShieldActive,
                knightShieldStrength,
                knightShieldRow,
                knightShieldCol,
                nextAbilityAvailableFloor);
        saveManager.saveGame(slotToPersist, profile, currentFloor, currentGold, dungeonGrid, metadata);
    }

    private void clearPersistedState() {
        if (saveManager == null) {
            return;
        }
        int slotToClear = activeSlotIndex >= 0 ? activeSlotIndex : DEFAULT_SLOT_INDEX;
        saveManager.deleteSave(slotToClear);
    }

    private void updateFloorDisplay() {
        String label = "Floor " + currentFloor;
        if (placedLockedStair != null) {
            label += " (Hard)";
        }
        floorText.setText(label);
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
                                gridAnimationLastFrameMs.remove(key);
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
                return "💰";
            case EMPTY:
                return "⬜";
            case ENEMY:
                if (!tile.hasMonster()) {
                    return getString(R.string.combat_tile_cleared);
                }
                String icon = tile.getMonster().getImage();
                return !TextUtils.isEmpty(icon) ? icon : "💀";
            case STAIR_DOWN:
                return "🪜";
            case STAIR_UP:
                return "⤴️";
            case STAIR_DOWN_LOCKED_RED:
                return "🔴🪜";
            case STAIR_DOWN_LOCKED_BLUE:
                return "🔵🪜";
            case STAIR_DOWN_LOCKED_GREEN:
                return "🟢🪜";
            case RED_KEY:
                return "🔴🔑";
            case BLUE_KEY:
                return "🔵🔑";
            case GREEN_KEY:
                return "🟢🔑";
            case TRAP_FIRE:
                return "🔥";
            case TRAP_POISON:
                return "☠️";
            case TRAP_ACID:
                return "🧪";
            case TRAP_FREEZE:
                return "❄️";
            case TRAP_PITFALL:
                return "🕳️";
            default:
                return type.toString();
        }
    }

    private String getColorBlindSuffix(Tile tile) {
        TileType type = tile.getType();
        switch (type) {
            case RED_KEY:
            case STAIR_DOWN_LOCKED_RED:
                return "R";
            case BLUE_KEY:
            case STAIR_DOWN_LOCKED_BLUE:
                return "B";
            case GREEN_KEY:
            case STAIR_DOWN_LOCKED_GREEN:
                return "G";
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
                    return !TextUtils.isEmpty(name) ? name.substring(0, 1).toUpperCase() : "E";
                }
                return "";
            case STAIR_DOWN:
                return "DN";
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
            case STAIR_UP:
                return getString(R.string.tile_desc_stairs_up);
            case STAIR_DOWN_LOCKED_RED:
                return getString(R.string.tile_desc_locked_stair_red);
            case STAIR_DOWN_LOCKED_BLUE:
                return getString(R.string.tile_desc_locked_stair_blue);
            case STAIR_DOWN_LOCKED_GREEN:
                return getString(R.string.tile_desc_locked_stair_green);
            case RED_KEY:
                return getString(R.string.tile_desc_key_red);
            case BLUE_KEY:
                return getString(R.string.tile_desc_key_blue);
            case GREEN_KEY:
                return getString(R.string.tile_desc_key_green);
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

    private void bindTileView(@NonNull View tileView,
                              @Nullable Tile tile,
                              int row,
                              int col) {
        TextView tileText = tileView.findViewById(R.id.textTile);
        ImageView tileImage = tileView.findViewById(R.id.imageTile);
        ProgressBar hpBar = tileView.findViewById(R.id.monsterHpBar);
        View overlay = tileView.findViewById(R.id.damageOverlay);

        tileImage.setVisibility(View.GONE);
        tileImage.setImageDrawable(null);
        hpBar.setVisibility(View.GONE);
        tileText.setVisibility(View.VISIBLE);

        String description;
        String coordKey = coordinateKey(row, col);
        if (tile == null || !tile.isRevealed()) {
            gridMonsterAnimations.remove(coordKey);
            gridAnimationLastFrameMs.remove(coordKey);
            tileImage.setVisibility(View.VISIBLE);
            tileImage.setImageResource(R.drawable.ic_tile_unknown_c);
            tileText.setVisibility(View.GONE);
            description = getString(R.string.tile_desc_hidden);
        } else if (tile.getType() == TileType.ENEMY && tile.hasMonster()) {
            tileText.setVisibility(View.GONE);
            tileImage.setVisibility(View.VISIBLE);
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
            gridAnimationLastFrameMs.remove(coordKey);
            tileText.setVisibility(View.VISIBLE);
            tileText.setText(getTileDisplay(tile));
            description = getTileContentDescription(tile);
        }

        boolean playerHere = tile != null && tile.hasPlayer();
        if (playerHere) {
            overlay.setVisibility(View.VISIBLE);
            overlay.setBackgroundColor(getColorCompat(R.color.player_highlight_overlay));
            overlay.setAlpha(0.5f);
            tileText.setVisibility(View.GONE);
            tileImage.setVisibility(View.VISIBLE);
            Bitmap frame = null;
            if (profile != null && profile.getAnimatedPlayer() != null) {
                frame = profile.getAnimatedPlayer().getCurrentFrame();
            }
            if (frame != null) {
                tileImage.setImageBitmap(frame);
            } else {
                tileImage.setImageResource(getPlayerIconResource());
            }
            activePlayerTileView = tileView;
            description = getString(R.string.tile_desc_player_here, description);
        } else {
            overlay.setVisibility(View.GONE);
        }

        tileView.setContentDescription(description);
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
        long now = SystemClock.uptimeMillis();
        AnimatedPlayer playerAnimator = profile != null ? profile.getAnimatedPlayer() : null;
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
            if (tile == null) {
                continue;
            }
            String coordKey = coordinateKey(row, col);
            boolean isVisible = tileView.isShown() && tileView.getGlobalVisibleRect(tempVisibleRect);
            if (!isVisible) {
                Long last = gridAnimationLastFrameMs.get(coordKey);
                if (last != null && now - last < OFFSCREEN_ANIMATION_THROTTLE_MS) {
                    continue;
                }
                // Throttle offscreen tiles so animation work is focused on visible cells.
                gridAnimationLastFrameMs.put(coordKey, now);
                continue;
            }
            ImageView tileImage = tileView.findViewById(R.id.imageTile);
            if (tile.hasPlayer() && playerAnimator != null && tileImage.getVisibility() == View.VISIBLE) {
                Bitmap frame = playerAnimator.getCurrentFrame();
                if (frame != null) {
                    tileImage.setImageBitmap(frame);
                    gridAnimationLastFrameMs.put(coordKey, now);
                }
            } else if (tile.isRevealed() && tile.getType() == TileType.ENEMY && tile.hasMonster()) {
                AnimatedMonster animator = ensureGridAnimatedMonster(row, col, tile);
                if (animator != null && tileImage.getVisibility() == View.VISIBLE) {
                    Bitmap frame = animator.getCurrentFrame();
                    if (frame != null) {
                        tileImage.setImageBitmap(frame);
                        gridAnimationLastFrameMs.put(coordKey, now);
                    }
                }
            }
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
                bindTileView(child, dungeonGrid[row][col], row, col);
                break;
            }
        }
    }

    private void refreshActiveCombatTileView() {
        if (activeCombatTileView == null || activeCombatTile == null) {
            return;
        }
        Object tagRow = activeCombatTileView.getTag(R.id.tag_row);
        Object tagCol = activeCombatTileView.getTag(R.id.tag_col);
        if (tagRow instanceof Integer && tagCol instanceof Integer) {
            bindTileView(activeCombatTileView,
                    activeCombatTile,
                    (Integer) tagRow,
                    (Integer) tagCol);
        }
    }

    private int getMonsterSpriteResource(@NonNull Monster monster) {
        String type = monster.getMonsterType();
        if (type == null) {
            return R.drawable.slime_main;
        }
        switch (type.toLowerCase()) {
            case "goblin":
                return R.drawable.goblin_main;
            case "skeleton":
                return R.drawable.skeleton_sprite_sheet;
            case "orc":
                return R.drawable.orc_sprite_sheet;
            case "troll":
                return R.drawable.troll_main;
            case "witch":
                return R.drawable.witch_main;
            case "vampire":
                return R.drawable.demon_main;
            case "demon":
                return R.drawable.demon_main;
            case "dragon":
                return R.drawable.dragon_main;
            default:
                return R.drawable.slime_main;
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

    private int getPlayerIconResource() {
        if (profile == null || profile.getPlayerClass() == null) {
            return R.drawable.icon_knight;
        }
        switch (profile.getPlayerClass()) {
            case THIEF:
                return R.drawable.icon_thief;
            case WIZARD:
                return R.drawable.icon_wizard;
            case KNIGHT:
            default:
                return R.drawable.icon_knight;
        }
    }

    private int getColorCompat(int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(colorRes);
        }
        return ContextCompat.getColor(this, colorRes);
    }

    private void startCombat(Tile tile, View tileView) {
        if (tile == null || !tile.hasMonster() || profile == null) {
            return;
        }
        if (getSupportFragmentManager().findFragmentByTag(TAG_COMBAT_DIALOG) != null) {
            return;
        }
        Monster monster = tile.getMonster();
        CombatDialogFragment fragment = CombatDialogFragment.newInstance(monster);
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
        if (!clickedTile.isRevealed()) {
            clickedTile.reveal();
            if (profile.getAnimatedPlayer() != null) {
                profile.getAnimatedPlayer().setAction("move");
            }
            TextView tileText = tileView.findViewById(R.id.textTile);
            revealTile(tileView, tileText, clickedTile);
            if (clickedTile.getType() != TileType.ENEMY) {
                revealedSafeTiles++;
                recordSafeTileReveal();
            }
            updatePlayerPosition(row, col);
            bindTileView(tileView, clickedTile, row, col);
            persistGameState();
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
        if (monsterBitmapCache != null) {
            monsterBitmapCache.evictAll();
            monsterBitmapCache = null;
        }
        if (profile != null && profile.getAnimatedPlayer() != null) {
            profile.getAnimatedPlayer().reset();
            profile.setAnimatedPlayer(null);
        }
    }

    private void handleAbilityTargetSelection(int row, int col) {
        if (pendingAbilityTargetMode == AbilityTargetMode.NONE) {
            return;
        }
        if (isPlayerPositionKnownInvert()) {
            Toast.makeText(this, R.string.player_position_unknown, Toast.LENGTH_SHORT).show();
            return;
        }
        if (isTargetWithinAbilityRangeInvert(row, col)) {
            Toast.makeText(this, getString(R.string.ability_range_error, ABILITY_RANGE), Toast.LENGTH_SHORT).show();
            return;
        }
        boolean resolved = false;
        switch (pendingAbilityTargetMode) {
            case WIZARD_FIREBALL:
                resolved = executeWizardFireball(row, col);
                break;
            case THIEF_SCAN:
                resolved = executeThiefScan(row, col);
                break;
            case KNIGHT_SHIELD:
                resolved = deployKnightShield(row, col);
                break;
            case NONE:
            default:
                break;
        }
        if (resolved) {
            consumeAbilityUse();
            persistGameState();
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
        if (tileText != null) {
            tileText.setText(getTileDisplay(tile));
            tileText.setContentDescription(getTileContentDescription(tile));
        }
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
                        getString(R.string.fireball_result_hit, damage),
                        Toast.LENGTH_SHORT).show();
            }
            FeedbackManager.playSound(this, FeedbackManager.SoundEffect.POSITIVE);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
        } else {
            Toast.makeText(this, R.string.fireball_result_whiff, Toast.LENGTH_SHORT).show();
        }
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
                TextView tileText = getTileTextView(r, c);
                if (tileText != null) {
                    tileText.setText(getTileDisplay(tile));
                    tileText.setContentDescription(getTileContentDescription(tile));
                }
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

    private boolean deployKnightShield(int row, int col) {
        if (isTargetWithinAbilityRangeInvert(row, col)) {
            Toast.makeText(this, getString(R.string.ability_range_error, ABILITY_RANGE), Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this,
                getString(R.string.knight_shield_activated, knightShieldStrength),
                Toast.LENGTH_SHORT).show();
        updateAbilityButtonState();
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
        profile.addExperience(xpReward);
        int goldReward = GameBalance.calculateGoldReward(monster, currentFloor, difficultyMode, random);
        currentGold += goldReward;
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        updateGoldCounter();
        recordGoldEarned(goldReward);
        recordEnemyDefeat();
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
                    getString(R.string.knight_shield_absorb, absorbed),
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
        final int cacheSize = Math.max(1024, maxMemory / 32);
        monsterBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value == null ? 0 : value.getByteCount() / 1024;
            }
        };
    }


    private void restoreRunMetadata(@Nullable SaveManager.RunMetadata metadata) {
        if (metadata == null) {
            resetPlayerPositionToCenter();
            clearKnightShield(0);
            nextAbilityAvailableFloor = currentFloor;
            return;
        }
        playerRow = metadata.playerRow >= 0 ? clampGridIndex(metadata.playerRow) : GRID_SIZE / 2;
        playerCol = metadata.playerCol >= 0 ? clampGridIndex(metadata.playerCol) : GRID_SIZE / 2;
        nextAbilityAvailableFloor = Math.max(currentFloor, metadata.nextAbilityAvailableFloor);
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
            profile.setCurrentHP(profile.getMaxHP());
        }
        clearPersistedState();
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You have been defeated. Would you like to restart or return to the main menu?")
                .setPositiveButton("Restart", (dialog, which) -> restartGame())
                .setNegativeButton("Main Menu", (dialog, which) -> goToMainMenu())
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
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        stopGridAnimationLoop();
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
        String key = profile.getPlayerClass().name().toLowerCase() + "_" + action.toLowerCase();
        return SoundManager.playAndReport(key);
    }

    private void revealTile(View tileView, TextView tileText, Tile tile) {
        tileText.setText(getTileDisplay(tile));
        tileText.setContentDescription(getTileContentDescription(tile));
        switch (tile.getType()) {
            case GOLD:
                int goldFound = GameBalance.calculateGoldPile(currentFloor, difficultyMode, random);
                currentGold += goldFound;
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
                updateGoldCounter();
                playCueWithFallback(SoundManager.KEY_EFFECT_TREASURE, FeedbackManager.SoundEffect.TREASURE);
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
                recordGoldEarned(goldFound);
                Toast.makeText(this, getString(R.string.gold_found_message, goldFound), Toast.LENGTH_SHORT).show();
                break;

            case ENEMY:
                if (tile.hasMonster()) {
                    startCombat(tile, tileView);
                }
                break;

            case STAIR_DOWN:
                playCueWithFallback(SoundManager.KEY_EFFECT_POSITIVE, FeedbackManager.SoundEffect.POSITIVE);
                fallToNextFloor();
                break;

            case STAIR_DOWN_LOCKED_RED:
            case STAIR_DOWN_LOCKED_BLUE:
            case STAIR_DOWN_LOCKED_GREEN:
                handleLockedStair(tileText, tile);
                break;

            case RED_KEY:
            case BLUE_KEY:
            case GREEN_KEY:
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
        String inventoryKeyName = getInventoryKeyNameForType(originalType);
        if (TextUtils.isEmpty(inventoryKeyName)) {
            return;
        }
        String displayName = getKeyDisplayName(originalType, tile.getCustomName());
        InventoryManager.adjustItemQuantity(this, inventoryKeyName, 1);
        playCueWithFallback(SoundManager.KEY_EFFECT_KEY_PICKUP, FeedbackManager.SoundEffect.POSITIVE);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
        Toast.makeText(this, getString(R.string.key_found_message, displayName), Toast.LENGTH_SHORT).show();
        addInventoryChange(displayName, true);
        tile.setType(TileType.EMPTY);
        tile.setMonster(null);
        if (tileText != null) {
            tileText.setText(getTileDisplay(tile));
            tileText.setContentDescription(getTileContentDescription(tile));
        }
    }

    private void handleLockedStair(TextView tileText, Tile tile) {
        String neededKey = getInventoryKeyNameForType(tile.getType());
        String neededKeyDisplay = getKeyDisplayName(tile.getType(), null);
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
            fallToNextFloor();
        } else {
            Toast.makeText(this,
                    getString(R.string.locked_stair_missing, neededKeyDisplay),
                    Toast.LENGTH_SHORT).show();
        }
        tileText.setText(getTileDisplay(tile));
        tileText.setContentDescription(getTileContentDescription(tile));
    }

    private String getInventoryKeyNameForType(TileType type) {
        switch (type) {
            case RED_KEY:
            case STAIR_DOWN_LOCKED_RED:
                return "RED KEY";
            case BLUE_KEY:
            case STAIR_DOWN_LOCKED_BLUE:
                return "BLUE KEY";
            case GREEN_KEY:
            case STAIR_DOWN_LOCKED_GREEN:
                return "GREEN KEY";
            default:
                return null;
        }
    }

    private String getKeyDisplayName(TileType type, String customName) {
        if (!TextUtils.isEmpty(customName)) {
            return customName;
        }
        switch (type) {
            case BLUE_KEY:
            case STAIR_DOWN_LOCKED_BLUE:
                return getString(R.string.tile_desc_key_blue);
            case GREEN_KEY:
            case STAIR_DOWN_LOCKED_GREEN:
                return getString(R.string.tile_desc_key_green);
            default:
                return getString(R.string.tile_desc_key_red);
        }
    }

    private void handleTrap(TextView tileText, Tile tile) {
        tileText.setText(getTileDisplay(tile));
        tileText.setContentDescription(getTileContentDescription(tile));
        if (InventoryManager.getItemQuantity(this, "Trap Disarm Kit") > 0) {
            Toast.makeText(this, "Trap disarmed!", Toast.LENGTH_SHORT).show();
            InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", -1);
            addInventoryChange("Trap Disarm Kit", false);
            tileText.setText("🧰");
            tileText.setContentDescription(getTileContentDescription(tile));
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

    private void poisonTick() {
        if (poisonTurnsLeft > 0) {
            poisonTurnsLeft--;
            applyTrapDamage();
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
            updateStatusText();
            new Handler(Looper.getMainLooper()).postDelayed(this::poisonTick, 1500);
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
            showVictoryDialog();
            unlockAchievement(R.string.achievement_victory_title);
        }
    }

    private void updateGoldCounter() {
        goldCounterText.setText(getString(R.string.gold_display_dynamic, currentGold));
    }

    private void updateHpCounter() {
        hpCounterText.setText(getString(R.string.hp_display_dynamic, profile.getCurrentHP(), profile.getMaxHP()));
    }

    private void updateStatusText() {
        StringBuilder status = new StringBuilder();
        if (frozenTurnsLeft > 0) status.append("Frozen: ").append(frozenTurnsLeft).append(" turns\n");
        if (poisonTurnsLeft > 0) status.append("Poisoned: ").append(poisonTurnsLeft).append(" turns\n");
        statusEffectText.setText(status.toString().trim());
    }

    private void addInventoryChange(String label, boolean gained) {
        String entry = getString(gained ? R.string.inventory_gain_message : R.string.inventory_use_message, label);
        inventoryChangeLog.add(0, entry);
        while (inventoryChangeLog.size() > 10) {
            inventoryChangeLog.remove(inventoryChangeLog.size() - 1);
        }
        Toast.makeText(this, entry, Toast.LENGTH_SHORT).show();
        playCueWithFallback(SoundManager.KEY_EFFECT_INVENTORY, FeedbackManager.SoundEffect.POSITIVE);
    }

    private void takeDamage(int amount) {
        int remaining = applyKnightShield(amount);
        if (remaining <= 0) {
            persistGameState();
            return;
        }
        profile.setCurrentHP(profile.getCurrentHP() - remaining);
        updateHpCounter();
        if (profile.getCurrentHP() <= 0) {
            showGameOverDialog();
        } else if (profile.getCurrentHP() == 1) {
            unlockAchievement(R.string.achievement_low_hp_survivor_title);
        }
        persistGameState();
    }

    @Override
    public void onCombatVictory(@NonNull Monster monster) {
        boolean convertedEnemy = activeCombatTile != null && activeCombatTile.getType() == TileType.ENEMY;
        if (activeCombatTile != null) {
            activeCombatTile.setMonster(null);
            activeCombatTile.setType(TileType.EMPTY);
        }
        refreshActiveCombatTileView();

        int xpReward = pendingCombatXpReward > 0
                ? pendingCombatXpReward
                : GameBalance.calculateXpReward(monster, currentFloor, difficultyMode, random);
        profile.addExperience(xpReward);
        int goldReward = pendingCombatGoldReward > 0
                ? pendingCombatGoldReward
                : GameBalance.calculateGoldReward(monster, currentFloor, difficultyMode, random);
        currentGold += goldReward;
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        updateGoldCounter();
        updateHpCounter();
        recordGoldEarned(goldReward);
        recordEnemyDefeat();
        FeedbackManager.playSound(this, FeedbackManager.SoundEffect.COMBAT_VICTORY);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
        Toast.makeText(this,
                getString(R.string.combat_result_victory, monster.getMonsterType(), xpReward, goldReward),
                Toast.LENGTH_SHORT).show();

        if (convertedEnemy) {
            safeTilesToReveal++;
            revealedSafeTiles++;
        }
        persistGameState();
        clearCombatTracking();
        if (convertedEnemy) {
            checkVictoryCondition();
        }
    }

    @Override
    public void onCombatDefeat() {
        FeedbackManager.playSound(this, FeedbackManager.SoundEffect.COMBAT_DEFEAT);
        FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.HEAVY);
        clearCombatTracking();
        handleGameOver();
    }

    @Override
    public void onCombatFled(int penaltyDamage) {
        if (penaltyDamage > 0) {
            takeDamage(penaltyDamage);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
            Toast.makeText(this,
                    getString(R.string.combat_result_flee, penaltyDamage),
                    Toast.LENGTH_SHORT).show();
            if (profile.isDead()) {
                clearCombatTracking();
                handleGameOver();
                return;
            }
        }

        refreshActiveCombatTileView();

        clearCombatTracking();
        persistGameState();
    }

    @Override
    public void onCombatStateUpdated() {
        updateHpCounter();
        persistGameState();
    }

    @Override
    public boolean onUseHealingPotionRequested(int healAmount) {
        if (InventoryManager.getItemQuantity(this, "Healing Potion") > 0) {
            InventoryManager.adjustItemQuantity(this, "Healing Potion", -1);
            addInventoryChange("Healing Potion", false);
            int healedHp = Math.min(profile.getCurrentHP() + healAmount, profile.getMaxHP());
            profile.setCurrentHP(healedHp);
            updateHpCounter();
            persistGameState();
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
                .setPositiveButton(R.string.play_again, (dialog, which) -> {
                    dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                    currentGold = 0;
                    profile.setCurrentHP(profile.getMaxHP());
                    generateDungeon();
                    renderGrid();
                    updateGoldCounter();
                    updateHpCounter();
                    persistGameState();
                })
                .setNegativeButton(R.string.main_menu, (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showGameOverDialog() {
        clearPersistedState();
        new AlertDialog.Builder(this)
                .setTitle(R.string.game_over_title)
                .setMessage(R.string.game_over_message)
                .setCancelable(false)
                .setPositiveButton(R.string.restart, (dialog, which) -> {
                    dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                    currentGold = 0;
                    profile.setCurrentHP(profile.getMaxHP());
                    generateDungeon();
                    renderGrid();
                    updateGoldCounter();
                    updateHpCounter();
                    persistGameState();
                })
                .setNegativeButton(R.string.main_menu, (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showInventoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_inventory, null, false);
        RecyclerView recycler = dialogView.findViewById(R.id.recyclerInventory);
        TextView goldView = dialogView.findViewById(R.id.textInventoryGold);
        TextView emptyView = dialogView.findViewById(R.id.textEmptyInventory);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        java.util.List<com.example.clickdungeon.model.InventoryItem> items = InventoryManager.loadInventory(this);
        recycler.setAdapter(new InventoryAdapter(items));
        goldView.setText(getString(R.string.gold_display_dynamic, InventoryManager.getGold(this)));
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        new AlertDialog.Builder(this)
                .setTitle(R.string.inventory_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGridAnimationLoop();
    }

    @Override
    protected void onPause() {
        stopGridAnimationLoop();
        super.onPause();
        persistGameState();
    }

    private static class MonsterTemplate {
        private final String name;
        private final String emoji;
        private final int baseHp;
        private final int baseAttack;
        private final int baseDefense;

        MonsterTemplate(String name, String emoji, int baseHp, int baseAttack, int baseDefense) {
            this.name = name;
            this.emoji = emoji;
            this.baseHp = baseHp;
            this.baseAttack = baseAttack;
            this.baseDefense = baseDefense;
        }

        Monster spawnForFloor(Random random, int floor, SettingsManager.Difficulty difficulty) {
            int scaling = Math.max(0, floor - 1);

            int hpBonus = scaling;
            int attackBonus = Math.max(0, (scaling + 1) / 2);
            int defenseBonus = Math.max(0, scaling / 3);

            if (scaling > 0) {
                hpBonus += random.nextInt(scaling + 1);
                attackBonus += random.nextInt(Math.max(1, (scaling / 2) + 1));
                defenseBonus += random.nextInt(Math.max(1, (scaling / 3) + 1));
            }

            int maxHp = Math.max(1, baseHp + hpBonus);
            int attack = Math.max(1, baseAttack + attackBonus);
            int defense = Math.max(0, baseDefense + defenseBonus);

            float scale = GameBalance.getFloorDifficultyScale(floor);
            maxHp = Math.max(1, Math.round(maxHp * scale));
            attack = Math.max(1, Math.round(attack * scale));
            defense = Math.max(0, Math.round(defense * scale));

            if (difficulty != null) {
                maxHp = difficulty.scaleMonsterHealth(maxHp);
                attack = difficulty.scaleMonsterAttack(attack);
                defense = difficulty.scaleMonsterDefense(defense);
            }

            return new Monster(name, maxHp, attack, defense, emoji);
        }
    }
}

