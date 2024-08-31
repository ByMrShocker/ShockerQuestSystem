package bymrshocker.shockerquestsystem.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public DatabaseInitializer(Connection connection) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS QuestsProgress ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "player TEXT NOT NULL, "
                + "questsInProgress TEXT NOT NULL, "
                + "questsComplete TEXT NOT NULL, "
                + "reputation TEXT NOT NULL "
                + ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
