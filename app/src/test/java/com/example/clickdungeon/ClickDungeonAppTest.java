package com.example.clickdungeon;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ClickDungeonAppTest {

    @Test
    public void applicationLifecycleHooksExecute() {
        ClickDungeonApp app = (ClickDungeonApp) ApplicationProvider.getApplicationContext();
        assertNotNull(app);

        app.onTrimMemory(android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
        app.onTerminate();
    }
}
