package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.util.InventoryManager;
import com.example.clickdungeon.util.SoundManager;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
public class GameActivityAudioAnimationTest {

    private final AtomicReference<String> lastPlayedKey = new AtomicReference<>();

    @Before
    public void setup() {
        SoundManager.setTestPlaybackListener(lastPlayedKey::set);
    }

    @After
    public void tearDown() {
        SoundManager.setTestPlaybackListener(null);
    }

    @Test
    public void goldRevealRequestsTreasureCue() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.KNIGHT);
        Tile[][] grid = buildEmptyGrid();
        grid[2][2] = new Tile(TileType.GOLD);
        setField(activity, "dungeonGrid", grid);
        invoke(activity, "renderGrid");

        View tileView = getTileView(activity, 2, 2);
        invoke(activity, "handleTileClick", 2, 2, tileView);

        assertEquals(SoundManager.KEY_EFFECT_TREASURE, lastPlayedKey.get());
    }

    @Test
    public void trapHitRequestsTrapCue() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.KNIGHT);
        Tile[][] grid = buildEmptyGrid();
        Tile trapTile = new Tile(TileType.TRAP_POISON);
        grid[1][1] = trapTile;
        setField(activity, "dungeonGrid", grid);
        invoke(activity, "renderGrid");

        InventoryManager.clearInventory(activity);
        TextView tileText = new TextView(activity);
        invoke(activity, "handleTrap", tileText, trapTile);

        assertEquals(SoundManager.KEY_EFFECT_TRAP, lastPlayedKey.get());
    }

    @Test
    public void wizardAbilityPlaysClassSound() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD);
        Tile[][] grid = buildEmptyGrid();
        grid[2][2] = new Tile(TileType.EMPTY);
        grid[2][3] = new Tile(TileType.EMPTY);
        setField(activity, "dungeonGrid", grid);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        setField(activity, "pendingAbilityTargetMode", enumValue(GameActivity.class, "AbilityTargetMode", "WIZARD_FIREBALL"));
        setField(activity, "nextAbilityAvailableFloor", 1);
        invoke(activity, "renderGrid");

        invoke(activity, "handleAbilityTargetSelection", 2, 3);

        assertEquals("wizard_attack", lastPlayedKey.get());
    }

    @Test
    public void gridAnimationThrottlesWhenInvisibleAndClearsOnDetach() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.KNIGHT);
        Tile[][] grid = buildEmptyGrid();
        grid[0][0] = new Tile(TileType.ENEMY, new Monster("Goblin", 4, 2, 0, "G"));
        grid[0][0].reveal();
        setField(activity, "dungeonGrid", grid);
        invoke(activity, "renderGrid");

        View tileView = getTileView(activity, 0, 0);
        assertNotNull(tileView);
        tileView.setVisibility(View.INVISIBLE);

        // First frame should record a timestamp even though it is skipped visually.
        invoke(activity, "animateGridFrame");
        Map<String, Long> frameTimes = getLongMapField(activity, "gridAnimationLastFrameMs");
        String key = "0_0";
        long first = frameTimes.get(key);

        // Second immediate frame should be throttled (timestamp unchanged).
        invoke(activity, "animateGridFrame");
        long second = frameTimes.get(key);
        assertEquals(first, second);

        // Detach should clear animation bookkeeping.
        GridLayout layout = activity.findViewById(R.id.gridDungeon);
        layout.removeView(tileView);
        Map<?, ?> animators = getMapField(activity, "gridMonsterAnimations");
        Map<String, Long> refreshedTimes = getLongMapField(activity, "gridAnimationLastFrameMs");
        assertFalse(animators.containsKey(key));
        assertFalse(refreshedTimes.containsKey(key));
    }

    private GameActivity launchWithProfile(PlayerClass playerClass) {
        Context context = ApplicationProvider.getApplicationContext();
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

    private View getTileView(GameActivity activity, int row, int col) {
        Object gridLayoutObj = getField(activity, "gridLayout");
        if (!(gridLayoutObj instanceof GridLayout)) {
            return null;
        }
        GridLayout layout = (GridLayout) gridLayoutObj;
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            Object tagRow = child.getTag(R.id.tag_row);
            Object tagCol = child.getTag(R.id.tag_col);
            if (tagRow instanceof Integer && tagCol instanceof Integer
                    && ((Integer) tagRow) == row && ((Integer) tagCol) == col) {
                return child;
            }
        }
        return null;
    }

    private void setField(GameActivity activity, String name, Object value) throws Exception {
        java.lang.reflect.Field field = GameActivity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(activity, value);
    }

    private Object getField(GameActivity activity, String name) {
        return ReflectionHelpers.getField(activity, name);
    }

    private Map<?, ?> getMapField(GameActivity activity, String name) {
        Object value = getField(activity, name);
        if (value instanceof Map) {
            return (Map<?, ?>) value;
        }
        throw new AssertionError("Expected map for " + name);
    }

    private Map<String, Long> getLongMapField(GameActivity activity, String name) {
        Map<?, ?> raw = getMapField(activity, name);
        Map<String, Long> casted = new java.util.HashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof Long) {
                casted.put((String) entry.getKey(), (Long) entry.getValue());
            }
        }
        return casted;
    }

    private void invoke(GameActivity activity, String methodName, Object... args) throws Exception {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Integer) {
                types[i] = int.class;
            } else if (args[i] instanceof Boolean) {
                types[i] = boolean.class;
            } else if (args[i] instanceof TextView) {
                types[i] = TextView.class;
            } else if (args[i] instanceof View) {
                types[i] = View.class;
            } else {
                types[i] = args[i].getClass();
            }
        }
        java.lang.reflect.Method method = GameActivity.class.getDeclaredMethod(methodName, types);
        method.setAccessible(true);
        method.invoke(activity, args);
    }

    private Enum<?> enumValue(Class<?> clazz, String enumName, String constant) {
        Class<?>[] inner = clazz.getDeclaredClasses();
        for (Class<?> candidate : inner) {
            if (candidate.getSimpleName().equals(enumName)) {
                return resolveEnum(candidate, constant);
            }
        }
        throw new IllegalArgumentException("Enum not found: " + enumName);
    }

    private Enum<?> resolveEnum(Class<?> enumClass, String constant) {
        Object[] constants = enumClass.getEnumConstants();
        if (constants != null) {
            for (Object value : constants) {
                if (value instanceof Enum) {
                    Enum<?> enumValue = (Enum<?>) value;
                    if (enumValue.name().equals(constant)) {
                        return enumValue;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Enum constant not found: " + constant);
    }
}
