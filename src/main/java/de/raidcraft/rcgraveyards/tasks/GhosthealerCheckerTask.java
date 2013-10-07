package de.raidcraft.rcgraveyards.tasks;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.RCConversationsPlugin;
import de.raidcraft.rcconversations.npc.ConversationsTrait;
import de.raidcraft.rcconversations.npc.NPCRegistry;
import de.raidcraft.rcconversations.util.ChunkLocation;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * @author Philip Urban
 */
public class GhosthealerCheckerTask implements Runnable {

    RCGraveyardsPlugin plugin;
    Graveyard graveyard;

    public GhosthealerCheckerTask(RCGraveyardsPlugin plugin, Graveyard graveyard) {

        this.plugin = plugin;
        this.graveyard = graveyard;
    }

    @Override
    public void run() {

        // check a second time
        boolean found = false;
        for(ChunkLocation cl : NPCRegistry.INST.getAffectedChunkLocations(graveyard.getLocation().getChunk())) {
            for(Entity entity : graveyard.getLocation().getChunk().getWorld().getChunkAt(cl.getX(), cl.getZ()).getEntities()) {
                if(!(entity instanceof LivingEntity)) continue;
                if(entity.getLocation().distance(graveyard.getLocation()) <= 5) {
                    NPC npc = RaidCraft.getComponent(RCConversationsPlugin.class).getCitizens().getNPCRegistry().getNPC(entity);
                    if(npc == null) continue;
                    ConversationsTrait trait = npc.getTrait(ConversationsTrait.class);
                    if(!trait.getConversationName().equalsIgnoreCase(plugin.getConfig().necromancerConversationName)) continue;

                    if(found) {
                        NPCRegistry.INST.unregisterNPC(npc);
                        npc.destroy();
                    }
                    else {
                        found = true;
                    }
                }
            }
        }

        if(!found) {
            RaidCraft.LOGGER.warning("[Graveyards] Created new Geisterheiler in " + graveyard.getFriendlyName());
            ConversationsTrait.create(graveyard.getLocation(), plugin.getConfig().necromancerConversationName, "Geisterheiler", false);
        }
    }
}
