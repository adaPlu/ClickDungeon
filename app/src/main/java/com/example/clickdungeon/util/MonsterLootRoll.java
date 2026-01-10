package com.example.clickdungeon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonsterLootRoll {
    private final int bonusGold;
    private final int smallKeyCount;
    private final List<String> itemNames;
    private final List<String> magicItemNames;

    public MonsterLootRoll(int bonusGold,
                           int smallKeyCount,
                           List<String> itemNames,
                           List<String> magicItemNames) {
        this.bonusGold = bonusGold;
        this.smallKeyCount = smallKeyCount;
        this.itemNames = itemNames == null ? new ArrayList<>() : new ArrayList<>(itemNames);
        this.magicItemNames = magicItemNames == null ? new ArrayList<>() : new ArrayList<>(magicItemNames);
    }

    public int getBonusGold() {
        return bonusGold;
    }

    public int getSmallKeyCount() {
        return smallKeyCount;
    }

    public List<String> getItemNames() {
        return Collections.unmodifiableList(itemNames);
    }

    public List<String> getMagicItemNames() {
        return Collections.unmodifiableList(magicItemNames);
    }

    public boolean hasLoot() {
        return bonusGold > 0 || smallKeyCount > 0 || !itemNames.isEmpty() || !magicItemNames.isEmpty();
    }
}
