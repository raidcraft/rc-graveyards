package de.raidcraft.rcgraveyards.events;

import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RCGraveyardPlayerRevivedEvent extends Event {

    private final GraveyardPlayer graveyardPlayer;
    private final ReviveReason reviveReason;

    public RCGraveyardPlayerRevivedEvent(GraveyardPlayer graveyardPlayer, ReviveReason reason) {

        this.graveyardPlayer = graveyardPlayer;
        this.reviveReason = reason;
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
