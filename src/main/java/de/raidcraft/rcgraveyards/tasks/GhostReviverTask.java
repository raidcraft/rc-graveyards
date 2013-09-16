package de.raidcraft.rcgraveyards.tasks;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.ReviveInformation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class GhostReviverTask implements Runnable {

    private Map<Player, ReviveInformation> ghosts = new HashMap<>();

    public GhostReviverTask() {

    }

    public GhostReviverTask(Player player, ReviveInformation reviveInformation) {

        addGhostToRevive(player, reviveInformation);
    }

    public boolean addGhostToRevive(Player player, ReviveInformation reviveInformation) {

        if(ghosts.containsKey(player)) {
            return false;
        }
        reviveInformation.increaseReviveDelay();
        ghosts.put(player, reviveInformation);
        return true;
    }

    @Override
    public void run() {

        Map<Player, ReviveInformation> ghostsCopy = new HashMap<>(ghosts);
        for(Map.Entry<Player, ReviveInformation> entry : ghostsCopy.entrySet()) {

            if(!entry.getKey().isOnline()) ghosts.remove(entry.getKey());

            ReviveInformation info = entry.getValue();
            info.decreaseReviveDelay();
            ghosts.put(entry.getKey(), info);
            int delay = info.getReviveDelay();
            if(delay > 10) continue;
            if(delay <= 0) {
                entry.getKey().sendMessage(ChatColor.GREEN + "Du bist wieder lebendig.");
                if(info.isLooted() && !info.getReason().isEquipmentOnly()) {
                    entry.getKey().sendMessage(ChatColor.GREEN + "Deine Leiche wurde jedoch von " + ChatColor.YELLOW +  info.getRobber() + ChatColor.GREEN + " ausgeraubt!");
                }
                RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().reviveGhost(entry.getKey(), info.getReason());
                ghosts.remove(entry.getKey());
                continue;
            }
            entry.getKey().sendMessage(ChatColor.GREEN + "* " + delay);
        }
    }
}
