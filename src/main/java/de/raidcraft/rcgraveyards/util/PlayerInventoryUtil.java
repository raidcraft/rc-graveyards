package de.raidcraft.rcgraveyards.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Philip Urban
 */
public class PlayerInventoryUtil {

    public static void putInInventory(Player player, ItemStack itemStack) {

        PlayerInventory inventory = player.getInventory();
        int firstEmpty = inventory.firstEmpty();
        if (firstEmpty == -1) {
            player.getLocation().getWorld().dropItemNaturally(player.getLocation(), itemStack);
        } else {
            inventory.setItem(firstEmpty, itemStack);
        }
        player.updateInventory();
    }

}
