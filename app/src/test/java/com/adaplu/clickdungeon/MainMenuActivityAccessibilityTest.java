package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MainMenuActivityAccessibilityTest {

    @Test
    public void compoundControlsExposeAccessibleLabel() {
        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        assertCompoundLabel(activity.findViewById(R.id.btnNewGame), R.id.btnNewGame_text);
        assertCompoundLabel(activity.findViewById(R.id.btnSettings), R.id.btnSettings_text);
        assertCompoundLabel(activity.findViewById(R.id.btnClasses), R.id.btnClasses_text);
        assertCompoundLabel(activity.findViewById(R.id.btnShop), R.id.btnShop_text);
        assertCompoundLabel(activity.findViewById(R.id.btnInventory), R.id.btnInventory_text);
        assertCompoundLabel(activity.findViewById(R.id.btnAchievements), R.id.btnAchievements_text);
    }

    @Test
    public void menuIconsAreDecorativeForAccessibility() {
        MainMenuActivity activity = Robolectric.buildActivity(MainMenuActivity.class).setup().get();

        assertDecorativeIcons(activity.findViewById(R.id.btnNewGame));
        assertDecorativeIcons(activity.findViewById(R.id.btnContinue));
        assertDecorativeIcons(activity.findViewById(R.id.btnSettings));
        assertDecorativeIcons(activity.findViewById(R.id.btnClasses));
        assertDecorativeIcons(activity.findViewById(R.id.btnShop));
        assertDecorativeIcons(activity.findViewById(R.id.btnInventory));
        assertDecorativeIcons(activity.findViewById(R.id.btnAchievements));
    }

    private static void assertCompoundLabel(FrameLayout control, int labelId) {
        TextView label = control.findViewById(labelId);

        assertNotNull(label);
        assertEquals(label.getText().toString(), String.valueOf(control.getContentDescription()));
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, label.getImportantForAccessibility());
    }

    private static void assertDecorativeIcons(View view) {
        if (view instanceof ImageView) {
            assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, view.getImportantForAccessibility());
            assertEquals(null, view.getContentDescription());
            return;
        }
        if (!(view instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            assertDecorativeIcons(group.getChildAt(i));
        }
    }
}
