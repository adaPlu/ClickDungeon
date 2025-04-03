package com.example.clickdungeon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.google.gson.Gson;

public class ClassSelectionActivity extends AppCompatActivity {

    private EditText nameInput;
    private Button btnKnight, btnThief, btnWizard;
    private PlayerClass selectedClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        nameInput = findViewById(R.id.editCharacterName);
        btnKnight = findViewById(R.id.btnKnight);
        btnThief = findViewById(R.id.btnThief);
        btnWizard = findViewById(R.id.btnWizard);

        btnKnight.setOnClickListener(v -> selectClass(PlayerClass.KNIGHT));
        btnThief.setOnClickListener(v -> selectClass(PlayerClass.THIEF));
        btnWizard.setOnClickListener(v -> selectClass(PlayerClass.WIZARD));
    }

    private void selectClass(PlayerClass playerClass) {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a character name", Toast.LENGTH_SHORT).show();
            return;
        }

        CharacterProfile profile = new CharacterProfile(name, playerClass);
        saveProfile(profile);

        Intent intent = new Intent(ClassSelectionActivity.this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveProfile(CharacterProfile profile) {
        SharedPreferences prefs = getSharedPreferences("player_profile", MODE_PRIVATE);
        String json = new Gson().toJson(profile);
        prefs.edit().putString("slot_1", json).apply();
    }
}
