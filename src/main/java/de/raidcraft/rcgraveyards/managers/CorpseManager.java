package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

/**
 * @author Philip Urban
 */
public class CorpseManager {

    private RCGraveyardsPlugin plugin;

    public CorpseManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;
    }

    public void registerCorpse(NPC npc) {

        //TODO save in database
    }

    public void deleteCorpse(Location location, String name) {

        //TODO delete NPC and remove db entry
    }
}
