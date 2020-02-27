package com.adaptivebiotech.test.cora.e2e;

import static com.adaptivebiotech.test.cora.CoraEnvironment.incomingPath;
import static com.adaptivebiotech.test.cora.CoraEnvironment.retryTimes;
import static com.adaptivebiotech.test.cora.CoraEnvironment.sftpServerHostName;
import static com.adaptivebiotech.test.cora.CoraEnvironment.sftpServerPassword;
import static com.adaptivebiotech.test.cora.CoraEnvironment.sftpServerUserName;
import static com.adaptivebiotech.test.cora.CoraEnvironment.waitTime;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.ui.order.OrderList;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.workflow.Debug;
import com.adaptivebiotech.cora.utils.SftpServerHelper;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.ReportType;
import com.adaptivebiotech.ui.cora.workflow.History;;

@Test (groups = { "E2E" })
public class KitE2ETestSuite extends KitE2ETestBase {

    // SR-T1772
    public void VerifyCEKitClonoSEQ () {

        // pre-condition to create clonality and tracking orders.

        Logging.info ("Pre-condition: Create Clonality and Tracking Order.");
        preCondition (SR_T1772ClonalityJFilePath, SR_T1772TrackingJFilePath);

        // Step1
        // Verify Clonality order name is "B-cell 2.0 Kit Clonality(IVD)" .
        // The tracking order tests have the test name of "B-cell 2.0 Kit Tracking (IVD)"
        OrderList orderlist = new OrderList ();
        OrderStatus status = new OrderStatus ();

        orderlist.searchAndClickOrder (clonalityOrder.orderName);
        Logging.info ("verify the clonality order tests have the test name of B-cell 2.0 Kit Clonality (IVD)");
        assertEquals ("B-cell 2.0 Kit Clonality (IVD)", status.getTestName ());
        testLog ("The clonality order tests have the test name of B-cell 2.0 Kit Clonality (IVD) ");

        orderlist.searchAndClickOrder (trackingOrder.orderName);

        Logging.info ("verify the tracking order tests have the test name of B-cell 2.0 Kit Tracking (IVD)");
        assertEquals ("B-cell 2.0 Kit Tracking (IVD)", status.getTestName ());
        testLog ("The tracking order tests have the test name of B-cell 2.0 Kit Tracking (IVD) ");

        // Step 2
        // Verify dropdown list contains KitClonoSEQRport and KitReportDelivery options
        orderlist.goToOrderTests ();
        Logging.info ("Verify the drop down list contains KitClonoSEQRport and KitReportDelivery options");
        verifyWorkflowStageDropdowncontainsSEQReportAndDelivery ();
        testLog ("The drop down list contains KitClonoSEQRport and KitReportDelivery options");

        // Step 3
        // Verify Workflow Stage column contains KitClonaSEQReport and KitReportDelivery Stage

        Logging.info ("Verify Workflow Stage column contains KitClonaSEQReport and KitReportDelivery Stage");
        orderlist.searchAndClickOrder (clonalityOrder.orderName);
        assertTrue (status.kitClonoSEQReportStageDisplayed ());
        assertTrue (status.kitReportDeliveryStageDisplayed ());
        testLog ("Workflow Stage column contains KitClonaSEQReport and KitReportDelivery Stage");

        // Step 4
        // Verify reportData.json ClinicalReport-A120x.pdf displays in debug page for Clonality
        // order
        // Verify sftp location displayed in status of history
        History orderHistory = new History ();
        orderHistory.gotoOrderDebug (/* "TSBUNIQUE1-MRD12020244151440" */ clonalityOrder.sampleName);
        Debug debugPage = new Debug ();

        Logging.info ("Verify reportData.json exists in the Files associated with workflow");
        debugPage.verifyReportDatajsonFile (retryTimes, waitTime);
        testLog ("reportData.json exists in the Files associated with workflow");

        Logging.info ("Verify ClinicalReport-A120X.pdf");
        debugPage.verifyClinicalReportPdfFile (retryTimes, waitTime);
        testLog ("ClinicalReport-A120X.pdf is displayed in debug page for Clonality order");

        clonalityOrder.reportFileName = debugPage.getClinicalReportFileName (retryTimes, waitTime);
        clonalityOrder.reportNum = clonalityOrder.reportFileName.substring (15,
                                                                            clonalityOrder.reportFileName.indexOf ('.'));
        clonalityOrder.pipelineVersion = debugPage.getPropertiesItem ("secAnlsPipeVer",
                                                                      retryTimes,
                                                                      waitTime);
        Logging.info ("Verify SFTP pdf location is displayed in status history table");
        verifySFTPLocation (ReportType.clonality);
        testLog ("SFTP pdf location is displayed in status history table");

        // get Tracking order data
        orderHistory.gotoOrderDebug (trackingOrder.sampleName);
        trackingOrder.reportFileName = debugPage.getClinicalReportFileName (retryTimes, waitTime);
        trackingOrder.reportNum = trackingOrder.reportFileName.substring (15,
                                                                          trackingOrder.reportFileName.indexOf ('.'));
        trackingOrder.pipelineVersion = debugPage.getPropertiesItem ("secAnlsPipeVer", retryTimes, waitTime);

        // Step 5
        // Verify sftp server file directory structure
        // 1. Verify Clicnical Reports folder is under INCOMING_PATH directory
        // 2. Within the "Clinical Reports" folder, there is a folder with the name
        // ID-ORDER-NUMBER_ID-ORDER-DATE where ID-ORDER-DATE is in the format of YYYYMMDD
        // 3. Within the folder, there is one pdf file. Pdf file shall have the naming convention
        // A119X.pdf where X is randomly generated number between 25-999999.
        // "A1" is the prefix for the tech transfer target for "Testing Research" project

        SftpServerHelper helper = new SftpServerHelper (sftpServerUserName, sftpServerPassword,
                sftpServerHostName);
        helper.startSftpChannel ();

        Logging.info ("Verify Clicnical Reports folder is under INCOMING_PATH directory");
        helper.verifyDirOrFileInSftpServer (incomingPath + "/Clinical Reports/", retryTimes, waitTime);
        testLog ("Clicnical Reports folder is under INCOMING_PATH directory");

        String dir = incomingPath + "/Clinical Reports/" + clonalityOrder.orderNum + "_" + clonalityOrder.orderDate;

        Logging.info ("Verify  Within the \"Clinical Reports\" folder, there is a folder with the name ID-ORDER-NUMBER_ID-ORDER-DATE where ID-ORDER-DATE is in the format of YYYYMMDD");
        helper.verifyDirOrFileInSftpServer (dir, retryTimes, waitTime);
        testLog (" Within the \"Clinical Reports\" folder, there is a folder with the name ID-ORDER-NUMBER_ID-ORDER-DATE where ID-ORDER-DATE is in the format of YYYYMMDD");

        Logging.info ("Verify Within the folder, there is one pdf file. Pdf file shall have the naming convention A120X.pdf where X is randomly generated number between 25-999999." + " \"A1\" is the prefix for the tech transfer target for \"Testing Research\" project");
        helper.verifyDirOrFileInSftpServer (dir + "/" + clonalityOrder.reportFileName.substring (15),
                                            retryTimes,
                                            waitTime);
        testLog ("Within the folder, there is one pdf file. Pdf file has the naming convention A120X.pdf where X is randomly generated number between 25-999999." + " \"A1\" is the prefix for the tech transfer target for \"Testing Research\" project");

        // Step 6
        // Verify Report_Tracking.tsv contains data of Clonality and Tracking order created in
        // pre-condiiton
        // Verify Clonality and Tracking order detail information is recorded in
        // Report_Tracking.tsv

        String path = String.format ("%s/Clinical Reports/Report_Tracking.tsv", incomingPath);
        Logging.info ("Verify Clonality order data is recorded in Report_Tracking.tsv");
        helper.verifyCorrectDataInReportTrackingTsv (path,
                                                     clonalityOrder,
                                                     "Active",
                                                     retryTimes,
                                                     waitTime);
        testLog ("Clonality order data was recorded in Report_Tracking.tsv");

        Logging.info ("Verify Tracking order data is recorded in Report_Tracking.tsv");
        helper.verifyCorrectDataInReportTrackingTsv (path,
                                                     trackingOrder,
                                                     "Active",
                                                     retryTimes,
                                                     waitTime);
        testLog ("Tracking order data was recorded in Report_Tracking.tsv");

        // Step 7
        // Verify correct data is displayed in pdf file in Clonality order
        Logging.info ("Verify Clonality order data is displaye din pdf report file correctly");
        String filePath = String.format ("%s/Clinical Reports/%s_%s/%s",
                                         incomingPath,
                                         clonalityOrder.orderNum,
                                         clonalityOrder.orderDate,
                                         clonalityOrder.reportFileName.substring (15));
        helper.verifyCorrectDataInReportTrackingPDF (filePath,
                                                     ReportType.clonality,
                                                     clonalityOrder);
        testLog ("Clonality order data is displayed in pdf report file correctly");

        // Step 8
        // Verify correct data is displayed in pdf file in Tracking order
        Logging.info ("Verify Tracking order data is displaye din pdf report file correctly");
        filePath = String.format ("%s/Clinical Reports/%s_%s/%s",
                                  incomingPath,
                                  trackingOrder.orderNum,
                                  trackingOrder.orderDate,
                                  trackingOrder.reportFileName.substring (15));
        helper.verifyCorrectDataInReportTrackingPDF (filePath,
                                                     ReportType.tracking,
                                                     trackingOrder);
        testLog ("Tracking order data is displayed in pdf report file correctly");

        helper.disconnectsftpChannel ();

    }

}
