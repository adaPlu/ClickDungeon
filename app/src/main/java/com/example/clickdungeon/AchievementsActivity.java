package com.example.clickdungeon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.adapter.AchievementAdapter;
import com.example.clickdungeon.model.Achievement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnBackToMenu;
    private List<Achievement> achievementList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.achievements);
        setContentView(R.layout.activity_achievements);

        recyclerView = findViewById(R.id.recyclerAchievements);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load saved achievements if available
        List<Achievement> saved = loadAchievements();
        if (saved != null && !saved.isEmpty()) {
            achievementList = saved;
        } else {
            achievementList = getMockAchievements();
        }

        recyclerView.setAdapter(new AchievementAdapter(achievementList));

        btnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AchievementsActivity.this, MainMenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveAchievements(achievementList);
    }

    private void saveAchievements(List<Achievement> list) {
        SharedPreferences prefs = getSharedPreferences("player_prefs", MODE_PRIVATE);
        String json = new Gson().toJson(list);
        prefs.edit().putString("achievements", json).apply();
    }

    private List<Achievement> loadAchievements() {
        SharedPreferences prefs = getSharedPreferences("player_prefs", MODE_PRIVATE);
        String json = prefs.getString("achievements", null);
        Type type = new TypeToken<List<Achievement>>(){}.getType();
        return json != null ? new Gson().fromJson(json, type) : new ArrayList<>();
    }

    private List<Achievement> getMockAchievements() {
        List<Achievement> list = new ArrayList<>();
        list.add(new Achievement("First Blood", "Defeat your first enemy.", false));
        list.add(new Achievement("Dungeon Explorer", "Reveal 50 dungeon tiles.", false));
        list.add(new Achievement("Gold Hoarder", "Collect 1000 gold.", false));
        list.add(new Achievement("Boss Slayer", "Defeat a dungeon boss.", false));
        return list;
    }
}
