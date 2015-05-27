package de.raidcraft.rcgraveyards.deathinfo;

import de.raidcraft.rcgraveyards.api.AbstractPlayerDeathInfo;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by Philip on 27.05.2015.
 */
public class OfflinePlayerDeathInfo extends AbstractPlayerDeathInfo {

    public OfflinePlayerDeathInfo(Location location, long timestamp, UUID uuid, String playerName) {
        super(location, timestamp, uuid, playerName);
    }
}
