package de.raidcraft.rcgraveyards.util;

/**
 * @author Philip Urban
 */
public enum EquipmentDamageLevel {

    NO(1),
    VERY_LOW(0.95),
    LOW(0.9),
    MIDDLE(0.7),
    HIGH(0.5),
    VERY_HIGH(0.3);

    public double modifier;

    private EquipmentDamageLevel(double modifier) {

        this.modifier = modifier;
    }

    public double getModifier() {

        return modifier;
    }
}
