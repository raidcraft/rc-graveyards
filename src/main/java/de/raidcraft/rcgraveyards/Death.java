package de.raidcraft.rcgraveyards;

import de.raidcraft.skills.api.hero.Hero;
import org.bukkit.Location;
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

//        this.hero = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(player);
        this.location = null;
        this.inventory = new ArrayList<>();
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

    public boolean wasPvp() {

//        return (hero.getLastDamageCause().getAttackSource() == AttackSource.HERO);
        return false;
    }

    public Location getLocation() {

        return location;
    }

    public void setLocation(Location location) {

        this.location = location;
    }
}
