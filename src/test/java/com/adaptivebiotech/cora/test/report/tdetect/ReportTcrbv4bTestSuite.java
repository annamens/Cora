/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.tdetect;

import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.LYME_DX;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.sample_112770_SN_7929;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildTdetectOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxContamination;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_CONSULTANT;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.AutoPassedClinicalQC;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.AutoReleasedReport;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import java.lang.reflect.Method;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportTDetect;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.pipeline.dto.dx.ClassifierOutput;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportTcrbv4bTestSuite extends ReportTestBase {

    private final String         reportData  = "reportData.json";
    private Login                login       = new Login ();
    private OrcaHistory          history     = new OrcaHistory ();
    private ReportTDetect        report      = new ReportTDetect ();
    private ThreadLocal <String> downloadDir = new ThreadLocal <> ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
    }

    /**
     * Note:
     * - for Auto QC to work, you have to complete NorthQC stage
     * 
     * @sdlc.requirements SR-5671, SR-6487, SR-6488
     */
    @Test (groups = { "tatsumiya", "fox-terrier" })
    public void verify_covid_report () {
        CoraTest test = getTDxTest (COVID19_DX_IVD);
        test.workflowProperties.lastAcceptedTsvPath = azE2EPath + "/HCYJNBGXJ_0_CLINICAL-CLINICAL_112770-SN-7929.adap.txt.results.tsv.gz";
        test.workflowProperties.sampleName = "112770-SN-7929";
        test.workflowProperties.workspaceName = "CLINICAL-CLINICAL";
        test.workflowProperties.lastFlowcellId = "HCYJNBGXJ";
        test.workflowProperties.lastFinishedPipelineJobId = "8a7a958877a26e74017a213f79fe6d45";

        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildTdetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                                   patient,
                                                   stage (NorthQC, Ready),
                                                   test,
                                                   COVID19_DX_IVD);
        assertEquals (coraApi.newTdetectOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        String reportDataJson = join ("/", downloadDir.get (), orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (NorthQC, Finished);
        history.waitFor (DxAnalysis, Finished);
        history.waitFor (DxContamination, Finished);
        history.waitFor (DxReport, Awaiting, CLINICAL_CONSULTANT);

        String log = "the workflow showed a substatus code of '%s/%s/%s' and message of '%s'";
        assertEquals (history.parseStatusHistory ().stream ()
                             .filter (s -> DxReport.equals (s.stageName))
                             .filter (s -> Awaiting.equals (s.stageStatus))
                             .filter (s -> CLINICAL_QC.equals (s.stageSubstatus))
                             .filter (s -> "Pass: AutoPass".equals (s.subStatusMessage)).count (),
                      1);
        testLog (format (log, DxReport, Awaiting, CLINICAL_QC, "Pass: AutoPass"));

        history.waitFor (DxReport, Finished);
        Map <String, String> properties = history.getWorkflowProperties ();
        assertEquals (properties.get (AutoPassedClinicalQC.name ()), "true");
        testLog (format ("workflow property: '%s' is set and has value: 'true'", AutoPassedClinicalQC));

        assertEquals (properties.get (AutoReleasedReport.name ()), "true");
        testLog (format ("workflow property: '%s' is set and has value: 'true'", AutoReleasedReport));

        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        ReportRender report = parseReportData (reportDataJson);
        assertNotNull (report.patientInfo);
        assertNotNull (report.commentInfo);
        assertFalse (report.isFailure);
        assertNull (report.previousReportDate);
        assertNull (report.isRetest);
        assertNull (report.specimenInfo);
        assertNull (report.dataEncoded);
        assertNull (report.data);
        assertEquals (report.dxResult, sample_112770_SN_7929 ());
        testLog ("found the corrct DTO structure in the " + reportData);
    }

    /**
     * @sdlc.requirements SR-7537
     */
    @Test (groups = "akita")
    public void verify_lyme_report () {
        CoraTest test = getTDxTest (LYME_DX);
        test.tsvPath = azPipelineClia + "/200613_NB551725_0151_AHM7N7BGXF/v3.1/20200615_1438/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HM7N7BGXF_0_Hospital12deOctubre-MartinezLopez_860011348.adap.txt.results.tsv.gz";
        test.workflowProperties.flowcell = "HM7N7BGXF";
        test.workflowProperties.workspaceName = "Hospital12deOctubre-MartinezLopez";
        test.workflowProperties.sampleName = "860011348";

        ClassifierOutput lyme = positiveLymeResult ();
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildTdetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                                   patient,
                                                   stage (DxReport, Ready),
                                                   test,
                                                   LYME_DX);
        diagnostic.dxResults = lyme;
        assertEquals (coraApi.newTdetectOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (LYME_DX);
        String reportDataJson = join ("/", downloadDir.get (), orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (DxReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (LYME_DX);
        report.releaseReport (LYME_DX, Pass);
        history.gotoOrderDebug (orderTest.sampleName);

        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        ReportRender report = parseReportData (reportDataJson);
        assertNotNull (report.patientInfo);
        assertNotNull (report.commentInfo);
        assertFalse (report.isFailure);
        assertNull (report.previousReportDate);
        assertNull (report.isRetest);
        assertNull (report.specimenInfo);
        assertNull (report.dataEncoded);
        assertNull (report.data);
        assertEquals (report.dxResult.disease, lyme.disease);
        assertEquals (report.dxResult.dxStatus, lyme.dxStatus);
        assertEquals (report.dxResult.dxScore, lyme.dxScore);
        assertEquals (report.dxResult.containerVersion, lyme.containerVersion);
        assertEquals (report.dxResult.classifierVersion, lyme.classifierVersion);
        assertEquals (report.dxResult.pipelineVersion, lyme.pipelineVersion);
        assertEquals (report.dxResult.configVersion, lyme.configVersion);
        assertEquals (report.dxResult.qcFlags, lyme.qcFlags);
        assertEquals (report.dxResult.countEnhancedSeq, lyme.countEnhancedSeq);
        assertEquals (report.dxResult.uniqueProductiveTemplates, lyme.uniqueProductiveTemplates);
        testLog ("found the corrct DTO structure in the " + reportData);
    }
}
