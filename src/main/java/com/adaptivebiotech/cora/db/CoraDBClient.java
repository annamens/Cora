package com.adaptivebiotech.cora.db;

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
import com.adaptivebiotech.test.utils.Logging;

public class CoraDBClient {

    private String     url        = "jdbc:postgresql://localhost:6000/coradb";

    private String     username;
    private String     password;

    private Connection connection = null;

    public CoraDBClient (String username, String pw) {
        this.username = username;
        this.password = pw;
    }

    public boolean openConnection () {
        closeConnection ();
        try {
            connection = DriverManager.getConnection (url, username, password);
            return true;
        } catch (SQLException e) {
            Logging.error ("Failed to access Database or Timeout error", e);
            throw new RuntimeException (e);
        }
    }

    public void closeConnection () {
        if (this.connection != null) {
            try {
                connection.close ();
                connection = null;
            } catch (SQLException e) {
                Logging.error ("Failed to access Database or Timeout error", e);
                throw new RuntimeException (e);
            }
        }
    }

    public List <Map <String, Object>> executeSelectQuery (String query) {
        List <Map <String, Object>> tableData = new LinkedList <> ();

        Logging.info ("query is: " + query);

        try {
            Statement statement = connection.createStatement ();

            ResultSet resultSet = statement.executeQuery (query);

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
            resultSet.close ();
            statement.close ();
        } catch (SQLException e) {
            Logging.error ("Failed to access Database: " + e);
            throw new RuntimeException (e);
        }
        return tableData;
    }

}
