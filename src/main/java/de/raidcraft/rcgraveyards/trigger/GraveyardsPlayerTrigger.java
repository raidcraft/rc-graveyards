package de.raidcraft.rcgraveyards.trigger;

import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.rcgraveyards.events.RCGraveyardDiscoveredEvent;
import de.raidcraft.rcgraveyards.events.RCGraveyardPlayerRevivedEvent;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * @author mdoering
 */
public class GraveyardsPlayerTrigger extends Trigger implements Listener {

    public GraveyardsPlayerTrigger() {

        super("graveyard", "revived", "discovered");
    }

    @Information(
            value = "rcgraveyards.graveyard.revived",
            desc = "Is triggered when the player is revived. Specify the reason the filter for revival reasons.",
            conf = {
                    "reason: FOUND_CORPSE, NECROMANCER, COMMAND, CUSTOM",
            }
    )
    @EventHandler(ignoreCancelled = true)
    public void onPlayerRevive(RCGraveyardPlayerRevivedEvent event) {

        informListeners("revived", event.getGraveyardPlayer().getPlayer(),
                config -> !config.isSet("reason") || ReviveReason.valueOf(config.getString("reason")) == event.getReviveReason());
    }

    @Information(
            value = "rcgraveyards.graveyard.discovered",
            desc = "Is triggered when the player discovers a/the graveyard.",
            conf = {
                    "graveyard: unique name of the graveyard (not the friendly name!)",
            }
    )
    @EventHandler(ignoreCancelled = true)
    public void onGraveyardDiscovery(RCGraveyardDiscoveredEvent event) {

        informListeners("discovered", event.getPlayer(),
                config -> !config.isSet("graveyard") || config.getString("graveyard").equalsIgnoreCase(event.getGraveyard().getName()));
    }
}
