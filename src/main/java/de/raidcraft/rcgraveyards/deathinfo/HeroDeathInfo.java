package de.raidcraft.rcgraveyards.deathinfo;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.storage.ItemStorage;
import de.raidcraft.rcgraveyards.RCGraveyardsPlugin;
import de.raidcraft.rcgraveyards.api.AbstractPlayerDeathInfo;
import de.raidcraft.rcgraveyards.api.PlayerDeathInfo;
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
import java.util.UUID;

/**
 * @author Philip Urban
 */
public class HeroDeathInfo extends AbstractPlayerDeathInfo {

    private Hero hero;

    public HeroDeathInfo(Player player) {

        super(null, 0, player.getUniqueId(), player.getName());
        this.hero = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(player);
    }

    public HeroDeathInfo(Player player, Location location, long timestamp) {

        super(location, timestamp, player.getUniqueId(), player.getName());
        this.hero = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(player);
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
}
