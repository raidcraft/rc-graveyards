package de.raidcraft.rcgraveyards.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;

/**
 * @author Philip Urban
 */
@ActionInformation(name = "CHECK_GRAVEYARD_COMPETENCE")
public class CheckGraveyardCompetenceAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(conversation.getPlayer().getName());

        String success = args.getString("onsuccess", null);
        String failure = args.getString("onfailure", null);

        Graveyard playerGraveyard = graveyardPlayer.getClosestGraveyard(graveyardPlayer.getLastDeath().getLocation());
        Graveyard necroGraveyard = plugin.getGraveyardManager().getClosestGraveyard(conversation.getHost().getLocation());
        if(playerGraveyard.getName().equalsIgnoreCase(necroGraveyard.getName())) {
            if(success != null) {
                conversation.setCurrentStage(success);
                conversation.triggerCurrentStage();
            }
        }
        else {
            if(failure != null) {
                conversation.setCurrentStage(failure);
                conversation.triggerCurrentStage();
            }
        }
    }
}
