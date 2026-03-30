package com.example.clickdungeon.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.List;

/**
 * Lightweight Play Billing scaffolding.
 * This provides minimal BillingClient wiring; production code should
 * add robust error handling, backend validation, and tests.
 */
public class BillingManager implements PurchasesUpdatedListener {

    private static final String TAG = "BillingManager";

    private final Context context;
    private BillingClient billingClient;

    public BillingManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public void initialize() {
        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(this)
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected");
            }

            @Override
            public void onBillingSetupFinished(@NonNull com.android.billingclient.api.BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "BillingClient setup finished");
                } else {
                    Log.w(TAG, "BillingClient setup failed: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    public void purchaseProduct(Activity activity, String productId) {
        if (billingClient == null || !billingClient.isReady()) {
            Log.w(TAG, "BillingClient not ready");
            return;
        }

        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(java.util.Collections.emptyList()) // placeholder; fill with real ProductDetails
                .build();

        billingClient.launchBillingFlow(activity, params);
    }

    public void queryPurchases() {
        if (billingClient == null || !billingClient.isReady()) return;

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(params, (billingResult, purchasesList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchasesList);
            } else {
                Log.w(TAG, "queryPurchases failed: " + billingResult.getDebugMessage());
            }
        });
    }

    private void handlePurchases(List<Purchase> purchases) {
        if (purchases == null) return;
        for (Purchase p : purchases) {
            // TODO: reconcile with InventoryManager / backend validation
            Log.i(TAG, "Found purchase: " + p.getProducts());
            if (!p.isAcknowledged()) {
                AcknowledgePurchaseParams ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(p.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(ackParams, (billingResult) -> {
                    Log.i(TAG, "Acknowledge result: " + billingResult.getDebugMessage());
                });
            }
        }
    }

    @Override
    public void onPurchasesUpdated(com.android.billingclient.api.BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled purchase");
        } else {
            Log.w(TAG, "Purchase failed: " + billingResult.getDebugMessage());
        }
    }

    public void teardown() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
            billingClient = null;
        }
    }
}
