package de.raidcraft.rcgraveyards.tables;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Philip Urban
 */
@Entity
@Table(name = "rcgraveyards_items")
public class TStoredItem {

    @Id
    private int id;
    private int storageId;
    private boolean lootable;
    private String player;
    private String world;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getStorageId() {

        return storageId;
    }

    public void setStorageId(int storageId) {

        this.storageId = storageId;
    }

    public String getPlayer() {

        return player;
    }

    public void setPlayer(String player) {

        this.player = player;
    }

    public String getWorld() {

        return world;
    }

    public void setWorld(String world) {

        this.world = world;
    }

    public boolean isLootable() {

        return lootable;
    }

    public void setLootable(boolean lootable) {

        this.lootable = lootable;
    }
}
