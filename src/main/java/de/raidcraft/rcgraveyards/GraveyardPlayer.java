package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class GraveyardPlayer {

    private Player player;
    private Map<String, Graveyard> graveyards = new HashMap<>();

    public GraveyardPlayer(Player player) {

        this.player = player;
        List<String> graveyardNames = RaidCraft.getTable(PlayerGraveyardsTable.class).getPlayerAssignments(player.getName());
        for(String graveyardName : graveyardNames) {

            Graveyard graveyard = RaidCraft.getComponent(RCGraveyardsPlugin.class).getGraveyardManager().getGraveyard(graveyardName);
            if(graveyard != null) {
                graveyards.put(graveyard.getName(), graveyard);
            }
        }
    }
}
