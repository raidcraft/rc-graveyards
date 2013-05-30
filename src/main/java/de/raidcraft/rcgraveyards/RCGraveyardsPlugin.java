package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.rcconversations.actions.ActionManager;
import de.raidcraft.rcconversations.npc.NPCListener;
import de.raidcraft.rcgraveyards.commands.GraveyardsCommands;
import de.raidcraft.rcgraveyards.conversations.ReviveGhostAction;
import de.raidcraft.rcgraveyards.listener.PlayerListener;
import de.raidcraft.rcgraveyards.managers.*;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.tables.GraveyardsTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;

/**
 * @author Philip Urban
 */
public class RCGraveyardsPlugin extends BasePlugin {

    private LocalConfiguration config;
    private GraveyardManager graveyardManager;
    private PlayerManager playerManager;
    private DynmapManager dynmapManager;
    private GhostManager ghostManager;
    private CorpseManager corpseManager;
    private Citizens citizens;

    @Override
    public void enable() {

        registerTable(GraveyardsTable.class, new GraveyardsTable());
        registerTable(PlayerGraveyardsTable.class, new PlayerGraveyardsTable());

        registerEvents(new PlayerListener());
        registerEvents(new NPCListener());
        registerCommands(GraveyardsCommands.class);

        ActionManager.registerAction(new ReviveGhostAction());

        // init managers
        graveyardManager = new GraveyardManager(this);
        playerManager = new PlayerManager(this);
        dynmapManager = new DynmapManager();
        ghostManager = new GhostManager(this);
        corpseManager = new CorpseManager(this);

        loadCitizens();
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

        @Setting("corpse-exist-duration") public int corpseDuration = 432000; // in seconds

        public LocalConfiguration(RCGraveyardsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("default-graveyard-size") public int defaultSize = 10;
    }

    private void loadCitizens() {

        try {
            registerEvents(new NPCListener());
            citizens = (Citizens) Bukkit.getPluginManager().getPlugin("Citizens");
            citizens.getTraitFactory().registerTrait(TraitInfo.create(CorpseTrait.class).withName("rcgraveyards"));
        } catch (Exception e) {
            RaidCraft.LOGGER.warning("[RCConv] Can't load NPC stuff! Citizens not found!");
        }
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

    public CorpseManager getCorpseManager() {

        return corpseManager;
    }

    public Citizens getCitizens() {

        return citizens;
    }
}
