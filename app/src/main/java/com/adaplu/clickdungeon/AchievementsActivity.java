package com.adaplu.clickdungeon;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adaplu.clickdungeon.adapter.AchievementAdapter;
import com.adaplu.clickdungeon.model.Achievement;
import com.adaplu.clickdungeon.util.AchievementManager;

import java.util.List;

/**
 * AchievementsActivity displays a list of all game achievements.
 * It indicates which tasks the player has completed and which are still locked.
 */
public class AchievementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.achievements);
        setContentView(R.layout.activity_achievements);

        // Bind UI components.
        RecyclerView recyclerView = findViewById(R.id.recyclerAchievements);
        Button btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // Configure the vertical list for achievements.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Fetch and bind the achievement data using the AchievementAdapter.
        recyclerView.setAdapter(new AchievementAdapter(loadAchievements()));

        // Return the player to the main navigation hub.
        btnBackToMenu.setOnClickListener(view -> {
            Intent intent = new Intent(AchievementsActivity.this, MainMenuActivity.class);
            // Ensure the main menu becomes the top of the stack.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Loads the current set of achievements from the manager.
     */
    private List<Achievement> loadAchievements() {
        return AchievementManager.loadAchievements(this);
    }
}
