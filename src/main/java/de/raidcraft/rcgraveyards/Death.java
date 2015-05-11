package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.storage.ItemStorage;
import de.raidcraft.rcgraveyards.tables.TStoredItem;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.AttackSource;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Philip Urban
 */
public class Death {

    private Hero hero;
    private Location location;
    private long timestamp;

    public Death(Player player) {

        this.hero = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(player);
        this.location = null;
    }

    public Death(Player player, Location location, long timestamp) {

        this.hero = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(player);
        this.location = location;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }

    public void saveInventory(List<ItemStack> items) {

        ItemStorage itemStorage = new ItemStorage("graveyards");
        for (ItemStack itemStack : items) {
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            boolean lootable = !CustomItemUtil.isEquipment(itemStack);
            int storedItemId = itemStorage.storeObject(itemStack);
            TStoredItem storedItem = new TStoredItem();
            storedItem.setStorageId(storedItemId);
            storedItem.setPlayerId(hero.getPlayer().getUniqueId());
            storedItem.setWorld(hero.getPlayer().getWorld().getName());
            storedItem.setLootable(lootable);
            RaidCraft.getDatabase(RCGraveyardsPlugin.class).save(storedItem);
        }
    }

    public boolean wasPvp() {

        return (hero.getLastDamageCause() != null && hero.getLastDamageCause().getAttackSource() == AttackSource.HERO);
    }

    public Location getLocation() {

        return location;
    }

    public void setLocation(Location location) {

        this.location = location;
    }
}
