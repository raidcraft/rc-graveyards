package de.raidcraft.rcgraveyards.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rcconversations.conversations.EndReason;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.ReviveInformation;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import org.bukkit.ChatColor;

/**
 * @author Philip Urban
 */
@ActionInformation(name = "REVIVE_GHOST")
public class ReviveGhostAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);

        int delay = args.getInt("delay", 0);

        if (delay > 0) {
            conversation.getPlayer().sendMessage(ChatColor.GREEN + "Deine Seele kehrt in " + delay + " Sek. zurück!");
        }

        plugin.getCorpseManager().delayingReviver.addGhostToRevive(conversation.getPlayer(), new ReviveInformation(delay, false, null, ReviveReason.NECROMANCER));
        conversation.endConversation(EndReason.INFORM);
    }
}
