package com.example.clickdungeon.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.example.clickdungeon.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class SoundManager {

    private static final String TAG = "SoundManager";
    /** Sound effect keys used across the UI. */
    public static final String KEY_EFFECT_TREASURE = "effect_treasure";
    public static final String KEY_EFFECT_POSITIVE = "effect_positive";
    public static final String KEY_EFFECT_TRAP = "effect_trap";
    public static final String KEY_EFFECT_VICTORY = "effect_victory";
    public static final String KEY_EFFECT_DEFEAT = "effect_defeat";
    public static final String KEY_EFFECT_KEY_PICKUP = "effect_key_pickup";
    public static final String KEY_EFFECT_LEVEL_UP = "effect_level_up";
    public static final String KEY_EFFECT_INVENTORY = "effect_inventory";
    public static final String KEY_EFFECT_SHOP_PURCHASE = "effect_shop_purchase";
    public static final String KEY_EFFECT_CHEST = "effect_chest";
    public static final String KEY_EFFECT_EQUIP = "effect_equip";

    /** Shared SoundPool instance for effect playback. */
    private static SoundPool soundPool;
    /** Tracks initialization state to avoid reloading sounds. */
    private static boolean isInitialized = false;
    /** Mapping of sound key to SoundPool sound ID. */
    private static final Map<String, Integer> soundMap = new HashMap<>();
    /** Tracks missing sound keys for diagnostics. */
    private static final java.util.List<String> missingKeys = new java.util.ArrayList<>();
    private static final int MAX_MISSING_KEYS = 50;
    /** True when audio output is muted. */
    private static boolean isMuted = false;
    /** Last key requested for playback (test/debug). */
    private static String lastPlayedKey;
    @Nullable
    private static PlaybackListener testPlaybackListener;

    private SoundManager() {
        // No instances.
    }

    /**
     * Initializes the SoundPool and registers all sound assets.
     */
    public static void init(Context context) {
        if (isInitialized) {
            return;
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(12)
                .setAudioAttributes(audioAttributes)
                .build();

        // Player class sounds
        register(context, "knight_attack", R.raw.knight_attack);
        register(context, "knight_defend", R.raw.knight_defend);
        register(context, "knight_move", R.raw.knight_move);

        register(context, "thief_attack", R.raw.thief_dagger);
        register(context, "thief_defend", R.raw.thief_cloak);
        register(context, "thief_move", R.raw.thief_steps);

        register(context, "wizard_attack", R.raw.wizard_spell_casting);
        register(context, "wizard_defend", R.raw.wizard_shield_activation);
        register(context, "wizard_move", R.raw.wizard_staff_movement);

        register(context, "player_attack", R.raw.player_attack);
        register(context, "player_defend", R.raw.player_defend);
        register(context, "player_move", R.raw.player_move);

        // Monster sounds
        register(context, "slime_attack", R.raw.slime_attack);
        register(context, "slime_defend", R.raw.slime_defend);
        register(context, "slime_move", R.raw.slime_move);

        register(context, "goblin_attack", R.raw.goblin_attack);
        register(context, "goblin_defend", R.raw.goblin_defend);
        register(context, "goblin_move", R.raw.goblin_move);

        register(context, "skeleton_attack", R.raw.skeleton_attack);
        register(context, "skeleton_defend", R.raw.skeleton_defend);
        register(context, "skeleton_move", R.raw.skeleton_move);

        register(context, "orc_attack", R.raw.orc_attack);
        register(context, "orc_defend", R.raw.orc_defend);
        register(context, "orc_move", R.raw.orc_move);

        register(context, "troll_attack", R.raw.troll_attack);
        register(context, "troll_defend", R.raw.troll_defend);
        register(context, "troll_move", R.raw.troll_move);

        register(context, "witch_attack", R.raw.witch_attack);
        register(context, "witch_defend", R.raw.witch_defend);
        register(context, "witch_move", R.raw.witch_move);

        register(context, "demon_attack", R.raw.demon_attack);
        register(context, "demon_defend", R.raw.demon_defend);
        register(context, "demon_move", R.raw.demon_move);

        register(context, "dragon_attack", R.raw.dragon_attack);
        register(context, "dragon_defend", R.raw.dragon_defend);
        register(context, "dragon_move", R.raw.dragon_move);

        // General effects
        register(context, KEY_EFFECT_TREASURE, R.raw.tile_reveal);
        register(context, KEY_EFFECT_POSITIVE, R.raw.player_move);
        register(context, KEY_EFFECT_TRAP, R.raw.slime_attack);
        register(context, KEY_EFFECT_VICTORY, R.raw.knight_defend);
        register(context, KEY_EFFECT_DEFEAT, R.raw.demon_attack);
        register(context, KEY_EFFECT_KEY_PICKUP, R.raw.player_move);
        register(context, KEY_EFFECT_LEVEL_UP, R.raw.wizard_spell_casting);
        register(context, KEY_EFFECT_INVENTORY, R.raw.player_defend);
        register(context, KEY_EFFECT_SHOP_PURCHASE, R.raw.knight_attack);
        register(context, KEY_EFFECT_CHEST, R.raw.tile_reveal);
        register(context, KEY_EFFECT_EQUIP, R.raw.player_defend);

        isInitialized = true;
        syncMuteFromSettings(context);
    }

    /**
     * Registers a sound resource to a logical key.
     */
    private static void register(Context context, String key, int resId) {
        try {
            int soundId = soundPool.load(context, resId, 1);
            soundMap.put(key, soundId);
        } catch (RuntimeException ex) {
            Log.w(TAG, "Failed to register sound for key=" + key + " (resource " + resId + ")", ex);
        }
    }

    /** Plays a sound by key when available. */
    public static void play(String key) {
        playAndReport(key);
    }

    /**
     * Attempts to play a sound and reports success. Always notifies the test listener so tests can
     * assert requested keys even if the pool is muted or uninitialised.
     *
     * @return true if a sound ID was found and play() was issued, false otherwise.
     */
    public static boolean playAndReport(String key) {
        notifyTestListener(key);
        if (key == null || key.isEmpty()) {
            return false;
        }
        Integer soundId = resolveSoundId(key);
        if (soundId == null) {
            Log.w(TAG, "Sound key not registered: " + key);
            recordMissingKey(key);
            return false;
        }
        if (!isInitialized || soundPool == null || isMuted) {
            return false;
        }
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
        lastPlayedKey = key;
        return true;
    }

    /** Plays a general effect sound. */
    public static void playEffect(String key) {
        playAndReport(key);
    }

    /** Plays a monster action sound (attack/move/defend). */
    public static void playForMonster(String monsterType, String action) {
        if (monsterType == null || action == null) {
            return;
        }
        String key = monsterType.toLowerCase(Locale.ROOT) + "_" + action.toLowerCase(Locale.ROOT);
        play(key);
    }

    /** Plays a player class action sound (attack/move/defend). */
    public static void playForClass(String playerClassName, String action) {
        if (playerClassName == null || action == null) {
            return;
        }
        String key = playerClassName.toLowerCase(Locale.ROOT) + "_" + action.toLowerCase(Locale.ROOT);
        play(key);
    }

    /** Syncs mute state based on SettingsManager. */
    public static void syncMuteFromSettings(Context context) {
        if (context == null) {
            return;
        }
        setMuted(!SettingsManager.isAudioEnabled(context.getApplicationContext()));
    }

    /** Sets the muted state and pauses/resumes the pool accordingly. */
    public static void setMuted(boolean muted) {
        isMuted = muted;
        if (muted) {
            pauseAll();
        } else {
            resumeAll();
        }
    }

    /** Returns true if audio is currently muted. */
    public static boolean isMuted() {
        return isMuted;
    }

    /** Toggles the muted state. */
    public static void toggleMute() {
        setMuted(!isMuted);
    }

    /** Pauses all currently playing sounds. */
    public static void pauseAll() {
        if (soundPool != null) {
            soundPool.autoPause();
        }
    }

    /** Resumes playback if not muted. */
    public static void resumeAll() {
        if (soundPool != null && !isMuted) {
            soundPool.autoResume();
        }
    }

    /** Releases SoundPool resources and clears cached state. */
    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        isInitialized = false;
        soundMap.clear();
        lastPlayedKey = null;
        missingKeys.clear();
    }

    /** Returns the last playback key requested (testing). */
    @VisibleForTesting
    public static String getLastPlayedKey() {
        return lastPlayedKey;
    }

    /** Returns registered sound keys (testing). */
    @VisibleForTesting
    public static java.util.Set<String> getRegisteredKeys() {
        return new java.util.HashSet<>(soundMap.keySet());
    }

    /** Returns recent missing sound keys for diagnostics. */
    public static java.util.List<String> getMissingKeys() {
        return new java.util.ArrayList<>(missingKeys);
    }

    /** Listener used by tests to observe playback requests. */
    @VisibleForTesting
    public interface PlaybackListener {
        void onPlayRequest(String key);
    }

    /** Registers a playback listener for tests. */
    @VisibleForTesting
    public static void setTestPlaybackListener(@Nullable PlaybackListener listener) {
        testPlaybackListener = listener;
    }

    /** Notifies the test listener of a playback request. */
    private static void notifyTestListener(@Nullable String key) {
        if (testPlaybackListener != null && key != null && !key.isEmpty()) {
            testPlaybackListener.onPlayRequest(key);
        }
    }

    /**
     * Resolves a sound ID for the key, falling back to generic player sounds.
     */
    @Nullable
    private static Integer resolveSoundId(@Nullable String key) {
        if (key == null) {
            return null;
        }
        Integer soundId = soundMap.get(key);
        if (soundId != null) {
            return soundId;
        }
        if (key.contains("_")) {
            String[] parts = key.split("_", 2);
            if (parts.length == 2) {
                // Fall back to generic player sounds for missing class/monster variants.
                String fallbackKey = "player_" + parts[1];
                soundId = soundMap.get(fallbackKey);
                if (soundId != null) {
                    Log.d(TAG, "Falling back to generic player sound for key=" + key);
                }
            }
        }
        return soundId;
    }

    private static void recordMissingKey(@Nullable String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (missingKeys.contains(key)) {
            return;
        }
        missingKeys.add(0, key);
        while (missingKeys.size() > MAX_MISSING_KEYS) {
            missingKeys.remove(missingKeys.size() - 1);
        }
    }
}
