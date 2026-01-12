package com.example.clickdungeon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable container for loot results generated after a monster defeat.
 */
public class MonsterLootRoll {
    /** Bonus gold awarded for this drop. */
    private final int bonusGold;
    /** Number of small keys awarded. */
    private final int smallKeyCount;
    /** Item names for base items awarded. */
    private final List<String> itemNames;
    /** Item names for magic items awarded. */
    private final List<String> magicItemNames;

    /**
     * Builds a loot roll result, defensively copying item lists.
     */
    public MonsterLootRoll(int bonusGold,
                           int smallKeyCount,
                           List<String> itemNames,
                           List<String> magicItemNames) {
        this.bonusGold = bonusGold;
        this.smallKeyCount = smallKeyCount;
        this.itemNames = itemNames == null ? new ArrayList<>() : new ArrayList<>(itemNames);
        this.magicItemNames = magicItemNames == null ? new ArrayList<>() : new ArrayList<>(magicItemNames);
    }

    /** Returns the bonus gold amount. */
    public int getBonusGold() {
        return bonusGold;
    }

    /** Returns the number of small keys awarded. */
    public int getSmallKeyCount() {
        return smallKeyCount;
    }

    /** Returns an unmodifiable list of base item names. */
    public List<String> getItemNames() {
        return Collections.unmodifiableList(itemNames);
    }

    /** Returns an unmodifiable list of magic item names. */
    public List<String> getMagicItemNames() {
        return Collections.unmodifiableList(magicItemNames);
    }

    /** Returns true if any loot was awarded. */
    public boolean hasLoot() {
        return bonusGold > 0 || smallKeyCount > 0 || !itemNames.isEmpty() || !magicItemNames.isEmpty();
    }
}
