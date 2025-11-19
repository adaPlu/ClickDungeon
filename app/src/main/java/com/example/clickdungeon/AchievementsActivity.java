package com.example.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
//import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clickdungeon.adapter.AchievementAdapter;
import com.example.clickdungeon.model.Achievement;
import com.example.clickdungeon.util.AchievementManager;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.achievements);
        setContentView(R.layout.activity_achievements);

        RecyclerView recyclerView = findViewById(R.id.recyclerAchievements);
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AchievementAdapter(loadAchievements()));

        btnBackToMenu.setOnClickListener(view -> {
            Intent intent = new Intent(AchievementsActivity.this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private List<Achievement> loadAchievements() {
        return AchievementManager.loadAchievements(this);
    }
}
