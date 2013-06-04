package de.raidcraft.rcgraveyards.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.npc.ConversationsTrait;
import de.raidcraft.rcconversations.npc.NPCRegistry;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.util.LocationUtil;
import de.raidcraft.rcgraveyards.util.MovementChecker;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.List;

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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        graveyardPlayer.getLastDeath().setLocation(player.getLocation().clone());
        graveyardPlayer.getLastDeath().setTimestamp(System.currentTimeMillis());
        graveyardPlayer.getLastDeath().saveInventory();
        event.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

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
        Bukkit.getScheduler().runTaskLater(plugin, new CorpseCreator(player, deathLocation), 4 * 20);
        Bukkit.getScheduler().runTaskLater(plugin, new GhosthealerChecker(plugin, graveyard), 4 * 20);
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

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Du kannst als Geist keine Items droppen!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {

        if(event.getEntityType() != EntityType.PLAYER) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLivingEntityDamage(EntityDamageByEntityEvent event) {

        if(event.getDamager() instanceof Player) {
            Player player = (Player)event.getDamager();
            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

            if(graveyardPlayer.isGhost()) event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = event.getPlayer();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Du kannst als Geist mit nichts interagieren!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void inHunger(FoodLevelChangeEvent event) {

        if(event.getEntityType() != EntityType.PLAYER) return;

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)event.getEntity();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(graveyardPlayer.isGhost()) {
            event.setCancelled(true);
        }
    }

    public class CorpseCreator implements Runnable {

        Player player;
        Location location;

        public CorpseCreator(Player player, Location location) {

            this.player = player;
            this.location = location;
        }

        public void run() {
            CorpseTrait.create(player, location);
        }
    }

    public class GhosthealerChecker implements Runnable {

        RCGraveyardsPlugin plugin;
        Graveyard graveyard;

        public GhosthealerChecker(RCGraveyardsPlugin plugin, Graveyard graveyard) {

            this.plugin = plugin;
            this.graveyard = graveyard;
        }

        @Override
        public void run() {

            List<NPC> npcs = NPCRegistry.INST.getSpawnedNPCs(graveyard.getLocation().getChunk());
            boolean found = false;
            for(NPC npc : npcs) {
                String conversationName = npc.getTrait(ConversationsTrait.class).getConversationName();
                if(conversationName != null && conversationName.equalsIgnoreCase(plugin.getConfig().necromancerConversationName)) {
                    if(found) {
                        NPCRegistry.INST.unregisterNPC(npc);
                        npc.destroy();
                        break;
                    }
                    else {
                        found = true;
                    }
                }
            }
            if(!found) {
                ConversationsTrait.create(graveyard.getLocation(), plugin.getConfig().necromancerConversationName, "Geisterheiler", false);
            }
        }
    }
}
