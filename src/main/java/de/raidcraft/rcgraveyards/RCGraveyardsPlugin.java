package de.raidcraft.rcgraveyards;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.rcconversations.actions.ActionManager;
import de.raidcraft.rcgraveyards.actions.RevivePlayerAction;
import de.raidcraft.rcgraveyards.commands.GhostsCommand;
import de.raidcraft.rcgraveyards.commands.GraveyardsCommands;
import de.raidcraft.rcgraveyards.conversations.CheckGraveyardCompetenceAction;
import de.raidcraft.rcgraveyards.conversations.CheckIfGhostAction;
import de.raidcraft.rcgraveyards.conversations.ReviveGhostAction;
import de.raidcraft.rcgraveyards.listener.MobListener;
import de.raidcraft.rcgraveyards.listener.PlayerListener;
import de.raidcraft.rcgraveyards.managers.CorpseManager;
import de.raidcraft.rcgraveyards.managers.DynmapManager;
import de.raidcraft.rcgraveyards.managers.GhostManager;
import de.raidcraft.rcgraveyards.managers.GraveyardManager;
import de.raidcraft.rcgraveyards.managers.PlayerManager;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.npc.NPCListener;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.tables.GraveyardsTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import de.raidcraft.rcgraveyards.tables.TStoredItem;
import de.raidcraft.rcgraveyards.trigger.GraveyardsPlayerTrigger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class RCGraveyardsPlugin extends BasePlugin {

    public final static String VISIBLE_FOR_GHOSTS_METADATA = "VISIBLE_FOR_GHOSTS";
    public final static String HIDDEN_FOR_LIVING_METADATA = "HIDDEN_FOR_LIVING";
    public final static String PLAYER_IS_GHOST_METADATA = "GHOST";
    public final static String NPC_REGISTER_SKELETON = "_skeleton";
    private LocalConfiguration config;
    private GraveyardManager graveyardManager;
    private PlayerManager playerManager;
    private DynmapManager dynmapManager;
    private GhostManager ghostManager;
    private CorpseManager corpseManager;

    @Override
    public void enable() {

        config = configure(new LocalConfiguration(this));

        registerTable(GraveyardsTable.class, new GraveyardsTable());
        registerTable(PlayerGraveyardsTable.class, new PlayerGraveyardsTable());
        registerTable(DeathsTable.class, new DeathsTable());

        registerCommands(GraveyardsCommands.class);
        registerCommands(GhostsCommand.class);

        ActionManager.registerAction(new ReviveGhostAction());
        ActionManager.registerAction(new CheckGraveyardCompetenceAction());
        ActionManager.registerAction(new CheckIfGhostAction());

        ActionAPI.register(this)
                .trigger(new GraveyardsPlayerTrigger())
		        .requirement("player.isAlive", (Player player, ConfigurationSection config) -> !getGhostManager().isGhost(player))
                .requirement("player.isGhost", (Player player, ConfigurationSection config) -> getGhostManager().isGhost(player))
		        .action("player.revive", new RevivePlayerAction());

        // init managers
        graveyardManager = new GraveyardManager(this);
        playerManager = new PlayerManager(this);
        dynmapManager = new DynmapManager();
        ghostManager = new GhostManager(this);
        corpseManager = new CorpseManager(this);

        registerEvents(new PlayerListener());
        registerEvents(new MobListener());
        registerEvents(ghostManager);

        // load NPC stuff
        registerEvents(new NPCListener());
        NPC_Manager.getInstance().registerTrait(CorpseTrait.class, RC_Traits.GRAVEYARDS);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            NPC_Manager.getInstance().loadNPCs(this.getName());
            NPC_Manager.getInstance().loadNPCs(this.getName() + NPC_REGISTER_SKELETON);
        }, 8 * 20l);
    }

    @Override
    public void reload() {

        getConfig().reload();
        graveyardManager.reload();
    }

    @Override
    public void disable() {

    }

    @Override
    public List<Class<?>> getDatabaseClasses() {

        List<Class<?>> databases = new ArrayList<>();
        databases.add(TStoredItem.class);
        return databases;
    }

    public class LocalConfiguration extends ConfigurationBase<RCGraveyardsPlugin> {

        @Setting("corpse-exist-duration")
        public int corpseDuration = 432000; // in seconds
        @Setting("ghost-revive-delay")
        public int ghostReviveDuration = 15; // in seconds
        @Setting("corpse-loot-item-indicator-material")
        public String corpseLootIndicatorMaterial = "DIAMOND";
        @Setting("necromancer-conversation-name")
        public String necromancerConversationName = "graveyard-necromancer";
        @Setting("world-guard-respawn-support")
        public boolean worldGuardRespawnSupport = true;

        public LocalConfiguration(RCGraveyardsPlugin plugin) {

            super(plugin, "config.yml");
        }

        @Setting("default-graveyard-size")
        public int defaultSize = 10;
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
}
