package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.rcgraveyards.deathinfo.HeroDeathInfo;
import de.raidcraft.rcgraveyards.deathinfo.OfflinePlayerDeathInfo;
import de.raidcraft.util.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Philip Urban
 */
public class DeathsTable extends Table {

    public DeathsTable() {

        super("deaths", "rcgraveyards_");
    }

    @Override
    public void createTable() {

        try {
            executeUpdate(
                    "CREATE TABLE `" + getTableName() + "` (\n" +
                            "`id` INT NOT NULL AUTO_INCREMENT ,\n" +
                            "`player` VARCHAR( 32 ) ,\n" +
                            "`player_id` VARCHAR( 32 ) NOT NULL ,\n" +
                            "`date` VARCHAR( 32 ) NOT NULL ,\n" +
                            "`pvp` TINYINT NOT NULL , \n" +
                            "`world` VARCHAR ( 32 ) NOT NULL ,\n" +
                            "`x` INT( 11 ) NOT NULL ,\n" +
                            "`y` INT( 11 ) NOT NULL ,\n" +
                            "`z` INT( 11 ) NOT NULL ,\n" +
                            "PRIMARY KEY ( `id` )\n" +
                            ")"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDeath(HeroDeathInfo heroDeathInfo, Player player) {

        delete(player);
        try {
            String query = "INSERT INTO " + getTableName() + " (player, player_id, date, pvp, world, x, y, z) " +
                    "VALUES (" +
                    "'" + player.getName() + "'" + "," +
                    "'" + player.getUniqueId() + "'" + "," +
                    "'" + DateUtil.getDateString(heroDeathInfo.getTimestamp()) + "'" + "," +
                    "'" + ((heroDeathInfo.wasPvp()) ? 1 : 0) + "'" + "," +
                    "'" + heroDeathInfo.getLocation().getWorld().getName() + "'" + "," +
                    "'" + heroDeathInfo.getLocation().getBlockX() + "'" + "," +
                    "'" + heroDeathInfo.getLocation().getBlockY() + "'" + "," +
                    "'" + heroDeathInfo.getLocation().getBlockZ() + "'" +
                    ");";

            executeUpdate(query);
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public HeroDeathInfo getDeath(Player player) {

        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName()
                            + " WHERE player_id = '" + player.getUniqueId()
                            + "' AND world = '" + player.getWorld().getName() + "'");

            while (resultSet.next()) {

                HeroDeathInfo heroDeathInfo = new HeroDeathInfo(player,
                        new Location(player.getWorld(), resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z")),
                        DateUtil.getTimeStamp(resultSet.getString("date")));
                resultSet.close();
                return heroDeathInfo;
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getLastDeath(UUID player, String world) {

        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName()
                            + " WHERE player_id = '" + player + "' AND world = '" + world + "'");

            while (resultSet.next()) {
                long timestamp = DateUtil.getTimeStamp(resultSet.getString("date"));
                resultSet.close();
                return timestamp;
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<OfflinePlayerDeathInfo> getDeaths(World world) {

        List<OfflinePlayerDeathInfo> deathInfoList = new ArrayList<>();
        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName()
                            + " WHERE world = '" + world.getName() + "'");

            while (resultSet.next()) {
                if(resultSet.getString("player_id").isEmpty() || resultSet.getString("player_id") == "") {
                    continue;
                }

                OfflinePlayerDeathInfo deathInfo = new OfflinePlayerDeathInfo(new Location(world, resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z")),
                        DateUtil.getTimeStamp(resultSet.getString("date")), UUID.fromString(resultSet.getString("player_id")), resultSet.getString("player"));
                deathInfoList.add(deathInfo);
            }
            resultSet.close();
            return deathInfoList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Location getDeathLocation(UUID player, World world) {

        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName()
                            + " WHERE player_id = '" + player
                            + "' AND world = '" + world.getName() + "'");

            while (resultSet.next()) {
                Location deathLocation = new Location(world, resultSet.getInt("x"), resultSet.getInt("y"), resultSet.getInt("z"));
                resultSet.close();
                return deathLocation;
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(Player player) {

        try {
            executeUpdate(
                    "DELETE FROM " + getTableName()
                            + " WHERE player_id = '" + player.getUniqueId()
                            + "' AND world = '" + player.getWorld().getName() + "'");
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }
}
