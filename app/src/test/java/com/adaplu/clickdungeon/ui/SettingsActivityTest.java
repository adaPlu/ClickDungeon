package com.adaplu.clickdungeon.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.R;
import com.adaplu.clickdungeon.SettingsActivity;
import com.adaplu.clickdungeon.util.SettingsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void loadsExistingPreferencesIntoControls() {
        SettingsManager.setAudioEnabled(context, false);
        SettingsManager.setVibrationEnabled(context, false);
        SettingsManager.setColorBlindModeEnabled(context, true);
        SettingsManager.setTutorialHintsEnabled(context, false);
        SettingsManager.setDifficulty(context, SettingsManager.Difficulty.HARDCORE);

        SettingsActivity activity = Robolectric.buildActivity(SettingsActivity.class)
                .setup()
                .get();

        SwitchMaterial audioSwitch = activity.findViewById(R.id.switchAudio);
        SwitchMaterial vibrationSwitch = activity.findViewById(R.id.switchVibration);
        SwitchMaterial colorBlindSwitch = activity.findViewById(R.id.switchColorBlind);
        SwitchMaterial tutorialSwitch = activity.findViewById(R.id.switchTutorialHints);
        Spinner difficultySpinner = activity.findViewById(R.id.spinnerDifficulty);
        TextView difficultySummary = activity.findViewById(R.id.textDifficultySummary);

        assertFalse(audioSwitch.isChecked());
        assertFalse(vibrationSwitch.isChecked());
        assertTrue(colorBlindSwitch.isChecked());
        assertFalse(tutorialSwitch.isChecked());
        assertEquals(2, difficultySpinner.getSelectedItemPosition());
        assertTrue(difficultySummary.getText().toString().contains("Hardcore"));
    }

    @Test
    public void changingControlsPersistsPreferences() {
        SettingsActivity activity = Robolectric.buildActivity(SettingsActivity.class)
                .setup()
                .get();

        SwitchMaterial audioSwitch = activity.findViewById(R.id.switchAudio);
        SwitchMaterial vibrationSwitch = activity.findViewById(R.id.switchVibration);
        SwitchMaterial colorBlindSwitch = activity.findViewById(R.id.switchColorBlind);
        SwitchMaterial tutorialSwitch = activity.findViewById(R.id.switchTutorialHints);
        Spinner difficultySpinner = activity.findViewById(R.id.spinnerDifficulty);
        TextView colorBlindSummary = activity.findViewById(R.id.textColorBlindSummary);
        TextView tutorialSummary = activity.findViewById(R.id.textTutorialSummary);

        audioSwitch.performClick();
        vibrationSwitch.performClick();
        colorBlindSwitch.performClick();
        tutorialSwitch.performClick();
        difficultySpinner.setSelection(1); // NORMAL

        assertFalse(SettingsManager.isAudioEnabled(activity));
        assertFalse(SettingsManager.isVibrationEnabled(activity));
        assertTrue(SettingsManager.isColorBlindModeEnabled(activity));
        assertFalse(SettingsManager.areTutorialHintsEnabled(activity));
        assertEquals(SettingsManager.Difficulty.NORMAL.getValue(), SettingsManager.getDifficulty(activity));
        assertEquals(activity.getString(R.string.settings_color_blind_summary_enabled),
                colorBlindSummary.getText().toString());
        assertEquals(activity.getString(R.string.settings_tutorial_hints_summary_disabled),
                tutorialSummary.getText().toString());
    }
}
