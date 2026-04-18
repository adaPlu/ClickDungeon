package com.adaplu.clickdungeon.util;

import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.MonsterAffinity;
import com.adaplu.clickdungeon.model.MonsterFamily;

import java.util.Random;

/**
 * Catalog of monster template definitions used to populate dungeon floors.
 */
public final class MonsterCatalog {

    public static final MonsterTemplate SLIME = new MonsterTemplate("Slime", "S", 3, 1, 0,
            MonsterFamily.ELEMENTAL, MonsterAffinity.POISON);
    public static final MonsterTemplate GOBLIN = new MonsterTemplate("Goblin", "G", 4, 2, 1,
            MonsterFamily.HUMANOID, MonsterAffinity.NONE);
    public static final MonsterTemplate SKELETON = new MonsterTemplate("Skeleton", "K", 5, 2, 2,
            MonsterFamily.UNDEAD, MonsterAffinity.SHADOW);
    public static final MonsterTemplate ORC = new MonsterTemplate("Orc", "O", 6, 3, 2,
            MonsterFamily.HUMANOID, MonsterAffinity.NONE);
    public static final MonsterTemplate TROLL = new MonsterTemplate("Troll", "T", 7, 3, 3,
            MonsterFamily.BEAST, MonsterAffinity.NONE);
    public static final MonsterTemplate WITCH = new MonsterTemplate("Witch", "W", 6, 4, 2,
            MonsterFamily.ARCANE, MonsterAffinity.ARCANE);
    public static final MonsterTemplate VAMPIRE = new MonsterTemplate("Vampire", "V", 6, 4, 3,
            MonsterFamily.UNDEAD, MonsterAffinity.SHADOW);
    public static final MonsterTemplate DEMON = new MonsterTemplate("Demon", "D", 8, 5, 3,
            MonsterFamily.DEMONIC, MonsterAffinity.FIRE);
    public static final MonsterTemplate DRAGON = new MonsterTemplate("Dragon", "R", 10, 6, 4,
            MonsterFamily.DRACONIC, MonsterAffinity.FIRE);
    public static final MonsterTemplate RAT = new MonsterTemplate("Rat", "r", 3, 1, 0,
            MonsterFamily.BEAST, MonsterAffinity.NONE);
    public static final MonsterTemplate BAT = new MonsterTemplate("Bat", "b", 3, 2, 0,
            MonsterFamily.BEAST, MonsterAffinity.NONE);
    public static final MonsterTemplate SPIDER = new MonsterTemplate("Spider", "s", 4, 2, 1,
            MonsterFamily.BEAST, MonsterAffinity.POISON);
    public static final MonsterTemplate WOLF = new MonsterTemplate("Wolf", "w", 5, 3, 1,
            MonsterFamily.BEAST, MonsterAffinity.NONE);
    public static final MonsterTemplate BANDIT = new MonsterTemplate("Bandit", "B", 6, 3, 2,
            MonsterFamily.HUMANOID, MonsterAffinity.NONE);
    public static final MonsterTemplate CULTIST = new MonsterTemplate("Cultist", "C", 6, 3, 2,
            MonsterFamily.HUMANOID, MonsterAffinity.ARCANE);
    public static final MonsterTemplate WARLOCK = new MonsterTemplate("Warlock", "L", 6, 4, 2,
            MonsterFamily.ARCANE, MonsterAffinity.ARCANE);
    public static final MonsterTemplate WRAITH = new MonsterTemplate("Wraith", "H", 7, 4, 3,
            MonsterFamily.UNDEAD, MonsterAffinity.SHADOW);
    public static final MonsterTemplate GOLEM = new MonsterTemplate("Golem", "M", 8, 4, 4,
            MonsterFamily.CONSTRUCT, MonsterAffinity.NONE);
    public static final MonsterTemplate LICH = new MonsterTemplate("Lich", "I", 9, 5, 4,
            MonsterFamily.UNDEAD, MonsterAffinity.ARCANE);
    public static final MonsterTemplate HELLHOUND = new MonsterTemplate("Hellhound", "h", 9, 6, 3,
            MonsterFamily.DEMONIC, MonsterAffinity.FIRE);
    public static final MonsterTemplate REVENANT = new MonsterTemplate("Revenant", "N", 8, 5, 3,
            MonsterFamily.UNDEAD, MonsterAffinity.SHADOW);
    public static final MonsterTemplate ARCHDEMON = new MonsterTemplate("Archdemon", "A", 11, 7, 5,
            MonsterFamily.DEMONIC, MonsterAffinity.FIRE);
    public static final MonsterTemplate ANCIENT_WYRM = new MonsterTemplate("Ancient Wyrm", "Y", 12, 7, 5,
            MonsterFamily.DRACONIC, MonsterAffinity.FIRE);

    private MonsterCatalog() {
    }

    /**
     * Immutable template that scales base stats per floor and difficulty.
     */
    public static class MonsterTemplate {
        public final String name;
        public final String emoji;
        public final int baseHp;
        public final int baseAttack;
        public final int baseDefense;
        public final MonsterFamily family;
        public final MonsterAffinity affinity;

        MonsterTemplate(String name,
                        String emoji,
                        int baseHp,
                        int baseAttack,
                        int baseDefense,
                        MonsterFamily family,
                        MonsterAffinity affinity) {
            this.name = name;
            this.emoji = emoji;
            this.baseHp = baseHp;
            this.baseAttack = baseAttack;
            this.baseDefense = baseDefense;
            this.family = family != null ? family : MonsterFamily.UNKNOWN;
            this.affinity = affinity != null ? affinity : MonsterAffinity.NONE;
        }

        /** Builds a Monster instance using per-floor scaling and difficulty multipliers. */
        public Monster spawnForFloor(Random random, int floor, SettingsManager.Difficulty difficulty) {
            int scaling = Math.max(0, floor - 1);

            int hpBonus = scaling;
            int attackBonus = Math.max(0, (scaling + 1) / 2);
            int defenseBonus = Math.max(0, scaling / 3);

            if (scaling > 0) {
                hpBonus += random.nextInt(scaling + 1);
                attackBonus += random.nextInt(Math.max(1, (scaling / 2) + 1));
                defenseBonus += random.nextInt(Math.max(1, (scaling / 3) + 1));
            }

            int maxHp = Math.max(1, baseHp + hpBonus);
            int attack = Math.max(1, baseAttack + attackBonus);
            int defense = Math.max(0, baseDefense + defenseBonus);

            float scale = GameBalance.getFloorDifficultyScale(floor);
            maxHp = Math.max(1, Math.round(maxHp * scale));
            attack = Math.max(1, Math.round(attack * scale));
            defense = Math.max(0, Math.round(defense * scale));

            if (difficulty != null) {
                maxHp = difficulty.scaleMonsterHealth(maxHp);
                attack = difficulty.scaleMonsterAttack(attack);
                defense = difficulty.scaleMonsterDefense(defense);
            }

            return new Monster(name, maxHp, attack, defense, emoji, family, affinity);
        }
    }
}
