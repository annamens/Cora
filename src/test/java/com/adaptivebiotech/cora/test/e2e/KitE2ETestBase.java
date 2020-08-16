package com.adaptivebiotech.cora.test.e2e;

import static com.adaptivebiotech.cora.test.CoraEnvironment.incomingPath;
import static com.adaptivebiotech.cora.test.CoraEnvironment.projectAccountID;
import static com.adaptivebiotech.cora.test.CoraEnvironment.projectID;
import static com.adaptivebiotech.cora.test.CoraEnvironment.projectName;
import static com.adaptivebiotech.cora.test.CoraEnvironment.retryTimes;
import static com.adaptivebiotech.cora.test.CoraEnvironment.sftpServerHostName;
import static com.adaptivebiotech.cora.test.CoraEnvironment.waitTime;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.clonality;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.tracking;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.KitClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Processing;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.PENDING_ANALYSIS;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.PENDING_OTHER_REPORTS;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.body;
import static com.seleniumfy.test.utils.HttpClientHelper.post;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.adaptivebiotech.cora.dto.HttpResponse;
import com.adaptivebiotech.cora.dto.KitOrder;
import com.adaptivebiotech.cora.dto.Research;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.workflow.Debug;
import com.adaptivebiotech.test.utils.PageHelper.ReportType;
import com.adaptivebiotech.ui.cora.order.Diagnostic;

public class KitE2ETestBase extends CoraBaseBrowser {
    protected Diagnostic diagnostic;
    protected String     url                        = coraTestUrl + "/cora/api/v1/test/scenarios/researchTechTransfer";
    protected String     SR_T1772ClonalityJFilePath = "SR-T1772_Clonality.json";
    protected String     SR_T1772TrackingJFilePath  = "SR-T1772_TrackingAboveLOQ1.json";
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
    //@SuppressWarnings ("unchecked")
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
            
            Research research;
            research = mapper.readValue (new File(jFileName), Research.class);
            research.project.accountId = projectAccountID;
            research.project.id = projectID;
            research.project.name = projectName;
            
            String collectionDate = research.techTransfer.specimens.get (0).collectionDate.toString ();
            collectionDate = collectionDate.substring (0, collectionDate .indexOf ("T"));
                      
            if (type == clonality) {
                research.techTransfer.specimens.get (0).samples.get (0).name = clonalityOrder.sampleName;
                clonalityOrder.collectionDate = collectionDate;
                clonalityOrder.sampleType =    research.techTransfer.specimens.get (0).sampleType.label;
                clonalityOrder.sampleSource =  research.techTransfer.specimens.get (0).sampleSource.label;
                research.techTransfer.specimens.get (0).externalSubjectId = clonalityOrder.externalSubjectId;
            } else {
                research.techTransfer.specimens.get (0).samples.get (0).name = trackingOrder.sampleName;
                trackingOrder.collectionDate = collectionDate;
                trackingOrder.sampleType = research.techTransfer.specimens.get (0).sampleType.label;
                trackingOrder.sampleSource = research.techTransfer.specimens.get (0).sampleSource.label;
                research.techTransfer.specimens.get (0).externalSubjectId = trackingOrder.externalSubjectId;
            }
     
            return mapper.writeValueAsString (research);
            
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
        OrdersList list = new OrdersList ();
        List <String> labels = list.getStageDropDownMenuItemLabelList ();
        assertTrue (labels.get (0).contains ("KitClonoSEQReport"));
        assertTrue (labels.get (0).contains ("KitReportDelivery"));
    }

    protected void verifySFTPLocation (ReportType type) {
        String actualMessage, expectedMessage;
        String fileName;

        Debug debugPage = new Debug ();
        debugPage.waitFor (KitClonoSEQReport, Processing);
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

        assertEquals (actualMessage, expectedMessage);
    }

}
