package de.raidcraft.rcgraveyards.util;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * @author Philip Urban
 */
public class LocationUtil {

    public static Location improveLocation(Location location) {

        Location improvedLocation = location;
        if(location.getBlock().getRelative(2, 0, 0).getType() == Material.AIR) {
            improvedLocation = location.getBlock().getRelative(2, 0, 0).getLocation();
        }
        else if(location.getBlock().getRelative(-2, 0, 0).getType() == Material.AIR) {
            improvedLocation = location.getBlock().getRelative(-2, 0, 0).getLocation();
        }
        else if(location.getBlock().getRelative(0, 0, 2).getType() == Material.AIR) {
            improvedLocation = location.getBlock().getRelative(0, 0, 2).getLocation();
        }
        else if(location.getBlock().getRelative(0, 0, -2).getType() == Material.AIR) {
            improvedLocation = location.getBlock().getRelative(0, 0, -2).getLocation();
        }
        return improvedLocation;
    }
}
