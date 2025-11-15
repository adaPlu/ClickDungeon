//Made By Ada Pluguez
//01/01/2025
//Java based Android click based RPG
package com.example.clickdungeon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.AnimatedMonster;
import com.example.clickdungeon.model.AnimatedPlayer;
import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.MonsterFactory;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.util.AchievementManager;
import com.example.clickdungeon.util.GameStateManager;
import com.example.clickdungeon.util.InventoryManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;

public class GameActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 5;
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

    private static final int FRAME_COUNT = 4;
    private static final int FRAME_SIZE = 64;
    private static final long FRAME_DURATION = 150;
    private int spriteResId;
    private int activeSlot = 1;

    private static final String[] MONSTER_TYPES = {
            "slime", "goblin", "skeleton", "orc",
            "troll", "witch", "demon", "dragon"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        activeSlot = getIntent().getIntExtra("save_slot", 1);

        gridLayout = findViewById(R.id.gridDungeon);
        goldCounterText = findViewById(R.id.textGoldCounter);
        hpCounterText = findViewById(R.id.textHpCounter);
        statusEffectText = findViewById(R.id.textStatus);
        floorText = findViewById(R.id.textFloor);
        classAbilityButton = findViewById(R.id.btnClassAbility);

        loadProfile();

        if (profile.getAnimatedPlayer() == null) {
            profile.setAnimatedPlayer(new AnimatedPlayer(this, profile.getPlayerClass(), FRAME_SIZE, FRAME_SIZE, FRAME_COUNT, FRAME_DURATION));
        }

        currentFloor = GameStateManager.loadFloor(this, activeSlot);
        Tile[][] savedGrid = GameStateManager.loadGrid(this, activeSlot);
        if (savedGrid != null) {
            dungeonGrid = savedGrid;
            currentGold = GameStateManager.loadGold(this, activeSlot);
        } else {
            dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
            generateDungeon();
            updateInventory("Trap Disarm Kit", 1);
        }

        gridLayout.setColumnCount(GRID_SIZE);
        gridLayout.setRowCount(GRID_SIZE);

        renderGrid();
        updateGoldCounter();
        updateHpCounter();
        updateStatusText();
        setupClassAbilityButton();
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        String json = prefs.getString("profile", null);
        if (json != null) {
            profile = new Gson().fromJson(json, CharacterProfile.class);
            switch (profile.getPlayerClass()) {
                case THIEF:
                    spriteResId = R.drawable.thief_sprite_sheet;
                    break;
                case WIZARD:
                    spriteResId = R.drawable.wizard_sprite_sheet;
                    break;
                default:
                    spriteResId = R.drawable.knight_sprite_sheet;
            }
            AnimatedPlayer playerAnim = new AnimatedPlayer(this, profile.getPlayerClass(), FRAME_SIZE, FRAME_SIZE, FRAME_COUNT, FRAME_DURATION);
            profile.setAnimatedPlayer(playerAnim);
        } else {
            Toast.makeText(this, R.string.no_profile_found, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ClassSelectionActivity.class));
            finish();
        }
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
                    tileText.setText(tile.getType().toString());
                    revealedSafeTiles++;
                } else {
                    tileText.setText("?");
                }

                final int r = row, c = col;
                tileView.setOnClickListener(v -> handleTileClick(r, c, tileText));
                gridLayout.addView(tileView);
            }
        }
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
            if (clickedTile.getType() != TileType.ENEMY) revealedSafeTiles++;
            GameStateManager.saveGrid(this, dungeonGrid, currentGold, activeSlot);
            checkVictoryCondition();
        }
    }

    private void revealTile(TextView tileText, Tile tile) {
        tileText.setText(tile.getType().toString());
        // You can extend this method to show icons, damage, or animations
    }

    private void searchAdjacentForTraps(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr;
                int nc = col + dc;
                if (nr >= 0 && nc >= 0 && nr < GRID_SIZE && nc < GRID_SIZE) {
                    Tile t = dungeonGrid[nr][nc];
                    if (!t.isRevealed() && t.getType().name().startsWith("TRAP")) {
                        Toast.makeText(this, "Trap nearby at (" + nr + "," + nc + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
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
            AchievementManager.unlock(this, "VICTORY");
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

    private void updateInventory(String itemName, int quantity) {
        List<InventoryItem> inventory = InventoryManager.loadInventory(this);
        boolean found = false;
        for (int i = 0; i < inventory.size(); i++) {
            InventoryItem item = inventory.get(i);
            if (item.getName().equals(itemName)) {
                int newQty = item.getQuantity() + quantity;
                inventory.set(i, new InventoryItem(item.getName(), Math.min(newQty, 3)));
                found = true;
                break;
            }
        }
        if (!found && quantity > 0) {
            inventory.add(new InventoryItem(itemName, Math.min(quantity, 3)));
        }
        InventoryManager.saveInventory(this, inventory);
    }

    private void showVictoryDialog() {
        GameStateManager.clearSlot(this, activeSlot);
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
        GameStateManager.clearSlot(this, activeSlot);
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
        GameStateManager.saveGrid(this, dungeonGrid, currentGold, activeSlot);
    }
}
