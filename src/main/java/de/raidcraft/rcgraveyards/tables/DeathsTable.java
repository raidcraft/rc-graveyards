package de.raidcraft.rcgraveyards.tables;

import de.raidcraft.api.database.Table;
import de.raidcraft.rcgraveyards.GraveyardPlayer;

import java.sql.SQLException;

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
                            "`player` INT( 11 ) NOT NULL ,\n" +
                            "`date` VARCHAR( 32 ) NOT NULL ,\n" +
                            "`pvp` TINYINT NOT NULL , \n" +
                            "PRIMARY KEY ( `id` )\n" +
                            ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDeath(GraveyardPlayer graveyardPlayer) {

    }
}
