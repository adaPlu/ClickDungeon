package com.example.clickdungeon;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class InventoryActivityTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @Test
    public void emptyInventoryShowsEmptyState() {
        InventoryActivity activity = Robolectric.buildActivity(InventoryActivity.class).setup().get();

        View emptyView = activity.findViewById(R.id.textEmptyInventory);
        assertEquals(View.VISIBLE, emptyView.getVisibility());
    }
}
