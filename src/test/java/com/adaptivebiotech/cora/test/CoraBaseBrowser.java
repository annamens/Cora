package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.test.CoraEnvironment.initialization;
import static com.adaptivebiotech.test.BaseEnvironment.gitcommitId;
import static com.adaptivebiotech.test.BaseEnvironment.version;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static java.lang.String.join;
import java.lang.reflect.Method;
import org.slf4j.MDC;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.adaptivebiotech.cora.api.CoraApi;
import com.adaptivebiotech.cora.db.CoraDb;
import com.adaptivebiotech.test.TestBase;

public class CoraBaseBrowser extends TestBase {

    protected static CoraApi coraApi;
    protected static CoraDb  coraDb;

    static {
        initialization ();
        testLog (format ("Current branch: %s - %s", version, gitcommitId));

        coraApi = new CoraApi ();
        coraApi.auth ();
        coraDb = new CoraDb ();
    }

    @BeforeSuite (alwaysRun = true)
    public final void baseBeforeSuite () {
        MDC.put (jobId, this.getClass ().getName ());
    }

    @AfterSuite (alwaysRun = true)
    public final void baseAfterSuite () {
        coraDb.closeConnection ();
    }

    @BeforeClass (alwaysRun = true)
    public final void baseBeforeClass () {
        info (format ("running: %s", getClass ()));
    }

    @BeforeMethod (alwaysRun = true)
    public final void baseBeforeMethod (Method method) {
        String testName = join (".", getClass ().getName (), method.getName ());
        MDC.put (jobId, testName);
        info (format ("running: %s()", testName));
        coraApi.addCoraToken ();
    }

    protected String artifacts (String... paths) {
        return join ("/", "target/logs", join ("/", paths));
    }
}
