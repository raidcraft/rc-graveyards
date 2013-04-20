package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tables.GraveyardsTable;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class GraveyardManager {

    private RCGraveyardsPlugin plugin;
    private Map<String, Map<Integer, Map<Integer, Map<Integer, Graveyard>>>> sortedGraveyards = new HashMap<>();
    private Map<String, Graveyard> graveyardsByName = new HashMap<>();


    public GraveyardManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;
        for(World world : Bukkit.getWorlds()) {
            List<Graveyard> graveyards = RaidCraft.getTable(GraveyardsTable.class).getAll(world.getName());
            for(Graveyard graveyard : graveyards) {
                graveyardsByName.put(graveyard.getName(), graveyard);

                int x, y, z;
                int xDiff = graveyard.getSize();
                int yDiff = graveyard.getSize();
                int zDiff = graveyard.getSize();
                for(int i = 0; i <= xDiff; i++) {
                    x = (graveyard.getLocation().getBlockX() - graveyard.getSize() / 2) + i;
                    for(int j = 0; j <= yDiff; j++) {
                        y = (graveyard.getLocation().getBlockY() - graveyard.getSize() / 2) + j;
                        for(int k = 0; k <= zDiff; k++) {
                            z = (graveyard.getLocation().getBlockZ() - graveyard.getSize() / 2) + k;

                            if(!sortedGraveyards.containsKey(world.getName())) {
                                sortedGraveyards.put(world.getName(), new HashMap<Integer, Map<Integer, Map<Integer, Graveyard>>>());
                            }

                            if(!sortedGraveyards.get(world.getName()).containsKey(x)) {
                                sortedGraveyards.get(world.getName()).put(x, new HashMap<Integer, Map<Integer, Graveyard>>());
                            }

                            if(!sortedGraveyards.get(world.getName()).get(x).containsKey(y)) {
                                sortedGraveyards.get(world.getName()).get(x).put(y, new HashMap<Integer, Graveyard>());
                            }

                            if(!sortedGraveyards.get(world.getName()).get(x).get(y).containsKey(z)) {
                                sortedGraveyards.get(world.getName()).get(x).get(y).put(z, graveyard);
                            }
                        }
                    }
                }
            }
            RaidCraft.LOGGER.info("[RCG] Loaded " + sortedGraveyards.size() + " graveyards!");
        }
    }

    public Graveyard getGraveyard(String name) {

        return graveyardsByName.get(name);
    }
}
