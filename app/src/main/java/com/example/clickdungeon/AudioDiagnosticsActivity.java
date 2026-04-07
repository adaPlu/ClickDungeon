package com.example.clickdungeon;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.clickdungeon.util.SoundManager;

import java.util.List;

/**
 * Displays recent missing audio cues for quick QA verification.
 */
public class AudioDiagnosticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.audio_diagnostics_title);
        setContentView(R.layout.activity_audio_diagnostics);

        TextView body = findViewById(R.id.textAudioDiagnosticsBody);
        List<String> missing = SoundManager.getMissingKeys();
        if (missing == null || missing.isEmpty()) {
            body.setText(getString(R.string.audio_diagnostics_empty));
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < missing.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(missing.get(i));
        }
        body.setText(builder.toString());
    }
}
