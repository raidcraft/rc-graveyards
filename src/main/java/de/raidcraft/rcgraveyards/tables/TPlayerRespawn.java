package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.ebean.BaseModel;
import de.raidcraft.api.storage.ItemStorage;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.util.InventoryUtils;
import de.raidcraft.util.SerializationUtil;
import io.ebean.EbeanServer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * After a player died his death is recorded in the {@link TPlayerDeath} table.
 * When he respawns a respawn record should be created in this table.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rc_graveyards_player_respawns")
public class TPlayerRespawn extends BaseModel {

    private String player;
    private UUID playerId;

    /**
     * The reason the player respawned.
     * Can be anything, e.g. COMMAND, PLUGIN, GRAVEYARD, CORPSE, TIMEOUT
     */
    private String reason;

    /**
     * Serialized base64 encoded string of the players inventory before respawn.
     * Serialize and deserialize with {@link de.raidcraft.util.SerializationUtil#playerInventoryToBase64(PlayerInventory)}
     * and {@link de.raidcraft.util.SerializationUtil#playerInventoryFromBase64(String[])}.
     */
    @Column(length = 4196)
    private String playerInventory;

    private String world;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;

    public void setPlayer(Player player) {
        Validate.notNull(player);

        this.player = player.getName();
        setPlayerId(player.getUniqueId());
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(getPlayerId());
    }

    public void setRespawnLocation(Location location) {
        Validate.notNull(location);

        setWorld(location.getWorld().getName());
        setX(location.getX());
        setY(location.getY());
        setZ(location.getZ());
        setPitch(location.getPitch());
        setYaw(location.getYaw());
    }

    public Location getRespawnLocation() {
        return new Location(Bukkit.getWorld(world), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    @Override
    protected EbeanServer database() {
        return RaidCraft.getDatabase(RCGraveyardsPlugin.class);
    }
}
