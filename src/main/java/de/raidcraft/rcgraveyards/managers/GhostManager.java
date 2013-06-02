package de.raidcraft.rcgraveyards.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

//        ProtocolLibrary.getProtocolManager().addPacketListener(
//            new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK) {
//                @Override
//                public void onPacketSending(PacketEvent event) {
//                    if(!isGhost(event.getPlayer())) return;
//                    switch (event.getPacketID()) {
//                        case Packets.Server.MAP_CHUNK:
//                            translateMapChunk(event.getPacket(), event.getPlayer());
//                            break;
//                        case Packets.Server.MAP_CHUNK_BULK:
//                            translateMapChunkBulk(event.getPacket(), event.getPlayer());
//                            break;
//                    }
//                }
//            }
//        );
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
}