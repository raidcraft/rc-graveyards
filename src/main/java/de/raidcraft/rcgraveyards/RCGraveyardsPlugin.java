package de.raidcraft.rcgraveyards;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.Setting;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.api.npc.NPC_Manager;
import de.raidcraft.api.npc.RC_Traits;
import de.raidcraft.rcgraveyards.actions.RevivePlayerAction;
import de.raidcraft.rcgraveyards.commands.GhostsCommand;
import de.raidcraft.rcgraveyards.commands.GraveyardsCommands;
import de.raidcraft.rcgraveyards.conversations.GraveyardConversation;
import de.raidcraft.rcgraveyards.listener.MobListener;
import de.raidcraft.rcgraveyards.listener.PlayerListener;
import de.raidcraft.rcgraveyards.managers.*;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.npc.NPCListener;
import de.raidcraft.rcgraveyards.requirements.IsGhostRequirement;
import de.raidcraft.rcgraveyards.tables.TStoredItem;
import de.raidcraft.rcgraveyards.trigger.GraveyardsPlayerTrigger;
import org.bukkit.Bukkit;

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
    public final static boolean SAVE_NPCS_EXTERNAL = false;
    public static String REGISTER_HOST;
    private LocalConfiguration config;
    private GraveyardManager graveyardManager;
    private PlayerManager playerManager;
    private DynmapManager dynmapManager;
    private GhostManager ghostManager;
    private CorpseManager corpseManager;

    @Override
    public void enable() {

        REGISTER_HOST = getName() + RCGraveyardsPlugin.NPC_REGISTER_SKELETON;

        config = configure(new LocalConfiguration(this));

        // TODO: refactor to ORM ebean
//        registerTable(GraveyardsTable.class, new GraveyardsTable());
//        registerTable(PlayerGraveyardsTable.class, new PlayerGraveyardsTable());
//        registerTable(DeathsTable.class, new DeathsTable());

        registerCommands(GraveyardsCommands.class);
        registerCommands(GhostsCommand.class);

        ActionAPI.register(this)
                .trigger(new GraveyardsPlayerTrigger())
                .global()
                .action(new RevivePlayerAction())
                .requirement(new IsGhostRequirement());

        Conversations.registerConversationType("graveyard-necromancer", GraveyardConversation.class);

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
            if(SAVE_NPCS_EXTERNAL) {
                NPC_Manager.getInstance().loadNPCs(this.getName());
                NPC_Manager.getInstance().loadNPCs(this.getName() + NPC_REGISTER_SKELETON);
            }
            else {
                corpseManager.spawnCorpseNPCs();
                graveyardManager.spawnNecromancerNPCs();
            }
        }, 8 * 20l);
    }

    @Override
    public void reload() {

        getConfig().reload();
        corpseManager.reload();
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
        @Setting("loot.min-count")
        public int minLootCount = 5;
        @Setting("loot.max-count")
        public int maxLootCount = 20;

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
