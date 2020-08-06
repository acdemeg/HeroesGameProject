package com.neolab.heroesGame.client.ai.version.first.minmax;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.*;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MinMaxBot extends Player {
    private static final String BOT_NAME = "Mazaev_v_MinMax";
    private static final Logger LOGGER = LoggerFactory.getLogger(MinMaxBot.class);
    private final long SEED = 5916;
    private static final int MAX_DEPTH = 6;
    private final Random RANDOM = new Random(SEED);
    private final Map<Integer, SquareCoordinate> coordinateMap;
    private final SquareCoordinate coordinateDoesntMatters = new SquareCoordinate(-1, -1);

    public MinMaxBot(final int id) {
        super(id, BOT_NAME);
        coordinateMap = createCoordinateMap();
    }

    private Map<Integer, SquareCoordinate> createCoordinateMap() {
        final Map<Integer, SquareCoordinate> coordinateMap = new HashMap<>();
        coordinateMap.put(0, new SquareCoordinate(0, 0));
        coordinateMap.put(1, new SquareCoordinate(1, 0));
        coordinateMap.put(2, new SquareCoordinate(2, 0));
        coordinateMap.put(3, new SquareCoordinate(0, 1));
        coordinateMap.put(4, new SquareCoordinate(1, 1));
        coordinateMap.put(5, new SquareCoordinate(2, 1));
        return coordinateMap;
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
        final List<Answer> actions = getAllAction(processor);
        tree.createAllChildren(actions);
        final BattleArena arena = processor.getBoard().getCopy();
        int heuristic = isItThisBot ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < actions.size(); i++) {
            processor.handleAnswer(actions.get(i));
            tree.downToChild(i);
            int nodeHeuristic = recursiveSimulation(processor, tree, heuristic);
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

    private int calculateHeuristic(BattleArena arena) {
        final Army botArmy = arena.getArmy(getId());
        final Army enemyArmy = arena.getEnemyArmy(getId());
        AtomicInteger heuristic = new AtomicInteger(0);
        botArmy.getHeroes().values().forEach(hero -> {
            int delta = hero.getDamage() * hero.getHp() * (hero instanceof Magician ? enemyArmy.getHeroes().size() : 1)
                    + hero.getHpMax() * hero.getDamage()
                    + (hero instanceof IWarlord ? 4000 : 0);
            heuristic.addAndGet(delta);
        });

        enemyArmy.getHeroes().values().forEach(hero -> {
            int delta = -hero.getDamage() * hero.getHp() * (hero instanceof Magician ? enemyArmy.getHeroes().size() : 1)
                    - hero.getHpMax() * hero.getDamage()
                    - (hero instanceof IWarlord ? 4000 : 0);
            heuristic.addAndGet(delta);
        });
        return heuristic.get();
    }

    private List<Answer> getAllAction(final GameProcessor processor) {
        final List<Answer> actions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            actions.addAll(getHeroAction(processor, coordinateMap.get(i)));
        }
        return actions;
    }

    private List<Answer> getHeroAction(final GameProcessor processor, final SquareCoordinate coordinate) {
        final Army currentArmy = processor.getActivePlayerArmy();
        final Army enemyArmy = processor.getWaitingPlayerArmy();
        final Integer activePlayerId = processor.getActivePlayerId();
        final Hero hero = currentArmy.getAvailableHeroes().get(coordinate);

        if (hero == null) {
            return Collections.emptyList();
        }

        final List<Answer> answers = new ArrayList<>();
        answers.add(new Answer(coordinate, HeroActions.DEFENCE, coordinateDoesntMatters, activePlayerId));

        if (hero instanceof Magician) {
            answers.add(new Answer(coordinate, HeroActions.ATTACK, coordinateDoesntMatters, activePlayerId));

        } else if (hero instanceof Archer) {
            for (final SquareCoordinate enemyCoordinate : enemyArmy.getHeroes().keySet()) {
                answers.add(new Answer(coordinate, HeroActions.ATTACK, enemyCoordinate, activePlayerId));
            }

        } else if (hero instanceof Healer) {
            for (final SquareCoordinate alliesCoordinate : currentArmy.getHeroes().keySet()) {
                if (currentArmy.getHero(alliesCoordinate).orElseThrow().isInjure()) {
                    answers.add(new Answer(coordinate, HeroActions.HEAL, alliesCoordinate, activePlayerId));
                }
            }

        } else {
            for (final SquareCoordinate enemyCoordinate : AnswerValidator.getCorrectTargetForFootman(coordinate, enemyArmy)) {
                answers.add(new Answer(coordinate, HeroActions.ATTACK, enemyCoordinate, activePlayerId));
            }
        }
        return answers;
    }
}
