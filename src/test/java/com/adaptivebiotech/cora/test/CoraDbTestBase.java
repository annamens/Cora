package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import com.adaptivebiotech.cora.db.CoraDBClient;
import com.adaptivebiotech.cora.utils.Tunnel;

public class CoraDbTestBase extends CoraBaseBrowser {

    private Tunnel         tunnel;
    protected CoraDBClient coraDBClient;

    @BeforeClass (alwaysRun = true)
    public void dbTestBeforeClass () {
        testLog ("Should connect to DB using tunnel? " + CoraEnvironment.isDbTunnel);
        if (CoraEnvironment.isDbTunnel) {
            testLog ("Creating a DB connection using tunnel");
            tunnel = Tunnel.getTunnel ();
            Thread t = new Thread (tunnel);
            t.start ();
            tunnel.waitForConnection ();
        }
        testLog ("Create DB Conection");
        coraDBClient = new CoraDBClient (CoraEnvironment.coraDBUser, CoraEnvironment.coraDBPass);

        assertTrue (coraDBClient.openConnection ());
        testLog ("DB connection successful");
    }

    @AfterClass (alwaysRun = true)
    public void dbTestafterClass () throws Exception {
        coraDBClient.closeConnection ();
        if (CoraEnvironment.isDbTunnel)
            tunnel.close ();
        testLog ("DB connection closed");
    }
}
