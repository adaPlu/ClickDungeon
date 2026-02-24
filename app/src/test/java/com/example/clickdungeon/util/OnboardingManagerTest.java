package com.example.clickdungeon.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.clickdungeon.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class OnboardingManagerTest {

    private static final String PREFS_NAME = "onboarding_prompts";
    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
        SettingsManager.setTutorialHintsEnabled(activity, true);
        clearOnboardingPrefs(activity);
    }

    @After
    public void tearDown() {
        ShadowAlertDialog.reset();
        if (activity != null) {
            activity.finish();
        }
    }

    @Test
    public void showDungeonTutorialIfNeeded_hintsDisabled_skipsDialog() {
        SettingsManager.setTutorialHintsEnabled(activity, false);

        OnboardingManager.showDungeonTutorialIfNeeded(activity, SettingsManager.Difficulty.NORMAL, false);

        assertNull(ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void showDungeonTutorialIfNeeded_firstRun_displaysDialogAndPersistsOnContinue() {
        OnboardingManager.showDungeonTutorialIfNeeded(activity, SettingsManager.Difficulty.CASUAL, true);

        android.app.AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();

        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        assertEquals(2, prefs.getInt("dungeon_tutorial_version", 0));
    }

    @Test
    public void showDungeonTutorialIfNeeded_disableHints_turnsOffPreference() {
        OnboardingManager.showDungeonTutorialIfNeeded(activity, SettingsManager.Difficulty.HARDCORE, false);

        android.app.AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).performClick();

        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        assertEquals(2, prefs.getInt("dungeon_tutorial_version", 0));
        assertFalse(SettingsManager.areTutorialHintsEnabled(activity));
    }

    @Test
    public void showDungeonTutorialIfNeeded_alreadyShown_doesNotReshow() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt("dungeon_tutorial_version", 2).apply();

        OnboardingManager.showDungeonTutorialIfNeeded(activity, SettingsManager.Difficulty.NORMAL, false);

        assertNull(ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void getDifficultyLabel_returnsLocalizedLabelsForAllModes() {
        String casual = org.robolectric.util.ReflectionHelpers.callStaticMethod(
                OnboardingManager.class,
                "getDifficultyLabel",
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(Activity.class, activity),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(SettingsManager.Difficulty.class, SettingsManager.Difficulty.CASUAL));
        String normal = org.robolectric.util.ReflectionHelpers.callStaticMethod(
                OnboardingManager.class,
                "getDifficultyLabel",
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(Activity.class, activity),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(SettingsManager.Difficulty.class, SettingsManager.Difficulty.NORMAL));
        String hardcore = org.robolectric.util.ReflectionHelpers.callStaticMethod(
                OnboardingManager.class,
                "getDifficultyLabel",
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(Activity.class, activity),
                org.robolectric.util.ReflectionHelpers.ClassParameter.from(SettingsManager.Difficulty.class, SettingsManager.Difficulty.HARDCORE));

        assertEquals(activity.getString(R.string.settings_difficulty_casual), casual);
        assertEquals(activity.getString(R.string.settings_difficulty_normal), normal);
        assertEquals(activity.getString(R.string.settings_difficulty_hardcore), hardcore);
    }

    private void clearOnboardingPrefs(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }
}
