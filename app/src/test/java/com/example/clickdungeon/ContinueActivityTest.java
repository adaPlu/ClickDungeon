package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ContinueActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        for (int i = 0; i < 4; i++) {
            context.getSharedPreferences("SaveSlot" + i, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit();
        }
    }

    @Test
    public void emptySlotShowsNewGameButton() {
        Intent intent = new Intent(context, ContinueActivity.class);
        ContinueActivity activity = Robolectric.buildActivity(ContinueActivity.class, intent).setup().get();

        TextView info = activity.findViewById(R.id.slot1Info);
        Button button = activity.findViewById(R.id.slot1Button);

        assertEquals(activity.getString(R.string.continue_slot_empty, 1), info.getText().toString());
        assertEquals(activity.getString(R.string.continue_slot_new_game), button.getText().toString());
    }
}
