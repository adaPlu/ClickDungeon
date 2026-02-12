package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides encrypted SharedPreferences when available, falling back to plain prefs if needed.
 */
public final class SecurePreferences {

    private static final String TAG = "SecurePreferences";
    private static final String KEYSET_PREFS = "cd_tink_keyset_prefs";
    private static final String KEYSET_NAME = "cd_tink_keyset";
    private static final String MASTER_KEY_URI = "android-keystore://clickdungeon_prefs_key";
    private static final String ENCRYPTED_PREFIX = "enc:";
    private static final String TYPE_STRING = "s|";
    private static final String TYPE_INT = "i|";
    private static final String TYPE_BOOL = "b|";

    private static volatile Aead aead;

    static {
        try {
            AeadConfig.register();
        } catch (GeneralSecurityException ex) {
            Log.w(TAG, "Failed to register Tink Aead config", ex);
        }
    }

    private SecurePreferences() {
    }

    /**
     * Returns encrypted SharedPreferences when supported, otherwise plain prefs.
     */
    public static SharedPreferences get(Context context, String name) {
        if (!PersistenceSecurityConfig.USE_ENCRYPTED_PREFS) {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
        try {
            return new TinkSharedPreferences(context.getSharedPreferences(name, Context.MODE_PRIVATE),
                    getAead(context));
        } catch (GeneralSecurityException | IOException ex) {
            Log.w(TAG, "Failed to initialize encrypted prefs for " + name, ex);
            // Failure policy decides whether to block or fall back to plain prefs.
            if (PersistenceSecurityConfig.ENCRYPTED_PREFS_FAILURE_POLICY
                    == PersistenceSecurityConfig.EncryptedPrefsFailurePolicy.FAIL_CLOSED) {
                return new BlockingSharedPreferences();
            }
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
    }

    @SuppressWarnings("deprecation")
    private static Aead getAead(Context context) throws GeneralSecurityException, IOException {
        if (aead == null) {
            synchronized (SecurePreferences.class) {
                if (aead == null) {
                    AndroidKeysetManager manager = new AndroidKeysetManager.Builder()
                            .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS)
                            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                            .withMasterKeyUri(MASTER_KEY_URI)
                            .build();
                    aead = manager.getKeysetHandle().getPrimitive(Aead.class);
                }
            }
        }
        return aead;
    }

    private static String encryptValue(Aead aead, String key, String payload) {
        try {
            byte[] ciphertext = aead.encrypt(payload.getBytes(StandardCharsets.UTF_8),
                    key.getBytes(StandardCharsets.UTF_8));
            return ENCRYPTED_PREFIX + Base64.encodeToString(ciphertext, Base64.NO_WRAP);
        } catch (GeneralSecurityException ex) {
            Log.w(TAG, "Failed to encrypt value for key " + key, ex);
            return null;
        }
    }

    private static String decryptValue(Aead aead, String key, String stored) {
        if (stored == null || !stored.startsWith(ENCRYPTED_PREFIX)) {
            return stored;
        }
        String payload = stored.substring(ENCRYPTED_PREFIX.length());
        try {
            byte[] plaintext = aead.decrypt(Base64.decode(payload, Base64.NO_WRAP),
                    key.getBytes(StandardCharsets.UTF_8));
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            Log.w(TAG, "Failed to decrypt value for key " + key, ex);
            return null;
        }
    }

    private static final class TinkSharedPreferences implements SharedPreferences {

        private final SharedPreferences delegate;
        private final Aead aead;

        private TinkSharedPreferences(SharedPreferences delegate, Aead aead) {
            this.delegate = delegate;
            this.aead = aead;
        }

        @Override
        public Map<String, ?> getAll() {
            Map<String, ?> raw = delegate.getAll();
            if (raw == null || raw.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, Object> decrypted = new HashMap<>();
            for (Map.Entry<String, ?> entry : raw.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    String decoded = decryptValue(aead, key, (String) value);
                    if (decoded != null) {
                        Object typed = decodeTypedValue(decoded);
                        decrypted.put(key, typed != null ? typed : decoded);
                    }
                } else {
                    decrypted.put(key, value);
                }
            }
            return decrypted;
        }

        @Override
        public String getString(String key, String defValue) {
            Object raw = delegate.getAll().get(key);
            if (raw instanceof String) {
                String decoded = decryptValue(aead, key, (String) raw);
                if (decoded == null) {
                    return defValue;
                }
                if (decoded.startsWith(TYPE_STRING)) {
                    return decoded.substring(TYPE_STRING.length());
                }
                if (decoded.startsWith(TYPE_INT) || decoded.startsWith(TYPE_BOOL)) {
                    return defValue;
                }
                return decoded;
            }
            return defValue;
        }

        @Override
        public java.util.Set<String> getStringSet(String key, java.util.Set<String> defValues) {
            return defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            Object raw = delegate.getAll().get(key);
            if (raw instanceof Integer) {
                return (Integer) raw;
            }
            if (raw instanceof String) {
                String decoded = decryptValue(aead, key, (String) raw);
                if (decoded == null) {
                    return defValue;
                }
                if (decoded.startsWith(TYPE_INT)) {
                    return parseInt(decoded.substring(TYPE_INT.length()), defValue);
                }
                return parseInt(decoded, defValue);
            }
            return defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            Object raw = delegate.getAll().get(key);
            if (raw instanceof Boolean) {
                return (Boolean) raw;
            }
            if (raw instanceof String) {
                String decoded = decryptValue(aead, key, (String) raw);
                if (decoded == null) {
                    return defValue;
                }
                if (decoded.startsWith(TYPE_BOOL)) {
                    return Boolean.parseBoolean(decoded.substring(TYPE_BOOL.length()));
                }
                return Boolean.parseBoolean(decoded);
            }
            return defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            Object raw = delegate.getAll().get(key);
            if (raw instanceof Long) {
                return (Long) raw;
            }
            if (raw instanceof String) {
                String decoded = decryptValue(aead, key, (String) raw);
                return parseLong(decoded, defValue);
            }
            return defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            Object raw = delegate.getAll().get(key);
            if (raw instanceof Float) {
                return (Float) raw;
            }
            if (raw instanceof String) {
                String decoded = decryptValue(aead, key, (String) raw);
                return parseFloat(decoded, defValue);
            }
            return defValue;
        }

        @Override
        public boolean contains(String key) {
            return delegate.contains(key);
        }

        @Override
        public Editor edit() {
            return new TinkEditor(delegate.edit(), aead);
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            delegate.registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            delegate.unregisterOnSharedPreferenceChangeListener(listener);
        }

        private static Object decodeTypedValue(String decoded) {
            if (decoded == null) {
                return null;
            }
            if (decoded.startsWith(TYPE_STRING)) {
                return decoded.substring(TYPE_STRING.length());
            }
            if (decoded.startsWith(TYPE_INT)) {
                return parseInt(decoded.substring(TYPE_INT.length()), 0);
            }
            if (decoded.startsWith(TYPE_BOOL)) {
                return Boolean.parseBoolean(decoded.substring(TYPE_BOOL.length()));
            }
            return decoded;
        }

        private static int parseInt(String value, int defValue) {
            if (value == null) {
                return defValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return defValue;
            }
        }

        private static long parseLong(String value, long defValue) {
            if (value == null) {
                return defValue;
            }
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ex) {
                return defValue;
            }
        }

        private static float parseFloat(String value, float defValue) {
            if (value == null) {
                return defValue;
            }
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException ex) {
                return defValue;
            }
        }
    }

    private static final class TinkEditor implements SharedPreferences.Editor {

        private final SharedPreferences.Editor delegate;
        private final Aead aead;

        private TinkEditor(SharedPreferences.Editor delegate, Aead aead) {
            this.delegate = delegate;
            this.aead = aead;
        }

        @Override
        public SharedPreferences.Editor putString(String key, String value) {
            if (value == null) {
                delegate.remove(key);
                return this;
            }
            String payload = TYPE_STRING + value;
            String encrypted = encryptValue(aead, key, payload);
            if (encrypted != null) {
                delegate.putString(key, encrypted);
            }
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            String payload = TYPE_INT + value;
            String encrypted = encryptValue(aead, key, payload);
            if (encrypted != null) {
                delegate.putString(key, encrypted);
            }
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            String payload = TYPE_BOOL + value;
            String encrypted = encryptValue(aead, key, payload);
            if (encrypted != null) {
                delegate.putString(key, encrypted);
            }
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            String encrypted = encryptValue(aead, key, String.valueOf(value));
            if (encrypted != null) {
                delegate.putString(key, encrypted);
            }
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            String encrypted = encryptValue(aead, key, String.valueOf(value));
            if (encrypted != null) {
                delegate.putString(key, encrypted);
            }
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            delegate.remove(key);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            delegate.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return delegate.commit();
        }

        @Override
        public void apply() {
            delegate.apply();
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, java.util.Set<String> values) {
            throw new UnsupportedOperationException("String sets are not supported");
        }
    }

    private static final class BlockingSharedPreferences implements SharedPreferences {

        @Override
        public Map<String, ?> getAll() {
            return Collections.emptyMap();
        }

        @Override
        public String getString(String key, String defValue) {
            return defValue;
        }

        @Override
        public java.util.Set<String> getStringSet(String key, java.util.Set<String> defValues) {
            return defValues;
        }

        @Override
        public int getInt(String key, int defValue) {
            return defValue;
        }

        @Override
        public long getLong(String key, long defValue) {
            return defValue;
        }

        @Override
        public float getFloat(String key, float defValue) {
            return defValue;
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return defValue;
        }

        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public Editor edit() {
            return new BlockingEditor();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            // No-op
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            // No-op
        }
    }

    private static final class BlockingEditor implements SharedPreferences.Editor {

        @Override
        public SharedPreferences.Editor putString(String key, String value) {
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, java.util.Set<String> values) {
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            return this;
        }

        @Override
        public boolean commit() {
            return false;
        }

        @Override
        public void apply() {
            // No-op
        }
    }
}
