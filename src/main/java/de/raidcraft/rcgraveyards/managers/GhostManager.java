package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GhostManager implements Listener, de.raidcraft.api.player.GhostManager {

    private static final long UPDATE_DELAY = 20L;

    // Players that are actually ghosts
    private Set<UUID> ghosts = new HashSet<>();

    public GhostManager(Plugin plugin) {

        createTask(plugin);
        RaidCraft.registerComponent(de.raidcraft.api.player.GhostManager.class, this);
        // TODO: finish packet hiding
        //        ProtocolLibrary.getProtocolManager().addPacketListener(
        //            new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, Packets.Server.ENTITY_METADATA) {
        //                @Override
        //                public void onPacketSending(PacketEvent event) {
        //                    if(!isGhost(event.getPlayer())) return;
        //                    switch (event.getPacketID()) {
        //                        case Packets.Server.ENTITY_METADATA:
        //                            Packet28EntityMetadata packet28 = new Packet28EntityMetadata(event.getPacket());
        //                            Entity entity = packet28.getEntity(event);
        //                            if(entity.hasMetadata(RCGraveyardsPlugin.VISIBLE_FOR_GHOSTS_METADATA)) return;
        //
        //                            WrappedDataWatcher watcher = new WrappedDataWatcher(packet28.getEntityMetadata());
        //                            Byte flag = watcher.getByte(0);
        //                            if (flag != null) {
        //                                packet28 = new Packet28EntityMetadata(packet28.getHandle().deepClone());
        //                                watcher = new WrappedDataWatcher(packet28.getEntityMetadata());
        //
        //                                if(entity instanceof Sheep) {
        //                                    Byte woolState = watcher.getByte(16);
        //                                    watcher.setObject(16, (byte) (woolState | 16));
        //                                }
        //
        //                                watcher.setObject(0, (byte) (flag | 32));
        //                                event.setPacket(packet28.getHandle());
        //                            }
        //                            break;
        //                    }
        //                }
        //            }
        //        );
    }

    private void createTask(Plugin plugin) {
        // TODO: why a delayed task?
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (isGhost(player)) {
                        addPotionEffects(player);
                    }
                }
            }
        }, UPDATE_DELAY, UPDATE_DELAY);
    }

    /**
     * Determine if the given player is tracked by this ghost manager and is a ghost.
     *
     * @param player - the player to test.
     *
     * @return TRUE if it is, FALSE otherwise.
     */
    @Override
    public boolean isGhost(Player player) {

        return player != null && ghosts.contains(player.getUniqueId());
    }

    public Set<UUID> getGhosts() {

        return ghosts;
    }

    /**
     * Set wheter or not a given player is a ghost.
     *
     * @param player  - the player to set as a ghost.
     * @param isGhost - TRUE to make the given player into a ghost, FALSE otherwise.
     */
    public void setGhost(Player player, boolean isGhost) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        if (isGhost) {
            ghosts.add(player.getUniqueId());
            player.setMetadata(RCGraveyardsPlugin.PLAYER_IS_GHOST_METADATA, new FixedMetadataValue(plugin, true));
            addPotionEffects(player);
        } else {
            ghosts.remove(player.getUniqueId());
            player.removeMetadata(RCGraveyardsPlugin.PLAYER_IS_GHOST_METADATA, plugin);
            removePotionEffects(player);
            resetFakes(player);
        }
    }

    public void removePlayer(Player player) {

        ghosts.remove(player);
    }

    private void addPotionEffects(Player player) {

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 1));
        // TODO: ghostfactory? e.g. invisbile 13?
        // TODO: register more listener to abort player interaction events
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
    }

    private void removePotionEffects(Player player) {

        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
    }

    // TODO: finish packet manipulation
    private void resetFakes(Player player) {

        //        try {
        //            for(Entity entity : player.getNearbyEntities(16, 16, 16)) {
        //
        //                if(!(entity instanceof LivingEntity)) continue;
        //
        //                PacketContainer packetContainer = ProtocolLibrary.getProtocolManager().createPacket(
        //                        Packets.Server.MAP_CHUNK);
        //
        //                Packet33ChunkData packet = new Packet33ChunkData(packetContainer);
        //                packet.
        //                packet.setEntityId(entity.getEntityId());
        //                WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getEntityMetadata());
        //                watcher
        //
        //                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        //            }
        //        } catch (InvocationTargetException e) {
        //        }
    }
}