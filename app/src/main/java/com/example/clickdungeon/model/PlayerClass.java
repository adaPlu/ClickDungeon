package com.example.clickdungeon.model;

public enum PlayerClass {
    KNIGHT(2, 2, 4, 1, false,
            "Shield Wall",
            "Deploy a shield that absorbs damage while you hold position."),
    THIEF(1, 4, 2, 1, false,
            "Trap Scan",
            "Reveal nearby traps within range."),
    WIZARD(1, 1, 1, 3, true,
            "Fireball",
            "Launch a ranged attack at a target tile.");

    public static final String ABILITY_KNIGHT_SHIELD_WALL = "Shield Wall";
    public static final String ABILITY_KNIGHT_TAUNT = "Taunt";
    public static final String ABILITY_KNIGHT_FORTIFY = "Fortify";
    public static final String ABILITY_KNIGHT_VALIANT_STRIKE = "Valiant Strike";
    public static final String ABILITY_KNIGHT_GUARDIANS_OATH = "Guardian's Oath";

    public static final String ABILITY_THIEF_TRAP_SCAN = "Trap Scan";
    public static final String ABILITY_THIEF_SHADOWSTEP = "Shadowstep";
    public static final String ABILITY_THIEF_DISARM_EXPERT = "Disarm Expert";
    public static final String ABILITY_THIEF_AMBUSH = "Ambush";
    public static final String ABILITY_THIEF_VEIL_OF_SMOKE = "Veil of Smoke";

    public static final String ABILITY_WIZARD_FIREBALL = "Fireball";
    public static final String ABILITY_WIZARD_FROST_NOVA = "Frost Nova";
    public static final String ABILITY_WIZARD_CHAIN_LIGHTNING = "Chain Lightning";
    public static final String ABILITY_WIZARD_ARCANE_SHIELD = "Arcane Shield";
    public static final String ABILITY_WIZARD_METEOR = "Meteor";

    private final int baseStrength;
    private final int baseDexterity;
    private final int baseConstitution;
    private final int baseIntelligence;
    private final boolean usesMp;
    private final String baseAbilityName;
    private final String baseAbilityDescription;
    private final AbilityDefinition[] levelAbilities;

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

    public int getBaseStrength() {
        return baseStrength;
    }

    public int getBaseDexterity() {
        return baseDexterity;
    }

    public int getBaseConstitution() {
        return baseConstitution;
    }

    public int getBaseIntelligence() {
        return baseIntelligence;
    }

    public boolean usesMp() {
        return usesMp;
    }

    public String getBaseAbilityName() {
        return baseAbilityName;
    }

    public String getBaseAbilityDescription() {
        return baseAbilityDescription;
    }

    public AbilityDefinition[] getAbilityProgression() {
        return levelAbilities.clone();
    }

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

    public static final class AbilityDefinition {
        private final int unlockLevel;
        private final String name;
        private final String description;

        AbilityDefinition(int unlockLevel, String name, String description) {
            this.unlockLevel = unlockLevel;
            this.name = name;
            this.description = description;
        }

        public int getUnlockLevel() {
            return unlockLevel;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
