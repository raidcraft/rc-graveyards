package de.raidcraft.rcgraveyards.tasks;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.npc.ConversationsTrait;
import de.raidcraft.rcconversations.npc.NPCRegistry;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import net.citizensnpcs.api.npc.NPC;

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

        boolean found = false;
        for(NPC npc : NPCRegistry.INST.getSpawnedNPCs(graveyard.getLocation().getChunk())) {
            if(npc.getBukkitEntity().getLocation().distance(graveyard.getLocation()) > 5) continue;
            String conversationName = npc.getTrait(ConversationsTrait.class).getConversationName();
            if(conversationName.equalsIgnoreCase(plugin.getConfig().necromancerConversationName)) {
                if(found) {
                    NPCRegistry.INST.unregisterNPC(npc);
                    npc.destroy();
                    break;
                }
                else {
                    found = true;
                }
            }
        }
        if(!found) {
            RaidCraft.LOGGER.warning("[Graveyards] Created new Geisterheiler in " + graveyard.getFriendlyName());
            ConversationsTrait.create(graveyard.getLocation(), plugin.getConfig().necromancerConversationName, "Geisterheiler", false);
        }
    }
}
