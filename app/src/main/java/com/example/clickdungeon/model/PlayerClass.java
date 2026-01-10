package com.example.clickdungeon.model;

public enum PlayerClass {
    KNIGHT(2, 2, 4, 1, false,
            "Shield Wall",
            "Brace to reduce incoming damage on the next hit."),
    THIEF(1, 4, 2, 1, false,
            "Quickstep",
            "Dodge the next trap or attack and reposition."),
    WIZARD(1, 1, 1, 3, true,
            "Arcane Bolt",
            "Spend mana to blast a single foe at range.");

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
                        new AbilityDefinition(1, "Shield Wall",
                                "Brace to reduce incoming damage on the next hit."),
                        new AbilityDefinition(5, "Fortify",
                                "Temporarily raise defense and ignore minor hits."),
                        new AbilityDefinition(10, "Guard Break",
                                "Deliver a heavy strike that weakens enemy defenses."),
                        new AbilityDefinition(15, "Bulwark",
                                "Gain a damage shield that persists for several turns."),
                        new AbilityDefinition(20, "Last Stand",
                                "Survive lethal damage once and retaliate.")
                };
        }
        if ("THIEF".equals(className)) {
                return new AbilityDefinition[] {
                        new AbilityDefinition(1, "Quickstep",
                                "Dodge the next trap or attack and reposition."),
                        new AbilityDefinition(5, "Smoke Veil",
                                "Slip past danger and reset enemy intent."),
                        new AbilityDefinition(10, "Poisoned Edge",
                                "Attacks apply a lingering damage effect."),
                        new AbilityDefinition(15, "Shadowstep",
                                "Blink to a revealed tile and strike first."),
                        new AbilityDefinition(20, "Fatal Gambit",
                                "Massive damage at the cost of temporary risk.")
                };
        }
        return new AbilityDefinition[] {
                new AbilityDefinition(1, "Arcane Bolt",
                        "Spend mana to blast a single foe at range."),
                new AbilityDefinition(5, "Frost Nova",
                        "Freeze nearby threats and buy time."),
                new AbilityDefinition(10, "Chain Lightning",
                        "Zap multiple enemies in sequence."),
                new AbilityDefinition(15, "Arcane Shield",
                        "Convert mana into a protective ward."),
                new AbilityDefinition(20, "Meteor",
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
