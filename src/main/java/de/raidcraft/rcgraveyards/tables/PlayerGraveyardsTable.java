package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.database.Table;
import de.raidcraft.rcgraveyards.Graveyard;
import de.raidcraft.util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                            "`player` VARCHAR( 32 ), " +
                            "`player_id` VARCHAR( 32 ) NOT NULL, " +
                            "`graveyard` VARCHAR( 32 ) NOT NULL, " +
                            "`discovered` VARCHAR ( 32 ) NOT NULL, " +
                            "PRIMARY KEY ( `id` )" +
                            ")"
            ).execute();
        } catch (SQLException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
        }
    }

    public void addAssignment(UUID player, Graveyard graveyard) {

        try {
            String query = "INSERT INTO " + getTableName() + " (player_id, graveyard, discovered) " +
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

    public List<String> getPlayerAssignments(UUID player) {

        List<String> graveyards = new ArrayList<>();
        try {
            ResultSet resultSet = executeQuery(
                    "SELECT * FROM " + getTableName() + " WHERE player_id = '" + player + "';");

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
