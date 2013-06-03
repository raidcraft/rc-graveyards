package de.raidcraft.rcgraveyards.npc;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import net.citizensnpcs.api.event.*;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Author: Philip
 * Date: 16.03.13 - 02:19
 */
public class NPCListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(NPCRightClickEvent event) {

        if(!checkClickEvent(event)) return;

        CorpseTrait trait = event.getNPC().getTrait(CorpseTrait.class);
        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().checkReviver(event.getClicker(), trait.getPlayerName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeftClick(NPCLeftClickEvent event) {

        if(!checkClickEvent(event)) return;

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

    private boolean checkClickEvent(NPCClickEvent event) {

        if (!event.getNPC().hasTrait(CorpseTrait.class)) {
            if(RaidCraft.getComponent(RCGraveyardsPlugin.class).getGhostManager().isGhost(event.getClicker())
                    && !event.getNPC().getBukkitEntity().hasMetadata(RCGraveyardsPlugin.VISIBLE_FOR_GHOSTS_METADATA)) {
                event.setCancelled(true);
                event.getClicker().sendMessage(ChatColor.RED + "Du kannst als Geist mit niemanden sprechen!");
            }
            return false;
        }
        return true;
    }
}
