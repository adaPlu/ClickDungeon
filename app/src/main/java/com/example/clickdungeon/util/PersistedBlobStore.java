package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Persists JSON blobs with schema + integrity metadata and a last-known-good backup.
 */
public final class PersistedBlobStore {

    /** Suffix used to store the integrity checksum. */
    static final String CHECKSUM_SUFFIX = "_checksum";
    /** Suffix used to store the schema version. */
    static final String SCHEMA_SUFFIX = "_schema";
    /** Suffix used to store the backup JSON blob. */
    static final String BACKUP_SUFFIX = "_backup";
    /** Suffix used to store the backup schema version. */
    static final String BACKUP_SCHEMA_SUFFIX = "_backup_schema";
    /** Suffix used to store the backup integrity checksum. */
    static final String BACKUP_CHECKSUM_SUFFIX = "_backup_checksum";

    private static final String TAG = "PersistedBlobStore";
    /** Shared prefs for integrity key storage. */
    private static final String SECURITY_PREFS = "security_prefs";
    /** Key for the HMAC secret. */
    private static final String KEY_INTEGRITY = "integrity_key_v1";
    /** Key for the previous HMAC secret (rotation fallback). */
    private static final String KEY_INTEGRITY_PREVIOUS = "integrity_key_prev";
    /** Key for tracking HMAC version. */
    private static final String KEY_INTEGRITY_VERSION = "integrity_key_version";
    /** HMAC algorithm used for integrity checks. */
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    /** Number of random bytes in the integrity key. */
    private static final int KEY_BYTES = 32;
    private static final int CURRENT_KEY_VERSION = PersistenceSecurityConfig.INTEGRITY_KEY_VERSION;

    private static volatile TelemetryListener telemetryListener;
    private static final int MAX_EVENT_LOG = 25;
    private static final java.util.List<String> eventLog = new java.util.ArrayList<>();

    private PersistedBlobStore() {
    }

    /**
     * Saves a JSON blob with schema and integrity metadata, backing up previous values.
     */
    public static void save(Context context,
                            String prefsName,
                            String dataKey,
                            int schemaVersion,
                            String json) {
        if (json == null) {
            return;
        }
        SharedPreferences prefs = SecurePreferences.get(context, prefsName);
        String currentJson = prefs.getString(dataKey, null);
        String currentChecksum = prefs.getString(dataKey + CHECKSUM_SUFFIX, null);
        int currentSchema = prefs.getInt(dataKey + SCHEMA_SUFFIX, -1);
        SharedPreferences.Editor editor = prefs.edit();

        if (isValid(context, currentJson, currentChecksum) && currentSchema >= 0) {
            editor.putString(dataKey + BACKUP_SUFFIX, currentJson)
                    .putString(dataKey + BACKUP_CHECKSUM_SUFFIX, currentChecksum)
                    .putInt(dataKey + BACKUP_SCHEMA_SUFFIX, currentSchema);
        }

        String checksum = computeHmac(context, json);
        editor.putString(dataKey, json)
                .putString(dataKey + CHECKSUM_SUFFIX, checksum)
                .putInt(dataKey + SCHEMA_SUFFIX, schemaVersion)
                .apply();
    }

    /**
     * Loads a JSON blob, validating integrity and schema.
     */
    public static LoadResult load(Context context, String prefsName, String dataKey, int expectedSchema) {
        SharedPreferences prefs = SecurePreferences.get(context, prefsName);
        String json = prefs.getString(dataKey, null);
        String checksum = prefs.getString(dataKey + CHECKSUM_SUFFIX, null);
        int schema = prefs.getInt(dataKey + SCHEMA_SUFFIX, -1);
        if (json == null) {
            return LoadResult.missing();
        }
        if (!isValid(context, json, checksum)) {
            Log.w(TAG, "Integrity check failed for " + dataKey + " in " + prefsName);
            LoadResult restored = restoreFromBackup(context, prefs, dataKey, expectedSchema);
            return restored != null ? restored : LoadResult.corrupt();
        }
        if (schema != expectedSchema) {
            Log.w(TAG, "Schema mismatch for " + dataKey + " in " + prefsName + ": " + schema);
            notifySchemaMismatch(prefsName, dataKey, schema, expectedSchema);
            return LoadResult.schemaMismatch(json, schema);
        }
        return LoadResult.ok(json, schema);
    }

    /**
     * Clears the primary and backup entries for the provided key.
     */
    public static void clear(Context context, String prefsName, String dataKey) {
        SharedPreferences prefs = SecurePreferences.get(context, prefsName);
        prefs.edit()
                .remove(dataKey)
                .remove(dataKey + CHECKSUM_SUFFIX)
                .remove(dataKey + SCHEMA_SUFFIX)
                .remove(dataKey + BACKUP_SUFFIX)
                .remove(dataKey + BACKUP_CHECKSUM_SUFFIX)
                .remove(dataKey + BACKUP_SCHEMA_SUFFIX)
                .apply();
    }

    /**
     * Attempts to restore the primary entry from a valid backup.
     */
    private static LoadResult restoreFromBackup(Context context,
                                                SharedPreferences prefs,
                                                String dataKey,
                                                int expectedSchema) {
        String backupJson = prefs.getString(dataKey + BACKUP_SUFFIX, null);
        String backupChecksum = prefs.getString(dataKey + BACKUP_CHECKSUM_SUFFIX, null);
        int backupSchema = prefs.getInt(dataKey + BACKUP_SCHEMA_SUFFIX, -1);
        if (!isValid(context, backupJson, backupChecksum) || backupSchema != expectedSchema) {
            notifyRestore(prefs.getClass().getSimpleName(), dataKey, false);
            return null;
        }
        prefs.edit()
                .putString(dataKey, backupJson)
                .putString(dataKey + CHECKSUM_SUFFIX, backupChecksum)
                .putInt(dataKey + SCHEMA_SUFFIX, backupSchema)
                .apply();
        Log.w(TAG, "Restored " + dataKey + " from backup in " + prefs.getClass().getSimpleName());
        notifyRestore(prefs.getClass().getSimpleName(), dataKey, true);
        return LoadResult.ok(backupJson, backupSchema);
    }

    /**
     * Public helper to attempt restoring from a backup entry.
     */
    public static LoadResult restoreBackup(Context context,
                                           String prefsName,
                                           String dataKey,
                                           int expectedSchema) {
        SharedPreferences prefs = SecurePreferences.get(context, prefsName);
        return restoreFromBackup(context, prefs, dataKey, expectedSchema);
    }

    /**
     * Validates a JSON blob against its checksum.
     */
    private static boolean isValid(Context context, String json, String checksum) {
        if (json == null || checksum == null) {
            return false;
        }
        for (byte[] key : getAllKeys(context)) {
            String computed = computeHmac(context, json, key);
            if (checksum.equals(computed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes an HMAC checksum for the provided JSON.
     */
    private static String computeHmac(Context context, String json) {
        byte[] key = getOrCreateKey(context);
        return computeHmac(context, json, key);
    }

    private static String computeHmac(Context context, String json, byte[] key) {
        if (key == null) {
            return "";
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(json.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (Exception ex) {
            Log.w(TAG, "Failed to compute HMAC", ex);
            return "";
        }
    }

    /**
     * Loads or generates the shared integrity key.
     */
    private static byte[] getOrCreateKey(Context context) {
        SharedPreferences prefs = SecurePreferences.get(context, SECURITY_PREFS);
        int version = prefs.getInt(KEY_INTEGRITY_VERSION, 0);
        String stored = prefs.getString(KEY_INTEGRITY, null);
        if (stored != null && version == CURRENT_KEY_VERSION) {
            return Base64.decode(stored, Base64.NO_WRAP);
        }
        if (stored != null) {
            prefs.edit()
                    .putString(KEY_INTEGRITY_PREVIOUS, stored)
                    .apply();
        }
        byte[] key = new byte[KEY_BYTES];
        new SecureRandom().nextBytes(key);
        prefs.edit()
                .putString(KEY_INTEGRITY, Base64.encodeToString(key, Base64.NO_WRAP))
                .putInt(KEY_INTEGRITY_VERSION, CURRENT_KEY_VERSION)
                .apply();
        return key;
    }

    private static java.util.List<byte[]> getAllKeys(Context context) {
        SharedPreferences prefs = SecurePreferences.get(context, SECURITY_PREFS);
        java.util.List<byte[]> keys = new java.util.ArrayList<>();
        String current = prefs.getString(KEY_INTEGRITY, null);
        if (current != null) {
            keys.add(Base64.decode(current, Base64.NO_WRAP));
        }
        String previous = prefs.getString(KEY_INTEGRITY_PREVIOUS, null);
        if (previous != null) {
            keys.add(Base64.decode(previous, Base64.NO_WRAP));
        }
        if (keys.isEmpty()) {
            keys.add(getOrCreateKey(context));
        }
        return keys;
    }

    public static void setTelemetryListener(TelemetryListener listener) {
        telemetryListener = listener;
    }

    private static void notifySchemaMismatch(String prefsName,
                                             String dataKey,
                                             int foundSchema,
                                             int expectedSchema) {
        recordEvent("schema_mismatch:" + prefsName + ":" + dataKey + ":" + foundSchema + "->" + expectedSchema);
        if (telemetryListener != null) {
            telemetryListener.onSchemaMismatch(prefsName, dataKey, foundSchema, expectedSchema);
        }
    }

    private static void notifyRestore(String prefsName, String dataKey, boolean success) {
        recordEvent("restore_backup:" + prefsName + ":" + dataKey + ":" + success);
        if (telemetryListener != null) {
            telemetryListener.onRestoreFromBackup(prefsName, dataKey, success);
        }
    }

    public static java.util.List<String> getRecentEvents() {
        synchronized (eventLog) {
            return new java.util.ArrayList<>(eventLog);
        }
    }

    private static void recordEvent(String message) {
        synchronized (eventLog) {
            eventLog.add(0, message);
            while (eventLog.size() > MAX_EVENT_LOG) {
                eventLog.remove(eventLog.size() - 1);
            }
        }
    }

    public interface TelemetryListener {
        void onSchemaMismatch(String prefsName, String dataKey, int foundSchema, int expectedSchema);
        void onRestoreFromBackup(String prefsName, String dataKey, boolean success);
    }

    /**
     * Result wrapper for load attempts with status metadata.
     */
    public static final class LoadResult {
        public enum Status {
            OK,
            MISSING,
            CORRUPT,
            SCHEMA_MISMATCH
        }

        public final Status status;
        public final String json;
        public final int schemaVersion;

        private LoadResult(Status status, String json, int schemaVersion) {
            this.status = status;
            this.json = json;
            this.schemaVersion = schemaVersion;
        }

        public static LoadResult ok(String json, int schemaVersion) {
            return new LoadResult(Status.OK, json, schemaVersion);
        }

        public static LoadResult missing() {
            return new LoadResult(Status.MISSING, null, -1);
        }

        public static LoadResult corrupt() {
            return new LoadResult(Status.CORRUPT, null, -1);
        }

        public static LoadResult schemaMismatch(String json, int schemaVersion) {
            return new LoadResult(Status.SCHEMA_MISMATCH, json, schemaVersion);
        }
    }
}
