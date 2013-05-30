package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class PlayerManager {

    private RCGraveyardsPlugin plugin;
    private Map<String, GraveyardPlayer> players = new HashMap<>();

    public PlayerManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;
    }

    public void login(Player player) {

        players.put(player.getName(), new GraveyardPlayer(player));
    }

    public void logout(String player) {

        players.remove(player);
    }

    public GraveyardPlayer getGraveyardPlayer(String player) {

        return players.get(player);
    }

    public long getLastDeath(String player) {

        //TODO get date from db
        return System.currentTimeMillis();
    }

    public void updatePlayerVisibility() {

        for(Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerVisibility(player);
        }
    }

    public void updatePlayerVisibility(Player player) {

        if(plugin.getGhostManager().isGhost(player) || player.hasPermission("rcgraveyards.seeall")) {
            for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                player.showPlayer(onlinePlayer);
            }
            return;
        }
        for(OfflinePlayer offlinePlayer : plugin.getGhostManager().getGhosts()) {
            if(!offlinePlayer.isOnline()) continue;
            RaidCraft.LOGGER.info("DEBUG: HIDE PLAYER FOR " + player.getName() + " : " + offlinePlayer.getName());
            player.hidePlayer(offlinePlayer.getPlayer());
        }
    }
}
