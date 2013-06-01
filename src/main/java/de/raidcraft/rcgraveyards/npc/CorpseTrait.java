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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Author: Philip
 * Date: 22.11.12 - 21:21
 * Description:
 */
public class CorpseTrait extends Trait {

    @Persist
    private String playerName;

    public CorpseTrait() {

        super("rcgraveyards");
    }

    @Override
    public void onSpawn() {

        super.onSpawn();
        npc.getTrait(Equipment.class).set(1, SkullUtil.getPlayerSkull(playerName));
    }

    public String getPlayerName() {

        return playerName;
    }

    public void setPlayerName(String playerName) {

        this.playerName = playerName;
    }

    public static void create(Player player, Location location) {

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.SKELETON, player.getName());
        npc.addTrait(CorpseTrait.class);
        npc.getTrait(CorpseTrait.class).setPlayerName(player.getName());

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

        RaidCraft.LOGGER.info("DEBUG: " + location.getWorld().getName() + " | " + location.getBlockX() + " | " + location.getBlockY() + " | " + location.getBlockZ());
        npc.spawn(location);

        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCitizens().storeNPCs(new CommandContext(new String[]{}));
    }
}
