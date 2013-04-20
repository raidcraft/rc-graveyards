package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Urban
 */
public class PlayerGraveyardsTable extends Table {

    public PlayerGraveyardsTable() {

        super("players_graveyards", "rcgraveyards_");
    }

    @Override
    public void createTable() {

        try {
            getConnection().prepareStatement(
                    "CREATE TABLE `" + getTableName() + "` (" +
                            "`id` INT NOT NULL AUTO_INCREMENT, " +
                            "`player` VARCHAR( 32 ) NOT NULL, " +
                            "`graveyard` VARCHAR( 32 ) NOT NULL, " +
                            "`discovered` VARCHAR ( 32 ) NOT NULL, " +
                            "PRIMARY KEY ( `id` )" +
                            ")").execute();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    public void addAssignment(String player, Graveyard graveyard) {

        try {
            String query = "INSERT INTO " + getTableName() + " (player, graveyard, discovered) " +
                    "VALUES (" +
                    "'" + player + "'" + "," +
                    "'" + graveyard.getName() + "'" + "," +
                    "'" + DateUtil.getCurrentDateString() + "'" +
                    ");";

            executeUpdate(query);
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    public List<String> getPlayerAssignments(String player) {

        List<String> graveyards = new ArrayList<>();
        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName() + " WHERE player = '" + player + "';");

            while (resultSet.next()) {

                graveyards.add(resultSet.getString("graveyard"));
            }
            resultSet.close();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
        return graveyards;
    }
}
