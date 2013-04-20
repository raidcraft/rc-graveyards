package de.raidcraft.rcgraveyards;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.rcgraveyards.commands.Commands;
import de.raidcraft.rcgraveyards.listener.PlayerListener;
import de.raidcraft.rcgraveyards.managers.GraveyardManager;
import de.raidcraft.rcgraveyards.managers.PlayerManager;
import de.raidcraft.rcgraveyards.tables.GraveyardsTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;

/**
 * @author Philip Urban
 */
public class RCGraveyardsPlugin extends BasePlugin {

    private GraveyardManager graveyardManager;
    private PlayerManager playerManager;

    @Override
    public void enable() {

        registerTable(GraveyardsTable.class, new GraveyardsTable());
        registerTable(PlayerGraveyardsTable.class, new PlayerGraveyardsTable());

        registerEvents(new PlayerListener());
        registerCommands(Commands.class);

        // init managers
        graveyardManager = new GraveyardManager(this);
        playerManager = new PlayerManager(this);

        reload();
    }

    @Override
    public void reload() {

    }

    @Override
    public void disable() {

    }

    public GraveyardManager getGraveyardManager() {

        return graveyardManager;
    }

    public PlayerManager getPlayerManager() {

        return playerManager;
    }
}
