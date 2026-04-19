package com.adaplu.clickdungeon.util;

import androidx.annotation.Nullable;

import com.adaplu.clickdungeon.model.Monster;
import com.adaplu.clickdungeon.model.MonsterAffinity;
import com.adaplu.clickdungeon.model.MonsterFamily;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalog of boss definitions keyed by floor.
 */
public final class BossCatalog {

    private static final Map<Integer, BossDefinition> BASE_BOSSES = new HashMap<>();
    private static final int BOSS_INTERVAL = 11;

    static {
        BASE_BOSSES.put(1, new BossDefinition("Lich", 14, 6, 4, 2,
                MonsterFamily.UNDEAD, MonsterAffinity.ARCANE));
        BASE_BOSSES.put(2, new BossDefinition("Archdemon", 18, 8, 6, 3,
                MonsterFamily.DEMONIC, MonsterAffinity.FIRE));
        BASE_BOSSES.put(3, new BossDefinition("Ancient Wyrm", 22, 9, 7, 3,
                MonsterFamily.ELEMENTAL, MonsterAffinity.FIRE));
    }

    private BossCatalog() {
    }

    public static boolean isBossFloor(int floor) {
        return floor > 0
                && floor <= GameBalance.FINAL_FLOOR
                && floor % BOSS_INTERVAL == 0;
    }

    @Nullable
    public static Monster createBossForFloor(int floor) {
        if (!isBossFloor(floor)) {
            return null;
        }
        int bossStage = floor / BOSS_INTERVAL;
        BossDefinition definition = BASE_BOSSES.get(((bossStage - 1) % BASE_BOSSES.size()) + 1);
        if (definition == null) {
            return null;
        }

        float vitalityScale = 1f + ((bossStage - 1) * 0.12f);
        int scaledHp = Math.max(1, Math.round(definition.maxHp * vitalityScale) + (bossStage - 1));
        int scaledAttack = Math.max(1, definition.attack + ((bossStage - 1) / 2));
        int scaledDefense = Math.max(0, definition.defense + ((bossStage - 1) / 3));
        int phaseCount = Math.min(5, definition.phaseCount + ((bossStage - 1) / 4));

        Monster boss = new Monster(definition.name, scaledHp,
                scaledAttack, scaledDefense, "");
        boss.setBoss(true);
        boss.setBossPhaseCount(phaseCount);
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
