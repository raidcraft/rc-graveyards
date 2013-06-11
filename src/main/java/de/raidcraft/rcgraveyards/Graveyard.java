package de.raidcraft.rcgraveyards;


import de.raidcraft.util.DateUtil;
import org.bukkit.Location;

/**
 * @author Philip Urban
 */
public class Graveyard {

    private String name;
    private Location location;
    private int size;
    private boolean main;
    private int radius;
    private String creator;
    private String creationDate;

    public Graveyard(String name, Location location, int size, boolean main, int radius, String creator, String creationDate) {

        this.name = name;
        this.location = location;
        this.size = size;
        this.main = main;
        this.radius = radius;
        this.creator = creator;
        this.creationDate = creationDate;
    }

    public Graveyard(String name, Location location, int size, boolean main, int radius, String creator) {

        this(name, location, size, main, radius, creator, DateUtil.getCurrentDateString());
    }

    public String getName() {

        return name;
    }

    public String getFriendlyName() {

        return name.replace("_", " ");
    }

    public Location getLocation() {

        return location;
    }

    public int getSize() {

        return size;
    }

    public boolean isMain() {

        return main;
    }

    public int getRadius() {

        return radius;
    }

    public String getCreator() {

        return creator;
    }

    public String getCreationDate() {

        return creationDate;
    }
}
