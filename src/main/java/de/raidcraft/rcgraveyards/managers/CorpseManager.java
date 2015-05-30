package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.rcgraveyards.deathinfo.HeroDeathInfo;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.deathinfo.OfflinePlayerDeathInfo;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.tasks.GhostReviverTask;
import de.raidcraft.rcgraveyards.util.PlayerInventoryUtil;
import de.raidcraft.rcgraveyards.util.ReviveInformation;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import de.raidcraft.util.UUIDUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Philip Urban
 */
public class CorpseManager {

    private RCGraveyardsPlugin plugin;
    private Map<UUID, NPC> registeredCorpse = new HashMap<>();
    public final GhostReviverTask delayingReviver = new GhostReviverTask();

    public CorpseManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, delayingReviver, 20, 20);
    }

    public void reload() {

        if(!RCGraveyardsPlugin.SAVE_NPCS_EXTERNAL) {
            // delete all corpse npcs
            NPC_Manager.getInstance().removeAllNPCs(RCGraveyardsPlugin.REGISTER_HOST);

            // spawn all corpse npcs
            spawnCorpseNPCs();
        }
    }

    public void spawnCorpseNPCs() {

        if(!RCGraveyardsPlugin.SAVE_NPCS_EXTERNAL) {
            for(World world : Bukkit.getWorlds()) {
                List<OfflinePlayerDeathInfo> deathInfoList = RaidCraft.getTable(DeathsTable.class).getDeaths(world);
                RaidCraft.LOGGER.info("[RCGraveyards] Spawn " + deathInfoList.size() + " corpses for world: '" + world.getName() + "'");
                for (OfflinePlayerDeathInfo deathInfo : deathInfoList)
                {
                    //spawn npc
                    CorpseTrait.create(deathInfo.getPlayerName(), deathInfo.getPlayerUUID(), deathInfo.getLocation());
                }
            }
        }
    }

    public void registerCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);
        // player is not death
        if (plugin.getPlayerManager().getLastDeath(trait.getPlayerId(), npc.getEntity().getWorld().getName()) == 0) {
            RaidCraft.LOGGER.warning("[RCGraveyards] Removed corpse of not death player '" + trait.getPlayerName() + "'");
            NPC_Manager.getInstance().removeNPC(npc, RCGraveyardsPlugin.REGISTER_HOST);
            return;
        }
        registeredCorpse.put(trait.getPlayerId(), npc);
    }

    public void unregisterCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);
        registeredCorpse.remove(trait.getPlayerId());
    }

    public void deleteCorpse(UUID playerId) {

        NPC npc = registeredCorpse.remove(playerId);
        if (npc == null) {
            RaidCraft.LOGGER.warning("[Graveyards] Cannot delete Corpse: " + playerId);
            return;
        }
        NPC_Manager.getInstance().removeNPC(npc, RCGraveyardsPlugin.REGISTER_HOST);
    }

    public void checkReviver(Player player, UUID playerId, NPC npc) {

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(ChatColor.RED + "Interaktion mit der Leiche unterbunden! Du befindest dich im Creativemode!");
            return;
        }

        if (!registeredCorpse.containsKey(playerId)) {
            NPC_Manager.getInstance().removeNPC(npc, RCGraveyardsPlugin.REGISTER_HOST);
            return;
        }

        boolean ghost = plugin.getGhostManager().isGhost(player);
        boolean looted = npc.getTrait(CorpseTrait.class).isLooted();
        UUID robberId = npc.getTrait(CorpseTrait.class).getRobberId();
        long lastDeath = plugin.getPlayerManager().getLastDeath(playerId, npc.getEntity().getWorld().getName());

        if (lastDeath == 0) {
            deleteCorpse(playerId);
            return;
        }

        if (player.getUniqueId().equals(playerId)) {

            ReviveReason reason = ReviveReason.FOUND_CORPSE;

            if (lastDeath < System.currentTimeMillis() - plugin.getConfig().corpseDuration * 1000) {
                reason = ReviveReason.NECROMANCER;
            }

            if (delayingReviver.addGhostToRevive(player, new ReviveInformation(plugin.getConfig().ghostReviveDuration, looted, robberId, reason))) {
                player.getInventory().clear();
                player.sendMessage(ChatColor.GREEN + "Deine Seele kehrt in " + plugin.getConfig().ghostReviveDuration
                        + " Sek. zurück. Bringe dich in Sicherheit!");
            } else {
                player.sendMessage(ChatColor.RED + "Deine Seele ist bereits auf den Weg zurück zu dir!");
            }
            return;
        }

        if (ghost) {
            player.sendMessage(ChatColor.RED + "Du kannst als Geist keine anderen Leichen berauben!");
        } else if (looted) {
            player.sendMessage(ChatColor.RED + "Diese Leiche wurde bereits von " + UUIDUtil.getNameFromUUID(robberId) + " ausgeraubt!");
        } else {
            lootCorpse(player, playerId);
        }
    }

    public void reviveGhost(Player player, ReviveReason reason) {

        if (!player.isOnline()) return;

        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        graveyardPlayer.revive(reason);
    }

    public void lootCorpse(Player player, UUID corpseId) {

        NPC npc = registeredCorpse.get(corpseId);
        if (npc != null) {
            npc.getTrait(CorpseTrait.class).setLooted(true, player.getUniqueId());
        }
        List<ItemStack> loot = plugin.getPlayerManager().getLootableDeathInventory(corpseId, player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                PlayerInventoryUtil.putInInventory(player, itemStack);
            }
        }

        player.sendMessage(ChatColor.GREEN + "Die Leiche von " + npc.getName() + " hat " + loot.size() + " Items fallen gelassen");
    }

    public void restoreInventory(GraveyardPlayer graveyardPlayer, ReviveReason reason) {

        graveyardPlayer.restoreInventory(reason);
    }

    public void restoreInventory(Player player) {

        restoreInventory(player, 0, ReviveReason.CUSTOM);
    }

    public void restoreInventory(Player player, double modifier, ReviveReason reason) {

        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        graveyardPlayer.restoreInventory(modifier, reason);
    }
}
