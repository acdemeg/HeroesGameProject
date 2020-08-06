package com.neolab.heroesGame.client.ai.server;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.first.MinMaxBot;
import com.neolab.heroesGame.client.ai.version.first.SimpleBotWithoutRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public class SelfPlayServer {
    public static final Integer NUMBER_TRIES = 10;
    public static final Integer DIFFERENT_ARMIES = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfPlayServer.class);
    private static final long SEED = 87941;
    private static final Random RANDOM = new Random(SEED);
    private static final AtomicInteger countGame = new AtomicInteger(0);
    private static final AtomicInteger countEndGame = new AtomicInteger(0);
    private static final Integer MAX_COUNT_GAME_ROOMS = 6;
    final static long startTime = System.currentTimeMillis();

    public static void main(final String[] args) throws Exception {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(6);
        int counter;

        final ThreadGroup threadGroup = new ThreadGroup("matching");
        for (int j = 0; j < DIFFERENT_ARMIES; j++) {
            final String army = armies.get(RANDOM.nextInt(armies.size()));
            final Army firstArmy = new StringArmyFactory(army).create();
            final Army secondArmy = new StringArmyFactory(army).create();
            final Map<Integer, Army> mapArmies = new HashMap<>();
            mapArmies.put(1, firstArmy);
            mapArmies.put(2, secondArmy);
            final BattleArena arena = new BattleArena(mapArmies);
            for (int i = 0; i < NUMBER_TRIES; i++) {
                final Player firstPlayer = new SimpleBotWithoutRandom(1);
                final Player secondPlayer = new MinMaxBot(2);

                waitQueue(threadGroup);
                new Thread(threadGroup, new SelfPlayRoom(arena.getCopy(), firstPlayer, secondPlayer)).start();

                final Player firstPlayer2 = new SimpleBotWithoutRandom(2);
                final Player secondPlayer2 = new MinMaxBot(1);

                waitQueue(threadGroup);
                new Thread(threadGroup, new SelfPlayRoom(arena.getCopy(), secondPlayer2, firstPlayer2)).start();
            }
        }
        while (threadGroup.activeCount() > 0) {
            Thread.sleep(2000);
            counter = countGame.get();
            if (counter > threadGroup.activeCount()) {
                countEndGame.incrementAndGet();
                countGame.decrementAndGet();
                final long endTime = System.currentTimeMillis();
                final long timeNeed = (((endTime - startTime) / countEndGame.get())
                        * (2 * DIFFERENT_ARMIES * NUMBER_TRIES - countEndGame.get())) / 1000;
                final int timeFromStart = (int) ((endTime - startTime) / 1000);
                System.out.printf("Прошло %d испытаний из %d. Прошло: %d секунд. Примерно осталось : %d секунд\n",
                        countEndGame.get(), 2 * DIFFERENT_ARMIES * NUMBER_TRIES, timeFromStart, timeNeed);
            }
        }
    }

    private static void waitQueue(final ThreadGroup threadGroup) throws Exception {
        while (threadGroup.activeCount() >= MAX_COUNT_GAME_ROOMS) {
            Thread.sleep(2000);
        }
        if (countGame.get() > threadGroup.activeCount()) {
            countEndGame.incrementAndGet();
            countGame.decrementAndGet();
            final long endTime = System.currentTimeMillis();
            final long timeNeed = (((endTime - startTime) / countEndGame.get())
                    * (2 * DIFFERENT_ARMIES * NUMBER_TRIES - countEndGame.get())) / 1000;
            final int timeFromStart = (int) ((endTime - startTime) / 1000);
            System.out.printf("Прошло %d испытаний из %d. Прошло: %d секунд. Примерно осталось : %d секунд\n",
                    countEndGame.get(), 2 * DIFFERENT_ARMIES * NUMBER_TRIES, timeFromStart, timeNeed);
        }
        countGame.incrementAndGet();
    }
}
