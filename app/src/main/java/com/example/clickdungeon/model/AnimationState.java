package com.example.clickdungeon.model;

public enum AnimationState {
    IDLE("idle", 160, 0),
    MOVE("move", 120, 700),
    ATTACK("attack", 90, 600),
    DEFEND("defend", 120, 600),
    // TODO: Implement extended states in AnimatedPlayer/AnimatedMonster and sprite sheets
    // See: docs/ANIMATION_ARCHITECTURE.md for detailed implementation guide
    HIT("hit", 90, 380),
    DEFEAT("defeat", 130, 900),
    CAST("cast", 100, 650),
    LOOT("loot", 110, 550),
    LEVEL_UP("level_up", 100, 700);

    private final String key;
    private final long frameDurationMs;
    private final long autoResetMs;

    AnimationState(String key, long frameDurationMs, long autoResetMs) {
        this.key = key;
        this.frameDurationMs = frameDurationMs;
        this.autoResetMs = autoResetMs;
    }

    public String key() {
        return key;
    }

    public long frameDurationMs() {
        return frameDurationMs;
    }

    public long autoResetMs() {
        return autoResetMs;
    }

    public static AnimationState fromKey(String action) {
        if (action == null || action.trim().isEmpty()) {
            return IDLE;
        }
        for (AnimationState state : values()) {
            if (state.key.equalsIgnoreCase(action.trim())) {
                return state;
            }
        }
        return IDLE;
    }
}
