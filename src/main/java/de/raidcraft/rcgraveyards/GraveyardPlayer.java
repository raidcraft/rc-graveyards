package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class GraveyardPlayer {

    private Player player;
    private Map<String, Graveyard> graveyards = new HashMap<>();
    private boolean ghost = false;
    private Location lastDeathLocation;

    public GraveyardPlayer(Player player) {

        this.player = player;
        for(Graveyard graveyard : RaidCraft.getComponent(RCGraveyardsPlugin.class).getGraveyardManager().getPlayerGraveyards(player.getName())) {
            graveyards.put(graveyard.getName(), graveyard);
        }
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
            plugin.getGhostManager().setGhost(player, true);
            player.setCompassTarget(lastDeathLocation);
        }
        else {
            plugin.getGhostManager().setGhost(player, false);
        }
    }

    public Location getLastDeathLocation() {

        return lastDeathLocation;
    }

    public void setLastDeathLocation(Location lastDeathLocation) {

        this.lastDeathLocation = lastDeathLocation;
    }
}
