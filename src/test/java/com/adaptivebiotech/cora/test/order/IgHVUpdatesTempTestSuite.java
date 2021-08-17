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
import com.fasterxml.jackson.core.type.TypeReference;
import com.seleniumfy.test.utils.HttpClientHelper;

public class IgHVUpdatesTempTestSuite extends CoraBaseBrowser {

    @Test
    public void verifyIgHVStageAndReportFeatureOrder1CLIAFeatureFlagOn () {
        releaseReport ("https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/1940a8b4-6bcb-4d6d-9285-83241fdae3a5/Workflow/reportData.json",
                       true);
    }

    @Test
    public void verifyIgHVStageAndReportFeatureOrder2CLIAFeatureFlagOn () {
        releaseReport ("https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/8076f7c0-a4b8-481e-9498-fec9c300485c/Workflow/reportData.json",
                       false);
    }

    @Test
    public void verifyIgHVStageAndReportFeatureOrder3IVDFeatureFlagOn () {
        releaseReport ("https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/15e5612a-863f-425d-ba2e-687622041487/Workflow/reportData.json",
                       true);
    }

    @Test
    public void verifyIgHVStageAndReportFeatureOrder4IVDFeatureFlagOn () {
        releaseReport ("https://cora-test.dna.corp.adaptivebiotech.com/cora/debug/file/963cc86a-f3ad-4866-92a1-48d404e214e3/Workflow/reportData.json",
                       true);
    }

    private void releaseReport (String fileUrl,
                                boolean expectedShmReportKey) {
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
