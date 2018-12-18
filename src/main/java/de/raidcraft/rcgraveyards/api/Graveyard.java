package de.raidcraft.rcgraveyards.api;

import de.raidcraft.rcgraveyards.GraveyardPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public interface Graveyard {

    /**
     * @return unique identifier of the graveyard.
     */
    String getIdentifier();

    /**
     * @return human readable friendly name of the graveyard.
     */
    String getName();

    /**
     * @return player respawn location of the graveyard.
     */
    Location getLocation();

    /**
     * @return list of graveyard types.
     */
    EnumSet<Type> getTypes();

    /**
     * The discovery radius defined how close the player must get to discover the graveyard.
     * @return radius when the player will discover the graveyard.
     */
    int getDiscoveryRadius();

    /**
     * Maximum radius this graveyard will trigger respawns for the player.
     * The nearest graveyard should always be used.
     * @return radius to trigger the respawn of the player
     */
    int getRespawnRadius();

    /**
     * A list of worldguard regions this graveyard is active in.
     * If the list is empty only the {@link #getRespawnRadius()} will be used.
     * @return list of worldguard regions that trigger the respawn in this graveyard.
     */
    Collection<String> getRegions();

    /**
     * Checks if the graveyard can respawn the given player.
     * This will check if the player is inside the region, in range and if the player unlocked the graveyard.
     *
     * @param player to check respawn capability for
     * @return true if graveyard can respawn the player as ghost
     */
    boolean canRespawn(GraveyardPlayer player);

    enum Type {

        MAIN,
        SECRET,
        RESTRICTED;

        public static final EnumSet<Type> ALL_OPTS = EnumSet.allOf(Type.class);
    }
}
