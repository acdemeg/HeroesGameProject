package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Footman extends Hero {

    public Footman(final int hp, final int damage, final float precision, final float armor) {
        super(hp, damage, precision, armor);
    }

    @JsonCreator
    protected Footman(@JsonProperty("unitId") final int unitId, @JsonProperty("hpDefault") final int hpDefault,
                      @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                      @JsonProperty("damageDefault") final int damageDefault, @JsonProperty("damage") final int damage,
                      @JsonProperty("armor") final float armor, @JsonProperty("armorDefault") final float armorDefault,
                      @JsonProperty("defence") final boolean defence) {
        super(unitId, hpDefault, hpMax, hp, damageDefault, damage, armor, armorDefault, defence);
    }

    @Override
    public String getClassName() {
        return "Мечник";
    }
}