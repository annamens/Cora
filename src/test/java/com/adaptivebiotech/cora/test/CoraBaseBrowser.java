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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import com.adaptivebiotech.cora.api.CoraApi;
import com.adaptivebiotech.test.TestBase;

public class CoraBaseBrowser extends TestBase {

    protected CoraApi coraApi = new CoraApi ();

    static {
        initialization ();
        testLog (format ("Current branch: %s - %s", version, gitcommitId));
    }

    @BeforeClass (alwaysRun = true)
    public void baseBeforeClass () {
        coraApi.login ();
        info (format ("running: %s", getClass ()));
    }

    @BeforeMethod (alwaysRun = true)
    public void baseBeforeMethod (Method method) {
        String testName = join (".", getClass ().getName (), method.getName ());
        MDC.put (jobId, testName);
        info (format ("running: %s()", testName));
    }

    protected String artifacts (String... paths) {
        return join ("/", "target/logs", join ("/", paths));
    }
}
