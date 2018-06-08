package com.adaptivebiotech.test.cora;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestPassword;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.BaseEnvironment.initialization;
import static com.adaptivebiotech.test.utils.HttpClientHelper.formPost;
import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.testng.Assert.fail;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.adaptivebiotech.test.utils.BaseBrowser;
import com.adaptivebiotech.ui.cora.CoraPage;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CoraBaseBrowser extends BaseBrowser {

    protected static ObjectMapper mapper = new ObjectMapper ();

    static {
        try {
            initialization ();
            mapper.setSerializationInclusion (NON_NULL);
            mapper.configure (FAIL_ON_UNKNOWN_PROPERTIES, false);
        } catch (Exception e) {
            error (String.valueOf (e), e);
            fail (String.valueOf (e));
        }
    }

    @BeforeSuite (alwaysRun = true)
    public void beforeSuite () {
        doCoraLogin ();
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method method) throws Exception {
        info ("running: " + getClass ().getSimpleName () + "." + method.getName () + "()");
        openBrowser ();
        new CoraPage ().doLogin ();
    }

    protected void doCoraLogin () {
        Map <String, String> forms = new HashMap <String, String> ();
        forms.put ("userName", coraTestUser);
        forms.put ("password", coraTestPassword);
        formPost (coraTestUrl + "/cora/login", forms);
    }
}
