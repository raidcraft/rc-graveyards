package de.raidcraft.rcgraveyards.conversations;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.conversations.conversation.ConversationEndReason;
import de.raidcraft.api.conversations.conversation.ConversationTemplate;
import de.raidcraft.api.conversations.host.ConversationHost;
import de.raidcraft.conversations.conversations.PlayerConversation;
import de.raidcraft.conversations.hosts.NPCHost;
import de.raidcraft.rcgraveyards.ConfiguredGraveyard;
import de.raidcraft.rcgraveyards.api.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GraveyardConversation extends PlayerConversation {

    private Graveyard playerGraveyard;
    private ConfiguredGraveyard necroGraveyard;

    public GraveyardConversation(Player player, ConversationTemplate conversationTemplate, ConversationHost conversationHost) {

        super(player, conversationTemplate, conversationHost);
        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
        playerGraveyard = graveyardPlayer.getClosestGraveyard(graveyardPlayer.getLastDeath().getLocation());
        necroGraveyard = plugin.getGraveyardManager().getClosestGraveyard(getHost().getLocation());

        // destroy npcs without graveyard
        if (necroGraveyard.getLocation().distance(getHost().getLocation()) > 5) {
            end(ConversationEndReason.ERROR);
            if (getHost() instanceof NPCHost) {
                ((NPCHost) getHost()).getType().destroy();
            }
        }

        set("player_graveyard", playerGraveyard.getFriendlyName());
        set("necro_graveyard", necroGraveyard.getFriendlyName());
        set("can_revive", necroGraveyard.equals(playerGraveyard));
        set("graveyard", playerGraveyard.getFriendlyName());
    }
}
