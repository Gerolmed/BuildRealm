package net.endrealm.buildrealm.repository.impl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLLiteDraftRepository extends SQLDraftRepository{
    private final String dbname ="drafts";
    private final File parent;

    public SQLLiteDraftRepository(File parent, Logger logger){
        super(logger);
        this.parent = parent;
        load();
    }

    public String SQLiteCreateTokensTable = ("CREATE TABLE IF NOT EXISTS %s (" +
            "`id` varchar(32) NOT NULL," +
            "`members` text NOT NULL," +
            "`membersRaw` text NOT NULL," +
            "`open` integer NOT NULL," +
            "`pieceType` varchar(64) NULL ," +
            "`group` varchar(64) NULL," +
            "`forkId` varchar(32) NULL," +
            "`note` text NOT NULL," +
            "`number` varchar(64) NULL," +
            "`forkCount` integer NULL," +
            "`lastUpdated` timestamp," +
            "PRIMARY KEY (`id`)" +
            ");").formatted(getTable());


    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(parent, dbname+".db");
        if (!dataFolder.exists()){
            try {
                parent.mkdirs();
                dataFolder.createNewFile();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            getLogger().log(Level.SEVERE, "You need the SQLite JBDC library.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
