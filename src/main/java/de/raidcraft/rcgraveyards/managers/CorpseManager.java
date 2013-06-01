package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import net.citizensnpcs.api.npc.NPC;
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

    public CorpseManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;
    }

    public void registerCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);

        // remove corpse if too old
        long lastDeath = plugin.getPlayerManager().getLastDeath(trait.getPlayerName(), npc.getBukkitEntity().getWorld().getName());
        if(lastDeath < System.currentTimeMillis() - plugin.getConfig().corpseDuration*1000) {
            RaidCraft.LOGGER.info("DEBUG: " + lastDeath);
            npc.destroy();
        }
        else {
            deleteCorpse(trait.getPlayerName());
            registeredCorpse.put(trait.getPlayerName().toLowerCase(), npc);
        }
        RaidCraft.LOGGER.info("DEBUG: NPC: " + npc);
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


        if(plugin.getGhostManager().isGhost(player) && player.getName().equalsIgnoreCase(corpseName)) {
                reviveGhost(player);
        }
        else {
            lootCorpse(player, corpseName);
        }
    }

    private void reviveGhost(Player player) {

        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        graveyardPlayer.setGhost(false);
        deleteCorpse(player.getName());
        List<ItemStack> loot = plugin.getPlayerManager().getDeathInventory(player.getName(), player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                player.getLocation().getWorld().dropItem(player.getLocation(), itemStack);
            }
        }

        player.sendMessage(ChatColor.GREEN + "Du hast dich wiederbelebt und deine Items zurück bekommen!");
    }

    private void lootCorpse(Player player, String corpseName) {

        deleteCorpse(corpseName);
        List<ItemStack> loot = plugin.getPlayerManager().getLootableDeathInventory(corpseName, player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                player.getLocation().getWorld().dropItem(player.getLocation(), itemStack);
            }
        }

        player.sendMessage(ChatColor.GREEN + "Du hast die Leiche von " + corpseName + " ausgeraubt (" + loot.size() + " Items)" );
    }
}
