package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.clickdungeon.model.CharacterProfile;
import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.PlayerClass;
import com.example.clickdungeon.model.Tile;
import com.example.clickdungeon.model.TileType;
import com.example.clickdungeon.util.InventoryManager;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class GameActivityAbilityTest {

    @Before
    public void clearInventory() {
        Context context = ApplicationProvider.getApplicationContext();
        InventoryManager.clearInventory(context);
    }

    @Test
    public void abilityTargetingRejectsOutOfRangeTiles() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD, 3);
        Tile[][] grid = buildEmptyGrid();
        setField(activity, "dungeonGrid", grid);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        setField(activity, "currentFloor", 1);
        setField(activity, "nextAbilityAvailableFloor", 1);
        setField(activity, "pendingAbilityTargetMode",
                enumValue(GameActivity.class, "AbilityTargetMode", "WIZARD_FIREBALL"));
        invoke(activity, "renderGrid");

        invoke(activity, "handleAbilityTargetSelection", 0, 0);

        Object pending = getField(activity, "pendingAbilityTargetMode");
        assertEquals(enumValue(GameActivity.class, "AbilityTargetMode", "WIZARD_FIREBALL"), pending);
        assertEquals(1, (int) getField(activity, "nextAbilityAvailableFloor"));
        assertFalse(grid[0][0].isRevealed());
    }

    @Test
    public void abilityUseTriggersCooldown() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD, 2);
        Tile[][] grid = buildEmptyGrid();
        setField(activity, "dungeonGrid", grid);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        setField(activity, "currentFloor", 1);
        setField(activity, "nextAbilityAvailableFloor", 1);
        setField(activity, "pendingAbilityTargetMode",
                enumValue(GameActivity.class, "AbilityTargetMode", "WIZARD_FIREBALL"));
        invoke(activity, "renderGrid");

        invoke(activity, "handleAbilityTargetSelection", 2, 3);

        Object pending = getField(activity, "pendingAbilityTargetMode");
        assertEquals(enumValue(GameActivity.class, "AbilityTargetMode", "NONE"), pending);
        assertEquals(3, (int) getField(activity, "nextAbilityAvailableFloor"));
    }

    @Test
    public void frostNovaRevealsTrapsAndDamagesMonsters() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD, 5);
        Tile[][] grid = buildEmptyGrid();
        Tile trapTile = new Tile(TileType.TRAP_POISON);
        Tile monsterTile = new Tile(TileType.ENEMY, new Monster("Goblin", 6, 2, 0, "G"));
        grid[1][1] = trapTile;
        grid[2][2] = monsterTile;
        setField(activity, "dungeonGrid", grid);
        setField(activity, "safeTilesToReveal", 25);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeWizardFrostNova", 2, 2);

        assertTrue(resolved);
        assertTrue(trapTile.isRevealed());
        assertFalse(monsterTile.hasMonster());
        assertEquals(TileType.EMPTY, monsterTile.getType());
    }

    @Test
    public void chainLightningHitsCenterAndAdjacent() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD, 1);
        Tile[][] grid = buildEmptyGrid();
        Monster center = new Monster("Goblin", 10, 2, 0, "G");
        Monster adjacent = new Monster("Goblin", 10, 2, 0, "G");
        grid[2][2] = new Tile(TileType.ENEMY, center);
        grid[2][3] = new Tile(TileType.ENEMY, adjacent);
        setField(activity, "dungeonGrid", grid);
        setField(activity, "safeTilesToReveal", 25);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeWizardChainLightning", 2, 2);

        assertTrue(resolved);
        assertEquals(6, center.getCurrentHP());
        assertEquals(7, adjacent.getCurrentHP());
    }

    @Test
    public void meteorClearsTrapsInBlastRadius() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD, 2);
        Tile[][] grid = buildEmptyGrid();
        Tile trapTile = new Tile(TileType.TRAP_FIRE);
        grid[2][2] = trapTile;
        setField(activity, "dungeonGrid", grid);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeWizardMeteor", 2, 2);

        assertTrue(resolved);
        assertEquals(TileType.EMPTY, trapTile.getType());
    }

    @Test
    public void shadowstepMovesToRevealedSafeTile() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.THIEF, 5);
        Tile[][] grid = buildEmptyGrid();
        Tile target = new Tile(TileType.EMPTY);
        target.reveal();
        grid[1][1] = target;
        setField(activity, "dungeonGrid", grid);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeThiefShadowstep", 1, 1);

        assertTrue(resolved);
        assertEquals(1, (int) getField(activity, "playerRow"));
        assertEquals(1, (int) getField(activity, "playerCol"));
    }

    @Test
    public void disarmExpertConsumesKitAndClearsTraps() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.THIEF, 5);
        Tile[][] grid = buildEmptyGrid();
        Tile trapTile = new Tile(TileType.TRAP_ACID);
        grid[2][2] = trapTile;
        setField(activity, "dungeonGrid", grid);
        InventoryManager.clearInventory(activity);
        InventoryManager.adjustItemQuantity(activity, "Trap Disarm Kit", 1);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeThiefDisarmExpert", 2, 2);

        assertTrue(resolved);
        assertEquals(0, InventoryManager.getItemQuantity(activity, "Trap Disarm Kit"));
        assertEquals(TileType.EMPTY, trapTile.getType());
    }

    @Test
    public void disarmExpertRevealsTrapsWithoutKit() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.THIEF, 5);
        Tile[][] grid = buildEmptyGrid();
        Tile trapTile = new Tile(TileType.TRAP_FREEZE);
        grid[2][2] = trapTile;
        setField(activity, "dungeonGrid", grid);
        InventoryManager.clearInventory(activity);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeThiefDisarmExpert", 2, 2);

        assertTrue(resolved);
        assertTrue(trapTile.isRevealed());
        assertEquals(TileType.TRAP_FREEZE, trapTile.getType());
    }

    @Test
    public void veilOfSmokeRevealsTilesAndSetsCharge() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.THIEF, 20);
        Tile[][] grid = buildEmptyGrid();
        setField(activity, "dungeonGrid", grid);
        setField(activity, "playerRow", 2);
        setField(activity, "playerCol", 2);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeThiefVeilOfSmoke");

        assertTrue(resolved);
        assertEquals(1, (int) getField(activity, "smokeVeilCharges"));
        assertTrue(grid[2][2].isRevealed());
        assertTrue(grid[1][1].isRevealed());
        assertTrue(grid[3][3].isRevealed());
    }

    @Test
    public void valiantStrikeKillsLowHealthTarget() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.KNIGHT, 10);
        Tile[][] grid = buildEmptyGrid();
        Tile monsterTile = new Tile(TileType.ENEMY, new Monster("Slime", 3, 1, 0, "S"));
        grid[2][2] = monsterTile;
        setField(activity, "dungeonGrid", grid);
        setField(activity, "safeTilesToReveal", 25);
        invoke(activity, "renderGrid");

        boolean resolved = (boolean) invoke(activity, "executeKnightValiantStrike", 2, 2);

        assertTrue(resolved);
        assertFalse(monsterTile.hasMonster());
        assertEquals(TileType.EMPTY, monsterTile.getType());
    }

    @Test
    public void fortifyClearsStatusAndHeals() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.KNIGHT, 5);
        CharacterProfile profile = (CharacterProfile) getField(activity, "profile");
        profile.setCurrentHP(2);
        setField(activity, "frozenTurnsLeft", 2);
        setField(activity, "poisonTurnsLeft", 2);

        boolean resolved = (boolean) invoke(activity, "executeKnightFortify");

        assertTrue(resolved);
        assertEquals(0, (int) getField(activity, "frozenTurnsLeft"));
        assertEquals(0, (int) getField(activity, "poisonTurnsLeft"));
        assertTrue(profile.getCurrentHP() > 2);
    }

    @Test
    public void guardiansOathActivatesShield() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.KNIGHT, 8);
        setField(activity, "playerRow", 3);
        setField(activity, "playerCol", 1);

        boolean resolved = (boolean) invoke(activity, "executeKnightGuardiansOath");

        assertTrue(resolved);
        assertTrue((boolean) getField(activity, "knightShieldActive"));
        assertEquals(3, (int) getField(activity, "knightShieldRow"));
        assertEquals(1, (int) getField(activity, "knightShieldCol"));
        assertTrue((int) getField(activity, "knightShieldStrength") > 0);
    }

    @Test
    public void arcaneShieldRestoresMana() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD, 6);
        CharacterProfile profile = (CharacterProfile) getField(activity, "profile");
        profile.setCurrentMP(1);
        int before = profile.getCurrentMP();

        boolean resolved = (boolean) invoke(activity, "executeWizardArcaneShield");

        assertTrue(resolved);
        assertTrue(profile.getCurrentMP() > before);
    }

    @Test
    public void consumeWizardMpUsesAbilityCosts() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.WIZARD, 6);
        CharacterProfile profile = (CharacterProfile) getField(activity, "profile");
        profile.setCurrentMP(6);

        boolean meteorAllowed = (boolean) invoke(activity, "consumeWizardMp", PlayerClass.ABILITY_WIZARD_METEOR);
        assertFalse(meteorAllowed);
        assertEquals(6, profile.getCurrentMP());

        boolean chainAllowed = (boolean) invoke(activity, "consumeWizardMp", PlayerClass.ABILITY_WIZARD_CHAIN_LIGHTNING);
        assertTrue(chainAllowed);
        assertEquals(1, profile.getCurrentMP());
    }

    @Test
    public void smokeVeilConsumesChargeAndClearsTrap() throws Exception {
        GameActivity activity = launchWithProfile(PlayerClass.THIEF, 10);
        Tile[][] grid = buildEmptyGrid();
        Tile trapTile = new Tile(TileType.TRAP_POISON);
        grid[2][2] = trapTile;
        setField(activity, "dungeonGrid", grid);
        setField(activity, "smokeVeilCharges", 1);
        TextView tileText = new TextView(activity);

        invoke(activity, "handleTrap", tileText, trapTile);

        assertEquals(0, (int) getField(activity, "smokeVeilCharges"));
        assertEquals(TileType.EMPTY, trapTile.getType());
    }

    private GameActivity launchWithProfile(PlayerClass playerClass, int level) {
        Context context = ApplicationProvider.getApplicationContext();
        CharacterProfile profile = new CharacterProfile("Tester", playerClass);
        profile.setLevel(level);
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

    private Object getField(GameActivity activity, String name) {
        return ReflectionHelpers.getField(activity, name);
    }

    private Object invoke(GameActivity activity, String methodName, Object... args) throws Exception {
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
        return method.invoke(activity, args);
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
