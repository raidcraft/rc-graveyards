package de.raidcraft.rcgraveyards.api;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by Philip on 27.05.2015.
 */
public abstract class AbstractPlayerDeathInfo implements PlayerDeathInfo {

    private Location location;
    private long timestamp;
    private UUID uuid;
    private String playerName;

    public AbstractPlayerDeathInfo(Location location, long timestamp, UUID uuid, String playerName) {

        this.location = location;
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.playerName = playerName;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public UUID getPlayerUUID() {
        return uuid;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void updateLocation(Location location) {
        this.location = location;
    }

    @Override
    public void updateTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
