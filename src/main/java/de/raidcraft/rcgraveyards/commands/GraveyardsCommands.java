package de.raidcraft.rcgraveyards.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.rcconversations.npc.ConversationsTrait;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.util.LocationUtil;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author Philip Urban
 */
public class GraveyardsCommands {

    public GraveyardsCommands(RCGraveyardsPlugin module) {

    }

    @Command(
            aliases = {"rcg", "graveyards"},
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
                aliases = {"reload"},
                desc = "Reload plugin"
        )
        @CommandPermissions("rcgraveyards.admin")
        public void reload(CommandContext context, CommandSender sender) throws CommandException {

            RaidCraft.getComponent(RCGraveyardsPlugin.class).reload();
            sender.sendMessage(ChatColor.GREEN + "RCGraveyards wurde neugeladen!");
        }

        @Command(
                aliases = {"create"},
                desc = "Create graveyard",
                min = 1,
                flags = "m"
        )
        @CommandPermissions("rcgraveyards.admin")
        public void create(CommandContext context, CommandSender sender) throws CommandException {

            if(sender instanceof ConsoleCommandSender) {
                sender.sendMessage("Player context required!");
                return;
            }

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player)sender;

            String name = context.getString(0);
            if(plugin.getGraveyardManager().getGraveyard(name) != null) {
                throw new CommandException("Es gibt bereits einen Friedhof mit diesem Namen!");
            }

            int size = plugin.getConfig().defaultSize;
            if(context.argsLength() > 1) {
                size = context.getInteger(1);
            }

            Graveyard graveyard = new Graveyard(name, player.getLocation(), size, context.hasFlag('m'), player.getName());
            plugin.getGraveyardManager().registerNewGraveyard(graveyard);
            ConversationsTrait.create(player.getLocation(), plugin.getConfig().necromancerConversationName, "Geisterheiler", false);
            sender.sendMessage(ChatColor.GREEN + "Friedhof " + ChatColor.YELLOW + name +  ChatColor.GREEN + " wurde erstellt!");
        }

        @Command(
                aliases = {"delete", "remove"},
                desc = "Delete graveyard",
                min = 1
        )
        @CommandPermissions("rcgraveyards.admin")
        public void delete(CommandContext context, CommandSender sender) throws CommandException {

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);

            String name = context.getString(0);
            Graveyard graveyard = plugin.getGraveyardManager().getGraveyard(name);
            if(graveyard == null) {
                throw new CommandException("Es gibt keinen Friedhof mit diesem Namen!");
            }

            plugin.getGraveyardManager().deleteGraveyard(graveyard);
            sender.sendMessage(ChatColor.GREEN + "Friedhof " + ChatColor.YELLOW + name +  ChatColor.GREEN + " wurde gelöscht!");
        }

        @Command(
                aliases = {"tp", "warp", "teleport"},
                desc = "Warp to graveyard",
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

            String name = context.getString(0);
            Graveyard graveyard = plugin.getGraveyardManager().getGraveyard(name);
            if(graveyard == null) {
                throw new CommandException("Es gibt keinen Friedhof mit diesem Namen!");
            }

            player.teleport(graveyard.getLocation());
            player.sendMessage(ChatColor.GREEN + "Zum Friedhof " + ChatColor.YELLOW + graveyard.getName() + ChatColor.GREEN + " teleportiert.");
        }

        @Command(
                aliases = {"list"},
                desc = "List all graveyards"
        )
        @CommandPermissions("rcgraveyards.admin")
        public void list(CommandContext context, CommandSender sender) throws CommandException {

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player)sender;

            String list = "";
            ChatColor color;
            int i = 0;
            for (Graveyard graveyard : plugin.getGraveyardManager().getGraveyards()) {
                i++;
                if(i % 2 == 0) color = ChatColor.YELLOW;
                else color = ChatColor.WHITE;
                list += color + graveyard.getFriendlyName() + ", ";
            }

            player.sendMessage(ChatColor.GREEN + "Es gibt derzeit " + i + " Friedhöfe:");
            sender.sendMessage(list);
        }

        @Command(
                aliases = {"test"},
                desc = "Test command"
        )
        @CommandPermissions("rcgraveyards.admin")
        public void test(CommandContext context, CommandSender sender) throws CommandException {

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player)sender;
            GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());

            player.setCompassTarget(graveyardPlayer.getLastDeath().getLocation());

            player.sendMessage(ChatColor.GREEN + "Dein Kompass zeigt nun auf deinen letzen Todespunkt!");
        }
    }
}
