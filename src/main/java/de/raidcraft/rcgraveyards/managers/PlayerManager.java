package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.storage.ItemStorage;
import de.raidcraft.api.storage.StorageException;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.tables.TStoredItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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

    // TODO: uuid rework
    public GraveyardPlayer getGraveyardPlayer(String player) {

        GraveyardPlayer graveyardPlayer = players.get(player);
        // TODO: sense?
        if(graveyardPlayer == null) {
            for(Map.Entry<String, GraveyardPlayer> entry : players.entrySet()) {
                if(entry.getKey().toLowerCase().startsWith(player.toLowerCase())) {
                    graveyardPlayer = entry.getValue();
                }
            }
        }
        return graveyardPlayer;
    }

    public long getLastDeath(String player, String world) {

        return RaidCraft.getTable(DeathsTable.class).getLastDeath(player, world);
    }

    public void updatePlayerVisibility() {

        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.hasPermission("rcgraveyards.seeall")) {
                continue;
            }
            updatePlayerVisibility(player);
        }
    }

    public void updatePlayerVisibility(Player player) {

        boolean ghost = plugin.getGhostManager().isGhost(player);
        for(Player otherPlayer : Bukkit.getOnlinePlayers()) {

            if(otherPlayer.hasPermission("rcgraveyards.seeall")) {
                continue;
            }
            if(plugin.getGhostManager().isGhost(otherPlayer)) {
                if(ghost) {
                    player.showPlayer(otherPlayer);
                }
                else {
                    player.hidePlayer(otherPlayer);
                }
            }
            else {
                if(ghost) {
                    player.hidePlayer(otherPlayer);
                }
                else {
                    player.showPlayer(otherPlayer);
                }
            }
        }
    }

    public List<ItemStack> getLootableDeathInventory(String corpseName, String world) {

        ItemStorage itemStorage = new ItemStorage("graveyards");
        List<ItemStack> items = new ArrayList<>();
        List<TStoredItem> storedItems = RaidCraft.getDatabase(RCGraveyardsPlugin.class)
                .find(TStoredItem.class).where().ieq("player", corpseName).ieq("world", world).eq("lootable", true).findList();

        for(TStoredItem storedItem : storedItems) {
            ItemStack item;
            try {
                item = itemStorage.getObject(storedItem.getStorageId());
                itemStorage.removeObject(storedItem.getStorageId());
            } catch (StorageException e) {
                continue;
            }
            items.add(item);
            RaidCraft.getDatabase(RCGraveyardsPlugin.class).delete(storedItem);
        }
        return items;
    }

    public List<ItemStack> getDeathInventory(String corpseName, String world) {

        ItemStorage itemStorage = new ItemStorage("graveyards");
        List<ItemStack> items = new ArrayList<>();
        List<TStoredItem> storedItems = RaidCraft.getDatabase(RCGraveyardsPlugin.class)
                .find(TStoredItem.class).where().ieq("player", corpseName).ieq("world", world).findList();

        for(TStoredItem storedItem : storedItems) {
            ItemStack item;
            try {
                item = itemStorage.getObject(storedItem.getStorageId());
                itemStorage.removeObject(storedItem.getStorageId());
            } catch (StorageException e) {
                continue;
            }
            items.add(item);
            RaidCraft.getDatabase(RCGraveyardsPlugin.class).delete(storedItem);
        }
        return items;
    }
}
