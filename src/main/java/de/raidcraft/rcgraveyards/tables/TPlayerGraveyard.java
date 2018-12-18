package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.ebean.BaseModel;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import io.ebean.EbeanServer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a graveyard that was discovered by a player and is stored in the database.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rc_graveyards_player_graveyards")
public class TPlayerGraveyard extends BaseModel {

    /**
     * Name of the player.
     */
    private String player;

    /**
     * Unique id of the player.
     */
    private UUID playerId;

    /**
     * Id of the referenced graveyard.
     */
    @ManyToOne(cascade = CascadeType.REMOVE)
    private TGraveyard graveyard;

    @Override
    protected EbeanServer database() {
        return RaidCraft.getDatabase(RCGraveyardsPlugin.class);
    }
}
