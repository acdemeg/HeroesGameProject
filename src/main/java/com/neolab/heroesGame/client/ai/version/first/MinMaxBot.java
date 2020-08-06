package com.neolab.heroesGame.client.ai.version.first;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.IWarlord;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;
import com.neolab.heroesGame.client.ai.version.mechanics.trees.MinMaxTree;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MinMaxBot extends Player {
    private static final String BOT_NAME = "Mazaev_v_MinMax";
    private static final Logger LOGGER = LoggerFactory.getLogger(MinMaxBot.class);
    private final long SEED = 5916;
    private static final int MAX_DEPTH = 6;
    private final Random RANDOM = new Random(SEED);

    public MinMaxBot(final int id) {
        super(id, BOT_NAME);
    }

    @Override
    public Answer getAnswer(final com.neolab.heroesGame.arena.BattleArena board) throws HeroExceptions {
        final long startTime = System.currentTimeMillis();
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final MinMaxTree tree = new MinMaxTree();
        final GameProcessor processor = new GameProcessor(arena.getEnemyId(getId()), arena.getCopy());
        recursiveSimulation(processor, tree, Integer.MAX_VALUE);
        LOGGER.info("На обход дерева глубиной {} потрачено {}мс", MAX_DEPTH, System.currentTimeMillis() - startTime);
        return tree.getBestHeuristicAnswer();
    }

    @Override
    public String getStringArmyFirst(final int armySize) {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(armySize);
        return armies.get(RANDOM.nextInt(armies.size()));
    }

    @Override
    public String getStringArmySecond(final int armySize, final com.neolab.heroesGame.arena.Army army) {
        return getStringArmyFirst(armySize);
    }

    private int recursiveSimulation(final GameProcessor processor, final MinMaxTree tree,
                                    final int prevHeuristic) throws HeroExceptions {
        final GameEvent event = processor.matchOver();
        if (tree.isMaxDepth(MAX_DEPTH) || event != GameEvent.NOTHING_HAPPEN) {
            final int heuristic = calculateHeuristic(processor.getBoard());
            tree.setHeuristic(heuristic);
            return heuristic;
        }
        final boolean isItThisBot = processor.getActivePlayerId() == getId();
        final List<Answer> actions = processor.getAllActionsForCurrentPlayer();
        tree.createAllChildren(actions);
        final BattleArena arena = processor.getBoard().getCopy();
        int heuristic = isItThisBot ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < actions.size(); i++) {
            processor.handleAnswer(actions.get(i));
            tree.downToChild(i);
            final int nodeHeuristic = recursiveSimulation(processor, tree, heuristic);
            if ((isItThisBot && processor.getActivePlayerId() != getId())
                    || (!isItThisBot && processor.getActivePlayerId() == getId())) {
                processor.swapActivePlayer();
            }
            tree.upToParent();
            if (isItThisBot ? prevHeuristic < nodeHeuristic : prevHeuristic > nodeHeuristic) {
                tree.setHeuristic(nodeHeuristic);
                return nodeHeuristic;
            }
            if (isItThisBot ? nodeHeuristic > heuristic : nodeHeuristic < heuristic) {
                heuristic = nodeHeuristic;
            }
            processor.setBoard(arena.getCopy());
        }
        tree.setHeuristic(heuristic);
        return heuristic;
    }

    private int calculateHeuristic(final BattleArena arena) {
        final Army botArmy = arena.getArmy(getId());
        final Army enemyArmy = arena.getEnemyArmy(getId());
        final AtomicInteger heuristic = new AtomicInteger(0);
        botArmy.getHeroes().values().forEach(hero -> {
            final int delta = hero.getDamage() * hero.getHp() * (hero instanceof Magician ? enemyArmy.getHeroes().size() : 1)
                    + hero.getHpMax() * hero.getDamage()
                    + (hero instanceof IWarlord ? 4000 : 0);
            heuristic.addAndGet(delta);
        });

        enemyArmy.getHeroes().values().forEach(hero -> {
            final int delta = -hero.getDamage() * hero.getHp() * (hero instanceof Magician ? enemyArmy.getHeroes().size() : 1)
                    - hero.getHpMax() * hero.getDamage()
                    - (hero instanceof IWarlord ? 4000 : 0);
            heuristic.addAndGet(delta);
        });
        return heuristic.get();
    }
}
