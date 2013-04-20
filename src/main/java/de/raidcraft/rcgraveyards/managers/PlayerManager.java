package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
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
}
