package com.adaptivebiotech.cora.db;

import static com.adaptivebiotech.test.BaseEnvironment.coraDBHost;
import static com.adaptivebiotech.test.BaseEnvironment.coraDBPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraDBUser;
import static com.adaptivebiotech.test.BaseEnvironment.useDbTunnel;
import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.Logging.info;
import static org.testng.Assert.assertFalse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import com.adaptivebiotech.cora.utils.Tunnel;

public class CoraDBClient {

    private final String sshUrl = "jdbc:postgresql://localhost:6000/coradb";
    private final String dbUrl  = "jdbc:postgresql://" + coraDBHost + ":5432/coradb";
    private Connection   connection;
    private Tunnel       tunnel;

    public CoraDBClient () {
        try {
            if (useDbTunnel) {
                info ("Creating a DB connection using tunnel");
                tunnel = Tunnel.getTunnel ();
                Thread t = new Thread (tunnel);
                t.start ();
                tunnel.waitForConnection ();
            }

            String url = useDbTunnel ? sshUrl : dbUrl;
            connection = DriverManager.getConnection (url, coraDBUser, coraDBPass);
        } catch (Exception e) {
            error ("Failed to open database connection", e);
            throw new RuntimeException (e);
        }
    }

    public void closeConnection () {
        try {
            connection.close ();
            connection = null;

            if (useDbTunnel)
                tunnel.close ();
            info ("DB connection closed");
        } catch (Exception e) {
            error ("Failed to close Database connection", e);
            throw new RuntimeException (e);
        }
    }

    public List <Map <String, Object>> executeSelect (String query) {
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
        } catch (Exception e) {
            error ("Failed to run SQL query", e);
            throw new RuntimeException (e);
        }
        info ("query results: " + tableData);
        return tableData;
    }

    public int executeUpdate (String query) {
        info ("query is: " + query);

        try (Statement statement = connection.createStatement ()) {
            assertFalse (statement.execute (query)); // returns false on update
            return statement.getUpdateCount ();
        } catch (Exception e) {
            error ("Failed to run SQL query", e);
            throw new RuntimeException (e);
        }
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
