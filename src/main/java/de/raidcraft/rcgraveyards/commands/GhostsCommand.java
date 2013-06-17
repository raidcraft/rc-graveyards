package de.raidcraft.rcgraveyards.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.util.LocationUtil;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author Philip Urban
 */
public class GhostsCommand {

    public GhostsCommand(RCGraveyardsPlugin module) {

    }

    @Command(
            aliases = {"ghost", "geist"},
            desc = "Main Command"
    )
    @NestedCommand(NestedCommands.class)
    public void rcg(CommandContext context, CommandSender sender) throws CommandException {
    }

    @Command(
            aliases = {"revive", "unghost"},
            desc = "Revive player"
    )
    @CommandPermissions("rcgraveyards.admin")
    public void revive(CommandContext context, CommandSender sender) throws CommandException {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)sender;
        String target = player.getName();
        if(context.argsLength() > 0) target = context.getString(0);
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(target);
        if(graveyardPlayer == null) {
            throw new CommandException("Es wurde kein Online-Spieler gefunden mit dem Name: " + target);
        }
        if(plugin.getGhostManager().isGhost(graveyardPlayer.getPlayer())) {
            plugin.getCorpseManager().reviveGhost(graveyardPlayer.getPlayer(), ReviveReason.COMMAND);
            graveyardPlayer.getPlayer().teleport(graveyardPlayer.getLastDeath().getLocation());
            if(!sender.getName().equalsIgnoreCase(target)) {
                player.sendMessage(ChatColor.GREEN + "Du hast " + ChatColor.YELLOW + graveyardPlayer.getPlayer().getName() + ChatColor.GREEN + " wiederbelebt!");
            }
            graveyardPlayer.getPlayer().sendMessage(ChatColor.GREEN + sender.getName() + " hat dich wiederbelebt!");
        }
        else {
            throw new CommandException("Der Spieler " + target + " ist kein Geist!");
        }
    }

    @Command(
            aliases = {"friedhof", "geisterheiler"},
            desc = "Teleports player to deathpoint"
    )
    public void friedhof(CommandContext context, CommandSender sender) throws CommandException {

        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
        Player player = (Player)sender;
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

        if(!graveyardPlayer.isGhost()) {
            throw new CommandException("Nur Geister können sich zu ihrem letzten Friedhof teleportieren!");
        }
        player.teleport(LocationUtil.improveLocation(graveyardPlayer.getClosestGraveyard(graveyardPlayer.getLastDeath().getLocation()).getLocation()));
        player.sendMessage(ChatColor.GREEN + "Du wurdest zu deinem Friedhof teleportiert!");
    }

    public static class NestedCommands {

        private final RCGraveyardsPlugin module;

        public NestedCommands(RCGraveyardsPlugin module) {

            this.module = module;
        }

        @Command(
                aliases = {"tp", "warp", "teleport"},
                desc = "Warp to players deathpoint",
                min = 1
        )
        @CommandPermissions("rcgraveyards.admin")
        public void warp(CommandContext context, CommandSender sender) throws CommandException {

            if(sender instanceof ConsoleCommandSender) {
                sender.sendMessage("Player context required!");
                return;
            }

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player)sender;

            Location deathLocation = RaidCraft.getTable(DeathsTable.class).getDeathLocation(context.getString(0), player.getWorld());
            if(deathLocation == null) {
                throw new CommandException("Es wurde für den Spieler '" + context.getString(0) + "' keinen Todespunkt gefunden!");
            }

            player.teleport(deathLocation);
            player.sendMessage(ChatColor.GREEN + "Du wurdest zum Todespunkt von '" + context.getString(0) + "' teleportiert!");
        }
    }
}
