package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;

public class Magician extends Hero {

    public Magician(final int hp, final int damage, final float precision, final float armor) {
        super(hp, damage, precision, armor);
    }

    @JsonCreator
    protected Magician(@JsonProperty("unitId") final int unitId, @JsonProperty("hpDefault") final int hpDefault,
                       @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                       @JsonProperty("damageDefault") final int damageDefault, @JsonProperty("damage") final int damage,
                       @JsonProperty("armor") final float armor, @JsonProperty("armorDefault") final float armorDefault,
                       @JsonProperty("defence") final boolean defence) {
        super(unitId, hpDefault, hpMax, hp, damageDefault, damage, armor, armorDefault, defence);
    }

    @Override
    public void toAct(final SquareCoordinate position, final Army army) {
        army.getHeroes().keySet().forEach(coordinate -> {
            final Hero h = army.getHero(coordinate).orElseThrow();
            h.setHp(h.getHp() - calculateDamage(h));
        });
    }

    @Override
    public String getClassName() {
        return "Маг";
    }
}
