package com.adaplu.clickdungeon.model;

/**
 * Defines player classes, their starting HP, and their ability unlock/recharge rules.
 */
public enum PlayerClass {
    KNIGHT(18, buildKnightAbilities()),
    RANGER(14, buildRangerAbilities()),
    THIEF(13, buildThiefAbilities()),
    WIZARD(12, buildWizardAbilities());

    public static final String ABILITY_KNIGHT_SHIELD_WALL = "Shield Wall";
    public static final String ABILITY_KNIGHT_TAUNT = "Taunt";
    public static final String ABILITY_KNIGHT_FORTIFY = "Fortify";
    public static final String ABILITY_KNIGHT_VALIANT_STRIKE = "Valiant Strike";
    public static final String ABILITY_KNIGHT_GUARDIANS_OATH = "Guardian's Oath";

    public static final String ABILITY_RANGER_PIERCING_SHOT = "Piercing Shot";
    public static final String ABILITY_RANGER_RAPID_VOLLEY = "Rapid Volley";
    public static final String ABILITY_RANGER_CAMOUFLAGE = "Camouflage";
    public static final String ABILITY_RANGER_NET_TRAP = "Net Trap";
    public static final String ABILITY_RANGER_EAGLE_EYE = "Eagle Eye";

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

    private final int startingHealth;
    private final AbilityDefinition[] abilities;

    PlayerClass(int startingHealth, AbilityDefinition[] abilities) {
        this.startingHealth = startingHealth;
        this.abilities = abilities;
    }

    public int getStartingHealth() {
        return startingHealth;
    }

    public int getBaseStrength() {
        switch (this) {
            case KNIGHT: return 2;
            case RANGER: return 1;
            case THIEF: return 1;
            case WIZARD:
            default: return 1;
        }
    }

    public int getBaseDexterity() {
        switch (this) {
            case KNIGHT: return 2;
            case RANGER: return 3;
            case THIEF: return 4;
            case WIZARD:
            default: return 1;
        }
    }

    public int getBaseConstitution() {
        switch (this) {
            case KNIGHT: return 4;
            case RANGER: return 2;
            case THIEF: return 2;
            case WIZARD:
            default: return 1;
        }
    }

    public int getBaseIntelligence() {
        switch (this) {
            case KNIGHT: return 1;
            case RANGER: return 1;
            case THIEF: return 1;
            case WIZARD:
            default: return 3;
        }
    }

    public String getBaseAbilityName() {
        return getStarterAbility().getName();
    }

    public String getBaseAbilityDescription() {
        return getStarterAbility().getDescription();
    }

    public AbilityDefinition getStarterAbility() {
        return abilities[0];
    }

    public AbilityDefinition[] getAbilityProgression() {
        return abilities.clone();
    }

    public AbilityDefinition[] getAbilitiesUpToLevel(int level) {
        AbilityDefinition[] unlocked = new AbilityDefinition[Math.max(1, Math.min(abilities.length, level >= 20 ? 5 : level >= 15 ? 4 : level >= 10 ? 3 : level >= 5 ? 2 : 1))];
        System.arraycopy(abilities, 0, unlocked, 0, unlocked.length);
        return unlocked;
    }

    public AbilityDefinition findAbility(String abilityName) {
        if (abilityName == null) {
            return null;
        }
        for (AbilityDefinition ability : abilities) {
            if (ability.getName().equals(abilityName)) {
                return ability;
            }
        }
        return null;
    }

    public boolean usesMp() {
        return false;
    }

    private static AbilityDefinition[] buildKnightAbilities() {
        return new AbilityDefinition[] {
                new AbilityDefinition(0, ABILITY_KNIGHT_SHIELD_WALL,
                        "Deploy a shield that absorbs damage while you hold position.", 35000L),
                new AbilityDefinition(24, ABILITY_KNIGHT_TAUNT,
                        "Force a nearby enemy to engage you.", 45000L),
                new AbilityDefinition(56, ABILITY_KNIGHT_FORTIFY,
                        "Cleanse status effects and restore health.", 65000L),
                new AbilityDefinition(96, ABILITY_KNIGHT_VALIANT_STRIKE,
                        "Deliver a crushing blow to a target enemy.", 80000L),
                new AbilityDefinition(150, ABILITY_KNIGHT_GUARDIANS_OATH,
                        "Channel a vow that reinforces your defenses.", 95000L)
        };
    }

    private static AbilityDefinition[] buildRangerAbilities() {
        return new AbilityDefinition[] {
                new AbilityDefinition(0, ABILITY_RANGER_PIERCING_SHOT,
                        "Fire a ranged shot that bypasses enemy defenses.", 30000L),
                new AbilityDefinition(24, ABILITY_RANGER_RAPID_VOLLEY,
                        "Shoot multiple arrows quickly at nearby enemies.", 45000L),
                new AbilityDefinition(56, ABILITY_RANGER_CAMOUFLAGE,
                        "Recover health and scout the space around you.", 60000L),
                new AbilityDefinition(96, ABILITY_RANGER_NET_TRAP,
                        "Snare an enemy and weaken it.", 75000L),
                new AbilityDefinition(150, ABILITY_RANGER_EAGLE_EYE,
                        "Critically hit with enhanced accuracy.", 90000L)
        };
    }

    private static AbilityDefinition[] buildThiefAbilities() {
        return new AbilityDefinition[] {
                new AbilityDefinition(0, ABILITY_THIEF_TRAP_SCAN,
                        "Reveal traps within range.", 25000L),
                new AbilityDefinition(24, ABILITY_THIEF_SHADOWSTEP,
                        "Blink to a nearby safe tile.", 40000L),
                new AbilityDefinition(56, ABILITY_THIEF_DISARM_EXPERT,
                        "Clear traps in a small area, consuming a kit if present.", 55000L),
                new AbilityDefinition(96, ABILITY_THIEF_AMBUSH,
                        "Strike first when engaging an enemy.", 70000L),
                new AbilityDefinition(150, ABILITY_THIEF_VEIL_OF_SMOKE,
                        "Reveal nearby tiles and evade the next trap.", 85000L)
        };
    }

    private static AbilityDefinition[] buildWizardAbilities() {
        return new AbilityDefinition[] {
                new AbilityDefinition(0, ABILITY_WIZARD_FIREBALL,
                        "Blast a target tile with arcane fire.", 30000L),
                new AbilityDefinition(24, ABILITY_WIZARD_FROST_NOVA,
                        "Reveal and chill threats in a small area.", 45000L),
                new AbilityDefinition(56, ABILITY_WIZARD_CHAIN_LIGHTNING,
                        "Zap enemies that cluster together.", 60000L),
                new AbilityDefinition(96, ABILITY_WIZARD_ARCANE_SHIELD,
                        "Stabilize yourself and recover health.", 75000L),
                new AbilityDefinition(150, ABILITY_WIZARD_METEOR,
                        "Call down a devastating strike.", 100000L)
        };
    }

    public static final class AbilityDefinition {
        private final int unlockXpCost;
        private final String name;
        private final String description;
        private final long rechargeDurationMillis;

        AbilityDefinition(int unlockXpCost,
                          String name,
                          String description,
                          long rechargeDurationMillis) {
            this.unlockXpCost = unlockXpCost;
            this.name = name;
            this.description = description;
            this.rechargeDurationMillis = rechargeDurationMillis;
        }

        public int getUnlockXpCost() {
            return unlockXpCost;
        }

        public int getUnlockLevel() {
            return unlockXpCost;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public long getRechargeDurationMillis() {
            return rechargeDurationMillis;
        }
    }
}
