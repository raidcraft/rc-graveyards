package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.ebean.BaseModel;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import io.ebean.EbeanServer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Every graveyard that is configured will be assigned a unique database entry for reference purposes.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rc_graveyards_graveyards")
public class TGraveyard extends BaseModel {

    /**
     * Unique identifier of the graveyard config file.
     */
    private String identifier;

    /**
     * Plugin that registered the graveyard config.
     */
    private String plugin;

    /**
     * List of players that discovered this graveyard.
     */
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<TPlayerGraveyard> playerGraveyards = new ArrayList<>();

    @Override
    protected EbeanServer database() {
        return RaidCraft.getDatabase(RCGraveyardsPlugin.class);
    }
}
