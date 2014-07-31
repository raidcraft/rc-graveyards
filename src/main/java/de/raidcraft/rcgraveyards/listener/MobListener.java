package de.raidcraft.rcgraveyards.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/**
 * @author Philip Urban
 */
public class MobListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onMobTargeting(EntityTargetLivingEntityEvent event) {

        Player target;
        if (!(event.getTarget() instanceof Player)) {
            return;
        }
        target = (Player) event.getTarget();
        if (RaidCraft.getComponent(RCGraveyardsPlugin.class).getGhostManager().isGhost(target)) {
            event.setCancelled(true);
        }
    }
}
