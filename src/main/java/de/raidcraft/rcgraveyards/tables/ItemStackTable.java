package de.raidcraft.rcgraveyards.tables;

import com.sk89q.commandbook.CommandBook;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.util.ItemUtils;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
                            "`player` INT( 11 ) NOT NULL ,\n" +
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

    public void addInventory(List<ItemStack> items, String player) {

        delete(player);
        try {
            PreparedStatement statement;

            String query = "INSERT INTO " + getTableName() + " (player, material, durability, itemmeta, amount) " +
                    "VALUES (?, ?, ?, ?, ?);";

            getConnection().setAutoCommit(false);
            statement = getConnection().prepareStatement(query);
            ItemUtils.Serialization serialization;

            int i = 0;
            for(ItemStack item : items) {

                serialization = new ItemUtils.Serialization(item);

                statement.setString(1, player.toLowerCase());
                statement.setString(2, item.getType().name());
                statement.setShort(3, item.getDurability());
                statement.setString(4, serialization.getSerializedItemData());
                statement.setInt(5, item.getAmount());
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
            CommandBook.logger().warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(String player) {

        try {
            executeUpdate(
                    "DELETE FROM " + getTableName() + " WHERE player = '" + player.toLowerCase() + "'");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
