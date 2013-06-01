package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.tables.ItemStackTable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
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

    public long getLastDeath(String player, String world) {

        return RaidCraft.getTable(DeathsTable.class).getLastDeath(player, world);
    }

    public void updatePlayerVisibility() {

        for(Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerVisibility(player);
        }
    }

    public void updatePlayerVisibility(Player player) {

        if(plugin.getGhostManager().isGhost(player) || player.hasPermission("rcgraveyards.seeall")) {
            for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if(onlinePlayer.getName().equalsIgnoreCase(player.getName())) continue;
                player.showPlayer(onlinePlayer);
            }
            return;
        }
        for(OfflinePlayer offlinePlayer : plugin.getGhostManager().getGhosts()) {
            if(!offlinePlayer.isOnline() || offlinePlayer.getName().equalsIgnoreCase(player.getName())) continue;
            player.hidePlayer(offlinePlayer.getPlayer());
        }
    }

    public List<ItemStack> getLootableDeathInventory(String corpseName, String world) {

        List<ItemStack> items = RaidCraft.getTable(ItemStackTable.class).getInventory(corpseName, world, true);
        RaidCraft.getTable(ItemStackTable.class).delete(corpseName, world, true);
        return items;
    }

    public List<ItemStack> getDeathInventory(String corpseName, String world) {

        List<ItemStack> items = RaidCraft.getTable(ItemStackTable.class).getInventory(corpseName, world, false);
        RaidCraft.getTable(ItemStackTable.class).delete(corpseName, world, false);
        return items;
    }
}
