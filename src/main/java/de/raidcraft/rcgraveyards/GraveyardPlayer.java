package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItem;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.rcgraveyards.deathinfo.HeroDeathInfo;
import de.raidcraft.rcgraveyards.events.RCGraveyardPlayerRevivedEvent;
import de.raidcraft.rcgraveyards.tables.DeathsTable;
import de.raidcraft.rcgraveyards.tables.PlayerGraveyardsTable;
import de.raidcraft.rcgraveyards.util.EquipmentDamageLevel;
import de.raidcraft.rcgraveyards.util.PlayerInventoryUtil;
import de.raidcraft.rcgraveyards.util.ReviveReason;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.ItemUtils;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * @author Philip Urban
 */
public class GraveyardPlayer {

    private Player player;
    private Map<String, Graveyard> graveyards = new HashMap<>();
    private boolean ghost = false;
    private HeroDeathInfo lastHeroDeathInfo;
    private long lastRevive = 0;

    public GraveyardPlayer(Player player) {

        this.player = player;

        // get player graveyards
        for (Graveyard graveyard : RaidCraft.getComponent(RCGraveyardsPlugin.class)
                .getGraveyardManager().getPlayerGraveyards(player.getUniqueId())) {
            graveyards.put(graveyard.getName(), graveyard);
        }

        // get all main graveyards
        for (Graveyard graveyard : RaidCraft.getComponent(RCGraveyardsPlugin.class)
                .getGraveyardManager().getMainGraveyards()) {
            graveyards.put(graveyard.getName(), graveyard);
        }

        // load from database
        lastHeroDeathInfo = RaidCraft.getTable(DeathsTable.class).getDeath(player);
        if (lastHeroDeathInfo == null) {
            lastHeroDeathInfo = new HeroDeathInfo(player);
        } else {
            setGhost(true);
        }
    }

    public Player getPlayer() {

        return player;
    }

    public Graveyard getClosestGraveyard(Location location) {

        double distance = 0;
        Graveyard closestGraveyard = null;
        for (Graveyard graveyard : graveyards.values()) {
            int newDistance = LocationUtil.getBlockDistance(location, graveyard.getLocation());
            if (closestGraveyard == null || newDistance < distance && (graveyard.getRadius() == 0 || graveyard.getRadius() < newDistance)) {
                closestGraveyard = graveyard;
                distance = newDistance;
            }
        }
        return closestGraveyard;
    }

    public Graveyard getLastDeathGraveyard() {

        return getClosestGraveyard(getLastDeath().getLocation());
    }

    public boolean knowGraveyard(Graveyard graveyard) {

        return graveyards.containsKey(graveyard.getName());
    }

    public void addGraveyard(Graveyard graveyard) {

        graveyards.put(graveyard.getName(), graveyard);
        RaidCraft.getTable(PlayerGraveyardsTable.class).addAssignment(player.getUniqueId(), graveyard);
    }

    public boolean isGhost() {

        return ghost;
    }

    public void revive(ReviveReason reason) {

        if (!player.isOnline()) return;
        if (!isGhost()) return;

        player.getInventory().clear();
        restoreInventory(reason);
        RaidCraft.getComponent(RCGraveyardsPlugin.class).getCorpseManager().deleteCorpse(player.getUniqueId());
        setGhost(false);
        RaidCraft.callEvent(new RCGraveyardPlayerRevivedEvent(this, reason));
    }

    public void restoreInventory(ReviveReason reason) {

        double modifier = reason.getDamageLevel().getModifier();
        if (getLastDeath().wasPvp()) {
            modifier = EquipmentDamageLevel.VERY_LOW.getModifier();
        }
        restoreInventory(modifier, reason);
    }

    public void restoreInventory() {

        restoreInventory(0, ReviveReason.CUSTOM);
    }

    public void restoreInventory(double modifier, ReviveReason reason) {

        Set<Map.Entry<Integer, ItemStack>> entries = RaidCraft.getComponent(RCGraveyardsPlugin.class).getPlayerManager()
                .getDeathInventory(player.getUniqueId(), player.getWorld().getName()).entrySet();
        for (Map.Entry<Integer, ItemStack> entry : entries) {
            ItemStack itemStack = entry.getValue();
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                if (modifier > 0 && CustomItemUtil.isCustomItem(itemStack)) {
                    CustomItemStack customItem = RaidCraft.getCustomItem(itemStack);
                    if (customItem == null) {
                        continue;
                    }
                    if (reason.isEquipmentOnly()) {
                        CustomItem item = customItem.getItem();
                        if (item.getType() != ItemType.EQUIPMENT && item.getType() != ItemType.ARMOR && item.getType() != ItemType.WEAPON) {
                            continue;
                        }
                    }
                    try {
                        customItem.setCustomDurability(customItem.getCustomDurability() - (int) ((double) customItem.getMaxDurability() * modifier));
                        customItem.rebuild(player);
                        itemStack = customItem;
                    } catch (CustomItemException ignored) {
                    }
                } else {
                    ItemUtils.Item item = ItemUtils.Item.getItemByMaterial(itemStack.getType());
                    if (reason.isEquipmentOnly() && (item == null || item.getType() != ItemUtils.ItemType.TOOL)) continue;
                }
                PlayerInventoryUtil.putInInventory(player, itemStack, entry.getKey());
            }
        }
    }

    public void setGhost(boolean ghost) {

        this.ghost = ghost;
        RCGraveyardsPlugin plugin = RaidCraft.getComponent(RCGraveyardsPlugin.class);

        // set player opacity
        if (ghost) {
            // set as ghost
            plugin.getGhostManager().setGhost(player, true);
            // clear inventory
            player.getInventory().clear();
            // give compass
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta itemMeta = compass.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GOLD + plugin.getTranslationProvider().tr(player,
                    "ghost.compass.title", "Shows you the way to your corpse."));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + plugin.getTranslationProvider().tr(player,
                    "ghost.compass.right-click", "Right Click: Returns you to the graveyard you spawned at."));
            itemMeta.setLore(lore);
            compass.setItemMeta(itemMeta);
            player.getInventory().setItemInHand(compass);
            // set compass target
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {

                    player.setCompassTarget(lastHeroDeathInfo.getLocation());
                }
            }, 1);

            PlayerInventoryUtil.putInInventory(player, new ItemStack(Material.ENDER_PEARL, 64));
            PlayerInventoryUtil.putInInventory(player, new ItemStack(Material.BOAT, 2));
            save();
        } else {
            player.setFireTicks(0);
            plugin.getGhostManager().setGhost(player, false);
            lastRevive = System.currentTimeMillis();
            // delete db entries
            RaidCraft.getTable(DeathsTable.class).delete(player);
        }
        plugin.getPlayerManager().updatePlayerVisibility();
    }

    public HeroDeathInfo getLastDeath() {

        return lastHeroDeathInfo;
    }

    public long getLastRevive() {

        return lastRevive;
    }

    public void save() {

        RaidCraft.getTable(DeathsTable.class).addDeath(lastHeroDeathInfo, player);
    }
}
