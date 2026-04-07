package com.example.clickdungeon.model;

/**
 * Monster represents an enemy combatant in the dungeon.
 * It holds stats for health, attack, and defense, as well as family and affinity 
 * metadata used for terrain-based balancing and combat logic.
 */
public class Monster {
    /** Maximum HP for this monster. */
    private int maxHP;
    /** Current HP remaining in combat. */
    private int currentHP;
    /** Base attack value used for damage. */
    private int attack;
    /** Whether the monster can attack from range. */
    private boolean hasRangedAttack;

    // Emoji or resource identifier string.
    private String image;

    /** Base defense value used to reduce damage. */
    private int defense;

    /** Display/type identifier for this monster instance. */
    private String monsterType;
    /** Broad family category for weighting and rules. */
    private MonsterFamily family;
    /** Elemental or thematic affinity for terrain effects. */
    private MonsterAffinity affinity;
    /** True when this monster is a boss encounter. */
    private boolean boss;
    /** Number of phases for boss encounters. */
    private int bossPhaseCount = 1;

    /** Main constructor for standard monster creation. */
    public Monster(String monsterType, int maxHP, int attack, int defense, String image) {
        this.monsterType = monsterType;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.defense = defense;
        this.image = image;
        this.hasRangedAttack = false;
        this.family = MonsterFamily.UNKNOWN;
        this.affinity = MonsterAffinity.NONE;
        this.boss = false;
        this.bossPhaseCount = 1;
    }

    /** Extended constructor for monsters with specific family/affinity weights. */
    public Monster(String monsterType,
                   int maxHP,
                   int attack,
                   int defense,
                   String image,
                   MonsterFamily family,
                   MonsterAffinity affinity) {
        this(monsterType, maxHP, attack, defense, image);
        this.family = family != null ? family : MonsterFamily.UNKNOWN;
        this.affinity = affinity != null ? affinity : MonsterAffinity.NONE;
    }

    /** Returns the maximum HP value. */
    public int getMaxHP() {
        return maxHP;
    }

    /** Returns the current HP value. */
    public int getCurrentHP() {
        return currentHP;
    }

    /** Returns the base attack value. */
    public int getAttack() {
        return attack;
    }

    /** Returns the base defense value. */
    public int getDefense() {
        return defense;
    }

    /** Returns the monster type string. */
    public String getMonsterType() {
        return monsterType;
    }

    /** Returns the image/emoji identifier string. */
    public String getImage() {
        return image;
    }

    /** Returns true if this monster can attack from range. */
    public boolean hasRangedAttack() {
        return hasRangedAttack;
    }

    /** Sets whether the monster can attack from range. */
    public void setHasRangedAttack(boolean hasRangedAttack) {
        this.hasRangedAttack = hasRangedAttack;
    }

    /** Returns the assigned monster family (defaults to UNKNOWN). */
    public MonsterFamily getFamily() {
        return family != null ? family : MonsterFamily.UNKNOWN;
    }

    /** Sets the monster family, defaulting to UNKNOWN when null. */
    public void setFamily(MonsterFamily family) {
        this.family = family != null ? family : MonsterFamily.UNKNOWN;
    }

    /** Returns the assigned affinity (defaults to NONE). */
    public MonsterAffinity getAffinity() {
        return affinity != null ? affinity : MonsterAffinity.NONE;
    }

    /** Sets the affinity, defaulting to NONE when null. */
    public void setAffinity(MonsterAffinity affinity) {
        this.affinity = affinity != null ? affinity : MonsterAffinity.NONE;
    }

    /** Returns true if this monster is marked as a boss. */
    public boolean isBoss() {
        return boss;
    }

    /** Marks this monster as a boss encounter. */
    public void setBoss(boolean boss) {
        this.boss = boss;
    }

    /** Returns the configured boss phase count (defaults to 1). */
    public int getBossPhaseCount() {
        return bossPhaseCount;
    }

    /** Sets the boss phase count (min 1). */
    public void setBossPhaseCount(int bossPhaseCount) {
        this.bossPhaseCount = Math.max(1, bossPhaseCount);
    }

    /** Returns the current boss phase based on remaining HP. */
    public int getBossPhase() {
        if (!boss || bossPhaseCount <= 1) {
            return 1;
        }
        float ratio = maxHP > 0 ? (currentHP / (float) maxHP) : 0f;
        int phase = (int) Math.ceil(ratio * bossPhaseCount);
        return Math.max(1, Math.min(bossPhaseCount, phase));
    }

    /** Updates the base attack value, clamped to 1+. */
    public void setAttack(int attack) {
        this.attack = Math.max(1, attack);
    }

    /** Updates the base defense value, clamped to 0+. */
    public void setDefense(int defense) {
        this.defense = Math.max(0, defense);
    }

    /** Returns true if current health has dropped to zero or below. */
    public boolean isDead() {
        return currentHP <= 0;
    }

    /**
     * Reduces current HP by the specified amount, clamping at zero.
     */
    public void takeDamage(int damage) {
        currentHP -= damage;
        if (currentHP < 0) {
            currentHP = 0;
        }
    }
}
