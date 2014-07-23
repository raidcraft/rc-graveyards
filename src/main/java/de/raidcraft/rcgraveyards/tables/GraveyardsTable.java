package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.rcgraveyards.Graveyard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class GraveyardsTable extends Table {

    public GraveyardsTable() {

        super("graveyards", "rcgraveyards_");
    }

    @Override
    public void createTable() {

        try {
            getConnection().prepareStatement(
                    "CREATE TABLE `" + getTableName() + "` (" +
                            "`id` INT NOT NULL AUTO_INCREMENT, " +
                            "`name` VARCHAR( 64 ) NOT NULL, " +
                            "`world` VARCHAR ( 32 ) NOT NULL ,\n" +
                            "`x` INT( 11 ) NOT NULL ,\n" +
                            "`y` INT( 11 ) NOT NULL ,\n" +
                            "`z` INT( 11 ) NOT NULL ,\n" +
                            "`size` INT( 11 ) NOT NULL ,\n" +
                            "`main` TINYINT( 1 ) NOT NULL, " +
                            "`radius` INT( 11 ) NOT NULL ,\n" +
                            "`creator` VARCHAR ( 32 ) NOT NULL, " +
                            "`created` VARCHAR ( 32 ) NOT NULL, " +
                            "PRIMARY KEY ( `id` )" +
                            ")"
            ).execute();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    public void createGraveyard(Graveyard graveyard) {

        try {
            String query = "INSERT INTO " + getTableName() + " (name, world, x, y, z, size, main, radius, creator, created) " +
                    "VALUES (" +
                    "'" + graveyard.getName() + "'" + "," +
                    "'" + graveyard.getLocation().getWorld().getName() + "'" + "," +
                    "'" + graveyard.getLocation().getBlockX() + "'" + "," +
                    "'" + graveyard.getLocation().getBlockY() + "'" + "," +
                    "'" + graveyard.getLocation().getBlockZ() + "'" + "," +
                    "'" + graveyard.getSize() + "'" + "," +
                    "'" + ((graveyard.isMain()) ? 1 : 0) + "'" + "," +
                    "'" + graveyard.getRadius() + "'" + "," +
                    "'" + graveyard.getCreator() + "'" + "," +
                    "'" + graveyard.getCreationDate() + "'" +
                    ");";

            executeUpdate(query);
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Graveyard> getAll(String worldName) {

        List<Graveyard> graveyards = new ArrayList<>();
        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName() + " WHERE world = '" + worldName + "';");

            while (resultSet.next()) {
                World world = Bukkit.getWorld(resultSet.getString("world"));
                if (world == null) continue;

                Graveyard graveyard = new Graveyard(resultSet.getString("name")
                        , new Location(world, resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"))
                        , resultSet.getInt("size")
                        , resultSet.getBoolean("main")
                        , resultSet.getInt("radius")
                        , resultSet.getString("creator")
                        , resultSet.getString("created"));

                graveyards.add(graveyard);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return graveyards;
    }

    public void delete(String name) {

        try {
            executeUpdate(
                    "DELETE FROM " + getTableName() + " WHERE name = '" + name + "'");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
