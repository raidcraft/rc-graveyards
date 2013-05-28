package de.raidcraft.rcgraveyards;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.rcgraveyards.commands.GraveyardsCommands;
import de.raidcraft.rcgraveyards.listener.PlayerListener;
import de.raidcraft.rcgraveyards.managers.GhostManager;
import de.raidcraft.rcgraveyards.managers.GraveyardManager;
import de.raidcraft.rcgraveyards.managers.PlayerManager;
import de.raidcraft.rcgraveyards.tables.GraveyardsTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import de.raidcraft.rcgraveyards.managers.DynmapManager;

/**
 * @author Philip Urban
 */
public class RCGraveyardsPlugin extends BasePlugin {

    private LocalConfiguration config;
    private GraveyardManager graveyardManager;
    private PlayerManager playerManager;
    private DynmapManager dynmapManager;
    private GhostManager ghostManager;

    @Override
    public void enable() {

        registerTable(GraveyardsTable.class, new GraveyardsTable());
        registerTable(PlayerGraveyardsTable.class, new PlayerGraveyardsTable());

        registerEvents(new PlayerListener());
        registerCommands(GraveyardsCommands.class);

        // init managers
        graveyardManager = new GraveyardManager(this);
        playerManager = new PlayerManager(this);
        dynmapManager = new DynmapManager();
        ghostManager = new GhostManager(this);

        reload();
    }

    @Override
    public void reload() {

        config = configure(new LocalConfiguration(this));
        graveyardManager.reload();
    }

    @Override
    public void disable() {

    }

    public class LocalConfiguration extends ConfigurationBase<RCGraveyardsPlugin> {

        public LocalConfiguration(RCGraveyardsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("default-graveyard-size") public int defaultSize = 10;
    }

    public LocalConfiguration getConfig() {

        return config;
    }

    public GraveyardManager getGraveyardManager() {

        return graveyardManager;
    }

    public PlayerManager getPlayerManager() {

        return playerManager;
    }

    public DynmapManager getDynmapManager() {

        return dynmapManager;
    }

    public GhostManager getGhostManager() {

        return ghostManager;
    }
}
