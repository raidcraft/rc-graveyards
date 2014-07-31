package de.raidcraft.rcgraveyards.tables;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * @author Philip Urban
 */
@Setter
@Getter
@Entity
@Table(name = "rcgraveyards_items")
public class TStoredItem {

    @Id
    private int id;
    private int storageId;
    private boolean lootable;
    private UUID playerId;
    private String world;
}
