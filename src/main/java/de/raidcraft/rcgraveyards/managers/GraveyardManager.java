package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tables.GraveyardsTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
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
        reload();
    }

    public void reload() {

        sortedGraveyards.clear();
        graveyardsByName.clear();

        for(World world : Bukkit.getWorlds()) {
            List<Graveyard> graveyards = RaidCraft.getTable(GraveyardsTable.class).getAll(world.getName());
            for(Graveyard graveyard : graveyards) {
                graveyardsByName.put(graveyard.getName().toLowerCase(), graveyard);

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

        return graveyardsByName.get(name.toLowerCase());
    }

    public Graveyard getGraveyard(Location location) {

        String worldName = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if(sortedGraveyards.containsKey(worldName)) {
            if(sortedGraveyards.get(worldName).containsKey(x)) {
                if(sortedGraveyards.get(worldName).get(x).containsKey(y)) {
                    if(sortedGraveyards.get(worldName).get(x).get(y).containsKey(z)) {
                        return sortedGraveyards.get(worldName).get(x).get(y).get(z);
                    }
                }
            }
        }
        return null;
    }

    public List<Graveyard> getPlayerGraveyards(String player) {

        List<Graveyard> graveyards = new ArrayList<>();
        List<String> graveyardNames = RaidCraft.getTable(PlayerGraveyardsTable.class).getPlayerAssignments(player);
        for(String graveyardName : graveyardNames) {

            Graveyard graveyard = getGraveyard(graveyardName);
            if(graveyard != null) {
                graveyards.add(graveyard);
            }
        }
        for(Map.Entry<String, Graveyard> entry : graveyardsByName.entrySet()) {
            if(entry.getValue().isMain() && !graveyards.contains(entry.getValue())) {
                graveyards.add(entry.getValue());
            }
        }

        return graveyards;
    }

    public void registerNewGraveyard(Graveyard graveyard) {

        RaidCraft.getTable(GraveyardsTable.class).createGraveyard(graveyard);
        reload();
    }
}
