package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    public void bindTileView_hiddenTile_showsQuestionMark() {
        GameActivity activity = buildActivity();
        View tileView = LayoutInflater.from(activity).inflate(R.layout.item_tile, null, false);
        Tile tile = new Tile(TileType.EMPTY);

        callBindTileView(activity, tileView, tile, 0, 0);

        TextView textTile = tileView.findViewById(R.id.textTile);
        ImageView imageTile = tileView.findViewById(R.id.imageTile);
        ProgressBar hpBar = tileView.findViewById(R.id.monsterHpBar);

        assertEquals(View.VISIBLE, textTile.getVisibility());
        assertEquals("?", textTile.getText().toString());
        assertEquals(View.GONE, imageTile.getVisibility());
        assertEquals(View.GONE, hpBar.getVisibility());
        assertEquals(activity.getString(R.string.tile_desc_hidden), tileView.getContentDescription());
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
