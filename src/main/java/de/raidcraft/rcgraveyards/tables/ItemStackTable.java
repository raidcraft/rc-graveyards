package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.SerializationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                            "`equipment` TINYINT NOT NULL , \n" +
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

            String query = "INSERT INTO " + getTableName() + " (player, world, material, durability, itemmeta, amount, equipment) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";

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

                boolean equipment = false;
                if(CustomItemUtil.isEquipment(item)) equipment = true;
                statement.setInt(7, (equipment) ? 1 : 0);
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

        return getInventory(player.getName(), player.getWorld().getName(), false);
    }

    public List<ItemStack> getInventory(String player, String world, boolean withoutEquipment) {

        List<ItemStack> items = new ArrayList<>();
        String withoutEquipmentFilter = "";
        if(withoutEquipment) {
            withoutEquipmentFilter = "AND equipment = '0'";
        }
        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName() + " WHERE player = '" + player.toLowerCase() + "' AND world = '" + world + "' " + withoutEquipmentFilter);

            while (resultSet.next()) {
                ItemStack itemStack = new ItemStack(
                        Material.getMaterial(resultSet.getString("material")),
                        resultSet.getInt("amount"),
                        resultSet.getShort("durability")
                );

                String itemData = resultSet.getString("itemmeta");
                itemStack.setItemMeta((ItemMeta)SerializationUtil.fromByteStream(itemData, itemStack.getType()));
                items.add(itemStack);
            }
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public void delete(Player player) {

        delete(player.getName(), player.getWorld().getName(), false);
    }

    public void delete(String player, String world, boolean withoutEquipment) {

        String withoutEquipmentFilter = "";
        if(withoutEquipment) {
            withoutEquipmentFilter = "AND equipment = '0'";
        }
        try {
            executeUpdate(
                    "DELETE FROM " + getTableName() + " WHERE player = '" + player.toLowerCase() + "' AND world = '" + world + "' " + withoutEquipmentFilter);
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
