package de.raidcraft.rcgraveyards.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.MovementChecker;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

/**
 * @author Philip Urban
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().login(event.getPlayer());
        plugin.getGhostManager().addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().logout(event.getPlayer().getName());
        plugin.getGhostManager().removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(event.getEntity().getName());
        graveyardPlayer.setLastDeathLocation(event.getEntity().getLocation());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        Location deathLocation = graveyardPlayer.getLastDeathLocation();
        if(deathLocation == null) return;

        Graveyard graveyard = graveyardPlayer.getClosestGraveyard(deathLocation);
        if(graveyard == null) return;
        event.setRespawnLocation(graveyard.getLocation());
        player.sendMessage(ChatColor.DARK_GREEN + "Du bist am Friedhof " + ChatColor.GOLD + graveyard.getFriendlyName() + ChatColor.DARK_GREEN + " respawned.");
        graveyardPlayer.setGhost(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if(!MovementChecker.INST.hasMoved(event.getPlayer().getName(), event.getTo())) {
            return;
        }
        MovementChecker.INST.setPlayerLocation(event.getPlayer().getName(), event.getTo());

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Graveyard graveyard = plugin.getGraveyardManager().getGraveyard(event.getPlayer().getLocation());
        if(graveyard == null) return;
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(event.getPlayer().getName());
        if(graveyardPlayer.knowGraveyard(graveyard)) {
            return;
        }

        if(!graveyardPlayer.isGhost()) {
            graveyardPlayer.addGraveyard(graveyard);
            event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Du hast den Friedhof " + ChatColor.GOLD + graveyard.getFriendlyName() + ChatColor.DARK_GREEN + " gefunden!");
        }
    }

    /*
     * Cancel events if player is ghost
     */

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Du kannst als Geist keine Items droppen!");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {

        if(event.getEntityType() != EntityType.PLAYER) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Du kannst als Geist mit nichts interagieren!");
    }
}
