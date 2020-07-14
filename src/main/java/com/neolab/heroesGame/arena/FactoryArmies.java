package com.neolab.heroesGame.arena;

import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.*;

import java.util.*;

public class FactoryArmies {

    private static final long SEED = 5916;
    private static final Random RANDOM = new Random(SEED);

    public static Map<Integer, Army> generateArmies(final Integer firstPlayerId,
                                                    final Integer secondPlayerId) throws HeroExceptions {
        final Map<Integer, Army> armies = new HashMap<>();
        armies.put(firstPlayerId, createArmy());
        armies.put(secondPlayerId, createArmy());
        return armies;
    }

    /**
     * Генерируем армию. Варлорд: с шансом 50% файтер, по 25% на магов
     * Первую линию забивает Footman
     * Вторую линию забивает Healer, Magician, Archer с равным шансом
     * Место для варлорда выбирает Set.iterator().next()
     */
    private static Army createArmy() throws HeroExceptions {
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();
        final Hero warlord = createWarlord();
        final SquareCoordinate warlordCoord;
        final Set<SquareCoordinate> firstLine = makeLine(1);
        final Set<SquareCoordinate> secondLine = makeLine(0);
        if (warlord instanceof WarlordFootman){
            warlordCoord = addWarlord(heroes, firstLine, warlord);
        } else{
            warlordCoord = addWarlord(heroes, secondLine, warlord);
        }
        for (final SquareCoordinate key : firstLine) {
            heroes.put(key, Footman.createInstance());
        }
        for (final SquareCoordinate key : secondLine) {
            heroes.put(key, createSecondLineUnit());
        }
        return new Army(heroes);
    }

    private static SquareCoordinate addWarlord(final Map<SquareCoordinate, Hero> heroes, final Set<SquareCoordinate> line, final Hero warlord) {
        final SquareCoordinate temp = line.iterator().next();
        heroes.put(temp, warlord);
        line.remove(temp);
        return temp;
    }

    private static Hero createWarlord() {
        final int switcher = RANDOM.nextInt(4);
        if (switcher == 0) {
            return WarlordMagician.createInstance();
        } else if (switcher == 1) {
            return WarlordVampire.createInstance();
        } else {
            return WarlordFootman.createInstance();
        }
    }

    private static Hero createSecondLineUnit() {
        final int switcher = RANDOM.nextInt(3) % 3;
        if (switcher == 0) {
            return Archer.createInstance();
        } else if (switcher == 1) {
            return Magician.createInstance();
        } else {
            return Healer.createInstance();
        }
    }

    private static Set<SquareCoordinate> makeLine(final Integer y) {
        final Set<SquareCoordinate> line = new HashSet<>();
        for (int x = 0; x <= 2; x++) {
            line.add(new SquareCoordinate(x, y));
        }
        return line;
    }
}
