package com.example.clickdungeon.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import com.example.clickdungeon.R;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static SoundPool soundPool;
    private static boolean isInitialized = false;
    private static final Map<String, Integer> soundMap = new HashMap<>();
    private static boolean isMuted = false;

    public static void init(Context context) {
        if (isInitialized) return;

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(12)
                .setAudioAttributes(audioAttributes)
                .build();

        // 🎵 Player class sounds
        soundMap.put("knight_attack", soundPool.load(context, R.raw.knight_attack, 1));
        soundMap.put("knight_defend", soundPool.load(context, R.raw.knight_defend, 1));
        soundMap.put("knight_move", soundPool.load(context, R.raw.knight_move, 1));

        soundMap.put("thief_attack", soundPool.load(context, R.raw.thief_dagger, 1));
        soundMap.put("thief_defend", soundPool.load(context, R.raw.thief_cloak, 1));
        soundMap.put("thief_move", soundPool.load(context, R.raw.thief_steps, 1));

        soundMap.put("wizard_attack", soundPool.load(context, R.raw.wizard_spell_casting, 1));
        soundMap.put("wizard_defend", soundPool.load(context, R.raw.wizard_shield_activation, 1));
        soundMap.put("wizard_move", soundPool.load(context, R.raw.wizard_staff_movement, 1));

        // 🎵 Fallback player sounds
        soundMap.put("player_attack", soundPool.load(context, R.raw.player_attack, 1));
        soundMap.put("player_defend", soundPool.load(context, R.raw.player_defend, 1));
        soundMap.put("player_move", soundPool.load(context, R.raw.player_move, 1));

        // 🎵 Monster sounds
        soundMap.put("slime_attack", soundPool.load(context, R.raw.slime_attack, 1));
        soundMap.put("slime_defend", soundPool.load(context, R.raw.slime_defend, 1));
        soundMap.put("slime_move", soundPool.load(context, R.raw.slime_move, 1));

        soundMap.put("goblin_attack", soundPool.load(context, R.raw.goblin_attack, 1));
        soundMap.put("goblin_defend", soundPool.load(context, R.raw.goblin_defend, 1));
        soundMap.put("goblin_move", soundPool.load(context, R.raw.goblin_move, 1));

        soundMap.put("skeleton_attack", soundPool.load(context, R.raw.skeleton_attack, 1));
        soundMap.put("skeleton_defend", soundPool.load(context, R.raw.skeleton_defend, 1));
        soundMap.put("skeleton_move", soundPool.load(context, R.raw.skeleton_move, 1));

        soundMap.put("orc_attack", soundPool.load(context, R.raw.orc_attack, 1));
        soundMap.put("orc_defend", soundPool.load(context, R.raw.orc_defend, 1));
        soundMap.put("orc_move", soundPool.load(context, R.raw.orc_move, 1));

        soundMap.put("troll_attack", soundPool.load(context, R.raw.troll_attack, 1));
        soundMap.put("troll_defend", soundPool.load(context, R.raw.troll_defend, 1));
        soundMap.put("troll_move", soundPool.load(context, R.raw.troll_move, 1));

        soundMap.put("witch_attack", soundPool.load(context, R.raw.witch_attack, 1));
        soundMap.put("witch_defend", soundPool.load(context, R.raw.witch_defend, 1));
        soundMap.put("witch_move", soundPool.load(context, R.raw.witch_move, 1));

        soundMap.put("demon_attack", soundPool.load(context, R.raw.demon_attack, 1));
        soundMap.put("demon_defend", soundPool.load(context, R.raw.demon_defend, 1));
        soundMap.put("demon_move", soundPool.load(context, R.raw.demon_move, 1));

        soundMap.put("dragon_attack", soundPool.load(context, R.raw.dragon_attack, 1));
        soundMap.put("dragon_defend", soundPool.load(context, R.raw.dragon_defend, 1));
        soundMap.put("dragon_move", soundPool.load(context, R.raw.dragon_move, 1));

        isInitialized = true;
    }

    public static void play(String key) {
        if (!isInitialized) return;

        Integer soundId = soundMap.get(key);

        // Fallback logic for missing keys
        if (soundId == null && key.contains("_")) {
            String fallbackKey = "player_" + key.split("_")[1];
            soundId = soundMap.get(fallbackKey);
        }

        if (soundId != null) {
            soundPool.play(soundId, isMuted ? 0 : 1, isMuted ? 0 : 1, 1, 0, 1);
        }
    }

    public static void playForMonster(String monsterType, String action) {
        String key = monsterType.toLowerCase() + "_" + action.toLowerCase();
        play(key);
    }

    public static void playForClass(String playerClassName, String action) {
        String key = playerClassName.toLowerCase() + "_" + action.toLowerCase();
        play(key);
    }

    public static void setMuted(boolean muted) {
        isMuted = muted;
    }

    public static boolean isMuted() {
        return isMuted;
    }

    public static void toggleMute() {
        isMuted = !isMuted;
    }

    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            isInitialized = false;
            soundMap.clear();
        }
    }
}
