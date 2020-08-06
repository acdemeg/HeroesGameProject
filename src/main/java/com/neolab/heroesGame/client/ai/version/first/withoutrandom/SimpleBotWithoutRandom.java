package com.neolab.heroesGame.client.ai.version.first.withoutrandom;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerBot;
import com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Archer;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Healer;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SimpleBotWithoutRandom extends Player {
    private static final String BOT_NAME = "Mazaev_v_1_2";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerBot.class);
    private final long SEED = 5916;
    private final Random RANDOM = new Random(SEED);
    private int currentRound = -1;
    private final Map<Integer, SquareCoordinate> coordinateMap;
    private final List<Double> geneticCoefficients;
    private final SquareCoordinate coordinateDoesntMatters = new SquareCoordinate(-1, -1);


    public SimpleBotWithoutRandom(final int id) {
        super(id, BOT_NAME);
        coordinateMap = createCoordinateMap();
        geneticCoefficients = createCoefficient();
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

    /**
     * Массив коэффициентов приорететов действий юнитов:
     * 0 - защита юнитов второй линии
     * 1 - защита юнитов первой линии
     * 2 - модификатор атаки от урона
     * 3 - модификатор хила
     *
     * @return Массив коэффициентов приорететов действий юнитов
     */
    private List<Double> createCoefficient() {
        final List<Double> coefficients = new ArrayList<>();
        coefficients.add(0.1);
        coefficients.add(1.0);
        coefficients.add(10.0);
        coefficients.add(5.0);
        return coefficients;
    }

    @Override
    public Answer getAnswer(final com.neolab.heroesGame.arena.BattleArena board) throws HeroExceptions {
        final long startTime = System.currentTimeMillis();
        if (board.getArmy(getId()).getHeroes().size() == board.getArmy(getId()).getAvailableHeroes().size()) {
            currentRound++;
        }
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final SimulationsTree tree = new SimulationsTree();
        for (int i = 0; ; i++) {
            if (System.currentTimeMillis() - startTime > 1000) {
                LOGGER.info("Количество симуляций за {}мс: {}", System.currentTimeMillis() - startTime, i);
                break;
            }
            final GameProcessor processor = new GameProcessor(getId(), arena.getCopy(), currentRound);
            recursiveSimulation(processor, tree);
            tree.toRoot();
        }
        return tree.getBestAction();
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

    private int recursiveSimulation(final GameProcessor processor, final SimulationsTree tree) throws HeroExceptions {
        if (tree.isNodeNew()) {
            final Map<Integer, Answer> actions = getAllAction(processor);
            tree.fieldNewNode(actions, calculateActionPriority(actions, processor));
        }
        final int actionNumber = chooseAction(tree.getActionPriority());
        processor.handleAnswer(tree.getAnswer(actionNumber));
        final int activePlayerId = processor.getActivePlayerId();
        final GameEvent event = processor.matchOver();

        if (event == GameEvent.NOTHING_HAPPEN) {
            tree.downToChild(actionNumber);
            final int winner = recursiveSimulation(processor, tree);

            if (winner == -1) {
                tree.increase(GameEvent.GAME_END_WITH_A_TIE);
            } else if (winner == activePlayerId) {
                tree.increase(GameEvent.YOU_WIN_GAME);
            } else {
                tree.increase(GameEvent.YOU_LOSE_GAME);
            }
            tree.upToParent();
            return winner;

        } else {
            tree.increase(event);
            tree.upToParent();
            if (event == GameEvent.GAME_END_WITH_A_TIE) {
                return -1;
            } else if (event == GameEvent.YOU_WIN_GAME) {
                return processor.getActivePlayerId();
            }
            return processor.getWaitingPlayerId();
        }
    }

    private Map<Integer, Answer> getAllAction(final GameProcessor processor) {
        final Map<Integer, Answer> actions = new HashMap<>();
        int counter = 0;
        for (int i = 0; i < 6; i++) {
            final List<Answer> answers = getHeroAction(processor, coordinateMap.get(i));
            for (final Answer answer : answers) {
                actions.put(counter++, answer);
            }
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

    private @NotNull List<Double> calculateActionPriority(@NotNull final Map<Integer, Answer> actions,
                                                          @NotNull final GameProcessor processor) {
        final List<Double> actionPriority = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++) {
            actionPriority.add(modificate(actions.get(i), processor));
        }
        return actionPriority;
    }

    private double modificate(final Answer answer, final GameProcessor processor) {
        if (answer.getAction() == HeroActions.DEFENCE) {
            if (answer.getActiveHeroCoordinate().getY() == 0) {
                return geneticCoefficients.get(0);
            } else {
                return geneticCoefficients.get(1);
            }
        } else if (answer.getAction() == HeroActions.ATTACK) {
            return geneticCoefficients.get(2) * calculateDamage(answer, processor);
        }
        return geneticCoefficients.get(3) * calculateHeal(answer, processor);
    }

    private Double calculateHeal(final Answer answer, final GameProcessor processor) {
        final Hero hero = processor.getActivePlayerArmy().getHero(answer.getActiveHeroCoordinate()).orElseThrow();
        final Hero target = processor.getActivePlayerArmy().getHero(answer.getTargetUnitCoordinate()).orElseThrow();
        if (target.getHp() < hero.getDamage()) {
            return (double) (target.getHpMax() / target.getHp() * target.getDamage() * hero.getDamage());
        }
        return (double) Math.min(hero.getDamage(), target.getHpMax() - target.getHp());
    }

    private Double calculateDamage(final Answer answer, final GameProcessor processor) {
        final Hero hero = processor.getActivePlayerArmy().getHero(answer.getActiveHeroCoordinate()).orElseThrow();
        if (hero instanceof Magician) {
            return (double) hero.getDamage() * processor.getWaitingPlayerArmy().getHeroes().size();
        }
        if (hero.getDamage() > processor.getWaitingPlayerArmy().getHero(answer.getTargetUnitCoordinate()).orElseThrow().getHp()) {
            return hero.getDamage() * 2d;
        }
        return (double) hero.getDamage();
    }

    private int chooseAction(@NotNull final List<Double> actionPriority) {
        final Double random = RANDOM.nextDouble() * actionPriority.get(actionPriority.size() - 1);
        for (int i = 0; i < actionPriority.size(); i++) {
            if (actionPriority.get(i) > random) {
                return i;
            }
        }
        LOGGER.trace("WTF!!!");
        for (final Double aDouble : actionPriority) {
            LOGGER.trace("RANDOM: {}, Action: {}", random, aDouble);
        }
        return 0;
    }
}

