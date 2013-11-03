package de.raidcraft.rcgraveyards.tasks;

import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Philip Urban
 */
public class CorpseCreateTask implements Runnable {

    Player player;
    Location location;

    public CorpseCreateTask(Player player, Location location) {

        this.player = player;
        this.location = location;
    }

    public void run() {
        if(location.getY() < 0) return;
        CorpseTrait.create(player, location);
    }
}
