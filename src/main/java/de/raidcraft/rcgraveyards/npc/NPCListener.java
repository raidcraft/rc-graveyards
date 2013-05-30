package de.raidcraft.rcgraveyards.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Author: Philip
 * Date: 16.03.13 - 02:19
 */
public class NPCListener implements Listener {

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {

        RaidCraft.LOGGER.info("DEBUG: NPC RIGHT CLICK : " + event.getNPC().getName());
        if (!event.getNPC().hasTrait(CorpseTrait.class)) {
            return;
        }

        CorpseTrait trait = event.getNPC().getTrait(CorpseTrait.class);
        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().checkReviver(event.getClicker(), trait.getPlayerName());
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent event) {

        if (!event.getNPC().hasTrait(CorpseTrait.class)) {
            return;
        }

        CorpseTrait trait = event.getNPC().getTrait(CorpseTrait.class);
        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().checkReviver(event.getClicker(), trait.getPlayerName());
    }

    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent event) {

        if (!event.getNPC().hasTrait(CorpseTrait.class)) {
            return;
        }

        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().registerCorpse(event.getNPC());
    }

    @EventHandler
    public void onNPCDespawn(NPCDespawnEvent event) {

        if (!event.getNPC().hasTrait(CorpseTrait.class)) {
            return;
        }

        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().unregisterCorpse(event.getNPC());
    }
}
