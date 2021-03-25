package com.adaptivebiotech.cora.utils.mira;

import static com.seleniumfy.test.utils.Logging.info;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.utils.TestScenarioBuilder;
import com.seleniumfy.test.utils.HttpClientHelper;

public class MiraHttpClient {

    public void postTestScenarioToCora (Research research) {

        HttpClientHelper.headers.add (new BasicHeader ("X-Api-UserName", CoraEnvironment.coraTestUser));
        HttpResponse httpResponse = TestScenarioBuilder.newResearchOrder (research);
        info ("response is: " + httpResponse.toString ());
    }

    public void doCoraApiLogin () {
        Map <String, String> forms = new HashMap <> ();
        forms.put ("userName", CoraEnvironment.coraTestUser);
        forms.put ("password", CoraEnvironment.coraTestPass);
        HttpClientHelper.formPost (CoraEnvironment.coraTestUrl + "/cora/login", forms);
    }

}
