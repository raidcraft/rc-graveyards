package de.raidcraft.rcgraveyards.tasks;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.api.PlayerDeathInfo;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Philip Urban
 */
public class CorpseCreateTask implements Runnable {

    PlayerDeathInfo deathInfo;

    public CorpseCreateTask(PlayerDeathInfo deathInfo) {

        this.deathInfo = deathInfo;
    }

    public void run() {

        if (deathInfo.getLocation().getY() < 0) return;
        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().spawnCorpseNPC(deathInfo);
    }
}
