package com.adaptivebiotech.test.cora;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestPassword;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.BaseEnvironment.initialization;
import static com.adaptivebiotech.test.utils.HttpClientHelper.body;
import static com.adaptivebiotech.test.utils.HttpClientHelper.formPost;
import static com.adaptivebiotech.test.utils.HttpClientHelper.get;
import static com.adaptivebiotech.test.utils.HttpClientHelper.post;
import static com.adaptivebiotech.test.utils.Logging.error;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static org.testng.Assert.fail;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import com.adaptivebiotech.dto.AssayResponse;
import com.adaptivebiotech.dto.Diagnostic;
import com.adaptivebiotech.dto.HttpResponse;
import com.adaptivebiotech.dto.Patient;
import com.adaptivebiotech.dto.PatientResponse;
import com.adaptivebiotech.test.utils.BaseBrowser;
import com.adaptivebiotech.ui.cora.CoraPage;

public class CoraBaseBrowser extends BaseBrowser {

    protected final Header[] headers = new Header[] { new BasicHeader ("X-Api-UserName", "svc_test_user") };

    static {
        try {
            initialization ();
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

    protected HttpResponse newDiagnosticOrder (Diagnostic diagnostic) {
        try {
            String json = mapper.writeValueAsString (diagnostic);
            info ("payload=" + json);

            String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticClarity";
            return mapper.readValue (post (url, body (json), headers), HttpResponse.class);
        } catch (Exception e) {
            error (String.valueOf (e), e);
            fail (String.valueOf (e));
            return null;
        }
    }

    protected AssayResponse getTests () {
        try {
            String url = coraTestUrl + "/cora/api/v1/tests?categoryId=63780203-caeb-483d-930c-8392afb5d927";
            return mapper.readValue (get (url), AssayResponse.class);
        } catch (Exception e) {
            error (String.valueOf (e), e);
            fail (String.valueOf (e));
            return null;
        }
    }

    protected Patient getPatient (Patient patient) {
        try {
            String url = coraTestUrl + "/cora/api/v1/patients?firstName=" + patient.firstName + "&lastName=" + patient.lastName;
            return mapper.readValue (get (url), PatientResponse.class).get (patient);
        } catch (Exception e) {
            error (String.valueOf (e), e);
            fail (String.valueOf (e));
            return null;
        }
    }
}
