package com.example.clickdungeon;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import com.example.clickdungeon.util.SettingsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private boolean isInitializing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        setContentView(R.layout.activity_settings);

        SwitchMaterial audioSwitch = findViewById(R.id.switchAudio);
        SwitchMaterial vibrationSwitch = findViewById(R.id.switchVibration);
        SwitchMaterial colorBlindSwitch = findViewById(R.id.switchColorBlind);
        SwitchMaterial tutorialSwitch = findViewById(R.id.switchTutorialHints);
        Spinner difficultySpinner = findViewById(R.id.spinnerDifficulty);
        TextView difficultySummaryText = findViewById(R.id.textDifficultySummary);
        TextView colorBlindSummaryText = findViewById(R.id.textColorBlindSummary);
        TextView tutorialSummaryText = findViewById(R.id.textTutorialSummary);

        isInitializing = true;

        audioSwitch.setChecked(SettingsManager.isAudioEnabled(this));
        vibrationSwitch.setChecked(SettingsManager.isVibrationEnabled(this));
        colorBlindSwitch.setChecked(SettingsManager.isColorBlindModeEnabled(this));
        tutorialSwitch.setChecked(SettingsManager.areTutorialHintsEnabled(this));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.settings_difficulty_entries,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        String[] difficultyValues = getResources().getStringArray(R.array.settings_difficulty_values);
        String[] difficultySummaries = getResources().getStringArray(R.array.settings_difficulty_descriptions);
        String savedDifficulty = SettingsManager.getDifficulty(this);
        int selectedIndex = 0;
        for (int i = 0; i < difficultyValues.length; i++) {
            if (difficultyValues[i].equalsIgnoreCase(savedDifficulty)) {
                selectedIndex = i;
                break;
            }
        }
        difficultySpinner.setSelection(selectedIndex, false);
        updateDifficultySummary(difficultySummaryText, difficultySummaries, selectedIndex);
        TooltipCompat.setTooltipText(difficultySpinner,
                selectedIndex >= 0 && selectedIndex < difficultySummaries.length
                        ? difficultySummaries[selectedIndex]
                        : null);

        updateColorBlindSummary(colorBlindSummaryText, colorBlindSwitch.isChecked());
        updateTutorialSummary(tutorialSummaryText, tutorialSwitch.isChecked());

        isInitializing = false;

        audioSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setAudioEnabled(this, isChecked);
            }
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setVibrationEnabled(this, isChecked);
            }
        });

        colorBlindSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setColorBlindModeEnabled(this, isChecked);
            }
            updateColorBlindSummary(colorBlindSummaryText, isChecked);
        });

        tutorialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setTutorialHintsEnabled(this, isChecked);
            }
            updateTutorialSummary(tutorialSummaryText, isChecked);
        });

        difficultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isInitializing && position >= 0 && position < difficultyValues.length) {
                    SettingsManager.setDifficulty(SettingsActivity.this, difficultyValues[position]);
                }
                updateDifficultySummary(difficultySummaryText, difficultySummaries, position);
                TooltipCompat.setTooltipText(difficultySpinner,
                        position >= 0 && position < difficultySummaries.length
                                ? difficultySummaries[position]
                                : null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });
    }

    private void updateDifficultySummary(TextView summaryView, String[] summaries, int index) {
        if (summaryView == null || summaries == null) {
            return;
        }
        if (index >= 0 && index < summaries.length) {
            summaryView.setText(summaries[index]);
        }
    }

    private void updateColorBlindSummary(TextView summaryView, boolean enabled) {
        if (summaryView == null) {
            return;
        }
        summaryView.setText(enabled
                ? getString(R.string.settings_color_blind_summary_enabled)
                : getString(R.string.settings_color_blind_summary));
    }

    private void updateTutorialSummary(TextView summaryView, boolean enabled) {
        if (summaryView == null) {
            return;
        }
        summaryView.setText(enabled
                ? getString(R.string.settings_tutorial_hints_summary)
                : getString(R.string.settings_tutorial_hints_summary_disabled));
    }
}
