package com.example.clickdungeon.model;

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
