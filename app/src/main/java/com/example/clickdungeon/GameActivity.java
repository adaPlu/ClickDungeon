package com.example.clickdungeon;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.model.Monster;
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
    private TileType placedLockedStair = null;

    private CharacterProfile profile;
    private String placedKeyName = null;
    private final Monster[] monsterPool = new Monster[] {
            new Monster("Slime", 1, 1, "🟢"),
            new Monster("Goblin", 2, 1, "🧌"),
            new Monster("Skeleton", 2, 2, "💀"),
            new Monster("Orc", 3, 2, "🧟"),
            new Monster("Troll", 4, 3, "👹"),
            new Monster("Witch", 3, 3, "🧙"),
            new Monster("Demon", 5, 4, "😈"),
            new Monster("Dragon", 8, 5, "🐉")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gridLayout = findViewById(R.id.gridDungeon);
        goldCounterText = findViewById(R.id.textGoldCounter);
        hpCounterText = findViewById(R.id.textHpCounter);
        statusEffectText = findViewById(R.id.textStatus);
        floorText = findViewById(R.id.textFloor);
        classAbilityButton = findViewById(R.id.btnClassAbility);

        loadProfile();
        currentFloor = GameStateManager.loadFloor(this);
        Tile[][] savedGrid = GameStateManager.loadGrid(this);
        if (savedGrid != null) {
            dungeonGrid = savedGrid;
            currentGold = GameStateManager.loadGold(this);
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

    private void generateDungeon() {
        List<Tile> tiles = new ArrayList<>();

        for (int i = 0; i < 5; i++) tiles.add(new Tile(TileType.GOLD));
        for (int i = 0; i < 5; i++) {
            Monster m = monsterPool[new Random().nextInt(monsterPool.length)];
            tiles.add(new Tile(TileType.ENEMY, m));
        }
        for (int i = 0; i < 2; i++) tiles.add(new Tile(TileType.TRAP_FIRE));
        for (int i = 0; i < 2; i++) tiles.add(new Tile(TileType.TRAP_POISON));
        tiles.add(new Tile(TileType.TRAP_ACID));
        tiles.add(new Tile(TileType.TRAP_FREEZE));
        tiles.add(new Tile(TileType.TRAP_PITFALL));

        TileType[] keyTypes = {TileType.RED_KEY, TileType.BLUE_KEY, TileType.GREEN_KEY};
        TileType[] lockTypes = {TileType.STAIR_DOWN_LOCKED_RED, TileType.STAIR_DOWN_LOCKED_BLUE, TileType.STAIR_DOWN_LOCKED_GREEN};
        int keyIndex = (currentFloor - 1) % keyTypes.length;
        TileType selectedKeyType = keyTypes[keyIndex];
        placedKeyName = selectedKeyType.name().replace("_KEY", " Key (F" + currentFloor + ")");
        placedLockedStair = lockTypes[keyIndex];

        tiles.add(new Tile(selectedKeyType));
        tiles.add(new Tile(placedLockedStair));
        tiles.add(new Tile(TileType.STAIR_DOWN));
        if (currentFloor > 1) tiles.add(new Tile(TileType.STAIR_UP));

        while (tiles.size() < GRID_SIZE * GRID_SIZE) tiles.add(new Tile(TileType.EMPTY));
        Collections.shuffle(tiles);

        int index = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                dungeonGrid[row][col] = tiles.get(index++);
            }
        }

        safeTilesToReveal = 0;
        for (Tile t : tiles) {
            if (t.getType() != TileType.ENEMY) safeTilesToReveal++;
        }
    }
    private void fallToNextFloor() {
        currentFloor++;
        GameStateManager.saveFloor(this, currentFloor);
        generateDungeon();
        renderGrid();
        updateFloorDisplay();
        frozenTurnsLeft = 0;
        poisonTurnsLeft = 0;
        updateStatusText();
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

    private void updateFloorDisplay() {
        String label = "Floor " + currentFloor;

        // Optional: show a hint if this floor was reached via locked stairs
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
                    tileText.setText(tile.getType().toString());
                    if (tile.getType() != TileType.ENEMY) revealedSafeTiles++;
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
            revealTile(tileText, clickedTile);
            if (clickedTile.getType() != TileType.ENEMY) revealedSafeTiles++;
            GameStateManager.saveGrid(this, dungeonGrid, currentGold);
            checkVictoryCondition();
        }
    }

    private void revealTile(TextView tileText, Tile tile) {
        switch (tile.getType()) {
            case GOLD:
                tileText.setText("💰");
                currentGold++;
                updateGoldCounter();
                updateInventory("Gold", 1);
                break;

            case ENEMY:
                tileText.setText("💀");
                takeDamage(1);
                break;

            case EMPTY:
                tileText.setText("⬜");
                break;

            case STAIR_DOWN:
                tileText.setText("🪜");
                fallToNextFloor();
                break;

            case STAIR_DOWN_LOCKED_RED:
            case STAIR_DOWN_LOCKED_BLUE:
                String neededKey = tile.getType() == TileType.STAIR_DOWN_LOCKED_RED ? "Red Key" : "Blue Key";
                List<InventoryItem> inventory = InventoryManager.loadInventory(this);
                boolean hasKey = false;

                for (InventoryItem item : inventory) {
                    if (item.getName().equals(neededKey) && item.getQuantity() > 0) {
                        hasKey = true;
                        updateInventory(neededKey, -1);
                        break;
                    }
                }

                if (hasKey) {
                    Toast.makeText(this, "Unlocked stair with " + neededKey + "!", Toast.LENGTH_SHORT).show();
                    tileText.setText(tile.getType() == TileType.STAIR_DOWN_LOCKED_RED ? "🔴🪜" : "🔵🪜");
                    fallToNextFloor();
                } else {
                    tileText.setText(tile.getType() == TileType.STAIR_DOWN_LOCKED_RED ? "🔴🪜" : "🔵🪜");
                    Toast.makeText(this, "You need the " + neededKey + "!", Toast.LENGTH_SHORT).show();
                }
                break;

            case STAIR_UP:
                tileText.setText("⤴️");
                break;

            default:
                handleTrap(tileText, tile);
                break;
        }
    }


    private void handleTrap(TextView tileText, Tile tile) {
        List<InventoryItem> inventory = InventoryManager.loadInventory(this);
        InventoryItem kit = null;
        for (InventoryItem item : inventory) {
            if (item.getName().equals("Trap Disarm Kit") && item.getQuantity() > 0) {
                kit = item;
                break;
            }
        }

        if (kit != null) {
            Toast.makeText(this, "Trap disarmed!", Toast.LENGTH_SHORT).show();
            updateInventory("Trap Disarm Kit", -1);
            tileText.setText("🧰");
        } else {
            switch (tile.getType()) {
                case TRAP_FIRE:
                    tileText.setText("🔥");
                    takeDamage(1);
                    break;
                case TRAP_ACID:
                    tileText.setText("🧪");
                    takeDamage(1);
                    break;
                case TRAP_POISON:
                    tileText.setText("☠️");
                    poisonTurnsLeft = 3;
                    poisonTick();
                    break;
                case TRAP_FREEZE:
                    tileText.setText("❄️");
                    frozenTurnsLeft = 2 + (int) (Math.random() * 2);
                    updateStatusText();
                    break;
                case TRAP_PITFALL:
                    tileText.setText("🕳️");
                    fallToNextFloor();
                    break;
            }
        }
    }

    private void poisonTick() {
        if (poisonTurnsLeft > 0) {
            poisonTurnsLeft--;
            takeDamage(1);
            updateStatusText();
            new Handler().postDelayed(this::poisonTick, 1500);
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

    private void searchAdjacentForTraps(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr, nc = col + dc;
                if (nr >= 0 && nc >= 0 && nr < GRID_SIZE && nc < GRID_SIZE) {
                    Tile t = dungeonGrid[nr][nc];
                    if (!t.isRevealed() && t.getType().name().startsWith("TRAP")) {
                        Toast.makeText(this, "Trap nearby at (" + nr + "," + nc + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
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

    private void takeDamage(int amount) {
        profile.setCurrentHP(profile.getCurrentHP() - amount);
        updateHpCounter();
        if (profile.getCurrentHP() <= 0) showGameOverDialog();
        else if (profile.getCurrentHP() == 1) AchievementManager.unlock(this, "LOW_HP_SURVIVOR");
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        String json = prefs.getString("profile", null);
        if (json != null) {
            profile = new Gson().fromJson(json, CharacterProfile.class);
        } else {
            Toast.makeText(this, "No profile found! Returning to menu...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ClassSelectionActivity.class));
            finish();
        }
    }

    private void showVictoryDialog() {
        GameStateManager.clearState(this);
        new AlertDialog.Builder(this)
                .setTitle("Victory!")
                .setMessage("You revealed all safe tiles. Well done!")
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                    currentGold = 0;
                    profile.setCurrentHP(profile.getMaxHP());
                    generateDungeon();
                    renderGrid();
                    updateGoldCounter();
                    updateHpCounter();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void showGameOverDialog() {
        GameStateManager.clearState(this);
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You've run out of HP!")
                .setCancelable(false)
                .setPositiveButton("Restart", (dialog, which) -> {
                    dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                    currentGold = 0;
                    profile.setCurrentHP(profile.getMaxHP());
                    generateDungeon();
                    renderGrid();
                    updateGoldCounter();
                    updateHpCounter();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GameStateManager.saveGrid(this, dungeonGrid, currentGold);
    }
}
