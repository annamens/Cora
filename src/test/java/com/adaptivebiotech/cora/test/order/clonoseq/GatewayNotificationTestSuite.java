package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_TCRB;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_TCRB;
import static com.adaptivebiotech.cora.utils.PageHelper.CorrectionType.Amended;
import static com.adaptivebiotech.cora.utils.PageHelper.CorrectionType.Updated;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUrl;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.SENDING_REPORT_NOTIFICATION;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.order.OrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderDetail;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.ui.task.TaskDetail;
import com.adaptivebiotech.cora.ui.task.TaskStatus;

@Test (groups = { "akita", "regression" })
public class GatewayNotificationTestSuite extends OrderTestBase {

    private final String   bcellIdTsv     = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios/above-loq.id.tsv.gz";
    private final String   bcellMrdTsv    = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios/above-loq.mrd.tsv.gz";
    private final String   tcellTsv       = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios/HKJVGBGXC_0_CLINICAL-CLINICAL_68353-01MB.adap.txt.results.tsv.gz";
    private final String   lastFlowcellId = "HKJVGBGXC";
    private final String   gatewayJson    = "gatewayMessage.json";
    private Login          login          = new Login ();
    private OrdersList     ordersList     = new OrdersList ();
    private OrcaHistory    history        = new OrcaHistory ();
    private ReportClonoSeq report         = new ReportClonoSeq ();
    private TaskStatus     taskStatus     = new TaskStatus ();
    private TaskDetail     taskDetail     = new TaskDetail ();
    private OrderStatus    orderStatus    = new OrderStatus ();
    private OrderDetail    orderDetail    = new OrderDetail ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc_requirements SR-7287, SR-7369
     */
    public void verifyClonoSeqBcellGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildDiagnosticOrder (patient,
                                                      stage (SecondaryAnalysis, Ready),
                                                      genCDxTest (ID_BCell2_CLIA, bcellIdTsv));
        assertEquals (coraApi.newDiagnosticOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new BCell ID order");

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        orderDetail.clickReportTab (ID_BCell2_CLIA);
        report.releaseReport (ID_BCell2_CLIA, Pass);
        testLog ("released ID report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        assertTrue (history.isFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (ID_BCell2_CLIA);
        report.clickCorrectReport ();
        report.selectCorrectionType (Updated);
        report.enterReasonForCorrection ("Testing gateway notifications for BCell ID order");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        assertTrue (navigateTo (format ("%s/cora/task/%s?p=status", coraTestUrl, report.getCorrectedReportTaskId ())));
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with corrected report sent");

        taskDetail.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Finished);

        diagnostic = buildDiagnosticOrder (patient,
                                           stage (SecondaryAnalysis, Ready),
                                           genCDxTest (MRD_BCell2_CLIA, bcellMrdTsv));
        assertEquals (coraApi.newDiagnosticOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new BCell MRD order");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        orderStatus.isCorrectPage ();
        orderDetail.clickReportTab (MRD_BCell2_CLIA);
        report.releaseReport (MRD_BCell2_CLIA, Pass);
        testLog ("released MRD report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        assertTrue (history.isFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (MRD_BCell2_CLIA);
        report.clickCorrectReport ();
        report.selectCorrectionType (Amended);
        report.enterReasonForCorrection ("Testing gateway notifications for BCell MRD order");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released amended report");

        assertTrue (navigateTo (format ("%s/cora/task/%s?p=status", coraTestUrl, report.getCorrectedReportTaskId ())));
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with amended report sent");
    }

    /**
     * @sdlc_requirements SR-7287, SR-7369
     */
    public void verifyClonoSeqTcellGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildDiagnosticOrder (patient,
                                                      stage (NorthQC, Ready),
                                                      genTcrTest (ID_TCRB, lastFlowcellId, tcellTsv));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.createPortalJob (diagnostic).patientId, patient.id);
        testLog ("submitted new TCell ID order");

        OrderTest orderTest = diagnostic.findOrderTest (ID_TCRB);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        orderStatus.isCorrectPage ();
        orderDetail.clickReportTab (ID_TCRB);
        report.releaseReport (ID_TCRB, Pass);
        testLog ("released TCRB ID report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        assertTrue (history.isFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (ID_TCRB);
        report.clickCorrectReport ();
        report.selectCorrectionType (Updated);
        report.enterReasonForCorrection ("Testing gateway notifications for TCRB ID report");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        assertTrue (navigateTo (format ("%s/cora/task/%s?p=status", coraTestUrl, report.getCorrectedReportTaskId ())));
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with corrected report sent");

        taskDetail.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Finished);

        diagnostic = buildDiagnosticOrder (patient,
                                           stage (NorthQC, Ready),
                                           genTcrTest (MRD_TCRB, lastFlowcellId, tcellTsv));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.createPortalJob (diagnostic).patientId, patient.id);
        testLog ("submitted new TCell MRD order");

        orderTest = diagnostic.findOrderTest (MRD_TCRB);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        orderStatus.isCorrectPage ();
        orderDetail.clickReportTab (MRD_TCRB);
        report.releaseReport (MRD_TCRB, Pass);
        testLog ("released TCRB MRD report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        assertTrue (history.isFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (MRD_TCRB);
        report.clickCorrectReport ();
        report.selectCorrectionType (Updated);
        report.enterReasonForCorrection ("Testing gateway notifications for TCRB MRD report");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        assertTrue (navigateTo (format ("%s/cora/task/%s?p=status", coraTestUrl, report.getCorrectedReportTaskId ())));
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with corrected report sent");
    }
}
