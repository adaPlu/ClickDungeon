package com.adaplu.clickdungeon.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MonsterFamilyTest {

    @Test
    public void fromNameResolvesKnownValues() {
        assertEquals(MonsterFamily.BEAST, MonsterFamily.fromName("beast"));
        assertEquals(MonsterFamily.DRACONIC, MonsterFamily.fromName("DRACONIC"));
    }

    @Test
    public void fromNameFallsBackToUnknown() {
        assertEquals(MonsterFamily.UNKNOWN, MonsterFamily.fromName(null));
        assertEquals(MonsterFamily.UNKNOWN, MonsterFamily.fromName(""));
        assertEquals(MonsterFamily.UNKNOWN, MonsterFamily.fromName("mystery"));
    }
}
