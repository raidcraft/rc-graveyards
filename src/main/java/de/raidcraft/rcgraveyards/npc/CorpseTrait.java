package de.raidcraft.rcgraveyards.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.Skull;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Author: Philip
 * Date: 22.11.12 - 21:21
 * Description:
 */
public class CorpseTrait extends Trait {

    @Persist
    private String playerName;
    @Persist
    private boolean looted;
    @Persist
    private String robber;

    public CorpseTrait() {

        super(RC_Traits.GRAVEYARDS);
    }

    @Override
    public void onSpawn() {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        npc.getTrait(Equipment.class).set(1, Skull.getSkull(playerName));
        npc.getEntity().setMetadata(RCGraveyardsPlugin.VISIBLE_FOR_GHOSTS_METADATA, new FixedMetadataValue(plugin, true));
        updateLootIndicator();
    }

    public String getPlayerName() {

        return playerName;
    }

    public void setPlayerName(String playerName) {

        this.playerName = playerName;
    }

    public boolean isLooted() {

        return looted;
    }

    public void setLooted(boolean looted, String robber) {

        this.looted = looted;
        this.robber = robber;
        updateLootIndicator();
    }

    public String getRobber() {

        return robber;
    }

    public void updateLootIndicator() {

        if (npc.getEntity() == null) return;
        if (looted) {
            npc.getTrait(Equipment.class).set(0, new ItemStack(Material.AIR));
        } else {
            Material material = Material.getMaterial(RaidCraft.getComponent(RCGraveyardsPlugin.class).getConfig().corpseLootIndicatorMaterial);
            npc.getTrait(Equipment.class).set(0, new ItemStack(material));
        }
    }

    public static void create(Player player, Location location) {

        NPC npc = NPC_Manager.getInstance().createPersistNpc(
                player.getName(), RaidCraft.getComponent(RCGraveyardsPlugin.class).getName());
        npc.setBukkitEntityType(EntityType.SKELETON);
        npc.setProtected(true);
        npc.addTrait(CitizensAPI.getTraitFactory().getTraitClass("lookclose"));

        npc.addTrait(CorpseTrait.class);
        npc.getTrait(CorpseTrait.class).setPlayerName(player.getName());
        npc.getTrait(CorpseTrait.class).setLooted(false, null);

        npc.spawn(location);
        NPC_Manager.getInstance().store(RaidCraft.getComponent(RCGraveyardsPlugin.class).getName());
    }
}
