package com.example.clickdungeon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Provides encrypted SharedPreferences when available, falling back to plain prefs if needed.
 */
public final class SecurePreferences {

    private static final String TAG = "SecurePreferences";
    private static volatile MasterKey masterKey;

    private SecurePreferences() {
    }

    public static SharedPreferences get(Context context, String name) {
        if (!PersistenceSecurityConfig.USE_ENCRYPTED_PREFS) {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
        try {
            return getEncryptedPreferences(context, name);
        } catch (GeneralSecurityException | IOException ex) {
            Log.w(TAG, "Falling back to plain prefs for " + name, ex);
            return context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }
    }

    private static SharedPreferences getEncryptedPreferences(Context context, String name)
            throws GeneralSecurityException, IOException {
        MasterKey key = getMasterKey(context);
        return EncryptedSharedPreferences.create(
                context,
                name,
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    private static MasterKey getMasterKey(Context context) throws GeneralSecurityException, IOException {
        if (masterKey == null) {
            synchronized (SecurePreferences.class) {
                if (masterKey == null) {
                    masterKey = new MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build();
                }
            }
        }
        return masterKey;
    }
}
