package de.raidcraft.rcgraveyards.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import de.raidcraft.reference.Colors;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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
    public void rcg(CommandContext context, CommandSender sender) {

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
        public void reload(CommandContext context, CommandSender sender) {

            RaidCraft.getComponent(RCGraveyardsPlugin.class).reload();
            sender.sendMessage(ChatColor.GREEN + "RCGraveyards wurde neugeladen!");
        }

        @Command(
                aliases = {"create"},
                desc = "Create graveyard",
                min = 1,
                flags = "m",
                help = "<name> [radius]\n-m: Muss nicht entdeckt werden (Hauptfriedhof)"
        )
        @CommandPermissions("rcgraveyards.admin")
        public void create(CommandContext context, CommandSender sender) throws CommandException {

            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("Player context required!");
                return;
            }

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player) sender;

            String name = context.getString(0);
            if (plugin.getGraveyardManager().getGraveyard(name) != null) {
                throw new CommandException("Es gibt bereits einen Friedhof mit diesem Namen!");
            }

            int size = plugin.getConfig().defaultSize;
            if (context.argsLength() > 1) {
                size = context.getInteger(1);
            }

            int radius = 0;
            if (context.argsLength() > 2) {
                radius = context.getInteger(2);
            }

            Graveyard graveyard = new Graveyard(name, player.getLocation(), size, context.hasFlag('m'), radius, player.getUniqueId());
            plugin.getGraveyardManager().registerNewGraveyard(graveyard);
            sender.sendMessage(ChatColor.GREEN + "Friedhof " + ChatColor.YELLOW + name + ChatColor.GREEN + " wurde erstellt!");
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
            if (graveyard == null) {
                throw new CommandException("Es gibt keinen Friedhof mit diesem Namen!");
            }

            plugin.getGraveyardManager().deleteGraveyard(graveyard);
            sender.sendMessage(ChatColor.GREEN + "Friedhof " + ChatColor.YELLOW + name + ChatColor.GREEN + " wurde gelöscht!");
        }

        @Command(
                aliases = {"tp", "warp", "teleport"},
                desc = "Warp to graveyard",
                min = 1
        )
        @CommandPermissions("rcgraveyards.warp")
        public void warp(CommandContext context, CommandSender sender) throws CommandException {

            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("Player context required!");
                return;
            }

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player) sender;

            String name = context.getString(0);
            Graveyard graveyard = plugin.getGraveyardManager().getGraveyard(name);
            if (graveyard == null) {
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
        public void list(CommandContext context, CommandSender sender) {

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player) sender;

            String list = "";
            ChatColor color;
            int i = 0;
            for (Graveyard graveyard : plugin.getGraveyardManager().getGraveyards()) {
                i++;
                if (i % 2 == 0) {
                    color = ChatColor.YELLOW;
                } else {
                    color = ChatColor.WHITE;
                }
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
        public void test(CommandContext context, CommandSender sender) {

            RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);
            Player player = (Player) sender;
            GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getUniqueId());
            if (graveyardPlayer == null) {
                player.sendMessage(ChatColor.GREEN + "Todespunkt wurde nicht gefunden!");
            }

            player.setCompassTarget(graveyardPlayer.getLastDeath().getLocation());
            player.sendMessage(ChatColor.GREEN + "Dein Kompass zeigt nun auf deinen letzen Todespunkt!");
        }

        @Command(
                aliases = {"discovered", "explored", "visited"},
                desc = "Show all discovered Graveyards"
        )
        public void discovered(CommandContext context, CommandSender sender) {

            Player player = (Player) sender;
            String list = "";
            List<String> graveyardNames = RaidCraft.getTable(PlayerGraveyardsTable.class)
                    .getPlayerAssignments(player.getUniqueId());
            for (String graveyard : graveyardNames) {
                list += Colors.Chat.INFO + graveyard + ChatColor.WHITE + ", ";
            }
            if (list.equals("")) {
                list = "Du hast keine Friedhöfe entdeckt.";
            } else {
                sender.sendMessage(Colors.Chat.SUCCESS + "Folgende Friedhöfe has du bereits entdeckt:");
            }
            sender.sendMessage(list);
        }
    }
}
