package com.example.clickdungeon.model;

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

    private final String displayName;

    TerrainType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

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
