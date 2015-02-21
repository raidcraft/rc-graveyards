package de.raidcraft.rcgraveyards.util;

/**
 * @author Philip Urban
 */
public enum ReviveReason {

    FOUND_CORPSE(false, EquipmentDamageLevel.LOW),
    NECROMANCER(true, EquipmentDamageLevel.HIGH),
    COMMAND(false, EquipmentDamageLevel.NO),
    CUSTOM(false, EquipmentDamageLevel.NO);

    public boolean equipmentOnly;
    public EquipmentDamageLevel damageLevel;

    private ReviveReason(boolean equipmentOnly, EquipmentDamageLevel damageLevel) {

        this.equipmentOnly = equipmentOnly;
        this.damageLevel = damageLevel;
    }

    public boolean isEquipmentOnly() {

        return equipmentOnly;
    }

    public EquipmentDamageLevel getDamageLevel() {

        return damageLevel;
    }
}
