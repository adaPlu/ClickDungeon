package com.adaplu.clickdungeon.util;

import org.junit.Test;

/**
 * Unit tests for TelemetryManager.
 *
 * Firebase Analytics cannot be initialized in unit tests (no real Firebase SDK context),
 * so these tests verify the null-guard contract: every public static method must silently
 * return rather than throw when sAnalytics has not been initialized.
 *
 * Integration-level event verification (actual events reaching Firebase) is covered by
 * manual DebugView testing on a device (see docs/TELEMETRY_EVENTS.md).
 */
public class TelemetryManagerTest {

    // When init() has never been called, sAnalytics == null.
    // Each method must exit silently without NPE.

    @Test
    public void logSessionStart_beforeInit_doesNotThrow() {
        TelemetryManager.logSessionStart();
    }

    @Test
    public void logRunStart_beforeInit_doesNotThrow() {
        TelemetryManager.logRunStart("KNIGHT", "NORMAL", 0);
    }

    @Test
    public void logRunStart_withNullArgs_doesNotThrow() {
        TelemetryManager.logRunStart(null, null, -1);
    }

    @Test
    public void logRunFailed_beforeInit_doesNotThrow() {
        TelemetryManager.logRunFailed(5, "WIZARD", 3, 120);
    }

    @Test
    public void logRunCompleted_beforeInit_doesNotThrow() {
        TelemetryManager.logRunCompleted(15, "THIEF", 8, 500);
    }

    @Test
    public void logCombatEnded_beforeInit_doesNotThrow() {
        TelemetryManager.logCombatEnded("VICTORY", "Goblin", 3);
    }

    @Test
    public void logCombatEnded_withNullArgs_doesNotThrow() {
        TelemetryManager.logCombatEnded(null, null, 0);
    }

    @Test
    public void logLevelUp_beforeInit_doesNotThrow() {
        TelemetryManager.logLevelUp(5, 3);
    }

    @Test
    public void logPurchase_beforeInit_doesNotThrow() {
        TelemetryManager.logPurchase("Health Potion", "GOLD", 50);
    }

    @Test
    public void logPurchase_withNullArgs_doesNotThrow() {
        TelemetryManager.logPurchase(null, null, 0);
    }

    @Test
    public void logSaveFailed_beforeInit_doesNotThrow() {
        // logSaveFailed also records to Crashlytics — the try/catch must absorb failures.
        TelemetryManager.logSaveFailed("ENCRYPTION_ERROR", 1);
    }

    @Test
    public void logSaveFailed_withNullReason_doesNotThrow() {
        TelemetryManager.logSaveFailed(null, 0);
    }
}
