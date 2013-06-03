package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.util.*;
import de.raidcraft.util.CustomItemUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class CorpseManager {

    private RCGraveyardsPlugin plugin;
    private Map<String, NPC> registeredCorpse = new HashMap<>();
    public final GhostReviver delayingReviver = new GhostReviver();

    public CorpseManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, delayingReviver, 20, 20);
    }

    public void registerCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);
        deleteCorpse(trait.getPlayerName());
        registeredCorpse.put(trait.getPlayerName().toLowerCase(), npc);
    }

    public void unregisterCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);
        registeredCorpse.remove(trait.getPlayerName().toLowerCase());
    }

    public void deleteCorpse(String name) {

        NPC npc = registeredCorpse.remove(name.toLowerCase());
        if(npc != null) {
            npc.destroy();
        }
    }

    public void checkReviver(Player player, String corpseName) {

        boolean ghost = plugin.getGhostManager().isGhost(player);
        NPC npc = registeredCorpse.get(corpseName.toLowerCase());
        boolean looted = npc.getTrait(CorpseTrait.class).isLooted();
        String robber = npc.getTrait(CorpseTrait.class).getRobber();


        if(player.getName().equalsIgnoreCase(corpseName)) {

            ReviveReason reason = ReviveReason.FOUND_CORPSE;
            long lastDeath = plugin.getPlayerManager().getLastDeath(corpseName, npc.getBukkitEntity().getWorld().getName());
            if(lastDeath < System.currentTimeMillis() - plugin.getConfig().corpseDuration*1000) {
                reason = ReviveReason.NECROMANCER;
            }

            if(delayingReviver.addGhostToRevive(player, new ReviveInformation(plugin.getConfig().ghostReviveDuration, looted, robber, reason))) {
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

        player.getInventory().clear();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        List<ItemStack> loot = plugin.getPlayerManager().getDeathInventory(player.getName(), player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                if(CustomItemUtil.isEquipment(itemStack)) {
                    double modifier = reason.getDamageLevel().getModifier();
                    if(graveyardPlayer.getLastDeath().wasPvp()) {
                        modifier = EquipmentDamageLevel.VERY_LOW.getModifier();
                    }
                    double durability = (short)((double)itemStack.getDurability() * modifier);
                    //TODO: use future methods in CustomItemUtil!
                    itemStack.setDurability((short)durability);
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
