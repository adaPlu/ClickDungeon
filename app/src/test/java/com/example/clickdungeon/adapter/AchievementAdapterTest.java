package com.example.clickdungeon.adapter;

import static org.junit.Assert.assertEquals;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.R;
import com.example.clickdungeon.model.Achievement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AchievementAdapterTest {

    @Test
    public void bindSetsTitleAndDescription() {
        Achievement achievement = new Achievement("First Blood", "Defeat one enemy", false);
        AchievementAdapter adapter = new AchievementAdapter(Collections.singletonList(achievement));
        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_achievement, null, false);

        AchievementAdapter.ViewHolder holder = new AchievementAdapter.ViewHolder(view);
        adapter.onBindViewHolder(holder, 0);

        TextView title = view.findViewById(R.id.textAchievementTitle);
        TextView description = view.findViewById(R.id.textAchievementDescription);
        TextView status = view.findViewById(R.id.textAchievementStatus);
        assertEquals("First Blood", title.getText().toString());
        assertEquals("Defeat one enemy", description.getText().toString());
        assertEquals("Locked", status.getText().toString());
    }
}
