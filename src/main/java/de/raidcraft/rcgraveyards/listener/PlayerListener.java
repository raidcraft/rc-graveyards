package de.raidcraft.rcgraveyards.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.MovementChecker;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class PlayerListener implements Listener {

    private Map<String, Location> playersDeathLocation = new HashMap<>();

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().login(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().logout(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        playersDeathLocation.put(event.getEntity().getName(), event.getEntity().getLocation());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        Location deathLocation = playersDeathLocation.remove(event.getPlayer().getName());
        if(deathLocation == null) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Graveyard graveyard = plugin.getPlayerManager().getGraveyardPlayer(event.getPlayer().getName()).getClosestGraveyard(deathLocation);
        if(graveyard == null) return;
        event.getPlayer().teleport(graveyard.getLocation());
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
        if(graveyardPlayer.knowGraveyard(graveyard)) return;

        graveyardPlayer.addGraveyard(graveyard);
        event.getPlayer().sendMessage(ChatColor.GREEN + "Du hast den Friedhof " + ChatColor.YELLOW + graveyard.getName() + ChatColor.GREEN + " gefunden!");
    }
}
