package com.example.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.util.SaveManager;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnNewGame;
    private Button btnContinue;
    private Button btnAchievements;
    private Button btnShop;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnNewGame = findViewById(R.id.btnNewGame);
        btnContinue = findViewById(R.id.btnContinue);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnShop = findViewById(R.id.btnShop);
        btnSettings = findViewById(R.id.btnSettings);

        updateContinueState();

        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ContinueActivity.class);
            intent.putExtra(ContinueActivity.EXTRA_FORCE_NEW_GAME, true);
            startActivity(intent);
        });

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ContinueActivity.class);
            startActivity(intent);
        });

        btnAchievements.setOnClickListener(v ->
                startActivity(new Intent(this, AchievementsActivity.class)));

        btnShop.setOnClickListener(v ->
                startActivity(new Intent(this, ShopActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateContinueState();
    }

    private void updateContinueState() {
        boolean hasSave = hasAnySavedSlot();
        btnContinue.setEnabled(hasSave);
        btnContinue.setAlpha(hasSave ? 1f : 0.5f);
    }

    private boolean hasAnySavedSlot() {
        SaveManager saveManager = new SaveManager(this);
        for (int i = 0; i < 4; i++) {
            if (saveManager.isSlotOccupied(i)) {
                return true;
            }
        }
        return false;
    }
}
