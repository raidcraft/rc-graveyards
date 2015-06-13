package de.raidcraft.rcgraveyards.listener;

import com.mewin.WGCustomFlags.FlagManager;
import com.mewin.WGCustomFlags.flags.CustomLocationFlag;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tasks.CorpseCreateTask;
import de.raidcraft.rcgraveyards.util.LocationUtil;
import de.raidcraft.rcgraveyards.util.MovementChecker;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Button;
import org.bukkit.material.Door;
import org.bukkit.material.Lever;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Philip Urban
 */
public class PlayerListener implements Listener {

    private Map<UUID, Location> respawnLocations = new HashMap<>();
    private WorldGuardPlugin worldGuard;

    public PlayerListener() {

        Plugin worldGuardPlugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null) {
            worldGuard = (WorldGuardPlugin) worldGuardPlugin;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().login(event.getPlayer());
        plugin.getPlayerManager().updatePlayerVisibility(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().logout(event.getPlayer());
        plugin.getGhostManager().removePlayer(event.getPlayer());
    }

    @EventHandler()
    public void onPlayerDeath(PlayerDeathEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        graveyardPlayer.getLastDeath().updateLocation(player.getLocation());
        graveyardPlayer.getLastDeath().updateTimestamp(System.currentTimeMillis());
        graveyardPlayer.getLastDeath().saveInventory(event.getDrops());
        graveyardPlayer.save();
        event.getDrops().clear();
    }

    // TODO: check if double call not possible
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        if (graveyardPlayer == null) {
            return;
        }

        Location deathLocation = graveyardPlayer.getLastDeath().getLocation();
        if (deathLocation == null) return;

        // check for world guard respawn plugin if support is enabled
        if (plugin.getConfig().worldGuardRespawnSupport && worldGuard != null) {
            CustomLocationFlag customFlag = (CustomLocationFlag) FlagManager.getCustomFlag("respawn-location");
            com.sk89q.worldedit.Location flagValue = worldGuard.getRegionManager(event.getPlayer().getWorld()).getApplicableRegions(deathLocation).queryValue(worldGuard.wrapPlayer(player), customFlag);
            if (flagValue != null) {
                event.setRespawnLocation(com.sk89q.worldedit.bukkit.BukkitUtil.toLocation(flagValue));
                graveyardPlayer.restoreInventory(ReviveReason.CUSTOM);
                graveyardPlayer.setGhost(false);
                return;
            }
        }

        Graveyard graveyard = graveyardPlayer.getClosestGraveyard(deathLocation);
        if (graveyard == null) return;
        // let the player rewspawn near the graveyard location
        event.setRespawnLocation(LocationUtil.improveLocation(graveyard.getLocation()));
        // TODO: i18n
        player.sendMessage("****");
        player.sendMessage(ChatColor.RED + "Du bist am Friedhof " + ChatColor.YELLOW + graveyard.getFriendlyName() + ChatColor.RED + " als Geist respawned.");
        player.sendMessage(ChatColor.GOLD + "Der Kompass zeigt dir den Weg zurück zu deiner Leiche und deinem Inventar.");
        player.sendMessage(ChatColor.GOLD + "Oder nutze den Geisterheiler hier auf dem Friedhof und verliere dadurch Items.");
        player.sendMessage(ChatColor.GRAY + "Falls du dich verläufst kommst du mit /friedhof hier her zurück.");
        player.sendMessage("****");
        graveyardPlayer.setGhost(true);
        // create corpse delayed
        if (deathLocation.getY() > 0) {
            // Spawn corpse delayed to wait for correct player object (sometimes player get only updated a few ticks after respawn)
            Bukkit.getScheduler().runTaskLater(plugin, new CorpseCreateTask(graveyardPlayer.getLastDeath()), 2 * 20);
        }
    }

    // TODO: performance, own event PlayerChunkMoveEvent?
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!MovementChecker.INST.hasMoved(event.getPlayer().getName(), event.getTo())) {
            return;
        }
        MovementChecker.INST.setPlayerLocation(event.getPlayer().getName(), event.getTo());

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Graveyard graveyard = plugin.getGraveyardManager().getGraveyard(event.getPlayer().getLocation());
        if (graveyard == null) return;
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(event.getPlayer().getUniqueId());
        if (graveyardPlayer == null) {
            return;
        }
        if (graveyardPlayer.knowGraveyard(graveyard)) {
            return;
        }

        if (!graveyardPlayer.isGhost()) {
            graveyardPlayer.addGraveyard(graveyard);
            event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Du hast den Friedhof " + ChatColor.GOLD + graveyard.getFriendlyName() + ChatColor.DARK_GREEN + " gefunden!");
        }
    }

    /*
     * Cancel events if player is ghost
     */

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemPickup(PlayerPickupItemEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        if (graveyardPlayer == null) {
            return;
        }

        if (graveyardPlayer.isGhost()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        if (graveyardPlayer == null) {
            return;
        }

        if (graveyardPlayer.isGhost()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Du kannst als Geist keine Items droppen!");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player) event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        if (graveyardPlayer == null) {
            return;
        }

        if (graveyardPlayer.isGhost()) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.FIRE) {
                player.setFireTicks(0);
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                // teleport the player back to his graveyard
                player.teleport(LocationUtil.improveLocation(graveyardPlayer.getClosestGraveyard(graveyardPlayer.getLastDeath().getLocation()).getLocation()));
                player.sendMessage(ChatColor.GREEN + "Du wurdest zu deinem Friedhof teleportiert!");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onLivingEntityDamage(EntityDamageByEntityEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
            if (graveyardPlayer == null) {
                return;
            }

            if (graveyardPlayer.isGhost()) event.setCancelled(true);
        } else if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster) {
            GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(((Player) event.getEntity()).getUniqueId());
            if (graveyardPlayer == null || !graveyardPlayer.isGhost()) {
                return;
            }
            // remove the mob target
            ((Monster) event.getDamager()).setTarget(null);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());

        if (graveyardPlayer == null) return;
        if (!graveyardPlayer.isGhost()) return;
        if (event.getAction() == Action.PHYSICAL) return;
        // ender pearl warping
        if (event.getPlayer().getItemInHand() != null) {
            if (event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Du musst mit der Enderperle in die Luft klicken!");
                    return;
                } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                    return;
                }
            } else if (event.getPlayer().getItemInHand().getType() == Material.COMPASS) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                    event.setCancelled(true);
                    player.teleport(LocationUtil.improveLocation(graveyardPlayer.getLastDeathGraveyard().getLocation()));
                    plugin.getTranslationProvider().msg(player, "ghost.teleport-back",
                            ChatColor.GREEN + "You have been teleported to the graveyard you spawned at.");
                }
            }
        }
        // boat placing
        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.BOAT) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    event.getClickedBlock().getType() != Material.WATER && event.getClickedBlock().getType() != Material.STATIONARY_WATER)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Du musst das Boot auf dem Wasser platzieren!");
                return;
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    (event.getClickedBlock().getType() == Material.WATER || event.getClickedBlock().getType() == Material.STATIONARY_WATER)) {
                return;
            }
        }

        // accept doors
        if(event.getClickedBlock() != null && event.getClickedBlock() instanceof Door) {
            return;
        }

        // accept buttons
        if(event.getClickedBlock() != null && event.getClickedBlock() instanceof Button) {
            return;
        }

        // accept levers
        if(event.getClickedBlock() != null && event.getClickedBlock() instanceof Lever) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Du kannst als Geist mit nichts interagieren!");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onMount(EntityMountEvent event)
    {
        // check if player
        if(event.getEntityType() != EntityType.PLAYER) return;
        if (event.getMount().getType() == EntityType.BOAT) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());

        if (graveyardPlayer == null) return;
        if (!graveyardPlayer.isGhost()) return;

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Du kannst als Geist mit nichts interagieren!");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onHunger(FoodLevelChangeEvent event) {

        if (event.getEntityType() != EntityType.PLAYER) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player) event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        if (graveyardPlayer == null) {
            return;
        }

        if (graveyardPlayer.isGhost()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {

        if(event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
        {
            return;
        }

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());

        if (graveyardPlayer == null) {
            return;
        }

        if (graveyardPlayer.isGhost()) {
            return;
        }

        // cancel all ender pearl teleports 5 seconds after revive
        if(graveyardPlayer.getLastRevive() > System.currentTimeMillis() -  5000) {
            event.setCancelled(true);
        }
    }
}
