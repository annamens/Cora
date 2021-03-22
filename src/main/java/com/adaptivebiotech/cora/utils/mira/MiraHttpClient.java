package com.adaptivebiotech.cora.utils.mira;

import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static com.seleniumfy.test.utils.Logging.error;
import static com.seleniumfy.test.utils.Logging.info;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.utils.mira.techtransfer.TestScenarioInfo;
import com.seleniumfy.test.utils.HttpClientHelper;

public class MiraHttpClient {

    private final String techTransferEndpoint = "/cora/api/v1/test/scenarios/researchTechTransfer";

    public void postTestScenarioToCora (TestScenarioInfo testScenarioInfo) {

        String url = CoraEnvironment.coraTestUrl + techTransferEndpoint;
        info ("url is: " + url);

        HttpClientHelper.headers.add (new BasicHeader ("X-Api-UserName", CoraEnvironment.coraTestUser));

        try {
            HttpResponse response = mapper.readValue (post (url,
                                                            body (TestScenarioInfo.toJson (testScenarioInfo)
                                                                                  .toString ())),
                                                      HttpResponse.class);
            info ("response is " + response.toString ());

        } catch (Exception e) {
            error (e.getMessage ());
            throw new RuntimeException (e);
        }
    }

    public void doCoraApiLogin () {
        Map <String, String> forms = new HashMap <> ();
        forms.put ("userName", CoraEnvironment.coraTestUser);
        forms.put ("password", CoraEnvironment.coraTestPass);
        HttpClientHelper.formPost (CoraEnvironment.coraTestUrl + "/cora/login", forms);
    }

}
