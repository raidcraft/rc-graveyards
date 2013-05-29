package de.raidcraft.rcgraveyards.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * @author Philip Urban
 */
public class SkullUtil {

    public static ItemStack getPlayerSkull(String name) {
        ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta skullMeta = (SkullMeta)skullItem.getItemMeta();
        skullMeta.setOwner(name);
        skullMeta.setDisplayName(ChatColor.RESET + name);
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }

}
