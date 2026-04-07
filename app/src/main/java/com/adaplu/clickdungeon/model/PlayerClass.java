package com.adaplu.clickdungeon.model;

/**
 * Defines player classes, base stats, and ability progression rules.
 */
public enum PlayerClass {
    KNIGHT(2, 2, 4, 1, false,
            "Shield Wall",
            "Deploy a shield that absorbs damage while you hold position."),
    RANGER(1, 3, 2, 1, false,
            "Piercing Shot",
            "Fire a ranged shot that bypasses enemy defenses."),
    THIEF(1, 4, 2, 1, false,
            "Trap Scan",
            "Reveal nearby traps within range."),
    WIZARD(1, 1, 1, 3, true,
            "Fireball",
            "Launch a ranged attack at a target tile.");

    /** Knight ability identifiers. */
    public static final String ABILITY_KNIGHT_SHIELD_WALL = "Shield Wall";
    public static final String ABILITY_KNIGHT_TAUNT = "Taunt";
    public static final String ABILITY_KNIGHT_FORTIFY = "Fortify";
    public static final String ABILITY_KNIGHT_VALIANT_STRIKE = "Valiant Strike";
    public static final String ABILITY_KNIGHT_GUARDIANS_OATH = "Guardian's Oath";

    /** Thief ability identifiers. */
    public static final String ABILITY_THIEF_TRAP_SCAN = "Trap Scan";
    public static final String ABILITY_THIEF_SHADOWSTEP = "Shadowstep";
    public static final String ABILITY_THIEF_DISARM_EXPERT = "Disarm Expert";
    public static final String ABILITY_THIEF_AMBUSH = "Ambush";
    public static final String ABILITY_THIEF_VEIL_OF_SMOKE = "Veil of Smoke";

    /** Wizard ability identifiers. */
    public static final String ABILITY_WIZARD_FIREBALL = "Fireball";
    public static final String ABILITY_WIZARD_FROST_NOVA = "Frost Nova";
    public static final String ABILITY_WIZARD_CHAIN_LIGHTNING = "Chain Lightning";
    public static final String ABILITY_WIZARD_ARCANE_SHIELD = "Arcane Shield";
    public static final String ABILITY_WIZARD_METEOR = "Meteor";

    /** Base STR for the class at level 1. */
    private final int baseStrength;
    /** Base DEX for the class at level 1. */
    private final int baseDexterity;
    /** Base CON for the class at level 1. */
    private final int baseConstitution;
    /** Base INT for the class at level 1. */
    private final int baseIntelligence;
    /** Whether the class uses MP for abilities. */
    private final boolean usesMp;
    /** Name of the starting ability. */
    private final String baseAbilityName;
    /** Description of the starting ability. */
    private final String baseAbilityDescription;
    /** Ordered list of ability unlocks by level. */
    private final AbilityDefinition[] levelAbilities;

    /**
     * Creates a class definition with base stats and initial ability metadata.
     */
    PlayerClass(int baseStrength,
                int baseDexterity,
                int baseConstitution,
                int baseIntelligence,
                boolean usesMp,
                String baseAbilityName,
                String baseAbilityDescription) {
        this.baseStrength = baseStrength;
        this.baseDexterity = baseDexterity;
        this.baseConstitution = baseConstitution;
        this.baseIntelligence = baseIntelligence;
        this.usesMp = usesMp;
        this.baseAbilityName = baseAbilityName;
        this.baseAbilityDescription = baseAbilityDescription;
        this.levelAbilities = buildAbilityProgression(this.name());
    }

    /** Returns base STR for this class. */
    public int getBaseStrength() {
        return baseStrength;
    }

    /** Returns base DEX for this class. */
    public int getBaseDexterity() {
        return baseDexterity;
    }

    /** Returns base CON for this class. */
    public int getBaseConstitution() {
        return baseConstitution;
    }

    /** Returns base INT for this class. */
    public int getBaseIntelligence() {
        return baseIntelligence;
    }

    /** Returns whether the class uses MP. */
    public boolean usesMp() {
        return usesMp;
    }

    /** Returns the name of the starting ability. */
    public String getBaseAbilityName() {
        return baseAbilityName;
    }

    /** Returns the description for the starting ability. */
    public String getBaseAbilityDescription() {
        return baseAbilityDescription;
    }

    /** Returns the full ability progression list. */
    public AbilityDefinition[] getAbilityProgression() {
        return levelAbilities.clone();
    }

    /** Returns the subset of abilities unlocked at the given level. */
    public AbilityDefinition[] getAbilitiesUpToLevel(int level) {
        int count = 0;
        for (AbilityDefinition ability : levelAbilities) {
            if (ability.unlockLevel <= level) {
                count++;
            }
        }
        AbilityDefinition[] unlocked = new AbilityDefinition[count];
        int index = 0;
        for (AbilityDefinition ability : levelAbilities) {
            if (ability.unlockLevel <= level) {
                unlocked[index++] = ability;
            }
        }
        return unlocked;
    }

    /**
     * Builds the ordered ability unlock list for the class name.
     */
    private static AbilityDefinition[] buildAbilityProgression(String className) {
        if ("KNIGHT".equals(className)) {
                return new AbilityDefinition[] {
                        new AbilityDefinition(1, ABILITY_KNIGHT_SHIELD_WALL,
                                "Deploy a shield that absorbs damage while you hold position."),
                        new AbilityDefinition(5, ABILITY_KNIGHT_TAUNT,
                                "Force a nearby enemy to engage you."),
                        new AbilityDefinition(10, ABILITY_KNIGHT_FORTIFY,
                                "Cleanse status effects and restore some health."),
                        new AbilityDefinition(15, ABILITY_KNIGHT_VALIANT_STRIKE,
                                "Deliver a crushing blow to a target enemy."),
                        new AbilityDefinition(20, ABILITY_KNIGHT_GUARDIANS_OATH,
                                "Channel a protective vow that reinforces your defenses.")
                };
        }
        if ("RANGER".equals(className)) {
                return new AbilityDefinition[] {
                        new AbilityDefinition(1, "Piercing Shot",
                                "Fire a ranged shot that bypasses enemy defenses."),
                        new AbilityDefinition(5, "Rapid Volley",
                                "Shoot multiple arrows quickly at nearby enemies."),
                        new AbilityDefinition(10, "Camouflage",
                                "Increase evasion and reduce aggro from enemies."),
                        new AbilityDefinition(15, "Net Trap",
                                "Slow down an enemy and reduce its damage output."),
                        new AbilityDefinition(20, "Eagle Eye",
                                "Critically hit due to enhanced accuracy.")
                };
        }
        if ("THIEF".equals(className)) {
                return new AbilityDefinition[] {
                        new AbilityDefinition(1, ABILITY_THIEF_TRAP_SCAN,
                                "Reveal traps within range."),
                        new AbilityDefinition(5, ABILITY_THIEF_SHADOWSTEP,
                                "Blink to a nearby safe tile."),
                        new AbilityDefinition(10, ABILITY_THIEF_DISARM_EXPERT,
                                "Clear traps in a small area, consuming a kit."),
                        new AbilityDefinition(15, ABILITY_THIEF_AMBUSH,
                                "Strike first when engaging an enemy."),
                        new AbilityDefinition(20, ABILITY_THIEF_VEIL_OF_SMOKE,
                                "Reveal nearby tiles and evade the next trap.")
                };
        }
        return new AbilityDefinition[] {
                new AbilityDefinition(1, ABILITY_WIZARD_FIREBALL,
                        "Blast a target tile with arcane fire."),
                new AbilityDefinition(5, ABILITY_WIZARD_FROST_NOVA,
                        "Reveal and chill threats in a small area."),
                new AbilityDefinition(10, ABILITY_WIZARD_CHAIN_LIGHTNING,
                        "Zap enemies that cluster together."),
                new AbilityDefinition(15, ABILITY_WIZARD_ARCANE_SHIELD,
                        "Restore mana and stabilize your defenses."),
                new AbilityDefinition(20, ABILITY_WIZARD_METEOR,
                        "Call down a devastating strike.")
        };
    }

    /**
     * Immutable descriptor for a class ability unlock milestone.
     */
    public static final class AbilityDefinition {
        private final int unlockLevel;
        private final String name;
        private final String description;

        AbilityDefinition(int unlockLevel, String name, String description) {
            this.unlockLevel = unlockLevel;
            this.name = name;
            this.description = description;
        }

        /** Returns the level at which this ability unlocks. */
        public int getUnlockLevel() {
            return unlockLevel;
        }

        /** Returns the ability display name. */
        public String getName() {
            return name;
        }

        /** Returns the ability description text. */
        public String getDescription() {
            return description;
        }
    }
}
