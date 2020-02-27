package com.adaptivebiotech.test.cora.e2e;

import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.cora.CoraEnvironment.incomingPath;
import static com.adaptivebiotech.test.cora.CoraEnvironment.projectAccountID;
import static com.adaptivebiotech.test.cora.CoraEnvironment.projectID;
import static com.adaptivebiotech.test.cora.CoraEnvironment.projectName;
import static com.adaptivebiotech.test.cora.CoraEnvironment.retryTimes;
import static com.adaptivebiotech.test.cora.CoraEnvironment.sftpServerHostName;
import static com.adaptivebiotech.test.cora.CoraEnvironment.waitTime;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.clonality;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.tracking;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.PENDING_ANALYSIS;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.PENDING_OTHER_REPORTS;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.KitOrder;
import com.adaptivebiotech.cora.ui.order.OrderList;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.workflow.Debug;
import com.adaptivebiotech.test.cora.CoraBaseBrowser;
import com.adaptivebiotech.test.utils.PageHelper.ReportType;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

public class KitE2ETestBase extends CoraBaseBrowser {
    protected Diagnostic diagnostic;
    protected String     url                        = coraTestUrl + "/cora/api/v1/test/scenarios/researchTechTransfer";
    protected String     SR_T1772ClonalityJFilePath = "src/test/resources/SR-T1772_Clonality.json";
    protected String     SR_T1772TrackingJFilePath  = "src/test/resources/SR-T1772_TrackingAboveLOQ1.json";
    protected KitOrder  clonalityOrder;
    protected KitOrder  trackingOrder;

    protected HttpResponse submitNewOrderRequest (String jsonString) {

        try {
            return mapper.readValue (post (url, body (jsonString)), HttpResponse.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    protected void preCondition (String clonalityJFile, String trackingJFile) {
        testLog ("Process pre-condition");
        clonalityOrder = new KitOrder ();
        clonalityOrder.type = clonality;
        createOrder (clonalityJFile, clonalityOrder);
        testLog ("Clonality order was created");

        trackingOrder = new KitOrder ();
        trackingOrder.type = tracking;
        createOrder (trackingJFile, trackingOrder);
        testLog ("Tracking order was created");

    }

    protected void createOrder (String jFileName, KitOrder order) {

        String jString = changeClonalityOrTrackingJsonFile (jFileName, order.type);
        if (order.type == clonality) {
            testLog ("Run Clonality request");
        } else {
            testLog ("Run Tracking request");
        }
        submitNewOrderRequest (jString);

        SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd");
        dateFormat.setTimeZone (TimeZone.getTimeZone ("UTC"));
        order.orderDate_ISO_DATE = dateFormat.format (new Date ());
        dateFormat.applyPattern ("yyyyMMdd");
        order.orderDate = dateFormat.format (new Date ());

        Debug orderDebug = new Debug ();
        testLog ("Go to debug page for " + order.sampleName);
        orderDebug.gotoOrderDebug (order.sampleName, retryTimes, waitTime);

        testLog ("Ensure order is forwarded to Secondary Analysis Stage");
        if (order.type == clonality) {
            orderDebug.waitFor (SecondaryAnalysis, Awaiting, PENDING_ANALYSIS);
        } else {
            orderDebug.waitFor (SecondaryAnalysis, Awaiting, PENDING_OTHER_REPORTS);
        }
        orderDebug.doOrderTestSearch (order.sampleName);
        orderDebug.clickOrderTest ();

        OrderStatus status = new OrderStatus ();
        order.orderNum = status.getOrderNum ();
        order.orderName = status.getOrderName ();

    }

    // Change element values as below in Clonality Json file :
    // 1. externalSubjecteId
    // 2. samples.name
    @SuppressWarnings ("unchecked")
    protected String changeClonalityOrTrackingJsonFile (String jFileName, ReportType type) {

        try {

            String strVar = getRandomString ();
            if (type == clonality) {
                clonalityOrder.sampleName = "TSBUNIQUE1-Clonality" + strVar;
                clonalityOrder.externalSubjectId = "TSBUNIQUE1SubjectId" + strVar;
            } else {
                trackingOrder.sampleName = "TSBUNIQUE1-MRD1" + strVar;
                trackingOrder.externalSubjectId = clonalityOrder.externalSubjectId;
            }

            // read the json file
            FileReader reader = new FileReader (jFileName);

            JSONParser jsonParser = new JSONParser ();
            JSONObject jsonObject = (JSONObject) jsonParser.parse (reader);

            JSONObject techTransObj = (JSONObject) jsonObject.get ("techTransfer");

            JSONArray specimensArray = (JSONArray) techTransObj.get ("specimens");

            JSONObject externObj = (JSONObject) specimensArray.get (0);

            externObj.put ("externalSubjectId", clonalityOrder.externalSubjectId);

            JSONArray sampleArray = (JSONArray) externObj.get ("samples");
            JSONObject sampleObj = (JSONObject) sampleArray.get (0);
            
            String coDate= (String) externObj.get ("collectionDate");
            String collectionDate = coDate.toString().substring (0, coDate.toString().indexOf ("T"));
             
            
            if (type == clonality) {
                sampleObj.put ("name", clonalityOrder.sampleName);
                clonalityOrder.collectionDate = collectionDate;
                clonalityOrder.sampleType = (String) externObj.get ("sampletype");
                clonalityOrder.sampleSource = (String) externObj.get ("sampleSource");
            } else {
                sampleObj.put ("name", trackingOrder.sampleName);
                trackingOrder.collectionDate = collectionDate;
                trackingOrder.sampleType = (String) externObj.get ("sampletype");
                trackingOrder.sampleSource = (String) externObj.get ("sampleSource");
            }

            JSONObject projectObj = (JSONObject) jsonObject.get ("project");
            projectObj.put ("id", projectID);
            projectObj.put ("accountId", projectAccountID);
            projectObj.put ("name", projectName);

            return mapper.writeValueAsString (jsonObject);

        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
    // Change element values as below in Clonality Json file :
    // 1. externalSubjecteId
    // 2. samples.name

    protected String getRandomString () {
        LocalDateTime now = LocalDateTime.now ();
        return Integer.toString (now.getYear ()) + Integer.toString (now.getMonthValue ()) + Integer.toString (now.getDayOfYear ()) + Integer.toString (now.getHour ()) + Integer.toString (now.getMinute ()) + Integer.toString (now.getSecond ());
    }

    protected void verifyWorkflowStageDropdowncontainsSEQReportAndDelivery () {
        OrderList list = new OrderList ();
        List <String> labels = list.getStageDropDownMenuItemLabelList ();
        assertTrue (labels.get (0).contains ("KitClonoSEQReport"));
        assertTrue (labels.get (0).contains ("KitReportDelivery"));
    }

    protected void verifySFTPLocation (ReportType type) {
        String actualMessage, expectedMessage;
        String fileName;

        Debug debugPage = new Debug ();
        actualMessage = debugPage.getStatusHistoryTableStringValue ("2", "4");
        if (type == ReportType.clonality) {
            fileName = debugPage.getClinicalReportFileName (retryTimes, waitTime);
        } else {
            fileName = debugPage.getClinicalReportFileName (retryTimes, waitTime);
        }
        fileName = fileName.substring (15, fileName.length ());
        String orderNumber = (type == ReportType.clonality) ? clonalityOrder.orderNum : trackingOrder.orderNum;
        String orderDate = (type == ReportType.clonality) ? clonalityOrder.orderDate : trackingOrder.orderDate;
        expectedMessage = String.format ("Report delivered to: sftp://%s/%s/" + "Clinical Reports/%s_%s/%s.",
                                         sftpServerHostName,
                                         incomingPath,
                                         orderNumber,
                                         orderDate,
                                         fileName);

        assertEquals (expectedMessage, actualMessage);

    }

}
