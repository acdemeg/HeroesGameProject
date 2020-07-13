package com.neolab.heroesGame.server.answers;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.Hero;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AnswerValidator {

    public static boolean validateAnswer(Answer answer, BattleArena arena) throws HeroExceptions {
        Army thisBotArmy = CommonFunction.getCurrentPlayerArmy(arena, answer.getPlayerId());
        Army enemyArmy = CommonFunction.getEnemyArmy(arena, thisBotArmy);
        Optional<Hero> heroOptional = thisBotArmy.getHero(answer.getActiveHero());
        Hero hero;
        if(heroOptional.isPresent()){
            hero = heroOptional.get();
        }
        else throw new HeroExceptions(HeroErrorCode.ERROR_ACTIVE_UNIT);

        if (isErrorActiveHero(hero, thisBotArmy)) {
            throw new HeroExceptions(HeroErrorCode.ERROR_ACTIVE_UNIT);
        }
        if (answer.getAction() == HeroActions.DEFENCE) {
            return true;
        }

        if (isHealerCorrect(hero, answer, thisBotArmy)) {
            return true;
        }

        if (answer.getAction() == HeroActions.HEAL) {
            throw new HeroExceptions(HeroErrorCode.ERROR_UNIT_HEAL);
        }
        if (CommonFunction.isUnitMagician(hero)) {
            return true;
        }
        if (CommonFunction.isUnitArcher(hero)) {
            if (enemyArmy.getHero(answer.getTargetUnit()).isEmpty()) {
                throw new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
            }
            return true;
        }

        footmanTargetCheck(answer.getActiveHero(), answer.getTargetUnit(), enemyArmy);
        return true;
    }

    private static void footmanTargetCheck(SquareCoordinate activeUnit, SquareCoordinate target, Army army) throws HeroExceptions {
        Set<SquareCoordinate> validateTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, army);
        if (validateTarget.isEmpty()) {
            throw new HeroExceptions(HeroErrorCode.ERROR_ON_BATTLE_ARENA);
        }
        if (!validateTarget.contains(target)) {
            throw new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
        }
    }

    private static boolean isErrorActiveHero(Hero hero, Army thisBotArmy) {
        return !thisBotArmy.getAvailableHero().containsValue(hero);
    }

    private static boolean isHealerCorrect(Hero hero, Answer answer, Army thisBotArmy) throws HeroExceptions {
        if (CommonFunction.isUnitHealer(hero)) {
            if (answer.getAction() == HeroActions.ATTACK) {
                throw new HeroExceptions(HeroErrorCode.ERROR_UNIT_ATTACK);
            }
            if (thisBotArmy.getHero(answer.getTargetUnit()).isEmpty()) {
                throw new HeroExceptions(HeroErrorCode.ERROR_TARGET_HEAL);
            }
            return true;
        }
        return false;
    }
}
