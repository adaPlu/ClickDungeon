package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.util.SaveManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import com.example.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class ContinueActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        for (int i = 0; i < 4; i++) {
            SecurePreferences.get(context, "SaveSlot" + i)
                    .edit()
                    .clear()
                    .commit();
        }
    }

    @Test
    public void emptySlotShowsNewGameButton() {
        Intent intent = new Intent(context, ContinueActivity.class);
        ContinueActivity activity = Robolectric.buildActivity(ContinueActivity.class, intent).setup().get();

        TextView info = activity.findViewById(R.id.slot1Info);
        Button button = activity.findViewById(R.id.slot1Button);

        assertEquals(activity.getString(R.string.continue_slot_empty, 1), info.getText().toString());
        assertEquals(activity.getString(R.string.continue_slot_new_game), button.getText().toString());
    }

    @Test
    public void schemaMismatchShowsReviewButton() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Test", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        saveManager.saveGame(0, profile, 1, 0, 0, grid, new SaveManager.RunMetadata(
                2, 2, false, 0, -1, -1, 1, null));

        SecurePreferences.get(context, "SaveSlot0")
                .edit()
                .putInt("SaveBlob_schema", 99)
                .commit();

        Intent intent = new Intent(context, ContinueActivity.class);
        ContinueActivity activity = Robolectric.buildActivity(ContinueActivity.class, intent).setup().get();

        TextView info = activity.findViewById(R.id.slot1Info);
        Button button = activity.findViewById(R.id.slot1Button);

        assertEquals(activity.getString(R.string.continue_slot_schema_mismatch, 1), info.getText().toString());
        assertEquals(activity.getString(R.string.continue_slot_review), button.getText().toString());
    }
}
