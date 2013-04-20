package de.raidcraft.rcgraveyards.util;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philip
 */
public class MovementChecker {

    public final static MovementChecker INST = new MovementChecker();

    private Map<String, Location> playerLocations = new HashMap<>();

    public void setPlayerLocation(String player, Location location) {
        playerLocations.put(player, location);
    }

    public boolean hasMoved(String player, Location newLocation) {
        Location location = playerLocations.get(player);
        if(location == null) {
            return true;
        }

        if(newLocation.getBlockX() == location.getBlockX() &&
                newLocation.getBlockY() == location.getBlockY() &&
                newLocation.getBlockZ() == location.getBlockZ()) {
            return false;
        }
        return true;
    }
}
