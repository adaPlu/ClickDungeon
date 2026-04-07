package com.adaplu.clickdungeon.model;

/**
 * Enumerates supported terrain themes for encounter effects and flavor.
 */
public enum TerrainType {
    CAVERN("Cavern"),
    CRYPT("Crypt"),
    LAVA_FIELD("Lava Field"),
    MIRE("Mire"),
    FROZEN_RUINS("Frozen Ruins"),
    THORN_WILDS("Thorn Wilds"),
    STORM_PLATEAU("Storm Plateau"),
    ARCANE_NEXUS("Arcane Nexus"),
    SUNKEN_TEMPLE("Sunken Temple"),
    ASH_WASTES("Ash Wastes");

    /** Human-readable label for UI display. */
    private final String displayName;

    TerrainType(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the display name used in UI. */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a terrain type from enum name or display name.
     */
    public static TerrainType fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (TerrainType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
