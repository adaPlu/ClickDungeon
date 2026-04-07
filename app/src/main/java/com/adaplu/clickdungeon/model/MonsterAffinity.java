package com.adaplu.clickdungeon.model;

/**
 * Elemental or thematic affinity for monsters, used by terrain/status logic.
 */
public enum MonsterAffinity {
    NONE,
    FIRE,
    ICE,
    LIGHTNING,
    ARCANE,
    POISON,
    SHADOW;

    /**
     * Resolves an affinity from a string, defaulting to NONE for invalid input.
     */
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
