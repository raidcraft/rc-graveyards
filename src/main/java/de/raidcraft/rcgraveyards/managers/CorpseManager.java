package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.tasks.GhostReviverTask;
import de.raidcraft.rcgraveyards.util.EquipmentDamageLevel;
import de.raidcraft.rcgraveyards.util.PlayerInventoryUtil;
import de.raidcraft.rcgraveyards.util.ReviveInformation;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.CustomItemUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class CorpseManager {

    private RCGraveyardsPlugin plugin;
    // TODO: UUID rework
    private Map<String, NPC> registeredCorpse = new CaseInsensitiveMap<>();
    public final GhostReviverTask delayingReviver = new GhostReviverTask();

    public CorpseManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, delayingReviver, 20, 20);
    }

    public void registerCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);
        deleteCorpse(trait.getPlayerName());
        // player is not death
        if(plugin.getPlayerManager().getLastDeath(trait.getPlayerName(), npc.getBukkitEntity().getWorld().getName()) == 0) {
            npc.destroy();
            return;
        }
        registeredCorpse.put(trait.getPlayerName().toLowerCase(), npc);
    }

    public void unregisterCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);
        registeredCorpse.remove(trait.getPlayerName().toLowerCase());
    }

    public void deleteCorpse(String name) {
        NPC npc = registeredCorpse.remove(name.toLowerCase());
        if(npc == null) {
            RaidCraft.LOGGER.warning("[Graveyards] Cannot delete Corpse: " + name.toLowerCase());
            return;
        }
        NPC_Manager.getInstance().removeNPC(npc, plugin.getName());
    }

    public void checkReviver(Player player, String corpseName) {

        if(player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(ChatColor.RED + "Interaktion mit der Leiche unterbunden! Du befindest dich im Creativemode!");
            return;
        }

        boolean ghost = plugin.getGhostManager().isGhost(player);
        NPC npc = registeredCorpse.get(corpseName.toLowerCase());
        boolean looted = npc.getTrait(CorpseTrait.class).isLooted();
        String robber = npc.getTrait(CorpseTrait.class).getRobber();
        long lastDeath = plugin.getPlayerManager().getLastDeath(corpseName, npc.getEntity().getWorld().getName());

        if(lastDeath == 0) {
            deleteCorpse(corpseName);
            return;
        }

        if(player.getName().equalsIgnoreCase(corpseName)) {

            ReviveReason reason = ReviveReason.FOUND_CORPSE;

            if(lastDeath < System.currentTimeMillis() - plugin.getConfig().corpseDuration*1000) {
                reason = ReviveReason.NECROMANCER;
            }

            if(delayingReviver.addGhostToRevive(player, new ReviveInformation(plugin.getConfig().ghostReviveDuration, looted, robber, reason))) {
                player.getInventory().clear();
                player.sendMessage(ChatColor.GREEN + "Deine Seele kehrt in " + plugin.getConfig().ghostReviveDuration
                        + " Sek. zurück. Bringe dich in Sicherheit!");
            }
            else {
                player.sendMessage(ChatColor.RED + "Deine Seele ist bereits auf den Weg zurück zu dir!");
            }
            return;
        }

        if(ghost) {
            player.sendMessage(ChatColor.RED + "Du kannst als Geist keine anderen Leichen berauben!");
        }
        else if(looted) {
            player.sendMessage(ChatColor.RED + "Diese Leiche wurde bereits von " + robber + " ausgeraubt!");
        }
        else {
            lootCorpse(player, corpseName);
        }
    }

    public void reviveGhost(Player player, ReviveReason reason) {

        if(!player.isOnline()) return;

        player.getInventory().clear();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        List<ItemStack> loot = plugin.getPlayerManager().getDeathInventory(player.getName(), player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                if(CustomItemUtil.isCustomItem(itemStack)) {
                    double modifier = reason.getDamageLevel().getModifier();
                    if(graveyardPlayer.getLastDeath().wasPvp()) {
                        modifier = EquipmentDamageLevel.VERY_LOW.getModifier();
                    }
                    CustomItemStack customItem = RaidCraft.getCustomItem(itemStack);
                    if (customItem == null) {
                        continue;
                    }
                    try {
                        customItem.setCustomDurability(customItem.getCustomDurability() - (int) ((double) customItem.getMaxDurability() * modifier));
                        customItem.rebuild(player);
                        itemStack = customItem;
                    } catch (CustomItemException ignored) {
                    }
                }
                else {
                    if(reason.isEquipmentOnly()) continue;
                }
                PlayerInventoryUtil.putInInventory(player, itemStack);
            }
        }
        deleteCorpse(player.getName());
        graveyardPlayer.setGhost(false);
    }

    public void lootCorpse(Player player, String corpseName) {

        NPC npc = registeredCorpse.get(corpseName.toLowerCase());
        if(npc != null) {
            npc.getTrait(CorpseTrait.class).setLooted(true, player.getName());
        }
        List<ItemStack> loot = plugin.getPlayerManager().getLootableDeathInventory(corpseName, player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                PlayerInventoryUtil.putInInventory(player, itemStack);
            }
        }

        player.sendMessage(ChatColor.GREEN + "Die Leiche von " + corpseName + " hat " + loot.size() + " Items fallen gelassen");
    }
}
