package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class GameActivityAbilityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("SaveSlot0", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void wizardFireballKillsMonsterAndStartsCooldown() throws Exception {
        GameActivity activity = launchActivity(PlayerClass.WIZARD);
        Tile[][] grid = buildEmptyGrid();
        Monster monster = new Monster("Goblin", 4, 2, 0, "G");
        grid[2][3] = new Tile(TileType.ENEMY, monster);
        setField(activity, "dungeonGrid", grid);
        invoke(activity, "renderGrid");

        setField(activity, "currentFloor", 3);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        setField(activity, "pendingAbilityTargetMode", enumValue(GameActivity.class, "AbilityTargetMode", "WIZARD_FIREBALL"));
        setField(activity, "nextAbilityAvailableFloor", 3);

        invoke(activity, "handleAbilityTargetSelection", 2, 3);

        Tile resultTile = grid[2][3];
        assertEquals(TileType.EMPTY, resultTile.getType());
        assertFalse(resultTile.hasMonster());
        int cooldownFloor = (int) getField(activity, "nextAbilityAvailableFloor");
        assertEquals(5, cooldownFloor);
    }

    @Test
    public void thiefScanRevealsTrapsAndStartsCooldown() throws Exception {
        GameActivity activity = launchActivity(PlayerClass.THIEF);
        Tile[][] grid = buildEmptyGrid();
        Tile trapTile = new Tile(TileType.TRAP_FIRE);
        grid[1][1] = trapTile;
        setField(activity, "dungeonGrid", grid);
        invoke(activity, "renderGrid");

        setField(activity, "currentFloor", 2);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        setField(activity, "pendingAbilityTargetMode", enumValue(GameActivity.class, "AbilityTargetMode", "THIEF_SCAN"));
        setField(activity, "nextAbilityAvailableFloor", 2);

        invoke(activity, "handleAbilityTargetSelection", 1, 1);

        assertTrue("Trap tile should now be revealed", trapTile.isRevealed());
        int cooldownFloor = (int) getField(activity, "nextAbilityAvailableFloor");
        assertEquals(4, cooldownFloor);
    }

    @Test
    public void knightShieldAbsorbsDamageUntilBroken() throws Exception {
        GameActivity activity = launchActivity(PlayerClass.KNIGHT);
        Tile[][] grid = buildEmptyGrid();
        grid[2][2].reveal();
        setField(activity, "dungeonGrid", grid);
        invoke(activity, "renderGrid");

        setField(activity, "currentFloor", 1);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        setField(activity, "pendingAbilityTargetMode", enumValue(GameActivity.class, "AbilityTargetMode", "KNIGHT_SHIELD"));
        setField(activity, "nextAbilityAvailableFloor", 1);

        invoke(activity, "handleAbilityTargetSelection", 2, 2);

        int initialShield = (int) getField(activity, "knightShieldStrength");
        assertTrue(initialShield > 0);

        // Damage less than shield strength should be fully absorbed
        invoke(activity, "takeDamage", 4);
        int remainingShield = (int) getField(activity, "knightShieldStrength");
        assertEquals(initialShield - 4, remainingShield);
        CharacterProfile profile = (CharacterProfile) getField(activity, "profile");
        assertEquals(profile.getMaxHP(), profile.getCurrentHP());

        // Moving should clear the shield
        invoke(activity, "updatePlayerPosition", 2, 3);
        boolean shieldActive = (boolean) getField(activity, "knightShieldActive");
        assertFalse(shieldActive);
    }

    private GameActivity launchActivity(PlayerClass playerClass) {
        CharacterProfile profile = new CharacterProfile("Tester", playerClass);
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_PROFILE_JSON, new Gson().toJson(profile));
        intent.putExtra(GameActivity.EXTRA_IS_NEW_GAME, true);
        intent.putExtra(GameActivity.EXTRA_SLOT_INDEX, 0);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent);
        return controller.setup().get();
    }

    private Tile[][] buildEmptyGrid() {
        Tile[][] grid = new Tile[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = new Tile(TileType.EMPTY);
            }
        }
        return grid;
    }

    private void setField(GameActivity activity, String name, Object value) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private Object getField(GameActivity activity, String name) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(activity);
    }

    private void invoke(GameActivity activity, String methodName, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                types[i] = int.class;
            } else if (args[i] instanceof Boolean) {
                types[i] = boolean.class;
            } else {
                types[i] = args[i].getClass();
            }
        }
        java.lang.reflect.Method method = GameActivity.class.getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        method.invoke(activity, args);
    }

    private Object enumValue(Class<?> clazz, String enumName, String constant) throws Exception {
        Class<?>[] inner = clazz.getDeclaredClasses();
        for (Class<?> candidate : inner) {
            if (candidate.getSimpleName().equals(enumName)) {
                return Enum.valueOf((Class<Enum>) candidate.asSubclass(Enum.class), constant);
            }
        }
        throw new IllegalArgumentException("Enum not found: " + enumName);
    }
}
