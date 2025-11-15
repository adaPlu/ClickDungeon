package com.example.clickdungeon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnNewGame, btnContinue, btnAchievements, btnShop, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnNewGame = findViewById(R.id.btnNewGame);
        btnContinue = findViewById(R.id.btnContinue);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnShop = findViewById(R.id.btnShop);
        btnSettings = findViewById(R.id.btnSettings);

        // Disable Continue if no saved slots exist
        if (!hasAnySavedSlot()) {
            btnContinue.setEnabled(false);
            btnContinue.setAlpha(0.5f); // visually indicate disabled state
        }

        // ✅ New Game: go to slot selection for character + slot
        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SlotSelectionActivity.class);
            intent.putExtra("mode", "new_game");
            startActivity(intent);
        });

        // ✅ Continue: pick from available slots to resume
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SlotSelectionActivity.class);
            intent.putExtra("mode", "continue");
            startActivity(intent);
        });

        btnAchievements.setOnClickListener(v -> {
            startActivity(new Intent(this, AchievementsActivity.class));
        });

        btnShop.setOnClickListener(v -> {
            startActivity(new Intent(this, ShopActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private boolean hasAnySavedSlot() {
        SharedPreferences prefs = getSharedPreferences("player_prefs", Context.MODE_PRIVATE);
        for (int i = 1; i <= 4; i++) {
            if (prefs.contains("grid_slot_" + i)) return true;
        }
        return false;
    }
}
