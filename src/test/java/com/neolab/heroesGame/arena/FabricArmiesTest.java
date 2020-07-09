package com.neolab.heroesGame.arena;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class FabricArmiesTest {

    @Test
    public void testCreateBattleArena() {
        Map<Integer, Army> armies = FabricArmies.generateArmies(1, 2);
        assertEquals(6, armies.get(1).getHeroes().size());
        assertEquals(6, armies.get(2).getHeroes().size());
        assertNotNull(armies.get(1).getWarlord());
        assertNotNull(armies.get(2).getWarlord());
    }
}