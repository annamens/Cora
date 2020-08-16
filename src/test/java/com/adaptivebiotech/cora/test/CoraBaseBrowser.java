package com.adaptivebiotech.cora.test;

import static com.adaptivebiotech.cora.test.CoraEnvironment.initialization;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestPass;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.BaseEnvironment.version;
import static com.adaptivebiotech.test.utils.Logging.info;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.formPost;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static com.seleniumfy.test.utils.HttpClientHelper.headers;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static java.lang.String.format;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.BeforeMethod;
import com.adaptivebiotech.common.dto.Patient;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.PatientResponse;
import com.seleniumfy.test.utils.BaseBrowser;

public class CoraBaseBrowser extends BaseBrowser {

    static {
        initialization ();
        testLog ("Current branch: " + version);
    }

    @BeforeMethod (alwaysRun = true)
    public void baseBeforeMethod (Method method) throws Exception {
        info (format ("running: %s.%s()", getClass ().getSimpleName (), method.getName ()));
    }

    protected void doCoraLogin () {
        Map <String, String> forms = new HashMap <> ();
        forms.put ("userName", coraTestUser);
        forms.put ("password", coraTestPass);
        formPost (coraTestUrl + "/cora/login", forms);
        headers.add (new BasicHeader ("X-Api-UserName", "svc_test_user"));
    }

    protected HttpResponse newDiagnosticOrder (Diagnostic diagnostic) {
        try {
            String url = coraTestUrl + "/cora/api/v1/test/scenarios/diagnosticClarity";
            return mapper.readValue (post (url, body (mapper.writeValueAsString (diagnostic))),
                                     HttpResponse.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    protected AssayResponse getTests () {
        try {
            String url = coraTestUrl + "/cora/api/v1/tests?categoryId=63780203-caeb-483d-930c-8392afb5d927";
            return mapper.readValue (get (url), AssayResponse.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    protected Patient getPatient (Patient patient) {
        try {
            String url = coraTestUrl + "/cora/api/v1/patients?firstName=" + patient.firstName + "&lastName=" + patient.lastName;
            return mapper.readValue (get (url), PatientResponse.class).get (patient);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
}
