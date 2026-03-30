package com.example.clickdungeon.util;

import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.robolectric.RobolectricTestRunner;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class BillingManagerTest {

    @Test
    public void instantiatesBillingManager() {
        Context ctx = ApplicationProvider.getApplicationContext();
        BillingManager bm = new BillingManager(ctx);
        assertNotNull(bm);
    }
}
