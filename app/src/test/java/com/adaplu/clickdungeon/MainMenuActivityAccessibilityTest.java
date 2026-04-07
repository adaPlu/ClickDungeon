package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MainMenuActivityAccessibilityTest {

    @Test
    public void compoundControlsExposeAccessibleLabel() {
        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        FrameLayout newGame = activity.findViewById(R.id.btnNewGame);
        TextView newGameLabel = newGame.findViewById(R.id.btnNewGame_text);

        assertNotNull(newGameLabel);
        assertEquals(newGameLabel.getText().toString(), String.valueOf(newGame.getContentDescription()));
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, newGameLabel.getImportantForAccessibility());
    }
}
