package de.raidcraft.rcgraveyards;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.AttackSource;
import de.raidcraft.skills.api.hero.Hero;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class Death {

    private Hero hero;
    private Location location;
    private List<ItemStack> inventory;
    private long timestamp;

    public Death(Player player) {

        this.hero = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(player);
        this.location = null;
        this.inventory = new ArrayList<>();
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

    public List<ItemStack> getInventory() {

        return inventory;
    }

    public void setInventory(List<ItemStack> inventory) {

        this.inventory = inventory;
    }

    public void saveInventory() {

        this.inventory.clear();
        for(ItemStack itemStack : hero.getPlayer().getInventory().getContents()) {
            if(itemStack == null || itemStack.getType() == Material.AIR) continue;
            this.inventory.add(itemStack.clone());
        }
        for(ItemStack itemStack : hero.getPlayer().getEquipment().getArmorContents()) {
            if(itemStack == null || itemStack.getType() == Material.AIR) continue;
            this.inventory.add(itemStack.clone());
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
