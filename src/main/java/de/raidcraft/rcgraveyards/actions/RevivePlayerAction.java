package de.raidcraft.rcgraveyards.actions;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.ReviveInformation;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import de.raidcraft.util.TimeUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
public class RevivePlayerAction implements Action<Player> {

    @Information(
            value = "player.revive",
            desc = "Revives the given player with the given reason. The reason influenced the damage done to the items.",
            conf = {
                    "delay: 0s",
                    "reason: CUSTOM/NECROMANCER(25%)/COMMAND/FOUND_CORPSE(10%)"
            },
            aliases = "REVIVE_GHOST"
    )
    @Override
    public void accept(Player player, ConfigurationSection config) {

        long delay = TimeUtil.parseTimeAsTicks(config.getString("delay", "0"));

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        ReviveReason reason = ReviveReason.valueOf(config.getString("reason", "CUSTOM"));

        if (delay > 0) {
            player.sendMessage("Deine Seele kehrt in " + TimeUtil.getFormattedTime(TimeUtil.ticksToSeconds(delay)) + " zurück.");
            plugin.getCorpseManager().delayingReviver.addGhostToRevive(player, new ReviveInformation(delay, false, null, reason));
        } else {
            graveyardPlayer.revive(reason);
        }
    }
}
