package de.raidcraft.rcgraveyards.api;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by Philip on 27.05.2015.
 */
public interface PlayerDeathInfo {

    Location getLocation();

    long getTimestamp();

    UUID getPlayerUUID();

    String getPlayerName();
}
