package com.adaplu.clickdungeon.util;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Stub for Play Billing - IAP wiring is deferred to v1.1.
 *
 * The billingclient dependency and BILLING manifest permission are removed for
 * the v1.0 release. Restore them and replace the no-op bodies here when adding
 * real in-app purchases.
 *
 * Public API is preserved so call sites compile without changes.
 */
public class BillingManager {

    public BillingManager(@NonNull Context context) {
        // no-op until v1.1
    }

    public void initialize() {
        // no-op until v1.1
    }

    public void purchaseProduct(Activity activity, String productId) {
        // no-op until v1.1
    }

    public void queryPurchases() {
        // no-op until v1.1
    }

    public void teardown() {
        // no-op until v1.1
    }
}
