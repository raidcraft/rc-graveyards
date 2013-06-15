package de.raidcraft.rcgraveyards.util;

/**
 * @author Philip Urban
 */
public enum EquipmentDamageLevel {

    NO(0),
    VERY_LOW(0.05),
    LOW(0.1),
    MIDDLE(0.3),
    HIGH(0.25),
    VERY_HIGH(0.7);

    public double modifier;

    private EquipmentDamageLevel(double modifier) {

        this.modifier = modifier;
    }

    public double getModifier() {

        return modifier;
    }
}
