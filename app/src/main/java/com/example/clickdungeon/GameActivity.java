package com.example.clickdungeon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.model.InventoryItem;
import com.example.clickdungeon.util.GameStateManager;
import com.example.clickdungeon.util.InventoryManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 5;
    private GridLayout gridLayout;
    private TextView goldCounterText;
    private Tile[][] dungeonGrid;
    private int currentGold = 0;
    private int safeTilesToReveal = 0;
    private int revealedSafeTiles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        View gridRoot = findViewById(R.id.gridDungeon);
        if (gridRoot instanceof GridLayout) {
            gridLayout = (GridLayout) gridRoot;
        } else {
            throw new IllegalStateException("Layout tag must be GridLayout with id gridDungeon");
        }

        goldCounterText = findViewById(R.id.textGoldCounter);

        Tile[][] savedGrid = GameStateManager.loadGrid(this);
        if (savedGrid != null) {
            dungeonGrid = savedGrid;
            currentGold = GameStateManager.loadGold(this);
        } else {
            dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
            generateDungeon();
        }

        gridLayout.setColumnCount(GRID_SIZE);
        gridLayout.setRowCount(GRID_SIZE);

        renderGrid();
        updateGoldCounter();
    }

    private void generateDungeon() {
        List<Tile> tiles = new ArrayList<>();

        for (int i = 0; i < 5; i++) tiles.add(new Tile(TileType.GOLD));
        for (int i = 0; i < 5; i++) tiles.add(new Tile(TileType.ENEMY));
        for (int i = 0; i < GRID_SIZE * GRID_SIZE - 10; i++) tiles.add(new Tile(TileType.EMPTY));

        safeTilesToReveal = GRID_SIZE * GRID_SIZE - 5; // exclude enemies

        Collections.shuffle(tiles);

        int index = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                dungeonGrid[row][col] = tiles.get(index++);
            }
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
                    revealTile(tileText, tile);
                    if (tile.getType() != TileType.ENEMY) revealedSafeTiles++;
                } else {
                    tileText.setText("?");
                }

                final int finalRow = row;
                final int finalCol = col;

                tileView.setOnClickListener(v -> {
                    Tile clickedTile = dungeonGrid[finalRow][finalCol];
                    if (!clickedTile.isRevealed()) {
                        clickedTile.reveal();
                        revealTile(tileText, clickedTile);
                        if (clickedTile.getType() != TileType.ENEMY) revealedSafeTiles++;
                        GameStateManager.saveGrid(this, dungeonGrid, currentGold);
                        checkVictoryCondition();
                    }
                });

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = GridLayout.LayoutParams.WRAP_CONTENT;
                params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                tileView.setLayoutParams(params);

                gridLayout.addView(tileView);
            }
        }
    }

    private void revealTile(TextView tileText, Tile tile) {
        switch (tile.getType()) {
            case GOLD:
                tileText.setText("\uD83D\uDCB0"); // 💰
                currentGold++;
                updateGoldCounter();
                updateInventory("Gold", 1);
                Toast.makeText(this, "You found gold!", Toast.LENGTH_SHORT).show();

                // TODO: Check for achievement unlocks (e.g. First Gold, 5 Gold)
                // AchievementManager.unlock(this, "FIRST_GOLD");
                break;

            case ENEMY:
                tileText.setText("\uD83D\uDC80"); // 💀
                showGameOverDialog();
                // TODO: Could track encounters/survivals for achievement later
                break;

            case EMPTY:
                tileText.setText("⬜");
                Toast.makeText(this, "Nothing here...", Toast.LENGTH_SHORT).show();

                // TODO: Count revealed empty tiles for future milestones
                break;
        }
    }

    private void showGameOverDialog() {
        GameStateManager.clearState(this);

        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("You clicked on an enemy! What would you like to do?")
                .setCancelable(false)
                .setPositiveButton("Restart", (dialog, which) -> {
                    dungeonGrid = new Tile[GRID_SIZE][GRID_SIZE];
                    currentGold = 0;
                    generateDungeon();
                    renderGrid();
                    updateGoldCounter();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
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
                    generateDungeon();
                    renderGrid();
                    updateGoldCounter();
                })
                .setNegativeButton("Main Menu", (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainMenuActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();

        // TODO: Unlock victory achievement
        // AchievementManager.unlock(this, "FIRST_WIN");
    }

    private void checkVictoryCondition() {
        if (revealedSafeTiles >= safeTilesToReveal) {
            showVictoryDialog();
        }
    }

    private void updateGoldCounter() {
        goldCounterText.setText("Gold: " + currentGold);
    }

    private void updateInventory(String itemName, int quantity) {
        List<InventoryItem> inventory = InventoryManager.loadInventory(this);
        boolean found = false;
        for (int i = 0; i < inventory.size(); i++) {
            InventoryItem item = inventory.get(i);
            if (item.getName().equals(itemName)) {
                inventory.set(i, new InventoryItem(item.getName(), item.getQuantity() + quantity));
                found = true;
                break;
            }
        }
        if (!found) {
            inventory.add(new InventoryItem(itemName, quantity));
        }
        InventoryManager.saveInventory(this, inventory);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GameStateManager.saveGrid(this, dungeonGrid, currentGold);
    }
}
