package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.test.CoraEnvironment.initialization;
import static com.adaptivebiotech.test.BaseEnvironment.gitcommitId;
import static com.adaptivebiotech.test.BaseEnvironment.version;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static java.lang.String.join;
import java.lang.reflect.Method;
import java.util.UUID;
import org.slf4j.MDC;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.util.Strings;
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
        MDC.put ("job.id", UUID.randomUUID ().toString ());
        coraApi.login ();
        info (format ("running: %s", getClass ()));
    }

    @BeforeMethod (alwaysRun = true)
    public void baseBeforeMethod (Method method) {
        if (Strings.isNullOrEmpty (MDC.get ("job.id")))
            MDC.put ("job.id", UUID.randomUUID ().toString ());
        info (format ("running: %s.%s()", getClass ().getSimpleName (), method.getName ()));
    }

    @AfterMethod (alwaysRun = true)
    public void baseAfterMethod (Method method) {
        MDC.clear ();
    }

    protected String artifacts (String... paths) {
        return join ("/", "target/logs", join ("/", paths));
    }
}
