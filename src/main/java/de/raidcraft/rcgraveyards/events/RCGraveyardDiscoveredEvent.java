package de.raidcraft.rcgraveyards.events;

import de.raidcraft.rcgraveyards.api.Graveyard;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RCGraveyardDiscoveredEvent extends Event {

    private final Player player;
    private final Graveyard graveyard;

    public RCGraveyardDiscoveredEvent(Player player, Graveyard graveyard) {

        this.player = player;
        this.graveyard = graveyard;
    }

    /*///////////////////////////////////////////////////
    //              Needed Bukkit Stuff
    ///////////////////////////////////////////////////*/

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
