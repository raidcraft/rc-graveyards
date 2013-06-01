package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.RaidCraft;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.npc.CorpseTrait;
import de.raidcraft.rcgraveyards.util.PlayerInventoryUtil;
import de.raidcraft.util.CustomItemUtil;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Philip Urban
 */
public class CorpseManager {

    private RCGraveyardsPlugin plugin;
    private Map<String, NPC> registeredCorpse = new HashMap<>();

    public CorpseManager(RCGraveyardsPlugin plugin) {

        this.plugin = plugin;
    }

    public void registerCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);

        // remove corpse if too old
        long lastDeath = plugin.getPlayerManager().getLastDeath(trait.getPlayerName(), npc.getBukkitEntity().getWorld().getName());
        if(lastDeath < System.currentTimeMillis() - plugin.getConfig().corpseDuration*1000) {
            npc.destroy();
        }
        else {
            deleteCorpse(trait.getPlayerName());
            registeredCorpse.put(trait.getPlayerName().toLowerCase(), npc);
        }
    }

    public void unregisterCorpse(NPC npc) {

        CorpseTrait trait = npc.getTrait(CorpseTrait.class);
        registeredCorpse.remove(trait.getPlayerName().toLowerCase());
    }

    public void deleteCorpse(String name) {

        NPC npc = registeredCorpse.remove(name.toLowerCase());
        if(npc != null) {
            npc.destroy();
        }
    }

    public void checkReviver(Player player, String corpseName) {

        boolean ghost = plugin.getGhostManager().isGhost(player);
        NPC npc = registeredCorpse.get(player.getName());
        boolean looted = npc.getTrait(CorpseTrait.class).isLooted();

        if(player.getName().equalsIgnoreCase(corpseName)) {

            player.sendMessage(ChatColor.GREEN + "Deine Seele ist in " + plugin.getConfig().ghostReviveDuration
                    + " Sek. zurück gekehrt. Bringe dich in Sicherheit!");
            Bukkit.getScheduler().runTaskLater(plugin, new GhostReviver(player, looted), 20 * plugin.getConfig().ghostReviveDuration);
            return;
        }

        if(ghost) {
            player.sendMessage(ChatColor.RED + "Du kannst als Geist keine anderen Leichen berauben!");
        }
        else if(looted) {
            player.sendMessage(ChatColor.RED + "Diese Leiche ist schon ausgeraubt!");
        }
        else {
            lootCorpse(player, corpseName);
        }
    }

    public void reviveGhost(Player player, ReviveReason reason) {

        player.getInventory().clear();
        GraveyardPlayer graveyardPlayer = plugin.getPlayerManager().getGraveyardPlayer(player.getName());
        List<ItemStack> loot = plugin.getPlayerManager().getDeathInventory(player.getName(), player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                if(CustomItemUtil.isEquipment(itemStack)) {
                    double modifier = reason.getDamageLevel().getModifier();
                    if(graveyardPlayer.getLastDeath().wasPvp()) {
                        modifier = EQUIPMENT_DAMAGE_LEVEL.VERY_LOW.getModifier();
                    }
                    double durability = (short)((double)itemStack.getDurability() * modifier);
                    RaidCraft.LOGGER.info("DEBUG: d:" + durability + " | m:" + modifier + " | o:" + itemStack.getDurability());
                    itemStack.setDurability((short)durability);
                }
                else {
                    if(reason.isEquipmentOnly()) continue;
                }
                PlayerInventoryUtil.putInInventory(player, itemStack);
            }
        }
        deleteCorpse(player.getName());
        graveyardPlayer.setGhost(false);
    }

    public void lootCorpse(Player player, String corpseName) {

        NPC npc = registeredCorpse.get(corpseName);
        if(npc != null) {
            npc.getTrait(CorpseTrait.class).setLooted(true);
        }
        List<ItemStack> loot = plugin.getPlayerManager().getLootableDeathInventory(corpseName, player.getWorld().getName());
        for (ItemStack itemStack : loot) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                player.getLocation().getWorld().dropItem(player.getLocation(), itemStack);
            }
        }

        player.sendMessage(ChatColor.GREEN + "Die Leiche von " + corpseName + " hat " + loot.size() + " Items fallen gelassen");
    }

    public enum ReviveReason {

        FOUND_CORPSE(false, EQUIPMENT_DAMAGE_LEVEL.LOW),
        NECROMANCER(true, EQUIPMENT_DAMAGE_LEVEL.HIGH),
        COMMAND(false, EQUIPMENT_DAMAGE_LEVEL.NO);

        public boolean equipmentOnly;
        public EQUIPMENT_DAMAGE_LEVEL damageLevel;

        private ReviveReason(boolean equipmentOnly, EQUIPMENT_DAMAGE_LEVEL damageLevel) {

            this.equipmentOnly = equipmentOnly;
            this.damageLevel = damageLevel;
        }

        public boolean isEquipmentOnly() {

            return equipmentOnly;
        }

        public EQUIPMENT_DAMAGE_LEVEL getDamageLevel() {

            return damageLevel;
        }
    }

    public enum EQUIPMENT_DAMAGE_LEVEL {

        NO(1),
        VERY_LOW(0.95),
        LOW(0.9),
        MIDDLE(0.7),
        HIGH(0.5),
        VERY_HIGH(0.3);

        public double modifier;

        private EQUIPMENT_DAMAGE_LEVEL(double modifier) {

            this.modifier = modifier;
        }

        public double getModifier() {

            return modifier;
        }
    }

    public class GhostReviver implements Runnable {

        Player player;
        boolean looted;

        public GhostReviver(Player player, boolean looted) {

            this.player = player;
            this.looted = looted;
        }

        @Override
        public void run() {

            String msg = "Du bist wieder lebendig. ";
            if(looted) {
                msg += "Ein andere Spieler hat deine Leiche ausgeraubt!";
            }
            else {
                msg += "Du hast deine Items wieder im Inventar.";
            }
            player.sendMessage(ChatColor.GREEN + msg);
            reviveGhost(player, ReviveReason.FOUND_CORPSE);
        }
    }
}
