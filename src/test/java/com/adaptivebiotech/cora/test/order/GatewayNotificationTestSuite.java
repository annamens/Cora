package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.createPortalJob;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.newCovidOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.newDiagnosticOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_TCRB;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.MRD_TCRB;
import static com.adaptivebiotech.test.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxContamination;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Stuck;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.SENDING_REPORT_NOTIFICATION;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.notifyGateway;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.sampleName;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.workspaceName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.ui.task.TaskDetail;
import com.adaptivebiotech.cora.ui.task.TaskList;
import com.adaptivebiotech.cora.ui.task.TaskStatus;
import com.adaptivebiotech.cora.utils.PageHelper.CorrectionType;

@Test (groups = { "akita", "regression" })
public class GatewayNotificationTestSuite extends OrderTestBase {

    private final String     bcellIdTsv         = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios/above-loq.id.tsv.gz";
    private final String     bcellMrdTsv        = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios/above-loq.mrd.tsv.gz";
    private final String     tcellTsv           = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/scenarios/HKJVGBGXC_0_CLINICAL-CLINICAL_68353-01MB.adap.txt.results.tsv.gz";
    private final String     covidTsv           = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results/200613_NB551725_0151_AHM7N7BGXF/v3.1/20200615_1438/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HM7N7BGXF_0_Hospital12deOctubre-MartinezLopez_860011348.adap.txt.results.tsv.gz";
    private final String     lastFlowcellId     = "HKJVGBGXC";
    private final String     covidWorkspaceName = "Hospital12deOctubre-MartinezLopez";
    private final String     covidSampleName    = "860011348";

    private Diagnostic       diagnostic;
    private Orders.OrderTest orderTest;
    private OrcaHistory      history;
    private ReportClonoSeq   report;
    private TaskList         taskList;
    private TaskStatus       taskStatus;
    private TaskDetail       task;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        doCoraLogin ();
        new Login ().doLogin ();
        history = new OrcaHistory ();
        report = new ReportClonoSeq ();
        taskList = new TaskList ();
        taskStatus = new TaskStatus ();
        task = new TaskDetail ();
    }

    /**
     * @sdlc_requirements SR-7287, SR-7369
     */
    public void verifyClonoSeqBcellGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        diagnostic = buildDiagnosticOrder (patient,
                                           stage (SecondaryAnalysis, Ready),
                                           genCDxTest (ID_BCell2_CLIA, bcellIdTsv));
        assertEquals (newDiagnosticOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new BCell ID order");

        orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        history.gotoOrderDebug (orderTest.sampleName);
        history.setWorkflowProperty (notifyGateway, "true");
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.releaseReport (ID_BCell2_CLIA, Pass);
        testLog ("released ID report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.isFilePresent ("gatewayMessage.json");
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (ID_BCell2_CLIA);
        report.clickCorrectReport ();
        report.selectCorrectionType (CorrectionType.Updated);
        report.enterReasonForCorrection ("Testing gateway notifications");
        report.clickCorrectReportSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        taskList.searchAndClickFirstTask ("ClonoSEQ 2.0 Corrected Report");
        task.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        task.clickTaskDetail ();
        assertTrue (task.taskFiles ().containsKey ("gatewayMessage.json"));
        testLog ("gateway message with corrected report sent");
        task.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Finished);

        diagnostic = buildDiagnosticOrder (patient,
                                           stage (SecondaryAnalysis, Ready),
                                           genCDxTest (MRD_BCell2_CLIA, bcellMrdTsv));
        assertEquals (newDiagnosticOrder (diagnostic).patientId, patient.id);
        testLog ("submitted new BCell MRD order");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        history.gotoOrderDebug (orderTest.sampleName);
        history.setWorkflowProperty (notifyGateway, "true");
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.releaseReport (MRD_BCell2_CLIA, Pass);
        testLog ("released MRD report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.isFilePresent ("gatewayMessage.json");
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (MRD_BCell2_CLIA);
        report.clickCorrectReport ();
        report.selectCorrectionType (CorrectionType.Amended);
        report.enterReasonForCorrection ("Testing gateway notifications");
        report.clickCorrectReportSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released amended report");

        taskList.searchAndClickFirstTask ("ClonoSEQ 2.0 Corrected Report");
        task.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        task.clickTaskDetail ();
        assertTrue (task.taskFiles ().containsKey ("gatewayMessage.json"));
        testLog ("gateway message with amended report sent");
    }

    /**
     * @sdlc_requirements SR-7287, SR-7369
     */
    public void verifyClonoSeqTcellGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        diagnostic = buildDiagnosticOrder (patient,
                                           stage (NorthQC, Ready),
                                           genTcrTest (ID_TCRB, lastFlowcellId, tcellTsv));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (createPortalJob (diagnostic).patientId, patient.id);
        testLog ("submitted new TCell ID order");
        orderTest = diagnostic.findOrderTest (ID_TCRB);
        history.gotoOrderDebug (orderTest.workflowName);
        history.setWorkflowProperty (notifyGateway, "true");
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.releaseReport (ID_TCRB, Pass);
        testLog ("released ID report");

        history.gotoOrderDebug (orderTest.workflowName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.isFilePresent ("gatewayMessage.json");
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (ID_TCRB);
        report.clickCorrectReport ();
        report.selectCorrectionType (CorrectionType.Updated);
        report.enterReasonForCorrection ("Testing gateway notifications");
        report.clickCorrectReportSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        taskList.searchAndClickFirstTask ("ClonoSEQ 2.0 Corrected Report");
        task.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        task.clickTaskDetail ();
        assertTrue (task.taskFiles ().containsKey ("gatewayMessage.json"));
        testLog ("gateway message with corrected report sent");
        task.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Finished);

        diagnostic = buildDiagnosticOrder (patient,
                                           stage (NorthQC, Ready),
                                           genTcrTest (MRD_TCRB, lastFlowcellId, tcellTsv));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (createPortalJob (diagnostic).patientId, patient.id);
        testLog ("submitted new TCell MRD order");
        orderTest = diagnostic.findOrderTest (MRD_TCRB);
        history.gotoOrderDebug (orderTest.workflowName);
        history.setWorkflowProperty (notifyGateway, "true");
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.releaseReport (MRD_TCRB, Pass);
        testLog ("released MRD report");

        history.gotoOrderDebug (orderTest.workflowName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.isFilePresent ("gatewayMessage.json");
        testLog ("gateway message sent");

        history.waitFor (ReportDelivery, Finished);
        history.clickOrderTest ();
        report.clickReportTab (MRD_TCRB);
        report.clickCorrectReport ();
        report.selectCorrectionType (CorrectionType.Updated);
        report.enterReasonForCorrection ("Testing gateway notifications");
        report.clickCorrectReportSaveAndUpdate ();
        report.releaseReportWithSignatureRequired ();
        testLog ("released updated report");

        taskList.searchAndClickFirstTask ("ClonoSEQ 2.0 Corrected Report");
        task.clickTaskStatus ();
        taskStatus.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        task.clickTaskDetail ();
        assertTrue (task.taskFiles ().containsKey ("gatewayMessage.json"));
        testLog ("gateway message with corrected report sent");
    }

    /**
     * @sdlc_requirements SR-7370
     */
    public void verifyCovidGatewayMessageUpdate () {
        Patient patient = scenarioBuilderPatient ();
        AssayResponse.CoraTest test = genTDxTest (COVID19_DX_IVD);
        test.tsvPath = covidTsv;
        diagnostic = buildCovidOrder (patient, stage (DxAnalysis, Ready), test);
        assertEquals (newCovidOrder (diagnostic).patientId, patient.id);
        testLog ("submitted a new Covid19 order in Cora");

        orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        history.gotoOrderDebug (orderTest.sampleName);
        history.setWorkflowProperty (workspaceName, covidWorkspaceName);
        history.setWorkflowProperty (sampleName, covidSampleName);
        history.waitFor (DxContamination, Stuck);
        history.forceStatusUpdate (DxContamination, Finished);
        history.waitFor (DxReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.releaseReport (COVID19_DX_IVD, Pass);
        testLog ("released the Covid report");

        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ReportDelivery, Awaiting, SENDING_REPORT_NOTIFICATION);
        history.isFilePresent ("gatewayMessage.json");
        testLog ("gateway message sent");
    }

}
