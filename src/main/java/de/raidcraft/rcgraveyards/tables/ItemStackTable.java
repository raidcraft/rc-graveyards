package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.util.SerializationUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class ItemStackTable extends Table {

    public ItemStackTable() {

        super("itemstacks", "rcgraveyards_");
    }

    @Override
    public void createTable() {

        try {
            executeUpdate(
                    "CREATE TABLE `" + getTableName() + "` (\n" +
                            "`id` INT NOT NULL AUTO_INCREMENT ,\n" +
                            "`player` VARCHAR( 32 ) NOT NULL ,\n" +
                            "`world` VARCHAR ( 32 ) NOT NULL ,\n" +
                            "`material` VARCHAR( 32 ) NOT NULL ,\n" +
                            "`durability` INT( 11 ) NOT NULL ,\n" +
                            "`amount` INT( 11 ) NOT NULL , \n" +
                            "`itemmeta` TEXT NOT NULL , \n" +
                            "PRIMARY KEY ( `id` )\n" +
                            ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addInventory(List<ItemStack> items, Player player) {

        delete(player);
        try {
            PreparedStatement statement;

            String query = "INSERT INTO " + getTableName() + " (player, world, material, durability, itemmeta, amount) " +
                    "VALUES (?, ?, ?, ?, ?);";

            getConnection().setAutoCommit(false);
            statement = getConnection().prepareStatement(query);

            int i = 0;
            for(ItemStack item : items) {
                statement.setString(1, player.getName().toLowerCase());
                statement.setString(2, player.getWorld().getName());
                statement.setString(3, item.getType().name());
                statement.setShort(4, item.getDurability());
                statement.setString(5, SerializationUtil.toByteStream(item.getItemMeta()));
                statement.setInt(6, item.getAmount());
                statement.executeUpdate();

                i++;

                if(i % 100 == 0) {
                    getConnection().commit();
                }
            }
            getConnection().commit();
            getConnection().setAutoCommit(true);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ItemStack> getInventory(Player player) {

        List<ItemStack> itemStacks = new ArrayList<>();
        //TODO implement
        return itemStacks;
    }

    public void delete(Player player) {

        try {
            executeUpdate(
                    "DELETE FROM " + getTableName() + " WHERE player = '" + player.getName().toLowerCase() + "' AND world = '" + player.getWorld().getName() + "'");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
