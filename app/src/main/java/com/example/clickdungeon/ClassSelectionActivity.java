// ClassSelectionActivity.java (legacy name preserved for backward compatibility)
package com.example.clickdungeon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.google.gson.Gson;

public class ClassSelectionActivity extends AppCompatActivity {

    private EditText editName;
    private RadioGroup classGroup;
    private Button btnStartGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        editName = findViewById(R.id.editCharacterName);
        classGroup = findViewById(R.id.radioClassGroup);
        btnStartGame = findViewById(R.id.btnStartGame);

        btnStartGame.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            int selectedId = classGroup.getCheckedRadioButtonId();

            if (name.isEmpty() || selectedId == -1) {
                Toast.makeText(this, "Enter name and select a class.", Toast.LENGTH_SHORT).show();
                return;
            }

            PlayerClass selectedClass;
            int maxHP;

            if (selectedId == R.id.radioKnight) {
                selectedClass = PlayerClass.KNIGHT;
                maxHP = 10;
            } else if (selectedId == R.id.radioThief) {
                selectedClass = PlayerClass.THIEF;
                maxHP = 6;
            } else {
                selectedClass = PlayerClass.WIZARD;
                maxHP = 4;
            }

            CharacterProfile profile = new CharacterProfile(name, selectedClass, maxHP);
            saveProfile(profile);

            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void saveProfile(CharacterProfile profile) {
        SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        prefs.edit().putString("profile", new Gson().toJson(profile)).apply();
    }
} 