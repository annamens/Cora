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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import com.seleniumfy.test.utils.BaseBrowser;

public class CoraBaseBrowser extends BaseBrowser {

    static {
        initialization ();
        testLog ("Current branch: " + version);
    }

    @BeforeClass (alwaysRun = true)
    public void baseBeforeClass () {
        info (format ("running: %s", getClass ()));
    }

    @BeforeMethod (alwaysRun = true)
    public void baseBeforeMethod (Method method) {
        info (format ("running: %s.%s()", getClass ().getSimpleName (), method.getName ()));
    }

    protected String artifacts (String... paths) {
        return join ("/", "target/logs", join ("/", paths));
    }

    protected void doCoraLogin () {
        Map <String, String> forms = new HashMap <> ();
        forms.put ("userName", coraTestUser);
        forms.put ("password", coraTestPass);
        formPost (coraTestUrl + "/cora/login", forms);
    }
}
