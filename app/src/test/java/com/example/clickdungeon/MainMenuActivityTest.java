package com.example.clickdungeon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.Button;

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

@RunWith(RobolectricTestRunner.class)
public class MainMenuActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        for (int i = 0; i < 4; i++) {
            context.getSharedPreferences("SaveSlot" + i, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit();
        }
    }

    @Test
    public void continueDisabledWhenNoSaves() {
        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        Button continueButton = activity.findViewById(R.id.btnContinue);
        assertFalse(continueButton.isEnabled());
    }

    @Test
    public void continueEnabledWhenSaveExists() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Hero", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[][] { { new Tile(TileType.EMPTY) } };
        saveManager.saveGame(0, profile, 1, 0, 0, grid, null);

        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        Button continueButton = activity.findViewById(R.id.btnContinue);
        assertTrue(continueButton.isEnabled());
    }
}
