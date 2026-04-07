package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowDialog;
import android.os.Looper;
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

    @Test
    public void schemaMismatchDialog_positiveMigratesAndContinues() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("MigrateMe", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        saveManager.saveGame(0, profile, 2, 10, 3, grid, new SaveManager.RunMetadata(
                2, 2, false, 0, -1, -1, 1, null));

        SecurePreferences.get(context, "SaveSlot0").edit()
                .putInt("SaveBlob_schema", 99)
                .commit();

        ContinueActivity activity = Robolectric.buildActivity(ContinueActivity.class).setup().get();
        Button review = activity.findViewById(R.id.slot1Button);
        review.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        androidx.appcompat.app.AlertDialog dialog =
                (androidx.appcompat.app.AlertDialog) ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(GameActivity.class.getName(), next.getComponent().getClassName());
        assertEquals(0, next.getIntExtra(GameActivity.EXTRA_SLOT_INDEX, -1));
    }

    @Test
    public void schemaMismatchDialog_neutralRestoreFailureMarksCorrupted() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("RestoreFail", PlayerClass.THIEF);
        Tile[][] grid = new Tile[][]{{new Tile(TileType.EMPTY)}};
        saveManager.saveGame(0, profile, 1, 1, 1, grid, new SaveManager.RunMetadata(
                -1, -1, false, 0, -1, -1, 1, null));
        SecurePreferences.get(context, "SaveSlot0").edit()
                .putInt("SaveBlob_schema", 99)
                .commit();

        ContinueActivity activity = Robolectric.buildActivity(ContinueActivity.class).setup().get();
        activity.findViewById(R.id.slot1Button).performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        androidx.appcompat.app.AlertDialog dialog =
                (androidx.appcompat.app.AlertDialog) ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        dialog.getButton(android.content.DialogInterface.BUTTON_NEUTRAL).performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        TextView info = activity.findViewById(R.id.slot1Info);
        assertEquals(activity.getString(R.string.continue_slot_corrupted, 1), info.getText().toString());
    }

    @Test
    public void overwriteConfirmation_startsNewGameWithSlotExtra() {
        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Overwrite", PlayerClass.WIZARD);
        Tile[][] grid = new Tile[][]{{new Tile(TileType.EMPTY)}};
        saveManager.saveGame(0, profile, 3, 20, 5, grid, new SaveManager.RunMetadata(
                -1, -1, false, 0, -1, -1, 1, null));

        Intent intent = new Intent(context, ContinueActivity.class);
        intent.putExtra(ContinueActivity.EXTRA_FORCE_NEW_GAME, true);
        ContinueActivity activity = Robolectric.buildActivity(ContinueActivity.class, intent).setup().get();

        Button slotButton = activity.findViewById(R.id.slot1Button);
        assertEquals(activity.getString(R.string.continue_slot_overwrite), slotButton.getText().toString());
        slotButton.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        androidx.appcompat.app.AlertDialog dialog =
                (androidx.appcompat.app.AlertDialog) ShadowDialog.getLatestDialog();
        assertNotNull(dialog);
        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(ClassSelectionActivity.class.getName(), next.getComponent().getClassName());
        assertEquals(0, next.getIntExtra(ClassSelectionActivity.EXTRA_SAVE_SLOT_INDEX, -1));
    }

    @Test
    public void slotButtons_useCorrectIntentExtrasForNewAndContinue() {
        ContinueActivity emptyActivity = Robolectric.buildActivity(ContinueActivity.class).setup().get();
        Button emptySlotButton = emptyActivity.findViewById(R.id.slot1Button);
        emptySlotButton.performClick();

        Intent newGameIntent = Shadows.shadowOf(emptyActivity).getNextStartedActivity();
        assertNotNull(newGameIntent);
        assertEquals(ClassSelectionActivity.class.getName(), newGameIntent.getComponent().getClassName());
        assertEquals(0, newGameIntent.getIntExtra(ClassSelectionActivity.EXTRA_SAVE_SLOT_INDEX, -1));

        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("ContinueMe", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[][]{{new Tile(TileType.EMPTY)}};
        saveManager.saveGame(0, profile, 2, 10, 2, grid, new SaveManager.RunMetadata(
                -1, -1, false, 0, -1, -1, 1, null));

        ContinueActivity continueActivity = Robolectric.buildActivity(ContinueActivity.class).setup().get();
        Button continueButton = continueActivity.findViewById(R.id.slot1Button);
        assertEquals(continueActivity.getString(R.string.continue_slot_continue), continueButton.getText().toString());
        continueButton.performClick();

        Intent continueIntent = Shadows.shadowOf(continueActivity).getNextStartedActivity();
        assertNotNull(continueIntent);
        assertEquals(GameActivity.class.getName(), continueIntent.getComponent().getClassName());
        assertEquals(0, continueIntent.getIntExtra(GameActivity.EXTRA_SLOT_INDEX, -1));
    }

    @Test
    public void refreshSlotsUI_updatesViewsAfterSlotDataChanges() {
        ContinueActivity activity = Robolectric.buildActivity(ContinueActivity.class).setup().get();
        TextView slot1Info = activity.findViewById(R.id.slot1Info);
        assertEquals(activity.getString(R.string.continue_slot_empty, 1), slot1Info.getText().toString());

        SaveManager saveManager = new SaveManager(context);
        CharacterProfile profile = new CharacterProfile("Refreshed", PlayerClass.KNIGHT);
        Tile[][] grid = new Tile[][]{{new Tile(TileType.EMPTY)}};
        saveManager.saveGame(0, profile, 7, 50, 4, grid, new SaveManager.RunMetadata(
                -1, -1, false, 0, -1, -1, 1, null));

        org.robolectric.util.ReflectionHelpers.callInstanceMethod(activity, "refreshSlotsUI");

        Button slot1Button = activity.findViewById(R.id.slot1Button);
        assertTrue(slot1Info.getText().toString().contains("Refreshed"));
        assertEquals(activity.getString(R.string.continue_slot_continue), slot1Button.getText().toString());
    }
}
