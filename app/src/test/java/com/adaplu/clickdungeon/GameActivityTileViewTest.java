package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.model.CharacterProfile;
import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.PlayerClass;
import com.adaplu.clickdungeon.model.TerrainType;
import com.adaplu.clickdungeon.model.Tile;
import com.adaplu.clickdungeon.model.TileType;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class GameActivityTileViewTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("player_profile", Context.MODE_PRIVATE);
        prefs.edit().putString("profile",
                new Gson().toJson(new CharacterProfile("Test", PlayerClass.KNIGHT)))
                .apply();
    }

    @Test
    public void bindTileView_hiddenTile_usesDoorCoverImage() {
        GameActivity activity = buildActivity();
        View tileView = LayoutInflater.from(activity).inflate(R.layout.item_tile, null, false);
        Tile tile = new Tile(TileType.EMPTY);

        callBindTileView(activity, tileView, tile, 0, 0);

        TextView textTile = tileView.findViewById(R.id.textTile);
        ImageView imageTile = tileView.findViewById(R.id.imageTile);
        ProgressBar hpBar = tileView.findViewById(R.id.monsterHpBar);

        assertEquals(View.GONE, textTile.getVisibility());
        assertEquals(View.VISIBLE, imageTile.getVisibility());
        assertEquals(View.GONE, hpBar.getVisibility());
        assertEquals(activity.getString(R.string.tile_desc_hidden), tileView.getContentDescription());
        assertEquals(ImageView.ScaleType.CENTER_CROP, imageTile.getScaleType());

        Drawable expected = activity.getDrawable(R.drawable.dungeon_door);
        assertNotNull(expected);
        assertNotNull(imageTile.getDrawable());
        assertEquals(expected.getConstantState(), imageTile.getDrawable().getConstantState());
    }

    @Test
    public void bindTileView_enemyTile_showsSpriteAndHpBar() {
        GameActivity activity = buildActivity();
        View tileView = LayoutInflater.from(activity).inflate(R.layout.item_tile, null, false);
        Tile tile = new Tile(TileType.ENEMY, new Monster("Slime", 6, 2, 0, "dY"));
        tile.reveal();

        callBindTileView(activity, tileView, tile, 0, 0);

        TextView textTile = tileView.findViewById(R.id.textTile);
        ImageView imageTile = tileView.findViewById(R.id.imageTile);
        ProgressBar hpBar = tileView.findViewById(R.id.monsterHpBar);

        assertEquals(View.GONE, textTile.getVisibility());
        assertEquals(View.VISIBLE, imageTile.getVisibility());
        assertEquals(View.VISIBLE, hpBar.getVisibility());
        assertEquals(ImageView.ScaleType.FIT_CENTER, imageTile.getScaleType());
        assertTrue(tileView.getContentDescription().toString().contains("HP"));
    }

    @Test
    public void bindTileView_playerTile_prefersAnimatedSprite() {
        GameActivity activity = buildActivity();
        View tileView = LayoutInflater.from(activity).inflate(R.layout.item_tile, null, false);
        Tile tile = new Tile(TileType.EMPTY);
        tile.reveal();
        tile.setHasPlayer(true);

        callBindTileView(activity, tileView, tile, 2, 2);

        TextView textTile = tileView.findViewById(R.id.textTile);
        ImageView imageTile = tileView.findViewById(R.id.imageTile);

        assertEquals(View.GONE, textTile.getVisibility());
        assertEquals(View.VISIBLE, imageTile.getVisibility());
    }

    @Test
    public void bindTileView_revealedTile_usesTerrainBackground() {
        GameActivity activity = buildActivity();
        ReflectionHelpers.setField(activity, "currentTerrain", TerrainType.CAVERN);

        View tileView = LayoutInflater.from(activity).inflate(R.layout.item_tile, null, false);
        Tile tile = new Tile(TileType.GOLD);
        tile.reveal();

        callBindTileView(activity, tileView, tile, 1, 1);

        Drawable expected = activity.getDrawable(R.drawable.cavern);
        assertNotNull(expected);
        assertNotNull(tileView.getBackground());
        assertEquals(expected.getConstantState(), tileView.getBackground().getConstantState());
    }

    @Test
    public void bindTileView_revealedGoldTile_showsBoardImage() {
        GameActivity activity = buildActivity();
        View tileView = LayoutInflater.from(activity).inflate(R.layout.item_tile, null, false);
        Tile tile = new Tile(TileType.GOLD);
        tile.reveal();

        callBindTileView(activity, tileView, tile, 1, 1);

        TextView textTile = tileView.findViewById(R.id.textTile);
        ImageView imageTile = tileView.findViewById(R.id.imageTile);

        assertEquals(View.GONE, textTile.getVisibility());
        assertEquals(View.VISIBLE, imageTile.getVisibility());
        assertEquals(ImageView.ScaleType.FIT_CENTER, imageTile.getScaleType());

        Drawable expected = activity.getDrawable(R.drawable.ic_tile_gold_128x128);
        assertNotNull(expected);
        assertNotNull(imageTile.getDrawable());
        assertEquals(expected.getConstantState(), imageTile.getDrawable().getConstantState());
    }

    @Test
    public void bindTileView_colorBlindTrapTile_usesColorBlindIcon() {
        GameActivity activity = buildActivity();
        ReflectionHelpers.setField(activity, "colorBlindModeEnabled", true);
        View tileView = LayoutInflater.from(activity).inflate(R.layout.item_tile, null, false);
        Tile tile = new Tile(TileType.TRAP_FIRE);
        tile.reveal();

        callBindTileView(activity, tileView, tile, 1, 1);

        TextView textTile = tileView.findViewById(R.id.textTile);
        ImageView imageTile = tileView.findViewById(R.id.imageTile);

        assertEquals(View.GONE, textTile.getVisibility());
        assertEquals(View.VISIBLE, imageTile.getVisibility());

        Drawable expected = activity.getDrawable(R.drawable.ic_trap_fire_cb_128x128);
        assertNotNull(expected);
        assertNotNull(imageTile.getDrawable());
        assertEquals(expected.getConstantState(), imageTile.getDrawable().getConstantState());
    }

    @Test
    public void getMonsterSpriteResource_mapsKnownTypesToSpriteSheets() {
        GameActivity activity = buildActivity();

        Monster goblin = new Monster("goblin", 4, 1, 0, "");
        Monster demon = new Monster("demon", 10, 4, 2, "");
        Monster ancientWyrm = new Monster("ancient wyrm", 15, 6, 3, "");

        int goblinRes = ReflectionHelpers.callInstanceMethod(activity, "getMonsterSpriteResource",
                ReflectionHelpers.ClassParameter.from(Monster.class, goblin));
        int demonRes = ReflectionHelpers.callInstanceMethod(activity, "getMonsterSpriteResource",
                ReflectionHelpers.ClassParameter.from(Monster.class, demon));
        int wyrmRes = ReflectionHelpers.callInstanceMethod(activity, "getMonsterSpriteResource",
                ReflectionHelpers.ClassParameter.from(Monster.class, ancientWyrm));

        assertEquals(R.drawable.goblin_sprite_sheet, goblinRes);
        assertEquals(R.drawable.demon_sprite_sheet, demonRes);
        assertEquals(R.drawable.ancient_wyrm_sprite_sheet, wyrmRes);
    }

    @Test
    public void getMonsterSpriteResource_unknownOrNullFallsBackToSlimeSheet() {
        GameActivity activity = buildActivity();

        Monster unknown = new Monster("not_a_real_monster", 3, 1, 0, "");
        Monster nullType = new Monster(null, 3, 1, 0, "");

        int unknownRes = ReflectionHelpers.callInstanceMethod(activity, "getMonsterSpriteResource",
                ReflectionHelpers.ClassParameter.from(Monster.class, unknown));
        int nullTypeRes = ReflectionHelpers.callInstanceMethod(activity, "getMonsterSpriteResource",
                ReflectionHelpers.ClassParameter.from(Monster.class, nullType));

        assertEquals(R.drawable.slime_sprite_sheet, unknownRes);
        assertEquals(R.drawable.slime_sprite_sheet, nullTypeRes);
    }

    @Test
    public void getPlayerSpriteSheetResource_mapsClassAndFallback() {
        GameActivity activity = buildActivity();

        ReflectionHelpers.setField(activity, "profile", new CharacterProfile("K", PlayerClass.KNIGHT));
        int knightRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerSpriteSheetResource");

        ReflectionHelpers.setField(activity, "profile", new CharacterProfile("T", PlayerClass.THIEF));
        int thiefRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerSpriteSheetResource");

        ReflectionHelpers.setField(activity, "profile", new CharacterProfile("W", PlayerClass.WIZARD));
        int wizardRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerSpriteSheetResource");

        ReflectionHelpers.setField(activity, "profile", new CharacterProfile("R", PlayerClass.RANGER));
        int rangerRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerSpriteSheetResource");

        ReflectionHelpers.setField(activity, "profile", null);
        int fallbackRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerSpriteSheetResource");

        assertEquals(R.drawable.knight_sprite_sheet, knightRes);
        assertEquals(R.drawable.thief_sprite_sheet, thiefRes);
        assertEquals(R.drawable.wizard_sprite_sheet, wizardRes);
        // Ranger has no sprite sheet yet; must fall back to knight (not crash).
        assertEquals(R.drawable.knight_sprite_sheet, rangerRes);
        assertEquals(R.drawable.knight_sprite_sheet, fallbackRes);
    }

    @Test
    public void getPlayerIconResource_mapsClassAndFallback() {
        GameActivity activity = buildActivity();

        ReflectionHelpers.setField(activity, "profile", new CharacterProfile("K", PlayerClass.KNIGHT));
        int knightRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerIconResource");

        ReflectionHelpers.setField(activity, "profile", new CharacterProfile("T", PlayerClass.THIEF));
        int thiefRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerIconResource");

        ReflectionHelpers.setField(activity, "profile", new CharacterProfile("W", PlayerClass.WIZARD));
        int wizardRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerIconResource");

        ReflectionHelpers.setField(activity, "profile", null);
        int fallbackRes = ReflectionHelpers.callInstanceMethod(activity, "getPlayerIconResource");

        assertEquals(R.drawable.icon_knight, knightRes);
        assertEquals(R.drawable.icon_thief, thiefRes);
        assertEquals(R.drawable.icon_wizard, wizardRes);
        assertEquals(R.drawable.icon_knight, fallbackRes);
    }

    private GameActivity buildActivity() {
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class);
        return controller.setup().get();
    }

    private void callBindTileView(GameActivity activity,
                                  View tileView,
                                  Tile tile,
                                  int row,
                                  int col) {
        ReflectionHelpers.callInstanceMethod(activity, "bindTileView",
                ReflectionHelpers.ClassParameter.from(View.class, tileView),
                ReflectionHelpers.ClassParameter.from(Tile.class, tile),
                ReflectionHelpers.ClassParameter.from(int.class, row),
                ReflectionHelpers.ClassParameter.from(int.class, col));
    }
}
