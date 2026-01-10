package com.example.clickdungeon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.google.gson.Gson;

public class ClassSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_SAVE_SLOT_INDEX = "com.example.clickdungeon.extra.SAVE_SLOT_INDEX";

    private EditText editName;
    private Button btnKnight;
    private Button btnThief;
    private Button btnWizard;
    private ImageView classPreview;
    private int targetSlotIndex = -1;
    private PlayerClass selectedClass = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_selection);

        editName = findViewById(R.id.editName);
        btnKnight = findViewById(R.id.btnKnight);
        btnThief = findViewById(R.id.btnThief);
        btnWizard = findViewById(R.id.btnWizard);
        Button btnStartGame = findViewById(R.id.btnStart);
        Button btnShop = findViewById(R.id.btnShop);
        classPreview = findViewById(R.id.imageClassPreview);
        TextView slotDisplay = findViewById(R.id.textSlotDisplay);
        targetSlotIndex = getIntent().getIntExtra(EXTRA_SAVE_SLOT_INDEX, -1);

        if (slotDisplay != null && targetSlotIndex >= 0) {
            slotDisplay.setText(getString(R.string.slot_display_label, targetSlotIndex + 1));
        }

        btnKnight.setOnClickListener(v -> selectClass(PlayerClass.KNIGHT));
        btnThief.setOnClickListener(v -> selectClass(PlayerClass.THIEF));
        btnWizard.setOnClickListener(v -> selectClass(PlayerClass.WIZARD));

        btnStartGame.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();

            if (name.isEmpty() || selectedClass == null) {
                Toast.makeText(this, R.string.choose_name_and_class, Toast.LENGTH_SHORT).show();
                return;
            }

            CharacterProfile profile = new CharacterProfile(name, selectedClass);
            String profileJson = new Gson().toJson(profile);

            int slotIndex = Math.max(targetSlotIndex, 0);
            saveProfile(profile, slotIndex);
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, slotIndex);
            intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
            intent.putExtra(GameActivity.EXTRA_PROFILE_JSON, profileJson);
            startActivity(intent);
            finish();
        });

        btnShop.setOnClickListener(v -> startActivity(new Intent(this, ShopActivity.class)));
    }

    private void saveProfile(CharacterProfile profile, int slot) {
        SharedPreferences prefs = getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("profile", new Gson().toJson(profile));
        editor.putInt("save_slot", slot);
        editor.apply();
    }

    private void selectClass(PlayerClass playerClass) {
        selectedClass = playerClass;
        updateClassPreview(playerClass);
        highlightSelection(playerClass);
    }

    private void updateClassPreview(PlayerClass playerClass) {
        if (classPreview == null) {
            return;
        }
        int resId;
        switch (playerClass) {
            case THIEF:
                resId = R.drawable.icon_thief;
                break;
            case WIZARD:
                resId = R.drawable.icon_wizard;
                break;
            case KNIGHT:
            default:
                resId = R.drawable.icon_knight;
                break;
        }
        classPreview.setImageResource(resId);
    }

    private void highlightSelection(PlayerClass playerClass) {
        btnKnight.setEnabled(playerClass != PlayerClass.KNIGHT);
        btnThief.setEnabled(playerClass != PlayerClass.THIEF);
        btnWizard.setEnabled(playerClass != PlayerClass.WIZARD);
    }
}
