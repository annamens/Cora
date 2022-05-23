/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.hl7.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_TCRB;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_TCRB;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.utils.PageHelper.CorrectionType.Amended;
import static com.adaptivebiotech.cora.utils.PageHelper.CorrectionType.Updated;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildDiagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.Analyzer;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.CalculateSampleSummary;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.SENDING_REPORT_NOTIFICATION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.hl7.HL7TestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.ui.task.TaskDetail;
import com.adaptivebiotech.cora.ui.task.TaskStatus;

/**
 * Note:
 * - we only have 1 pipeline node for secondary analysis, set this for single threaded test run
 * 
 * @author Olha Tereshchuk
 *         <a href="mailto:otereshchuk@adaptivebiotech.com">otereshchuk@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "akita" }, singleThreaded = true)
public class GatewayNotificationTestSuite extends HL7TestBase {

    private final String        bcellIdTsv     = azTsvPath + "/above-loq.id.tsv.gz";
    private final String        bcellMrdTsv    = azTsvPath + "/above-loq.mrd.tsv.gz";
    private final String        tcellTsv       = azTsvPath + "/HKJVGBGXC_0_CLINICAL-CLINICAL_68353-01MB.adap.txt.results.tsv.gz";
    private final String        lastFlowcellId = "HKJVGBGXC";
    private final String        gatewayJson    = "gatewayMessage.json";
    private Login               login          = new Login ();
    private OrdersList          ordersList     = new OrdersList ();
    private ReportClonoSeq      report         = new ReportClonoSeq ();
    private TaskStatus          taskStatus     = new TaskStatus ();
    private TaskDetail          taskDetail     = new TaskDetail ();
    private OrderStatus         orderStatus    = new OrderStatus ();
    private OrderDetailClonoSeq orderDetail    = new OrderDetailClonoSeq ();
    private OrcaHistory         history        = new OrcaHistory ();
    private Physician           physician;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        physician = coraApi.getPhysician (clonoSEQ_client);
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc.requirements SR-7287, SR-7369
     */
    public void verifyClonoSeqBcellGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildDiagnosticOrder (physician,
                                                      patient,
                                                      stage (SecondaryAnalysis, Ready),
                                                      genCDxTest (ID_BCell2_CLIA, bcellIdTsv));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new BCell ID order");

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, SecondaryAnalysis, Finished);
        orderStatus.waitFor (orderTest.sampleName, ShmAnalysis, Finished);
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (ID_BCell2_CLIA);
        report.isCorrectPage ();
        report.releaseReport (ID_BCell2_CLIA, Pass);
        testLog ("released ID report");

        report.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.gotoOrderDebug (orderTest.sampleName);
        assertTrue (history.waitForFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.clickOrder ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Finished);
        orderStatus.clickReportTab (ID_BCell2_CLIA);
        report.isCorrectPage ();
        report.clickCorrectReport ();
        report.selectCorrectionType (Updated);
        report.enterReasonForCorrection ("Testing gateway notifications for BCell ID order");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        taskStatus.gotoTaskStatus (report.getCorrectedReportTaskId ());
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        taskDetail.isCorrectPage ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with corrected report sent");

        taskDetail.clickTaskStatus ();
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Finished);

        diagnostic = buildDiagnosticOrder (physician,
                                           patient,
                                           stage (SecondaryAnalysis, Ready),
                                           genCDxTest (MRD_BCell2_CLIA, bcellMrdTsv));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new BCell MRD order");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, SecondaryAnalysis, Finished);
        orderStatus.waitFor (orderTest.sampleName, ShmAnalysis, Finished);
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (MRD_BCell2_CLIA);
        report.isCorrectPage ();
        report.releaseReport (MRD_BCell2_CLIA, Pass);
        testLog ("released MRD report");

        report.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.gotoOrderDebug (orderTest.sampleName);
        assertTrue (history.waitForFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.clickOrder ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Finished);
        orderStatus.clickReportTab (MRD_BCell2_CLIA);
        report.isCorrectPage ();
        report.clickCorrectReport ();
        report.selectCorrectionType (Amended);
        report.enterReasonForCorrection ("Testing gateway notifications for BCell MRD order");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released amended report");

        taskStatus.gotoTaskStatus (report.getCorrectedReportTaskId ());
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        taskDetail.isCorrectPage ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with amended report sent");
    }

    /**
     * @sdlc.requirements SR-7287, SR-7369
     */
    public void verifyClonoSeqTcellGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildDiagnosticOrder (physician,
                                                      patient,
                                                      stage (NorthQC, Ready),
                                                      genTcrTest (ID_TCRB, lastFlowcellId, tcellTsv));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.newTcellOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new TCell ID order");

        OrderTest orderTest = diagnostic.findOrderTest (ID_TCRB);
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, NorthQC, Finished);
        orderStatus.waitFor (orderTest.sampleName, CalculateSampleSummary, Finished);
        orderStatus.waitFor (orderTest.sampleName, Analyzer, Finished);
        orderStatus.waitFor (orderTest.sampleName, SecondaryAnalysis, Finished);
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (ID_TCRB);
        report.isCorrectPage ();
        report.releaseReport (ID_TCRB, Pass);
        testLog ("released TCRB ID report");

        report.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.gotoOrderDebug (orderTest.sampleName);
        assertTrue (history.waitForFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.clickOrder ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Finished);
        orderStatus.clickReportTab (ID_TCRB);
        report.isCorrectPage ();
        report.clickCorrectReport ();
        report.selectCorrectionType (Updated);
        report.enterReasonForCorrection ("Testing gateway notifications for TCRB ID report");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        taskStatus.gotoTaskStatus (report.getCorrectedReportTaskId ());
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with corrected report sent");

        taskDetail.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Finished);

        diagnostic = buildDiagnosticOrder (physician,
                                           patient,
                                           stage (NorthQC, Ready),
                                           genTcrTest (MRD_TCRB, lastFlowcellId, tcellTsv));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.newTcellOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new TCell MRD order");

        orderTest = diagnostic.findOrderTest (MRD_TCRB);
        orderStatus.gotoOrderStatusPage (orderTest.orderId);
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, NorthQC, Finished);
        orderStatus.waitFor (orderTest.sampleName, CalculateSampleSummary, Finished);
        orderStatus.waitFor (orderTest.sampleName, Analyzer, Finished);
        orderStatus.waitFor (orderTest.sampleName, SecondaryAnalysis, Finished);
        orderStatus.waitFor (orderTest.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        orderStatus.gotoOrderDetailsPage (orderTest.orderId);
        orderDetail.isCorrectPage ();
        orderDetail.clickReportTab (MRD_TCRB);
        report.isCorrectPage ();
        report.releaseReport (MRD_TCRB, Pass);
        testLog ("released TCRB MRD report");

        report.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.gotoOrderDebug (orderTest.sampleName);
        assertTrue (history.waitForFilePresent (gatewayJson));
        testLog ("gateway message sent");

        history.clickOrder ();
        orderStatus.isCorrectPage ();
        orderStatus.waitFor (orderTest.sampleName, ReportDelivery, Finished);
        orderStatus.clickReportTab (MRD_TCRB);
        report.isCorrectPage ();
        report.clickCorrectReport ();
        report.selectCorrectionType (Updated);
        report.enterReasonForCorrection ("Testing gateway notifications for TCRB MRD report");
        report.clickSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        taskStatus.gotoTaskStatus (report.getCorrectedReportTaskId ());
        taskStatus.isCorrectPage ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        taskStatus.clickTaskDetail ();
        assertTrue (taskDetail.taskFiles ().containsKey (gatewayJson));
        testLog ("gateway message with corrected report sent");
    }
}
