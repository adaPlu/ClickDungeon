package com.adaplu.clickdungeon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;
import com.adaplu.clickdungeon.util.SaveManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import com.adaplu.clickdungeon.util.SecurePreferences;

@RunWith(RobolectricTestRunner.class)
public class MainMenuActivityTest {

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
    public void continueDisabledWhenNoSaves() {
        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        View continueButton = activity.findViewById(R.id.btnContinue);
        assertFalse(continueButton.isEnabled());
    }

    @Test
    public void continueEnabledWhenSaveExists() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Hero", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[][] { { new Tile(TileType.EMPTY) } };
        saveManager.saveGame(0, profile, 1, 0, 0, grid, null);

        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        View continueButton = activity.findViewById(R.id.btnContinue);
        assertTrue(continueButton.isEnabled());
    }
}
