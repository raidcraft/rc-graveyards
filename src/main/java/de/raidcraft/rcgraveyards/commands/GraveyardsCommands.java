package de.raidcraft.rcgraveyards.commands;

import com.sk89q.minecraft.util.commands.*;
import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
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

    public static class NestedCommands {

        private final RCGraveyardsPlugin module;

        public NestedCommands(RCGraveyardsPlugin module) {

            this.module = module;
        }

        @Command(
                aliases = {"reload"},
                desc = "Reload plugin"
        )
        @CommandPermissions("rcgraveyards.reload")
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
        @CommandPermissions("rcgraveyards.create")
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
            sender.sendMessage(ChatColor.GREEN + "Friedhof " + ChatColor.YELLOW + name +  ChatColor.GREEN + " wurde erstellt!");
        }
    }
}
