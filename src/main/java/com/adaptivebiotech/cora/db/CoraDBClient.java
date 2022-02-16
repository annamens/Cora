package com.adaptivebiotech.cora.db;

import static com.adaptivebiotech.test.BaseEnvironment.coraDBHost;
import static com.adaptivebiotech.test.BaseEnvironment.useDbTunnel;
import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.Logging.info;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.postgresql.util.PGobject;

public class CoraDBClient {

    private final String sshUrl     = "jdbc:postgresql://localhost:6000/coradb";
    private final String dbUrl      = "jdbc:postgresql://" + coraDBHost + ":5432/coradb";
    protected String     username;
    protected String     password;

    private Connection   connection = null;

    public CoraDBClient (String username, String pw) {
        this.username = username;
        this.password = pw;
    }

    public boolean openConnection () {
        closeConnection ();
        try {
            String url = useDbTunnel ? sshUrl : dbUrl;
            connection = DriverManager.getConnection (url, username, password);
            return true;
        } catch (SQLException e) {
            error ("Failed to open database connection: ", e);
            throw new RuntimeException (e);
        }
    }

    public void closeConnection () {
        if (connection != null) {
            try {
                connection.close ();
                connection = null;
            } catch (SQLException e) {
                error ("Failed to close Database connection: ", e);
                throw new RuntimeException (e);
            }
        }
    }

    public List <Map <String, Object>> executeSelectQuery (String query) {
        info ("query is: " + query);
        List <Map <String, Object>> tableData = new LinkedList <> ();

        try (Statement statement = connection.createStatement ();
                ResultSet resultSet = statement.executeQuery (query);) {
            if (resultSet != null) {
                ResultSetMetaData metaData = resultSet.getMetaData ();
                int columns = metaData.getColumnCount ();

                while (resultSet.next ()) {
                    Map <String, Object> row = new HashMap <String, Object> (columns);
                    for (int i = 1; i <= columns; ++i) {
                        row.put (metaData.getColumnName (i), resultSet.getObject (i));
                    }
                    tableData.add (row);
                }
            }
        } catch (SQLException e) {
            error ("Failed to access Database: " + e);
            throw new RuntimeException (e);
        }
        info ("Query Results: " + tableData);
        return tableData;
    }

    public String jsonbToString (Object data) {
        try {
            return new JSONObject ( ((PGobject) data).getValue ()).toString ();
        } catch (Exception e) {
            error ("Unable to convert jsonb to string", e);
            throw new RuntimeException (e);
        }
    }
}
