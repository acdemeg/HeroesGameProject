package com.neolab.heroesGame;

import com.neolab.heroesGame.aditional.Analyzer;
import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.aditional.plotters.DynamicPlotter;
import com.neolab.heroesGame.aditional.plotters.MathDraw;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.FactoryArmies;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerBot;
import com.neolab.heroesGame.client.ai.version.first.SimpleBot;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.server.answers.AnswerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class OneGame {
    public static final Integer MAX_ROUND = 15;
    public static final Integer NUMBER_TRIES = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(OneGame.class);
    private static final long SEED = 5916;
    private static final Random RANDOM = new Random(SEED);
    private Player currentPlayer;
    private Player waitingPlayer;
    private final AnswerProcessor answerProcessor;
    private final BattleArena battleArena;
    private int counter;

    public OneGame(final BattleArena arena) {
        currentPlayer = new PlayerBot(1, "Bot1");
        waitingPlayer = new SimpleBot(2);
        battleArena = arena;
        answerProcessor = new AnswerProcessor(1, 2, battleArena);
        counter = 0;
    }

    private void changeCurrentAndWaitingPlayers() {
        final Player temp = currentPlayer;
        currentPlayer = waitingPlayer;
        waitingPlayer = temp;
        setAnswerProcessorPlayerId(currentPlayer, waitingPlayer);
    }

    private void setAnswerProcessorPlayerId(final Player currentPlayer, final Player waitingPlayer) {
        answerProcessor.setActivePlayerId(currentPlayer.getId());
        answerProcessor.setWaitingPlayerId(waitingPlayer.getId());
    }

    public static void main(final String[] args) throws Exception {
        OneGame.matches();
    }

    private static void matches() throws Exception {
        List<String> armies = CommonFunction.getAllAvailableArmiesCode(6);
        final long startTime = System.currentTimeMillis();

        final Army firstArmy = new StringArmyFactory(armies.get(RANDOM.nextInt(armies.size()))).create();
        final Army secondArmy = new StringArmyFactory(armies.get(RANDOM.nextInt(armies.size()))).create();
        final Map<Integer, Army> mapArmies = new HashMap<>();
        mapArmies.put(1, firstArmy);
        mapArmies.put(2, secondArmy);
        final BattleArena arena = new BattleArena(mapArmies);
        final OneGame gamingProcess = new OneGame(arena);
        System.out.println("Партия началась");
        Optional<Player> whoIsWin;
        while (true) {
            //Определение победы
            System.out.println(arena.toString());
            whoIsWin = gamingProcess.someoneWhoWin();
            if (whoIsWin.isPresent()) {
                break;
            }
            //Начало нового раунда, прерывание игры из-за ничьей
            if (!gamingProcess.battleArena.canSomeoneAct()) {
                gamingProcess.counter++;
                if (gamingProcess.counter > MAX_ROUND) {
                    StatisticWriter.writePlayerDrawStatistic(gamingProcess.currentPlayer.getName(),
                            gamingProcess.waitingPlayer.getName());
                    break;
                }
                gamingProcess.battleArena.endRound();
            }
            //Ход игрока
            if (gamingProcess.checkCanMove(gamingProcess.currentPlayer.getId())) {
                gamingProcess.askPlayerProcess();
            }
            //смена активного игрока
            gamingProcess.changeCurrentAndWaitingPlayers();
        }
        LOGGER.info("партия длилась: {}", System.currentTimeMillis() - startTime);
    }

    private void askPlayerProcess() throws HeroExceptions, IOException {
        final Answer answer = currentPlayer.getAnswer(battleArena);

        System.out.println(answer.toString());
        answerProcessor.handleAnswer(answer);

        System.out.println(answerProcessor.getActionEffect().toString());
    }

    private Optional<Player> someoneWhoWin() {
        Player isWinner = battleArena.isArmyDied(getCurrentPlayerId()) ? waitingPlayer : null;
        if (isWinner == null) {
            isWinner = battleArena.isArmyDied(getWaitingPlayerId()) ? currentPlayer : null;
        }
        return Optional.ofNullable(isWinner);
    }

    private boolean checkCanMove(final Integer id) {
        return !battleArena.haveAvailableHeroByArmyId(id);
    }

    private int getCurrentPlayerId() {
        return currentPlayer.getId();
    }

    private int getWaitingPlayerId() {
        return waitingPlayer.getId();
    }
}

