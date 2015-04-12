package de.raidcraft.rcgraveyards.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.Skull;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import lombok.Getter;
import lombok.Setter;
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

import java.util.UUID;

/**
 * Author: Philip
 * Date: 22.11.12 - 21:21
 * Description:
 */
public class CorpseTrait extends Trait {

    @Setter
    @Getter
    @Persist
    private String playerName;

    @Persist
    private String playerId;
    @Persist
    private boolean looted;

    private String robberId;

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

    public boolean isLooted() {

        return looted;
    }

    public void setLooted(boolean looted, UUID robber) {

        this.looted = looted;
        setRobberId(robber);
        updateLootIndicator();
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
                player.getName(), RCGraveyardsPlugin.REGISTER_HOST);
        npc.setBukkitEntityType(EntityType.SKELETON);
        npc.setProtected(true);
        npc.addTrait(CitizensAPI.getTraitFactory().getTraitClass("lookclose"));

        npc.addTrait(CorpseTrait.class);
        npc.getTrait(CorpseTrait.class).setPlayerId(player.getUniqueId());
        npc.getTrait(CorpseTrait.class).setPlayerName(player.getName());
        npc.getTrait(CorpseTrait.class).setLooted(false, null);

        npc.spawn(location);
        NPC_Manager.getInstance().store(RCGraveyardsPlugin.REGISTER_HOST);
    }

    public void setPlayerId(UUID playerUUID) {

        if (playerUUID == null) {
            this.playerId = null;
            return;
        }

        this.playerId = playerUUID.toString();
    }

    public UUID getPlayerId() {

        if (this.playerId == null) {
            return null;
        }
        return UUID.fromString(this.playerId);
    }

    public void setRobberId(UUID robberUUID) {

        if (robberUUID == null) {
            this.robberId = null;
            return;
        }
        this.robberId = robberUUID.toString();
    }

    public UUID getRobberId() {

        if (robberId == null) {
            return null;
        }
        return UUID.fromString(robberId);
    }

}
