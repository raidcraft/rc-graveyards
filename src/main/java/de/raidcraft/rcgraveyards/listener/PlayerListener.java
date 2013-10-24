package de.raidcraft.rcgraveyards.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tasks.CorpseCreateTask;
import de.raidcraft.rcgraveyards.tasks.GhosthealerCheckerTask;
import de.raidcraft.rcgraveyards.util.LocationUtil;
import de.raidcraft.rcgraveyards.util.MovementChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.*;

/**
 * @author Philip Urban
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().login(event.getPlayer());
        plugin.getPlayerManager().updatePlayerVisibility(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getPlayerManager().logout(event.getPlayer().getName());
        plugin.getGhostManager().removePlayer(event.getPlayer());
    }

    @EventHandler()
    public void onPlayerDeath(PlayerDeathEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        graveyardPlayer.getLastDeath().setLocation(player.getLocation().clone());
        graveyardPlayer.getLastDeath().setTimestamp(System.currentTimeMillis());
        graveyardPlayer.getLastDeath().saveInventory(event.getDrops());
        graveyardPlayer.save();
        event.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        if (graveyardPlayer == null) {
            return;
        }

        Location deathLocation = graveyardPlayer.getLastDeath().getLocation();
        if(deathLocation == null) return;

        Graveyard graveyard = graveyardPlayer.getClosestGraveyard(deathLocation);
        if(graveyard == null) return;
        // let the player rewspawn near the graveyard location
        event.setRespawnLocation(LocationUtil.improveLocation(graveyard.getLocation()));
        player.sendMessage("****");
        player.sendMessage(ChatColor.RED + "Du bist am Friedhof " + ChatColor.YELLOW + graveyard.getFriendlyName() + ChatColor.RED + " als Geist respawned.");
        player.sendMessage(ChatColor.GOLD + "Der Kompass zeigt dir den Weg zurück zu deiner Leiche und deinem Inventar.");
        player.sendMessage(ChatColor.GOLD + "Oder nutze den Geisterheiler hier auf dem Friedhof und verliere dadurch Items.");
        player.sendMessage(ChatColor.GRAY + "Falls du dich verläufst kommst du mit /friedhof hier her zurück.");
        player.sendMessage("****");
        graveyardPlayer.setGhost(true);
        // create corpse delayed
        Bukkit.getScheduler().runTaskLater(plugin, new CorpseCreateTask(player, deathLocation), 2 * 20);
        Bukkit.getScheduler().runTaskLater(plugin, new GhosthealerCheckerTask(plugin, graveyard), 20);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if(!MovementChecker.INST.hasMoved(event.getPlayer().getName(), event.getTo())) {
            return;
        }
        MovementChecker.INST.setPlayerLocation(event.getPlayer().getName(), event.getTo());

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Graveyard graveyard = plugin.getGraveyardManager().getGraveyard(event.getPlayer().getLocation());
        if(graveyard == null) return;
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(event.getPlayer().getName());
        if (graveyardPlayer == null) {
            return;
        }
        if(graveyardPlayer.knowGraveyard(graveyard)) {
            return;
        }

        if(!graveyardPlayer.isGhost()) {
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
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        if (graveyardPlayer == null) {
            return;
        }

        if(graveyardPlayer.isGhost()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        if (graveyardPlayer == null) {
            return;
        }

        if(graveyardPlayer.isGhost()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Du kannst als Geist keine Items droppen!");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {

        if(!(event.getEntity() instanceof Player)) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        if (graveyardPlayer == null) {
            return;
        }

        if(graveyardPlayer.isGhost()) {
            if(event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.FIRE) {
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
        if(event.getDamager() instanceof Player) {
            Player player = (Player)event.getDamager();
            GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
            if (graveyardPlayer == null) {
                return;
            }

            if(graveyardPlayer.isGhost()) event.setCancelled(true);
        } else if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster) {
            GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(((Player) event.getEntity()).getName());
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
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer == null) return;
        if(!graveyardPlayer.isGhost()) return;
        if(event.getAction() == Action.PHYSICAL) return;
        if(event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL) {
            if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Du musst mit der Enderperle in die Luft klicken!");
                return;
            }
            else if(event.getAction() == Action.RIGHT_CLICK_AIR) {
                return;
            }
        }
        if(event.getClickedBlock() != null
                && (event.getClickedBlock().getType() == Material.WOOD_DOOR
                || event.getClickedBlock().getType() == Material.WOOD_BUTTON
                || event.getClickedBlock().getType() == Material.STONE_BUTTON
                || event.getClickedBlock().getType() == Material.LEVER
        )) return;

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Du kannst als Geist mit nichts interagieren!");
    }

    @EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)
    public void inHunger(FoodLevelChangeEvent event) {

        if(event.getEntityType() != EntityType.PLAYER) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        if (graveyardPlayer == null) {
            return;
        }

        if(graveyardPlayer.isGhost()) {
            event.setCancelled(true);
        }
    }
}
