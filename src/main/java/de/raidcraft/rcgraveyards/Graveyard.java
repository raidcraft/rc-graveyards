package de.raidcraft.rcgraveyards;


import de.raidcraft.util.DateUtil;
import lombok.Getter;
import org.bukkit.Location;

import java.util.UUID;

/**
 * @author Philip Urban
 */
@Getter
public class Graveyard {

    private String name;
    private Location location;
    private int size;
    private boolean main;
    private int radius;
    private UUID creator;
    private String creationDate;

    public Graveyard(String name, Location location, int size, boolean main, int radius, UUID creator, String creationDate) {

        this.name = name;
        this.location = location;
        this.size = size;
        this.main = main;
        this.radius = radius;
        this.creator = creator;
        this.creationDate = creationDate;
    }

    public Graveyard(String name, Location location, int size, boolean main, int radius, UUID creator) {

        this(name, location, size, main, radius, creator, DateUtil.getCurrentDateString());
    }
}
