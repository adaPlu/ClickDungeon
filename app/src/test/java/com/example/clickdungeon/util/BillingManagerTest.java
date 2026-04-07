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

    @Test
    public void purchaseProduct_withoutInitialize_doesNotThrow() {
        Context ctx = ApplicationProvider.getApplicationContext();
        BillingManager bm = new BillingManager(ctx);
        try {
            bm.purchaseProduct(null, "test.product.id");
        } catch (Exception e) {
            org.junit.Assert.fail("purchaseProduct threw: " + e.getMessage());
        }
    }

    @Test
    public void queryPurchases_withoutInitialize_doesNotThrow() {
        Context ctx = ApplicationProvider.getApplicationContext();
        BillingManager bm = new BillingManager(ctx);
        try {
            bm.queryPurchases();
        } catch (Exception e) {
            org.junit.Assert.fail("queryPurchases threw: " + e.getMessage());
        }
    }

    @Test
    public void teardown_withoutInitialize_doesNotThrow() {
        Context ctx = ApplicationProvider.getApplicationContext();
        BillingManager bm = new BillingManager(ctx);
        try {
            bm.teardown();
        } catch (Exception e) {
            org.junit.Assert.fail("teardown threw: " + e.getMessage());
        }
    }
}
