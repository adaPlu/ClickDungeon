package com.example.clickdungeon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ClassSelectionActivity.class);
            startActivity(intent);
        });

        btnContinue.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("player_profile", MODE_PRIVATE);
            String profile = prefs.getString("slot_1", null);
            if (profile != null) {
                Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No saved game found.", Toast.LENGTH_SHORT).show();
            }
        });

        btnAchievements.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, AchievementsActivity.class);
            startActivity(intent);
        });

        btnShop.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
