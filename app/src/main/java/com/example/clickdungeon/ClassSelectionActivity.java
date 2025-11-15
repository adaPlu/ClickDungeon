package com.example.clickdungeon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.google.gson.Gson;

public class ClassSelectionActivity extends AppCompatActivity {

    private Button btnKnight, btnThief, btnWizard, btnStart;
    private EditText inputName;
    private ImageView classPreview;
    private PlayerClass selectedClass = PlayerClass.KNIGHT;
    private int saveSlot = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        btnKnight = findViewById(R.id.btnKnight);
        btnThief = findViewById(R.id.btnThief);
        btnWizard = findViewById(R.id.btnWizard);
        btnStart = findViewById(R.id.btnStart);
        inputName = findViewById(R.id.editName);
        classPreview = findViewById(R.id.imageClassPreview);

        // Get save slot from intent
        saveSlot = getIntent().getIntExtra("save_slot", 1);

        btnKnight.setOnClickListener(v -> {
            selectedClass = PlayerClass.KNIGHT;
            classPreview.setImageResource(R.drawable.icon_knight);
        });

        btnThief.setOnClickListener(v -> {
            selectedClass = PlayerClass.THIEF;
            classPreview.setImageResource(R.drawable.icon_thief);
        });

        btnWizard.setOnClickListener(v -> {
            selectedClass = PlayerClass.WIZARD;
            classPreview.setImageResource(R.drawable.icon_wizard);
        });

        btnStart.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            CharacterProfile profile = new CharacterProfile(name, selectedClass);
            saveProfile(profile, saveSlot);

            Intent intent = new Intent(ClassSelectionActivity.this, GameActivity.class);
            intent.putExtra("save_slot", saveSlot);
            startActivity(intent);
        });
    }

    private void saveProfile(CharacterProfile profile, int slot) {
        SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("profile", new Gson().toJson(profile));
        editor.putInt("save_slot", slot);
        editor.apply();

        // Store summary for slot display (used in SlotSelectionActivity)
        SharedPreferences slotPrefs = getSharedPreferences("player_prefs", Context.MODE_PRIVATE);
        slotPrefs.edit()
                .putString("class_slot_" + slot, profile.getPlayerClass().name())
                .putInt("xp_slot_" + slot, 0)
                .putLong("last_played_slot_" + slot, System.currentTimeMillis())
                .apply();
    }
}
