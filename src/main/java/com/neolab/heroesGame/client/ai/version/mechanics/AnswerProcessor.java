package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.server.ActionEffect;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AnswerProcessor {
    private final SquareCoordinate coordinateDoesntMatters = new SquareCoordinate(-1, -1);

    private int waitingPlayerId;
    private int activePlayerId;
    private final BattleArena board;

    public AnswerProcessor(final int activePlayerId, final int waitingPlayerId, final BattleArena board) {
        this.waitingPlayerId = waitingPlayerId;
        this.activePlayerId = activePlayerId;
        this.board = board;
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
            final Map<SquareCoordinate, Integer> effectActionMap;
            final Hero activeHero = getActiveHero(board, answer);

            if (activeHero.isDefence()) {
                activeHero.cancelDefence();
            }
            if (answer.getAction() == HeroActions.DEFENCE) {
                effectActionMap = Collections.emptyMap();
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
        army.getHeroes().keySet().forEach(army::tryToKill);
    }

    private void tryToKill(final SquareCoordinate target, final Army army) {
        army.tryToKill(target);
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
