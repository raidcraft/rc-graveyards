package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.ebean.BaseModel;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import io.ebean.EbeanServer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * When a player dies, a death record will be created in this table.
 * Every death will be recorded. Check if {@link #respawn} is null to see if the player was revived.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rc_graveyards_player_deaths")
public class TPlayerDeath extends BaseModel {

    private String player;
    private UUID playerId;

    /**
     * The reason the player died.
     * Can be anything, e.g. PVP, LAVA, MOB:ankanor.sandfloh, FALL, PLUGIN
     */
    private String reason;

    /**
     * Serialized base64 encoded string of the players inventory upon death.
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

    /**
     * Null until the player respawned/revived.
     * Records the reason and time the player was revived.
     */
    @OneToOne(cascade = CascadeType.REMOVE)
    private TPlayerRespawn respawn;

    public void setPlayer(Player player) {
        Validate.notNull(player);

        this.player = player.getName();
        setPlayerId(player.getUniqueId());
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(getPlayerId());
    }

    public void setDeathLocation(Location location) {
        Validate.notNull(location);

        setWorld(location.getWorld().getName());
        setX(location.getX());
        setY(location.getY());
        setZ(location.getZ());
        setPitch(location.getPitch());
        setYaw(location.getYaw());
    }

    public Location getDeathLocation() {
        return new Location(Bukkit.getWorld(world), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    @Override
    protected EbeanServer database() {
        return RaidCraft.getDatabase(RCGraveyardsPlugin.class);
    }
}
