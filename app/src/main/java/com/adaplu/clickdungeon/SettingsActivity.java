package com.adaplu.clickdungeon;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import com.adaplu.clickdungeon.BuildConfig;
import com.adaplu.clickdungeon.util.SettingsManager;
import com.adaplu.clickdungeon.util.SoundManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * SettingsActivity provides a UI for the player to customize their game experience.
 * Options include audio and vibration toggles, difficulty selection, 
 * color-blind accessibility aids, and tutorial hint preferences.
 */
public class SettingsActivity extends AppCompatActivity {

    /** Flag to prevent listener triggers during initial view setup. */
    private boolean isInitializing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        setContentView(R.layout.activity_settings);

        // Bind UI components.
        SwitchMaterial audioSwitch = findViewById(R.id.switchAudio);
        SwitchMaterial vibrationSwitch = findViewById(R.id.switchVibration);
        SwitchMaterial colorBlindSwitch = findViewById(R.id.switchColorBlind);
        SwitchMaterial tutorialSwitch = findViewById(R.id.switchTutorialHints);
        SwitchMaterial audioDiagnosticsSwitch = findViewById(R.id.switchAudioDiagnostics);
        Spinner difficultySpinner = findViewById(R.id.spinnerDifficulty);
        TextView difficultySummaryText = findViewById(R.id.textDifficultySummary);
        TextView colorBlindSummaryText = findViewById(R.id.textColorBlindSummary);
        TextView tutorialSummaryText = findViewById(R.id.textTutorialSummary);
        TextView audioDiagnosticsSummaryText = findViewById(R.id.textAudioDiagnosticsSummary);

        // Hide the audio diagnostics row entirely in non-debug (release) builds.
        if (!BuildConfig.DEBUG) {
            if (audioDiagnosticsSwitch != null) audioDiagnosticsSwitch.setVisibility(View.GONE);
            if (audioDiagnosticsSummaryText != null) audioDiagnosticsSummaryText.setVisibility(View.GONE);
            audioDiagnosticsSwitch = null;
        }

        isInitializing = true;

        // Initialize switch states from persistent manager.
        audioSwitch.setChecked(SettingsManager.isAudioEnabled(this));
        vibrationSwitch.setChecked(SettingsManager.isVibrationEnabled(this));
        colorBlindSwitch.setChecked(SettingsManager.isColorBlindModeEnabled(this));
        tutorialSwitch.setChecked(SettingsManager.areTutorialHintsEnabled(this));
        if (audioDiagnosticsSwitch != null) {
            audioDiagnosticsSwitch.setChecked(SettingsManager.isAudioDiagnosticsEnabled(this));
        }

        // Set up the difficulty spinner with localized entry names.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.settings_difficulty_entries,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);

        // Fetch difficulty configuration data.
        String[] difficultyValues = getResources().getStringArray(R.array.settings_difficulty_values);
        String[] difficultySummaries = getResources().getStringArray(R.array.settings_difficulty_descriptions);
        String savedDifficulty = SettingsManager.getDifficulty(this);
        
        // Match the saved difficulty string to its spinner index.
        int selectedIndex = 0;
        for (int i = 0; i < difficultyValues.length; i++) {
            if (difficultyValues[i].equalsIgnoreCase(savedDifficulty)) {
                selectedIndex = i;
                break;
            }
        }
        difficultySpinner.setSelection(selectedIndex, false);
        updateDifficultySummary(difficultySummaryText, difficultySummaries, selectedIndex);
        
        // Add accessibility tooltips.
        TooltipCompat.setTooltipText(difficultySpinner,
                selectedIndex < difficultySummaries.length
                        ? difficultySummaries[selectedIndex]
                        : null);

        updateColorBlindSummary(colorBlindSummaryText, colorBlindSwitch.isChecked());
        updateTutorialSummary(tutorialSummaryText, tutorialSwitch.isChecked());
        if (audioDiagnosticsSwitch != null) {
            updateAudioDiagnosticsSummary(audioDiagnosticsSummaryText, audioDiagnosticsSwitch.isChecked());
        }

        isInitializing = false;

        // Audio preference handler.
        audioSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setAudioEnabled(this, isChecked);
                SoundManager.syncMuteFromSettings(this);
                if (isChecked) {
                    SoundManager.resumeAll();
                } else {
                    SoundManager.pauseAll();
                }
            }
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setVibrationEnabled(this, isChecked);
            }
        });

        // Accessibility preference handler.
        colorBlindSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setColorBlindModeEnabled(this, isChecked);
            }
            updateColorBlindSummary(colorBlindSummaryText, isChecked);
        });

        // Tutorial onboarding preference handler.
        tutorialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isInitializing) {
                SettingsManager.setTutorialHintsEnabled(this, isChecked);
            }
            updateTutorialSummary(tutorialSummaryText, isChecked);
        });

        if (audioDiagnosticsSwitch != null) {
            audioDiagnosticsSwitch.setChecked(SettingsManager.isAudioDiagnosticsEnabled(this));
            updateAudioDiagnosticsSummary(audioDiagnosticsSummaryText, audioDiagnosticsSwitch.isChecked());
            audioDiagnosticsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isInitializing) {
                    SettingsManager.setAudioDiagnosticsEnabled(this, isChecked);
                }
                updateAudioDiagnosticsSummary(audioDiagnosticsSummaryText, isChecked);
                if (isChecked && BuildConfig.DEBUG) {
                    startActivity(new Intent(this, AudioDiagnosticsActivity.class));
                }
            });
        }

        // Difficulty selection handler.
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

    /**
     * Refreshes the text block describing the current difficulty level.
     */
    private void updateDifficultySummary(TextView summaryView, String[] summaries, int index) {
        if (summaryView == null || summaries == null) {
            return;
        }
        if (index >= 0 && index < summaries.length) {
            summaryView.setText(summaries[index]);
        }
    }

    /**
     * Refreshes the text block describing color-blind mode status.
     */
    private void updateColorBlindSummary(TextView summaryView, boolean enabled) {
        if (summaryView == null) {
            return;
        }
        summaryView.setText(enabled
                ? getString(R.string.settings_color_blind_summary_enabled)
                : getString(R.string.settings_color_blind_summary));
    }

    /**
     * Refreshes the text block describing tutorial hint status.
     */
    private void updateTutorialSummary(TextView summaryView, boolean enabled) {
        if (summaryView == null) {
            return;
        }
        summaryView.setText(enabled
                ? getString(R.string.settings_tutorial_hints_summary)
                : getString(R.string.settings_tutorial_hints_summary_disabled));
    }

    /**
     * Refreshes the text block describing audio diagnostics.
     */
    private void updateAudioDiagnosticsSummary(TextView summaryView, boolean enabled) {
        if (summaryView == null) {
            return;
        }
        summaryView.setText(getString(R.string.settings_audio_diagnostics_summary));
    }
}
