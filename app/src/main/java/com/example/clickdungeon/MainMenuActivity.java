package com.example.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.util.GameStateManager;

public class MainMenuActivity extends AppCompatActivity {

    private Button playButton, continueButton, settingsButton, shopButton, achievementsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        playButton = findViewById(R.id.btnPlay);
        continueButton = findViewById(R.id.btnContinue);
        settingsButton = findViewById(R.id.btnSettings);
        shopButton = findViewById(R.id.btnShop);
        achievementsButton = findViewById(R.id.btnAchievements);

        playButton.setOnClickListener(v -> {
            GameStateManager.clearState(this); // clear previous progress
            startActivity(new Intent(this, GameActivity.class));
        });

        continueButton.setOnClickListener(v -> {
            Tile[][] savedGrid = GameStateManager.loadGrid(this);
            if (savedGrid != null) {
                startActivity(new Intent(this, GameActivity.class));
            } else {
                Toast.makeText(this, "No game in progress", Toast.LENGTH_SHORT).show();
            }
        });

        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        shopButton.setOnClickListener(v ->
                startActivity(new Intent(this, ShopActivity.class)));

        achievementsButton.setOnClickListener(v ->
                startActivity(new Intent(this, AchievementsActivity.class)));
    }
}
