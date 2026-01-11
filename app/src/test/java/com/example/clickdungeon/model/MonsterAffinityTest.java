package com.example.clickdungeon.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MonsterAffinityTest {

    @Test
    public void fromNameResolvesKnownValues() {
        assertEquals(MonsterAffinity.FIRE, MonsterAffinity.fromName("fire"));
        assertEquals(MonsterAffinity.LIGHTNING, MonsterAffinity.fromName("LIGHTNING"));
    }

    @Test
    public void fromNameFallsBackToNone() {
        assertEquals(MonsterAffinity.NONE, MonsterAffinity.fromName(null));
        assertEquals(MonsterAffinity.NONE, MonsterAffinity.fromName(""));
        assertEquals(MonsterAffinity.NONE, MonsterAffinity.fromName("mystery"));
    }
}
