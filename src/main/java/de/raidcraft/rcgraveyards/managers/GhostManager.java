package de.raidcraft.rcgraveyards.managers;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import de.raidcraft.api.packets.Packet28EntityMetadata;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class GhostManager implements Listener {

    private static final long UPDATE_DELAY = 20L;

    // Players that are actually ghosts
    private Set<Player> ghosts = new HashSet<>();

    public GhostManager(Plugin plugin) {

        createTask(plugin);

        ProtocolLibrary.getProtocolManager().addPacketListener(
            new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, Packets.Server.ENTITY_METADATA) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if(!isGhost(event.getPlayer())) return;
                    switch (event.getPacketID()) {
                        case Packets.Server.ENTITY_METADATA:
                            Packet28EntityMetadata packet28 = new Packet28EntityMetadata(event.getPacket());
                            Entity entity = packet28.getEntity(event);
                            if(entity.hasMetadata(RCGraveyardsPlugin.VISIBLE_FOR_GHOSTS_METADATA)) return;

                            WrappedDataWatcher watcher = new WrappedDataWatcher(packet28.getEntityMetadata());
                            Byte flag = watcher.getByte(0);
                            if (flag != null) {
                                packet28 = new Packet28EntityMetadata(packet28.getHandle().deepClone());
                                watcher = new WrappedDataWatcher(packet28.getEntityMetadata());

                                if(entity instanceof Sheep) {
                                    Byte woolState = watcher.getByte(16);
                                    watcher.setObject(16, (byte) (woolState | 16));
                                }

                                watcher.setObject(0, (byte) (flag | 32));
                                event.setPacket(packet28.getHandle());
                            }
                            break;
                    }
                }
            }
        );
    }

    private void createTask(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(isGhost(player)) {
                        addPotionEffects(player);
                    }
                }
            }
        }, UPDATE_DELAY, UPDATE_DELAY);
    }

    /**
     * Determine if the given player is tracked by this ghost manager and is a ghost.
     * @param player - the player to test.
     * @return TRUE if it is, FALSE otherwise.
     */
    public boolean isGhost(Player player) {
        return player != null && ghosts.contains(player);
    }

    public Set<Player> getGhosts() {

        return ghosts;
    }

    /**
     * Set wheter or not a given player is a ghost.
     * @param player - the player to set as a ghost.
     * @param isGhost - TRUE to make the given player into a ghost, FALSE otherwise.
     */
    public void setGhost(Player player, boolean isGhost) {

        if (isGhost) {
            ghosts.add(player);
            addPotionEffects(player);
        } else if (!isGhost) {
            ghosts.remove(player);
            removePotionEffects(player);
            sendResetPackages(player);
        }
    }

    public void removePlayer(Player player) {

        ghosts.remove(player);
    }

    private void addPotionEffects(Player player) {

//        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
//        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
    }

    private void removePotionEffects(Player player) {

//        player.removePotionEffect(PotionEffectType.SPEED);
//        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    private void sendResetPackages(Player player) {

        for(Entity entity : player.getNearbyEntities(16, 16, 16)) {

            if(!(entity instanceof LivingEntity)) continue;
            ((LivingEntity) entity).removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
}