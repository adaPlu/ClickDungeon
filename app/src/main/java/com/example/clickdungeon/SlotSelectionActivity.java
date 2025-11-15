package com.example.clickdungeon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.util.GameStateManager;

public class SlotSelectionActivity extends AppCompatActivity {

    private String mode = "new_game"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_selection);

        if (getIntent().hasExtra("mode")) {
            mode = getIntent().getStringExtra("mode");
        }

        SharedPreferences prefs = getSharedPreferences("player_prefs", Context.MODE_PRIVATE);

        for (int i = 1; i <= 4; i++) {
            int slot = i;
            Button slotButton = findViewById(getResources().getIdentifier("btnSlot" + i, "id", getPackageName()));
            Button deleteBtn = findViewById(getResources().getIdentifier("btnDelete" + i, "id", getPackageName()));
            TextView slotInfo = findViewById(getResources().getIdentifier("txtSlot" + i, "id", getPackageName()));
            TextView extraText = findViewById(getResources().getIdentifier("txtExtra" + i, "id", getPackageName()));
            ImageView classIcon = findViewById(getResources().getIdentifier("imgClass" + i, "id", getPackageName()));

            boolean hasSave = prefs.contains("grid_slot_" + slot);
            int floor = prefs.getInt("floor_slot_" + slot, 0);
            int gold = prefs.getInt("gold_slot_" + slot, 0);
            String className = prefs.getString("class_slot_" + slot, "KNIGHT");
            int xp = prefs.getInt("xp_slot_" + slot, 0);
            long lastPlayed = prefs.getLong("last_played_slot_" + slot, 0);

            // Show preview info
            slotInfo.setText(hasSave ? "Floor " + floor + " | Gold: " + gold : "(Empty)");

            if (hasSave) {
                classIcon.setVisibility(ImageView.VISIBLE);
                extraText.setVisibility(TextView.VISIBLE);

                classIcon.setImageResource(getClassIconResId(className));

                String lastPlayedStr = android.text.format.DateFormat.format("MMM d, h:mm a", new java.util.Date(lastPlayed)).toString();
                extraText.setText("XP: " + xp + " | Last: " + lastPlayedStr);
            } else {
                classIcon.setVisibility(ImageView.GONE);
                extraText.setVisibility(TextView.GONE);
            }

            slotButton.setEnabled(hasSave || "new_game".equals(mode));
            slotButton.setAlpha((hasSave || "new_game".equals(mode)) ? 1f : 0.5f);

            deleteBtn.setEnabled(hasSave);
            deleteBtn.setAlpha(hasSave ? 1f : 0.5f);

            slotButton.setOnClickListener(v -> {
                if ("new_game".equals(mode)) {
                    Intent intent = new Intent(this, ClassSelectionActivity.class);
                    intent.putExtra("save_slot", slot);
                    startActivity(intent);
                } else {
                    if (hasSave) {
                        Intent intent = new Intent(this, GameActivity.class);
                        intent.putExtra("save_slot", slot);
                        startActivity(intent);
                    }
                }
            });

            deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Slot " + slot)
                        .setMessage("Are you sure you want to delete this save?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            GameStateManager.clearSlot(this, slot);
                            recreate();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private int getClassIconResId(String className) {
        switch (className.toUpperCase()) {
            case "WIZARD": return R.drawable.icon_wizard;
            case "THIEF": return R.drawable.icon_thief;
            case "KNIGHT":
            default: return R.drawable.icon_knight;
        }
    }
}
