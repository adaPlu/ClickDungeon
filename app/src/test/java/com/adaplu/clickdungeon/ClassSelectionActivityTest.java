package com.adaplu.clickdungeon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

@RunWith(RobolectricTestRunner.class)
public class ClassSelectionActivityTest {

    @Test
    public void selectingClassDisablesSelectedButton() {
        Context context = ApplicationProvider.getApplicationContext();
        ClassSelectionActivity activity = Robolectric.buildActivity(ClassSelectionActivity.class)
                .setup()
                .get();

        Button knight = activity.findViewById(R.id.btnKnight);
        Button thief = activity.findViewById(R.id.btnThief);

        knight.performClick();

        assertFalse(knight.isEnabled());
        assertTrue(thief.isEnabled());
    }

    @Test
    public void selectingClass_updatesPreviewToSpriteSheetAndHighlightsButton() {
        ClassSelectionActivity activity = Robolectric.buildActivity(ClassSelectionActivity.class)
                .setup()
                .get();

        ImageView preview = activity.findViewById(R.id.imageClassPreview);
        Button knight = activity.findViewById(R.id.btnKnight);
        Button thief = activity.findViewById(R.id.btnThief);
        Button wizard = activity.findViewById(R.id.btnWizard);

        thief.performClick();
        android.graphics.drawable.Drawable thiefDrawable = preview.getDrawable();
        assertNotNull(thiefDrawable);
        assertTrue(knight.isEnabled());
        assertFalse(thief.isEnabled());
        assertTrue(wizard.isEnabled());

        wizard.performClick();
        android.graphics.drawable.Drawable wizardDrawable = preview.getDrawable();
        assertNotNull(wizardDrawable);
        assertTrue(thiefDrawable.getConstantState() == null
                || wizardDrawable.getConstantState() == null
                || !thiefDrawable.getConstantState().equals(wizardDrawable.getConstantState()));
        assertTrue(knight.isEnabled());
        assertTrue(thief.isEnabled());
        assertFalse(wizard.isEnabled());
    }

    @Test
    public void selectingClass_updatesPreviewToExpectedClassIcon() {
        ClassSelectionActivity activity = Robolectric.buildActivity(ClassSelectionActivity.class)
                .setup()
                .get();

        ImageView preview = activity.findViewById(R.id.imageClassPreview);
        Button knight = activity.findViewById(R.id.btnKnight);
        Button ranger = activity.findViewById(R.id.btnRanger);
        Button thief = activity.findViewById(R.id.btnThief);
        Button wizard = activity.findViewById(R.id.btnWizard);

        knight.performClick();
        assertDrawable(preview.getDrawable(), activity.getDrawable(R.drawable.icon_knight));

        ranger.performClick();
        assertDrawable(preview.getDrawable(), activity.getDrawable(R.drawable.icon_ranger));

        thief.performClick();
        assertDrawable(preview.getDrawable(), activity.getDrawable(R.drawable.icon_thief));

        wizard.performClick();
        assertDrawable(preview.getDrawable(), activity.getDrawable(R.drawable.icon_wizard));
    }

    @Test
    public void startGameLaunchesGameActivityWithExtras() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, ClassSelectionActivity.class);
        intent.putExtra(ClassSelectionActivity.EXTRA_SAVE_SLOT_INDEX, 1);
        ClassSelectionActivity activity = Robolectric.buildActivity(ClassSelectionActivity.class, intent)
                .setup()
                .get();

        EditText name = activity.findViewById(R.id.editName);
        name.setText("Hero");
        activity.findViewById(R.id.btnKnight).performClick();
        activity.findViewById(R.id.btnStart).performClick();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(GameActivity.class.getName(), next.getComponent().getClassName());
        assertTrue(next.getBooleanExtra(GameActivity.EXTRA_IS_NEW_GAME, false));
        assertEquals(1, next.getIntExtra(GameActivity.EXTRA_SLOT_INDEX, -1));
    }

    private static void assertDrawable(Drawable actual, Drawable expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getConstantState(), actual.getConstantState());
    }
}
