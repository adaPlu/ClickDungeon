package com.adaplu.clickdungeon.model;

/**
 * Broad taxonomy for monsters, used for encounter weighting and rules.
 */
public enum MonsterFamily {
    UNKNOWN,
    BEAST,
    HUMANOID,
    UNDEAD,
    DEMONIC,
    ARCANE,
    CONSTRUCT,
    DRACONIC,
    ELEMENTAL;

    /**
     * Resolves a family from a string, defaulting to UNKNOWN for invalid input.
     */
    public static MonsterFamily fromName(String name) {
        if (name == null || name.isEmpty()) {
            return UNKNOWN;
        }
        for (MonsterFamily family : values()) {
            if (family.name().equalsIgnoreCase(name)) {
                return family;
            }
        }
        return UNKNOWN;
    }
}
