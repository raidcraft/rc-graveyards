package de.raidcraft.rcgraveyards.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public class RevivePlayerAction implements Action<Player> {

    @Information(
            value = "rcgraveyards.player.revive",
            desc = "Revives the given player with the ReviveReason.CUSTOM"
    )
    @Override
    public void accept(Player player, ConfigurationSection config) {

        GraveyardPlayer graveyardPlayer = RaidCraft.getComponent(RCGraveyardsPlugin.class).getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        graveyardPlayer.revive(ReviveReason.CUSTOM);
    }
}
