package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.test.CoraEnvironment.initialization;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.BaseEnvironment.version;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.seleniumfy.test.utils.HttpClientHelper.formPost;
import static java.lang.String.format;
import static java.lang.String.join;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.http.message.BasicHeader;
import org.slf4j.MDC;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.util.Strings;
import com.adaptivebiotech.test.TestBase;
import com.seleniumfy.test.utils.HttpClientHelper;

public class CoraBaseBrowser extends TestBase {

    static {
        initialization ();
        testLog ("Current branch: " + version);
    }

    @BeforeClass (alwaysRun = true)
    public void baseBeforeClass () {
        MDC.put ("job.id", UUID.randomUUID ().toString ());
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

    protected void doCoraLogin () {
        Map <String, String> forms = new HashMap <> ();
        forms.put ("userName", coraTestUser);
        forms.put ("password", coraTestPass);
        formPost (coraTestUrl + "/cora/login", forms);
        HttpClientHelper.headers.get ().add (new BasicHeader ("X-Api-UserName", coraTestUser));
    }

}
