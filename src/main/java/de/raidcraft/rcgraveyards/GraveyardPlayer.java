package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import de.raidcraft.rcgraveyards.util.PlayerInventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class GraveyardPlayer {

    private Player player;
    private Map<String, Graveyard> graveyards = new HashMap<>();
    private boolean ghost = false;
    private Death lastDeath;

    public GraveyardPlayer(Player player) {

        this.player = player;
        for (Graveyard graveyard : RaidCraft.getComponent(RCGraveyardsPlugin.class)
                .getGraveyardManager().getPlayerGraveyards(player.getUniqueId())) {
            graveyards.put(graveyard.getName(), graveyard);
        }

        // load from database
        lastDeath = RaidCraft.getTable(DeathsTable.class).getDeath(player);
        if (lastDeath == null) {
            lastDeath = new Death(player);
        } else {
            setGhost(true);
        }
    }

    public Player getPlayer() {

        return player;
    }

    public Graveyard getClosestGraveyard(Location location) {

        double distance = 0;
        Graveyard closestGraveyard = null;
        for (Map.Entry<String, Graveyard> entry : graveyards.entrySet()) {
            if ((closestGraveyard == null || entry.getValue().getLocation().distance(location) < distance)
                    && (entry.getValue().getRadius() == 0 || entry.getValue().getRadius() >= distance)) {
                closestGraveyard = entry.getValue();
                distance = entry.getValue().getLocation().distance(location);
            }
        }
        return closestGraveyard;
    }

    public Graveyard getLastDeathGraveyard() {

        return getClosestGraveyard(getLastDeath().getLocation());
    }

    public boolean knowGraveyard(Graveyard graveyard) {

        return graveyards.containsKey(graveyard.getName());
    }

    public void addGraveyard(Graveyard graveyard) {

        graveyards.put(graveyard.getName(), graveyard);
        RaidCraft.getTable(PlayerGraveyardsTable.class).addAssignment(player.getUniqueId(), graveyard);
    }

    public boolean isGhost() {

        return ghost;
    }

    public void setGhost(boolean ghost) {

        this.ghost = ghost;
        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);

        // set player opacity
        if (ghost) {
            // set as ghost
            plugin.getGhostManager().setGhost(player, true);
            // clear inventory
            player.getInventory().clear();
            // give compass
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta itemMeta = compass.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GOLD + plugin.getTranslationProvider().tr(player,
                    "ghost.compass.title", "Shows you the way to your corpse."));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + plugin.getTranslationProvider().tr(player,
                    "ghost.compass.right-click", "Right Click: Returns you to the graveyard you spawned at."));
            itemMeta.setLore(lore);
            compass.setItemMeta(itemMeta);
            player.getInventory().setItemInHand(compass);
            // set compass target
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {

                    player.setCompassTarget(lastDeath.getLocation());
                }
            }, 1);

            PlayerInventoryUtil.putInInventory(player, new ItemStack(Material.ENDER_PEARL, 64));
            PlayerInventoryUtil.putInInventory(player, new ItemStack(Material.BOAT, 2));
            save();
        } else {
            player.setFireTicks(0);
            plugin.getGhostManager().setGhost(player, false);
            // delete db entries
            RaidCraft.getTable(DeathsTable.class).delete(player);
        }
        plugin.getPlayerManager().updatePlayerVisibility();
    }

    public Death getLastDeath() {

        return lastDeath;
    }

    public void save() {

        RaidCraft.getTable(DeathsTable.class).addDeath(lastDeath, player);
    }
}
