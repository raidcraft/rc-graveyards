package de.raidcraft.rcgraveyards.api;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by Philip on 27.05.2015.
 */
public interface PlayerDeathInfo {

    public Location getLocation();

    public long getTimestamp();

    public UUID getPlayerUUID();

    public String getPlayerName();

    public void updateLocation(Location location);

    public void updateTimestamp(long timestamp);
}
