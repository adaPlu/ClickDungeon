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

    static final String CHECKSUM_SUFFIX = "_checksum";
    static final String SCHEMA_SUFFIX = "_schema";
    static final String BACKUP_SUFFIX = "_backup";
    static final String BACKUP_SCHEMA_SUFFIX = "_backup_schema";
    static final String BACKUP_CHECKSUM_SUFFIX = "_backup_checksum";

    private static final String TAG = "PersistedBlobStore";
    private static final String SECURITY_PREFS = "security_prefs";
    private static final String KEY_INTEGRITY = "integrity_key_v1";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int KEY_BYTES = 32;

    private PersistedBlobStore() {
    }

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
            return LoadResult.schemaMismatch(json, schema);
        }
        return LoadResult.ok(json, schema);
    }

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

    private static LoadResult restoreFromBackup(Context context,
                                                SharedPreferences prefs,
                                                String dataKey,
                                                int expectedSchema) {
        String backupJson = prefs.getString(dataKey + BACKUP_SUFFIX, null);
        String backupChecksum = prefs.getString(dataKey + BACKUP_CHECKSUM_SUFFIX, null);
        int backupSchema = prefs.getInt(dataKey + BACKUP_SCHEMA_SUFFIX, -1);
        if (!isValid(context, backupJson, backupChecksum) || backupSchema != expectedSchema) {
            return null;
        }
        prefs.edit()
                .putString(dataKey, backupJson)
                .putString(dataKey + CHECKSUM_SUFFIX, backupChecksum)
                .putInt(dataKey + SCHEMA_SUFFIX, backupSchema)
                .apply();
        Log.w(TAG, "Restored " + dataKey + " from backup in " + prefs.getClass().getSimpleName());
        return LoadResult.ok(backupJson, backupSchema);
    }

    private static boolean isValid(Context context, String json, String checksum) {
        if (json == null || checksum == null) {
            return false;
        }
        String computed = computeHmac(context, json);
        return checksum.equals(computed);
    }

    private static String computeHmac(Context context, String json) {
        try {
            byte[] key = getOrCreateKey(context);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] digest = mac.doFinal(json.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(digest, Base64.NO_WRAP);
        } catch (Exception ex) {
            Log.w(TAG, "Failed to compute HMAC", ex);
            return "";
        }
    }

    private static byte[] getOrCreateKey(Context context) {
        SharedPreferences prefs = SecurePreferences.get(context, SECURITY_PREFS);
        String stored = prefs.getString(KEY_INTEGRITY, null);
        if (stored != null) {
            return Base64.decode(stored, Base64.NO_WRAP);
        }
        byte[] key = new byte[KEY_BYTES];
        new SecureRandom().nextBytes(key);
        prefs.edit().putString(KEY_INTEGRITY, Base64.encodeToString(key, Base64.NO_WRAP)).apply();
        return key;
    }

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
