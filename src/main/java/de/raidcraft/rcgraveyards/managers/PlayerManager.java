package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.random.GenericRDSTable;
import de.raidcraft.api.random.GenericRDSValue;
import de.raidcraft.api.random.RDSObject;
import de.raidcraft.api.random.RDSRandom;
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
import java.util.Optional;
import java.util.UUID;

/**
 * @author Philip Urban
 */
public class PlayerManager {

    private RCGraveyardsPlugin plugin;
    private Map<UUID, GraveyardPlayer> players = new HashMap<>();

    public PlayerManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;
    }

    public void login(Player player) {

        players.put(player.getUniqueId(), new GraveyardPlayer(player));
    }

    public void logout(Player player) {

        players.remove(player.getUniqueId());
    }

    public GraveyardPlayer getGraveyardPlayer(UUID player) {
        return players.get(player);
    }

    public long getLastDeath(UUID player, String world) {

        return RaidCraft.getTable(DeathsTable.class).getLastDeath(player, world);
    }

    public void updatePlayerVisibility() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("rcgraveyards.seeall")) {
                continue;
            }
            updatePlayerVisibility(player);
        }
    }

    public void updatePlayerVisibility(Player player) {

        boolean ghost = plugin.getGhostManager().isGhost(player);
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {

            if (otherPlayer.hasPermission("rcgraveyards.seeall")) {
                continue;
            }
            if (plugin.getGhostManager().isGhost(otherPlayer)) {
                if (ghost) {
                    player.showPlayer(otherPlayer);
                } else {
                    player.hidePlayer(otherPlayer);
                }
            } else {
                if (ghost) {
                    player.hidePlayer(otherPlayer);
                } else {
                    player.showPlayer(otherPlayer);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<ItemStack> getLootableDeathInventory(UUID corpseId, String world) {

        ItemStorage itemStorage = new ItemStorage("graveyards");
        List<ItemStack> items = new ArrayList<>();
        List<TStoredItem> storedItems = RaidCraft.getDatabase(RCGraveyardsPlugin.class)
                .find(TStoredItem.class).where().ieq("player_id", corpseId.toString()).ieq("world", world).eq("lootable", true).findList();

        GenericRDSTable table = new GenericRDSTable();
        for (TStoredItem storedItem : storedItems) {
            table.addEntry(new GenericRDSValue<>(storedItem));
        }
        table.setCount(RDSRandom.getIntValue(plugin.getConfig().minLootCount, plugin.getConfig().maxLootCount));
        for (RDSObject object : table.getResult()) {
            if (object instanceof GenericRDSValue) {
                Optional<TStoredItem> value = ((GenericRDSValue<TStoredItem>) object).getValue();
                if (value.isPresent()) {
                    ItemStack item;
                    try {
                        item = itemStorage.getObject(value.get().getStorageId());
                        itemStorage.removeObject(value.get().getStorageId());
                        items.add(item);
                        RaidCraft.getDatabase(RCGraveyardsPlugin.class).delete(value.get());
                    } catch (StorageException ignored) {
                    }
                }
            }
        }
        return items;
    }

    public List<ItemStack> getDeathInventory(UUID corpseId, String world) {

        ItemStorage itemStorage = new ItemStorage("graveyards");
        List<ItemStack> items = new ArrayList<>();
        List<TStoredItem> storedItems = RaidCraft.getDatabase(RCGraveyardsPlugin.class)
                .find(TStoredItem.class).where().ieq("player_id", corpseId.toString()).ieq("world", world).findList();

        for (TStoredItem storedItem : storedItems) {
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
