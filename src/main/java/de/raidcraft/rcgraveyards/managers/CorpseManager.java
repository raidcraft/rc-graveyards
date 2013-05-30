package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
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
        long lastDeath = plugin.getPlayerManager().getLastDeath(trait.getPlayerName());
        if(lastDeath < System.currentTimeMillis() - plugin.getConfig().corpseDuration*1000) {
            npc.destroy();
        }
        else {
            registeredCorpse.put(trait.getPlayerName().toLowerCase(), npc);
        }
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
            lootCorpse(player);
        }
    }

    private void reviveGhost(Player player) {

        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        graveyardPlayer.setGhost(false);
        deleteCorpse(player.getName());

        player.sendMessage(ChatColor.GREEN + "Du hast dich wiederbelebt und deine Items zurück bekommen!");
    }

    private void lootCorpse(Player player) {

        deleteCorpse(player.getName());
    }
}
