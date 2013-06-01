package de.raidcraft.rcgraveyards.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.managers.CorpseManager;
import org.bukkit.ChatColor;

/**
 * @author Philip Urban
 */
@ActionInformation(name = "REVIVE_GHOST")
public class ReviveGhostAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList actionArgumentList) throws ActionArgumentException {
        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        plugin.getCorpseManager().reviveGhost(conversation.getPlayer(), CorpseManager.ReviveReason.NECROMANCER);
        conversation.getPlayer().sendMessage(ChatColor.GREEN + "Du hast dich wiederbelebt. Du hast dein Equipment zurück bekommen.");
    }
}
