package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LayoutAccessibilityTest {

    @Test
    public void settingsControlsDeclareMinimumTouchTargets() {
        SettingsActivity activity = Robolectric.buildActivity(SettingsActivity.class).setup().get();

        assertMinHeight(activity, activity.findViewById(R.id.switchAudio));
        assertMinHeight(activity, activity.findViewById(R.id.switchVibration));
        assertMinHeight(activity, activity.findViewById(R.id.switchColorBlind));
        assertMinHeight(activity, activity.findViewById(R.id.switchTutorialHints));
        assertMinHeight(activity, activity.findViewById(R.id.switchAudioDiagnostics));
        assertMinHeight(activity, activity.findViewById(R.id.spinnerDifficulty));

        Spinner difficulty = activity.findViewById(R.id.spinnerDifficulty);
        assertEquals(activity.getString(R.string.settings_difficulty_spinner_desc),
                difficulty.getContentDescription().toString());
    }

    @Test
    public void classSelectionIconAndActionsAreAccessible() {
        Context context = ApplicationProvider.getApplicationContext();
        View root = inflate(context, R.layout.activity_class_selection);

        ImageView preview = root.findViewById(R.id.imageClassPreview);
        assertEquals(context.getString(R.string.class_preview_content_desc),
                preview.getContentDescription().toString());

        assertFixedHeightAtLeast(context, root.findViewById(R.id.btnKnight));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.btnRanger));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.btnThief));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.btnWizard));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.btnShop));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.btnStart));
    }

    @Test
    public void continueSlotActionsMeetMinimumTouchTargets() {
        Context context = ApplicationProvider.getApplicationContext();
        View root = inflate(context, R.layout.activity_continue);

        assertFixedHeightAtLeast(context, root.findViewById(R.id.slot1Button));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.slot2Button));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.slot3Button));
        assertFixedHeightAtLeast(context, root.findViewById(R.id.slot4Button));
    }

    @Test
    public void gameHudIconControlsAndProgressAreAccessible() {
        Context context = ApplicationProvider.getApplicationContext();
        View root = inflate(context, R.layout.activity_game);

        ImageView portrait = root.findViewById(R.id.imagePlayerHudIcon);
        assertEquals(context.getString(R.string.player_hud_icon_desc),
                portrait.getContentDescription().toString());

        assertHudButton(context, root.findViewById(R.id.btnClassAbility),
                context.getString(R.string.use_ability_desc));
        assertHudButton(context, root.findViewById(R.id.btnClassMenu),
                context.getString(R.string.open_class_menu_desc));
        assertHudButton(context, root.findViewById(R.id.btnInventory),
                context.getString(R.string.open_inventory_desc));
        assertHudButton(context, root.findViewById(R.id.btnUsePotion),
                context.getString(R.string.use_potion_desc));

        ProgressBar hpProgress = root.findViewById(R.id.progressHpCounter);
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO,
                hpProgress.getImportantForAccessibility());
    }

    @Test
    public void tileChildVisualsAreHiddenFromAccessibility() {
        Context context = ApplicationProvider.getApplicationContext();
        View root = inflate(context, R.layout.item_tile);

        assertHiddenFromAccessibility(root.findViewById(R.id.imageTile));
        assertHiddenFromAccessibility(root.findViewById(R.id.monsterHpBar));
        assertHiddenFromAccessibility(root.findViewById(R.id.textFloatingDamage));
        assertHiddenFromAccessibility(root.findViewById(R.id.textTile));
    }

    private static View inflate(Context context, int layoutRes) {
        return LayoutInflater.from(context).inflate(layoutRes, null, false);
    }

    private static void assertHudButton(Context context, Button button, String expectedDescription) {
        assertFixedHeightAtLeast(context, button);
        assertEquals(expectedDescription, button.getContentDescription().toString());
    }

    private static void assertMinHeight(Context context, View view) {
        assertTrue(view.getMinimumHeight() >= dp(context, 48));
    }

    private static void assertFixedHeightAtLeast(Context context, View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        assertTrue(params.height >= dp(context, 48));
    }

    private static void assertHiddenFromAccessibility(View view) {
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, view.getImportantForAccessibility());
    }

    private static int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics());
    }
}
