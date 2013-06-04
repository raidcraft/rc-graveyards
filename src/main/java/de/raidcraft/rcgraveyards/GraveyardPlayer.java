package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.tables.ItemStackTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
        for(Graveyard graveyard : RaidCraft.getComponent(RCGraveyardsPlugin.class).getGraveyardManager().getPlayerGraveyards(player.getName())) {
            graveyards.put(graveyard.getName(), graveyard);
        }

        // load from database
        lastDeath = RaidCraft.getTable(DeathsTable.class).getDeath(player);
        if(lastDeath == null) {
            lastDeath = new Death(player);
        }
        else {
            lastDeath.setInventory(RaidCraft.getTable(ItemStackTable.class).getInventory(player));
            setGhost(true);
        }
    }

    public Player getPlayer() {

        return player;
    }

    public Graveyard getClosestGraveyard(Location location) {

        double distance = 0;
        Graveyard closestGraveyard = null;
        for(Map.Entry<String, Graveyard> entry : graveyards.entrySet()) {
            if(closestGraveyard == null || entry.getValue().getLocation().distance(location) < distance) {
                closestGraveyard = entry.getValue();
                distance = entry.getValue().getLocation().distance(location);
            }
        }
        return closestGraveyard;
    }

    public boolean knowGraveyard(Graveyard graveyard) {

        if(graveyards.containsKey(graveyard.getName())) {
            return true;
        }
        return false;
    }

    public void addGraveyard(Graveyard graveyard) {

        graveyards.put(graveyard.getName(), graveyard);
        RaidCraft.getTable(PlayerGraveyardsTable.class).addAssignment(player.getName(), graveyard);
    }

    public boolean isGhost() {

        return ghost;
    }

    public void setGhost(boolean ghost) {

        this.ghost = ghost;
        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);

        // set player opacity
        if(ghost) {
            // set as ghost
            plugin.getGhostManager().setGhost(player, true);
            // clear inventory
            player.getInventory().clear();
            // set compass target
            player.setCompassTarget(lastDeath.getLocation());
            // give compass
            player.getInventory().setItemInHand(new ItemStack(Material.COMPASS));

            save();
        }
        else {
            plugin.getGhostManager().setGhost(player, false);
            // delete db entries
            RaidCraft.getTable(DeathsTable.class).delete(player);
            RaidCraft.getTable(ItemStackTable.class).delete(player);
        }
        plugin.getPlayerManager().updatePlayerVisibility();
    }

    public Death getLastDeath() {

        return lastDeath;
    }

    public void save() {

        Bukkit.getScheduler().runTaskAsynchronously(RaidCraft.getComponent(RCGraveyardsPlugin.class), new Runnable() {
            @Override
            public void run() {

                RaidCraft.getTable(DeathsTable.class).addDeath(lastDeath, player);
                RaidCraft.getTable(ItemStackTable.class).addInventory(lastDeath.getInventory(), player);
            }
        });
    }
}
