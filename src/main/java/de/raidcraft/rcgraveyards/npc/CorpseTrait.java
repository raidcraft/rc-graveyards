package de.raidcraft.rcgraveyards.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.SkullUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.trait.LookClose;
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

    public CorpseTrait() {

        super("rcgraveyards");
    }

    @Override
    public void onSpawn() {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        super.onSpawn();
        npc.getTrait(Equipment.class).set(1, SkullUtil.getPlayerSkull(playerName));
        npc.getBukkitEntity().setMetadata(RCGraveyardsPlugin.VISIBLE_FOR_GHOSTS_METADATA, new FixedMetadataValue(plugin, true));
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

    public void setLooted(boolean looted) {

        this.looted = looted;
        updateLootIndicator();
    }

    public void updateLootIndicator() {

        if(npc.getBukkitEntity() == null) return;
        if(looted) {
            npc.getTrait(Equipment.class).set(0, new ItemStack(Material.AIR));
        }
        else {
            Material material = Material.getMaterial(RaidCraft.getComponent(RCGraveyardsPlugin.class).getConfig().corpseLootIndicatorMaterial);
            npc.getTrait(Equipment.class).set(0, new ItemStack(material));
        }
    }

    public static void create(Player player, Location location) {

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.SKELETON, player.getName());
        npc.addTrait(CorpseTrait.class);
        npc.getTrait(CorpseTrait.class).setPlayerName(player.getName());
        npc.getTrait(CorpseTrait.class).setLooted(false);

        // add traits
        npc.addTrait(MobType.class);
        npc.addTrait(Spawned.class);
        npc.addTrait(LookClose.class);
        npc.addTrait(Owner.class);
        npc.addTrait(Equipment.class);


        // configure traits
        npc.getTrait(MobType.class).setType(EntityType.SKELETON);
        npc.getTrait(Spawned.class).setSpawned(true);
        npc.getTrait(LookClose.class).lookClose(true);
        npc.getTrait(Owner.class).setOwner("rcgraveyards");
        npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, true);

        npc.spawn(location);

        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCitizens().storeNPCs(new CommandContext(new String[]{}));
    }
}
