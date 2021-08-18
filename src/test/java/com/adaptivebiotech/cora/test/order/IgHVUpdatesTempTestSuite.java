package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static org.testng.Assert.assertEquals;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.fasterxml.jackson.core.type.TypeReference;
import com.seleniumfy.test.utils.HttpClientHelper;

public class IgHVUpdatesTempTestSuite extends CoraBaseBrowser {

    @Test
    public void verifyIgHVStageAndReportFeatureOrder1CLIAFeatureFlagOn () {
//        String tempString = "https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/1940a8b4-6bcb-4d6d-9285-83241fdae3a5/Workflow/reportData.json";
        String tempString = "118422-01BC";
        releaseReport (tempString,
                       true);
    }

    @Test
    public void verifyIgHVStageAndReportFeatureOrder2CLIAFeatureFlagOn () {
//        String tempString = "https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/8076f7c0-a4b8-481e-9498-fec9c300485c/Workflow/reportData.json";
        String tempString = "118421-01BC";
        releaseReport (tempString,
                       false);
    }

    @Test
    public void verifyIgHVStageAndReportFeatureOrder3IVDFeatureFlagOn () {
//        String tempString = "https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/15e5612a-863f-425d-ba2e-687622041487/Workflow/reportData.json";
        String tempString = "118420-01MC";
        releaseReport (tempString,
                       true);
    }

    @Test
    public void verifyIgHVStageAndReportFeatureOrder4IVDFeatureFlagOn () {
//        String tempString = "https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/963cc86a-f3ad-4866-92a1-48d404e214e3/Workflow/reportData.json";
        String tempString = "118417-01BC";
        releaseReport (tempString,
                       true);
    }

    private void releaseReport (String sampleName,
                                boolean expectedShmReportKey) {
        new Login ().doLogin ();
        History history = new History ();
        
        history.gotoOrderDebug (sampleName);
        String fileUrl = history.getFileUrl ("reportData.json");
        // get file using get request
        doCoraLogin ();
        Map <String, Object> reportData = null;
        try {
            testLog ("File URL: " + fileUrl);
            String getResponse = get (fileUrl);
            testLog ("File URL Response: " + getResponse);
            reportData = mapper.readValue (getResponse,
                                           new TypeReference <Map <String, Object>> () {});
        } catch (Exception e) {
            e.printStackTrace ();
        }
        testLog ("Json File Data " + reportData);
        HttpClientHelper.headers.get ().remove (new BasicHeader ("X-Api-UserName", coraTestUser));
        assertEquals (reportData.containsKey ("shmReportResult"), expectedShmReportKey);
    }

}
