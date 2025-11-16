//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.example.clickdungeon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.AnimatedMonster;
import com.example.clickdungeon.model.AnimatedPlayer;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.MonsterFactory;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.ui.CombatDialogFragment;
import com.example.clickdungeon.util.AchievementManager;
import com.example.clickdungeon.util.FeedbackManager;
import com.example.clickdungeon.util.DungeonGenerator;
import com.example.clickdungeon.util.GameBalance;
import com.example.clickdungeon.util.GameStateManager;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.util.OnboardingManager;
import com.example.clickdungeon.util.SaveManager;
import com.example.clickdungeon.util.SettingsManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;

public class GameActivity extends AppCompatActivity implements CombatDialogFragment.CombatCallbacks {

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

    private static final int GRID_SIZE = 5;
    private static final String TAG_COMBAT_DIALOG = "CombatDialog";
    private GridLayout gridLayout;
    private TextView goldCounterText, hpCounterText, statusEffectText, floorText;
    private Button classAbilityButton;
    private Tile[][] dungeonGrid;
    private int currentGold = 0;
    private int safeTilesToReveal = 0;
    private int revealedSafeTiles = 0;
    private int currentFloor = 1;
    private int frozenTurnsLeft = 0;
    private int poisonTurnsLeft = 0;
    private boolean thiefScanMode = false;

    private CharacterProfile profile;
    private String placedKeyName = null;
    private final Random random = new Random();
    private final MonsterTemplate[] monsterTemplates = new MonsterTemplate[] {
            new MonsterTemplate("Slime", "🟢", 3, 1, 0),
            new MonsterTemplate("Goblin", "🧌", 4, 2, 1),
            new MonsterTemplate("Skeleton", "💀", 5, 2, 2),
            new MonsterTemplate("Orc", "🪓", 6, 3, 2),
            new MonsterTemplate("Troll", "👹", 7, 3, 3),
            new MonsterTemplate("Witch", "🧙", 6, 4, 2),
            new MonsterTemplate("Vampire", "🧛", 6, 4, 3),
            new MonsterTemplate("Demon", "😈", 8, 5, 3),
            new MonsterTemplate("Dragon", "🐉", 10, 6, 4)
    };

    private SaveManager saveManager;
    private SettingsManager.Difficulty difficultyMode;
    private int activeSlotIndex = -1;
    private boolean launchingNewSlotGame = false;
    private Tile activeCombatTile = null;
    private TextView activeCombatTileView = null;
    private boolean colorBlindModeEnabled = false;
    private int pendingCombatGoldReward = 0;
    private int pendingCombatXpReward = 0;

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

        AchievementManager.loadAchievements(this);

        saveManager = new SaveManager(this);
        Intent launchIntent = getIntent();
        activeSlotIndex = launchIntent.getIntExtra(EXTRA_SLOT_INDEX, -1);
        launchingNewSlotGame = launchIntent.getBooleanExtra(EXTRA_IS_NEW_GAME, false);
        String profileJsonOverride = launchIntent.getStringExtra(EXTRA_PROFILE_JSON);

        if (activeSlotIndex >= 0) {
            if (launchingNewSlotGame) {
                if (!loadProfileForSession(profileJsonOverride, true)) {
                    return;
                }
                currentFloor = 1;
                currentGold = 0;
                dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                generateDungeon();
                InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", 1, INVENTORY_STACK_LIMIT);
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
                persistGameState();
            } else {
                SaveManager.GameState gameState = saveManager.loadGame(activeSlotIndex);
                if (gameState == null) {
                    Toast.makeText(this, R.string.continue_slot_load_failed, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                profile = gameState.profile;
                currentFloor = gameState.currentFloor;
                currentGold = gameState.currentGold;
                dungeonGrid = gameState.dungeonGrid;
                recalculateSafeTileTargets(dungeonGrid);
                restoreLockedStairStateFromGrid(dungeonGrid);
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
            }
        } else {
            if (!loadProfileForSession(profileJsonOverride, true)) {
                return;
            }
            currentFloor = GameStateManager.loadFloor(this);
            Tile[][] savedGrid = GameStateManager.loadGrid(this);
            if (savedGrid != null) {
                dungeonGrid = savedGrid;
                currentGold = GameStateManager.loadGold(this);
                recalculateSafeTileTargets(dungeonGrid);
                restoreLockedStairStateFromGrid(dungeonGrid);
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
            } else {
                dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                generateDungeon();
                InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", 1, INVENTORY_STACK_LIMIT);
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
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
        OnboardingManager.showDungeonTutorialIfNeeded(this, difficultyMode, colorBlindModeEnabled);
    }

    private void generateDungeon() {
        DungeonGenerator.Result result = DungeonGenerator.generateFloor(GRID_SIZE, currentFloor,
                () -> createRandomMonsterForCurrentFloor());
        dungeonGrid = result.grid;
        placedLockedStair = result.lockedStair;
        placedKeyName = result.keyName;
        safeTilesToReveal = result.safeTiles;
    }

    private void fallToNextFloor() {
        currentFloor++;
        if (activeSlotIndex < 0) {
            GameStateManager.saveFloor(this, currentFloor);
        }
        generateDungeon();
        renderGrid();
        updateFloorDisplay();
        frozenTurnsLeft = 0;
        poisonTurnsLeft = 0;
        updateStatusText();
        persistGameState();
        Toast.makeText(this, "You fell to Floor " + currentFloor + "!", Toast.LENGTH_LONG).show();
    }

    private void setupClassAbilityButton() {
        if (profile.getPlayerClass() == PlayerClass.WIZARD || profile.getPlayerClass() == PlayerClass.THIEF) {
            classAbilityButton.setVisibility(View.VISIBLE);
            classAbilityButton.setOnClickListener(v -> {
                if (profile.getPlayerClass() == PlayerClass.WIZARD) {
                    revealAllTraps();
                    Toast.makeText(this, "All traps revealed!", Toast.LENGTH_SHORT).show();
                } else if (profile.getPlayerClass() == PlayerClass.THIEF) {
                    thiefScanMode = true;
                    Toast.makeText(this, "Tap a tile to scan for traps...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean loadProfileForSession(String profileJsonOverride, boolean allowStoredFallback) {
        if (profileJsonOverride != null) {
            profile = new Gson().fromJson(profileJsonOverride, CharacterProfile.class);
        }
        if (profile == null && allowStoredFallback) {
            SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
            String json = prefs.getString("profile", null);
            if (json != null) {
                profile = new Gson().fromJson(json, CharacterProfile.class);
            }
        }
        if (profile == null) {
            Toast.makeText(this, R.string.profile_missing_message, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ClassSelectionActivity.class));
            finish();
            return false;
        }
        return true;
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
        MonsterTemplate template = monsterTemplates[random.nextInt(monsterTemplates.length)];
        return template.spawnForFloor(random, currentFloor, difficultyMode);
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
        if (dungeonGrid == null || profile == null) {
            return;
        }
        InventoryManager.syncGoldWithCurrentRun(this, currentGold);
        if (activeSlotIndex >= 0) {
            if (saveManager != null) {
                saveManager.saveGame(activeSlotIndex, profile, currentFloor, currentGold, dungeonGrid);
            }
        } else {
            GameStateManager.saveGrid(this, dungeonGrid, currentGold);
            GameStateManager.saveFloor(this, currentFloor);
        }
    }

    private void clearPersistedState() {
        if (activeSlotIndex >= 0) {
            if (saveManager != null) {
                saveManager.deleteSave(activeSlotIndex);
            }
        } else {
            GameStateManager.clearState(this);
        }
    }

    private void updateFloorDisplay() {
        String label = "Floor " + currentFloor;
        if (placedLockedStair != null) {
            label += " (Hard)";
        }
        floorText.setText(label);
    }

    private void renderGrid() {
        LayoutInflater inflater = getLayoutInflater();
        gridLayout.removeAllViews();
        revealedSafeTiles = 0;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                View tileView = inflater.inflate(R.layout.item_tile, gridLayout, false);
                TextView tileText = tileView.findViewById(R.id.textTile);
                Tile tile = dungeonGrid[row][col];

                if (tile.isRevealed()) {
                    tileText.setText(getTileDisplay(tile));
                    tileText.setContentDescription(getTileContentDescription(tile));
                    if (tile.getType() != TileType.ENEMY) revealedSafeTiles++;
                } else {
                    tileText.setText("?");
                    tileText.setContentDescription(getString(R.string.tile_desc_hidden));
                }

                final int r = row, c = col;
                tileView.setOnClickListener(v -> handleTileClick(r, c, tileText));
                gridLayout.addView(tileView);
            }
        }
    }

    private String getTileDisplay(Tile tile) {
        if (tile == null) {
            return "?";
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

    private void startCombat(Tile tile, TextView tileText) {
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
        activeCombatTileView = tileText;
        fragment.show(getSupportFragmentManager(), TAG_COMBAT_DIALOG);
    }

    private void handleTileClick(int row, int col, TextView tileText) {
        Tile clickedTile = dungeonGrid[row][col];
        if (!clickedTile.isRevealed()) {
            if (thiefScanMode && profile.getPlayerClass() == PlayerClass.THIEF) {
                searchAdjacentForTraps(row, col);
                thiefScanMode = false;
                return;
            }
            clickedTile.reveal();
            if (profile.getAnimatedPlayer() != null) {
                profile.getAnimatedPlayer().setAction("move");
            }
            revealTile(tileText, clickedTile);
            if (clickedTile.getType() != TileType.ENEMY) {
                revealedSafeTiles++;
                recordSafeTileReveal();
            }
            persistGameState();
            checkVictoryCondition();
        }
    }

    private void handleGameOver() {
        clearPersistedState();
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You have been defeated. Would you like to restart or return to the main menu?")
                .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setNegativeButton("Main Menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToMainMenu();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void restartGame() {
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

    private void revealTile(TextView tileText, Tile tile) {
        tileText.setText(getTileDisplay(tile));
        tileText.setContentDescription(getTileContentDescription(tile));
        switch (tile.getType()) {
            case GOLD:
                int goldFound = GameBalance.calculateGoldPile(currentFloor, difficultyMode, random);
                currentGold += goldFound;
                InventoryManager.syncGoldWithCurrentRun(this, currentGold);
                updateGoldCounter();
                FeedbackManager.playSound(this, FeedbackManager.SoundEffect.TREASURE);
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
                recordGoldEarned(goldFound);
                Toast.makeText(this, getString(R.string.gold_found_message, goldFound), Toast.LENGTH_SHORT).show();
                break;

            case ENEMY:
                if (tile.hasMonster()) {
                    startCombat(tile, tileText);
                }
                break;

            case STAIR_DOWN:
                fallToNextFloor();
                break;

            case STAIR_DOWN_LOCKED_RED:
            case STAIR_DOWN_LOCKED_BLUE:
            case STAIR_DOWN_LOCKED_GREEN:
                handleLockedStair(tileText, tile);
                break;

            case STAIR_UP:
                // Visual update already applied above.
                break;

            case EMPTY:
                // Nothing else to do beyond revealing the glyph.
                break;

            default:
                handleTrap(tileText, tile);
                break;
        }
    }

    private void handleLockedStair(TextView tileText, Tile tile) {
        String neededKey;
        String neededKeyDisplay;
        switch (tile.getType()) {
            case STAIR_DOWN_LOCKED_RED:
                neededKey = "RED KEY";
                neededKeyDisplay = "Red Key";
                break;
            case STAIR_DOWN_LOCKED_BLUE:
                neededKey = "BLUE KEY";
                neededKeyDisplay = "Blue Key";
                break;
            case STAIR_DOWN_LOCKED_GREEN:
                neededKey = "GREEN KEY";
                neededKeyDisplay = "Green Key";
                break;
            default:
                return;
        }

        if (InventoryManager.getItemQuantity(this, neededKey) > 0) {
            InventoryManager.adjustItemQuantity(this, neededKey, -1);
            Toast.makeText(this, "Unlocked stair with " + neededKeyDisplay + "!", Toast.LENGTH_SHORT).show();
            fallToNextFloor();
        } else {
            Toast.makeText(this, "You need the " + neededKeyDisplay + "!", Toast.LENGTH_SHORT).show();
        }
        tileText.setText(getTileDisplay(tile));
        tileText.setContentDescription(getTileContentDescription(tile));
    }

    private void handleTrap(TextView tileText, Tile tile) {
        tileText.setText(getTileDisplay(tile));
        tileText.setContentDescription(getTileContentDescription(tile));
        if (InventoryManager.getItemQuantity(this, "Trap Disarm Kit") > 0) {
            Toast.makeText(this, "Trap disarmed!", Toast.LENGTH_SHORT).show();
            InventoryManager.adjustItemQuantity(this, "Trap Disarm Kit", -1);
            tileText.setText("🧰");
            tileText.setContentDescription(getTileContentDescription(tile));
            unlockAchievement(R.string.achievement_trap_dodger_title);
            FeedbackManager.playSound(this, FeedbackManager.SoundEffect.POSITIVE);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
            return;
        }

        switch (tile.getType()) {
            case TRAP_FIRE:
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
                applyTrapDamage(1);
                break;
            case TRAP_ACID:
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
                applyTrapDamage(1);
                break;
            case TRAP_POISON:
                poisonTurnsLeft = 3;
                poisonTick();
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
                FeedbackManager.playSound(this, FeedbackManager.SoundEffect.TRAP);
                break;
            case TRAP_FREEZE:
                frozenTurnsLeft = 2 + random.nextInt(2);
                updateStatusText();
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
                FeedbackManager.playSound(this, FeedbackManager.SoundEffect.TRAP);
                break;
            case TRAP_PITFALL:
                FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.HEAVY);
                FeedbackManager.playSound(this, FeedbackManager.SoundEffect.TRAP);
                fallToNextFloor();
                break;
            default:
                break;
        }
    }

    private void poisonTick() {
        if (poisonTurnsLeft > 0) {
            poisonTurnsLeft--;
            applyTrapDamage(1);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.LIGHT);
            updateStatusText();
            new Handler().postDelayed(this::poisonTick, 1500);
        }
    }

    private void applyTrapDamage(int baseDamage) {
        int finalDamage = baseDamage;
        if (difficultyMode != null) {
            finalDamage = difficultyMode.scaleTrapDamage(baseDamage);
        }
        if (finalDamage <= 0) {
            return;
        }
        takeDamage(finalDamage);
        FeedbackManager.playSound(this, FeedbackManager.SoundEffect.TRAP);
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

    private void takeDamage(int amount) {
        profile.setCurrentHP(profile.getCurrentHP() - amount);
        updateHpCounter();
        if (profile.getCurrentHP() <= 0) showGameOverDialog();
        else if (profile.getCurrentHP() == 1) {
            unlockAchievement(R.string.achievement_low_hp_survivor_title);
        }
    }

    @Override
    public void onCombatVictory(@NonNull Monster monster) {
        boolean convertedEnemy = activeCombatTile != null && activeCombatTile.getType() == TileType.ENEMY;
        if (activeCombatTile != null) {
            activeCombatTile.setMonster(null);
            activeCombatTile.setType(TileType.EMPTY);
        }
        if (activeCombatTileView != null) {
            activeCombatTileView.setText(getString(R.string.combat_tile_cleared));
        }

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
            profile.takeDamage(penaltyDamage);
            FeedbackManager.vibrate(this, FeedbackManager.VibrationPattern.MEDIUM);
            Toast.makeText(this,
                    getString(R.string.combat_result_flee, penaltyDamage),
                    Toast.LENGTH_SHORT).show();
            updateHpCounter();
            if (profile.isDead()) {
                clearCombatTracking();
                handleGameOver();
                return;
            }
        }

        if (activeCombatTileView != null && activeCombatTile != null) {
            activeCombatTileView.setText(getTileDisplay(activeCombatTile));
            activeCombatTileView.setContentDescription(getTileContentDescription(activeCombatTile));
        }

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

    private void generateDungeon() {
        List<Tile> tiles = new ArrayList<>();
        Random rand = new Random();
        int numGold = 4 + currentFloor;
        int numEnemies = 3 + currentFloor;
        int numTraps = 2 + (currentFloor / 2);

        for (int i = 0; i < numGold; i++) tiles.add(new Tile(TileType.GOLD));
        for (int i = 0; i < numEnemies; i++) {
            String type = MONSTER_TYPES[rand.nextInt(MONSTER_TYPES.length)];
            AnimatedMonster monster = MonsterFactory.create(this, type);
            tiles.add(new Tile(TileType.ENEMY, monster));
        }

        TileType[] trapTypes = {TileType.TRAP_FIRE, TileType.TRAP_POISON, TileType.TRAP_FREEZE, TileType.TRAP_ACID, TileType.TRAP_PITFALL};
        for (int i = 0; i < numTraps; i++) {
            TileType trap = trapTypes[rand.nextInt(trapTypes.length)];
            tiles.add(new Tile(trap));
        }

        tiles.add(new Tile(TileType.STAIR_DOWN));
        if (currentFloor > 1) tiles.add(new Tile(TileType.STAIR_UP));

        while (tiles.size() < GRID_SIZE * GRID_SIZE) {
            tiles.add(new Tile(TileType.EMPTY));
        }

        Collections.shuffle(tiles);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (dungeonGrid == null) dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                dungeonGrid[r][c] = tiles.get(r * GRID_SIZE + c);
                if (dungeonGrid[r][c].getType() != TileType.ENEMY) safeTilesToReveal++;
            }
        }
    }

    @Override
    protected void onPause() {
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

            if (difficulty != null) {
                maxHp = difficulty.scaleMonsterHealth(maxHp);
                attack = difficulty.scaleMonsterAttack(attack);
                defense = difficulty.scaleMonsterDefense(defense);
            }

            return new Monster(name, maxHp, attack, defense, emoji);
        }
    }
}
