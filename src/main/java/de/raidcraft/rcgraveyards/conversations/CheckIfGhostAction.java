package de.raidcraft.rcgraveyards.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;

/**
 * @author Philip Urban
 */
@ActionInformation(name = "CHECK_IF_GHOST")
public class CheckIfGhostAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(conversation.getPlayer().getName());

        String success = args.getString("onsuccess", null);
        String failure = args.getString("onfailure", null);

        if (graveyardPlayer.isGhost()) {
            changeStage(conversation, success);
        } else {
            changeStage(conversation, failure);
        }
    }

    private void changeStage(Conversation conversation, String stage) {

        if (stage != null) {
            conversation.setCurrentStage(stage);
            conversation.triggerCurrentStage();
        }
    }
}
