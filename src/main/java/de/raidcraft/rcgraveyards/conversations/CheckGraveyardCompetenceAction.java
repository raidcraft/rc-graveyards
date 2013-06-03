package de.raidcraft.rcgraveyards.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.api.action.AbstractAction;
import de.raidcraft.rcconversations.api.action.ActionArgumentException;
import de.raidcraft.rcconversations.api.action.ActionArgumentList;
import de.raidcraft.rcconversations.api.action.ActionInformation;
import de.raidcraft.rcconversations.api.conversation.Conversation;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;

/**
 * @author Philip Urban
 */
@ActionInformation(name = "CHECK_GRAVEYARD_COMPETENCE")
public class CheckGraveyardCompetenceAction extends AbstractAction {

    @Override
    public void run(Conversation conversation, ActionArgumentList args) throws ActionArgumentException {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);

        String success = args.getString("onsuccess", null);
        String failure = args.getString("onfailure", null);
    }
}
