package de.raidcraft.rcgraveyards;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.rcgraveyards.commands.Commands;
import de.raidcraft.rcgraveyards.listener.PlayerListener;

/**
 * @author Philip Urban
 */
public class RCGraveyardsPlugin extends BasePlugin {

    @Override
    public void enable() {

        registerEvents(new PlayerListener());
        registerCommands(Commands.class);

        reload();
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }
}
