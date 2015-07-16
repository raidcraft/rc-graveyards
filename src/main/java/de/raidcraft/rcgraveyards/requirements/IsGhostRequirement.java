package de.raidcraft.rcgraveyards.requirements;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public class IsGhostRequirement implements Requirement<Player> {

    @Override
    @Information(
            value = "player.is-ghost",
            desc = "Checks if the player is currently a ghost and can be revived.",
            aliases = {"CHECK_IF_GHOST", "IS_GHOST", "player.isghost"}
    )
    public boolean test(Player player, ConfigurationSection config) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        return graveyardPlayer != null && graveyardPlayer.isGhost();
    }
}
