package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.server.answers.Answer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GameProcessor {
    private final SquareCoordinate coordinateDoesntMatters = new SquareCoordinate(-1, -1);
    private int roundCounter = 0;
    private final int MAX_ROUND = 15;
    private int waitingPlayerId;
    private int activePlayerId;
    private BattleArena board;

    public GameProcessor(final int activePlayerId, final BattleArena board) {
        this.waitingPlayerId = board.getEnemyId(activePlayerId);
        this.activePlayerId = activePlayerId;
        this.board = board;
    }

    public GameProcessor(final int activePlayerId, final BattleArena board, final int roundCounter) {
        this.waitingPlayerId = board.getEnemyId(activePlayerId);
        this.activePlayerId = activePlayerId;
        this.board = board;
        this.roundCounter = roundCounter;
    }

    public BattleArena getBoard() {
        return board;
    }

    public void setBoard(BattleArena board) {
        this.board = board;
    }

    public Integer getActivePlayerId() {
        return activePlayerId;
    }

    public Integer getWaitingPlayerId() {
        return waitingPlayerId;
    }

    public Army getActivePlayerArmy() {
        return board.getArmy(activePlayerId);
    }

    public Army getWaitingPlayerArmy() {
        return board.getArmy(waitingPlayerId);
    }

    public void swapActivePlayer() {
        int temp = activePlayerId;
        activePlayerId = waitingPlayerId;
        waitingPlayerId = temp;
    }

    public void setWaitingPlayerId(final int waitingPlayerId) {
        this.waitingPlayerId = waitingPlayerId;
    }

    public void setActivePlayerId(final int activePlayerId) {
        this.activePlayerId = activePlayerId;
    }

    /**
     * Проверяем все ли в порядке с запросом.
     * Объявляем переменные, которые будут использоваться для формирования actionEffect
     * Если юнит защищался - сбрасываем защиту. Если защищается сейчас - устанавливаем.
     * Если юнит не защищается, то определяем над какой армией будет совершаться действие:
     * - для HEAL над нашей
     * - для ATTACK над вражеской
     *
     * @param answer - ответ игрока на вопрос "Что делаем?"
     * @throws HeroExceptions выбрасываем исключение в соответствии с ошибкой в запросе (answer)
     */
    public void handleAnswer(final Answer answer) throws HeroExceptions {
        if (AnswerValidator.isAnswerValidate(answer, board)) {
            final Hero activeHero = getActiveHero(board, answer);

            if (activeHero.isDefence()) {
                activeHero.cancelDefence();
            }
            if (answer.getAction() == HeroActions.DEFENCE) {
                activeHero.setDefence();
            } else {
                if (answer.getAction() == HeroActions.ATTACK) {
                    activeHero.toAct(answer.getTargetUnitCoordinate(), board.getArmy(waitingPlayerId));
                    if (answer.getTargetUnitCoordinate().equals(coordinateDoesntMatters)) {
                        tryToKillAll(board.getArmy(waitingPlayerId));
                    } else {
                        tryToKill(answer.getTargetUnitCoordinate(), board.getArmy(waitingPlayerId));
                    }
                } else {
                    activeHero.toAct(answer.getTargetUnitCoordinate(), board.getArmy(activePlayerId));
                }
            }

            removeUsedHero(activePlayerId, activeHero.getUnitId());
        } else {
            throw new HeroExceptions(HeroErrorCode.ERROR_ANSWER);
        }

    }

    private void tryToKillAll(final Army army) {
        Set<SquareCoordinate> keys = new HashSet<>(army.getHeroes().keySet());
        keys.forEach(army::tryToKill);
    }

    private void tryToKill(final SquareCoordinate target, final Army army) {
        army.tryToKill(target);
    }

    public GameEvent matchOver() {
        if (board.isArmyDied(waitingPlayerId)) {
            return GameEvent.YOU_WIN_GAME;
        } else if (board.isArmyDied(activePlayerId)) {
            return GameEvent.YOU_LOSE_GAME;
        } else if (roundCounter >= MAX_ROUND) {
            return GameEvent.GAME_END_WITH_A_TIE;
        }
        if (board.haveAvailableHeroByArmyId(waitingPlayerId)) {
            swapActivePlayer();
        } else if (board.noOneCanAct()) {
            board.endRound();
            roundCounter++;
            swapActivePlayer();
        }

        return GameEvent.NOTHING_HAPPEN;
    }

    private Hero getActiveHero(final BattleArena board, final Answer answer) throws HeroExceptions {
        final Optional<Hero> activeHero = board.getArmy(activePlayerId).getHero(answer.getActiveHeroCoordinate());
        if (activeHero.isPresent()) {
            return activeHero.get();
        }
        throw new HeroExceptions(HeroErrorCode.ERROR_ACTIVE_UNIT);
    }

    private void removeUsedHero(final int activePlayerId, final int heroId) {
        board.removeUsedHeroesById(heroId, activePlayerId);
    }

}
