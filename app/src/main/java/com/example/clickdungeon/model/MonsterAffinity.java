package com.example.clickdungeon.model;

public enum MonsterAffinity {
    NONE,
    FIRE,
    ICE,
    LIGHTNING,
    ARCANE,
    POISON,
    SHADOW;

    public static MonsterAffinity fromName(String name) {
        if (name == null || name.isEmpty()) {
            return NONE;
        }
        for (MonsterAffinity affinity : values()) {
            if (affinity.name().equalsIgnoreCase(name)) {
                return affinity;
            }
        }
        return NONE;
    }
}
