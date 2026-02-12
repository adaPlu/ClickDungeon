package com.example.clickdungeon.util;

import androidx.annotation.Nullable;

import com.example.clickdungeon.model.Monster;
import com.example.clickdungeon.model.MonsterAffinity;
import com.example.clickdungeon.model.MonsterFamily;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalog of boss definitions keyed by floor.
 */
public final class BossCatalog {

    private static final Map<Integer, BossDefinition> BOSSES = new HashMap<>();

    static {
        BOSSES.put(5, new BossDefinition("Lich", 14, 6, 4, 2,
                MonsterFamily.UNDEAD, MonsterAffinity.ARCANE));
        BOSSES.put(10, new BossDefinition("Archdemon", 18, 8, 6, 3,
                MonsterFamily.DEMONIC, MonsterAffinity.FIRE));
        BOSSES.put(15, new BossDefinition("Ancient Wyrm", 22, 9, 7, 3,
                MonsterFamily.ELEMENTAL, MonsterAffinity.FIRE));
    }

    private BossCatalog() {
    }

    public static boolean isBossFloor(int floor) {
        return BOSSES.containsKey(floor);
    }

    @Nullable
    public static Monster createBossForFloor(int floor) {
        BossDefinition definition = BOSSES.get(floor);
        if (definition == null) {
            return null;
        }
        Monster boss = new Monster(definition.name, definition.maxHp,
                definition.attack, definition.defense, "");
        boss.setBoss(true);
        boss.setBossPhaseCount(definition.phaseCount);
        boss.setFamily(definition.family);
        boss.setAffinity(definition.affinity);
        return boss;
    }

    private static final class BossDefinition {
        final String name;
        final int maxHp;
        final int attack;
        final int defense;
        final int phaseCount;
        final MonsterFamily family;
        final MonsterAffinity affinity;

        BossDefinition(String name,
                       int maxHp,
                       int attack,
                       int defense,
                       int phaseCount,
                       MonsterFamily family,
                       MonsterAffinity affinity) {
            this.name = name;
            this.maxHp = maxHp;
            this.attack = attack;
            this.defense = defense;
            this.phaseCount = phaseCount;
            this.family = family;
            this.affinity = affinity;
        }
    }
}
