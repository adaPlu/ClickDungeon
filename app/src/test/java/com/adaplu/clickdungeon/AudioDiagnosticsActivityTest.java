package com.adaplu.clickdungeon;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.adaplu.clickdungeon.util.SoundManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AudioDiagnosticsActivityTest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        SoundManager.release();
        SoundManager.init(context);
        SoundManager.playAndReport("missing_key_for_test_audio_diag");
    }

    @Test
    public void diagnosticsShowsMissingKeys() {
        AudioDiagnosticsActivity activity = Robolectric.buildActivity(AudioDiagnosticsActivity.class).setup().get();
        TextView body = activity.findViewById(R.id.textAudioDiagnosticsBody);
        assertTrue(body.getText().toString().contains("missing_key_for_test_audio_diag"));
    }
}
