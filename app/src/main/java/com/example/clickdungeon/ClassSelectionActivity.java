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

    public static final String EXTRA_SAVE_SLOT_INDEX = "com.example.clickdungeon.extra.SAVE_SLOT_INDEX";

    private EditText editName;
    private RadioGroup classGroup;
    private Button btnStartGame;
    private int targetSlotIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        editName = findViewById(R.id.editCharacterName);
        classGroup = findViewById(R.id.radioClassGroup);
        btnStartGame = findViewById(R.id.btnStartGame);
        targetSlotIndex = getIntent().getIntExtra(EXTRA_SAVE_SLOT_INDEX, -1);

        btnStartGame.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            int selectedId = classGroup.getCheckedRadioButtonId();

            if (name.isEmpty() || selectedId == -1) {
                Toast.makeText(this, "Enter name and select a class.", Toast.LENGTH_SHORT).show();
                return;
            }

            PlayerClass selectedClass;

            if (selectedId == R.id.radioKnight) {
                selectedClass = PlayerClass.KNIGHT;
            } else if (selectedId == R.id.radioThief) {
                selectedClass = PlayerClass.THIEF;
            } else {
                selectedClass = PlayerClass.WIZARD;
            }

            CharacterProfile profile = new CharacterProfile(name, selectedClass);
            String profileJson = new Gson().toJson(profile);

            if (targetSlotIndex >= 0) {
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, targetSlotIndex);
                intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
                intent.putExtra(GameActivity.EXTRA_PROFILE_JSON, profileJson);
                startActivity(intent);
            } else {
                saveProfile(profile);
                Intent intent = new Intent(this, GameActivity.class);
                intent.putExtra(GameActivity.EXTRA_PROFILE_JSON, profileJson);
                startActivity(intent);
            }
            finish();
        });
    }

    private void saveProfile(CharacterProfile profile) {
        SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        prefs.edit().putString("profile", new Gson().toJson(profile)).apply();
    }
}
