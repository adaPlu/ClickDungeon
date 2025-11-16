package com.example.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
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
        btnNewGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ClassSelectionActivity.class);
            startActivity(intent);
        });

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ContinueActivity.class);
            startActivity(intent);
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
