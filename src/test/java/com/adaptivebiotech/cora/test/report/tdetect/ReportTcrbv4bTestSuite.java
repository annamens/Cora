/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.tdetect;

import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.LYME_DX;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildTdetectOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
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

    private final String  reportData = "reportData.json";
    private Login         login      = new Login ();
    private OrcaHistory   history    = new OrcaHistory ();
    private ReportTDetect report     = new ReportTDetect ();
    private String        downloadDir;

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
    }

    /**
     * @sdlc.requirements SR-5671
     */
    @Test (groups = "tatsumiya")
    public void verify_covid_report () {
        CoraTest test = coraApi.getTDxTest (COVID19_DX_IVD);
        test.tsvPath = azPipelineFda + "/210205_NB501448_0761_AH752HBGXH/v3.1/20210207_0918/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev2/H752HBGXH_0_CLINICAL-CLINICAL_95352-SN-2230.adap.txt.results.tsv.gz";
        test.workflowProperties = sample_95352_SN_2230 ();

        ClassifierOutput covid = negativeCovidResult ();
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildTdetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                                   patient,
                                                   stage (DxReport, Ready),
                                                   test,
                                                   COVID19_DX_IVD);
        diagnostic.dxResults = covid;
        assertEquals (coraApi.newTdetectOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (DxReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (COVID19_DX_IVD);
        assertEquals (report.parseFlags ().get (0).name, "COVID_UPPER_UPR_THRESHOLD");
        testLog ("Flags section contained COVID_UPPER_UPR_THRESHOLD");

        report.releaseReport (COVID19_DX_IVD, Pass);
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
        assertEquals (report.dxResult.disease, covid.disease);
        assertEquals (report.dxResult.dxStatus, covid.dxStatus);
        assertEquals (report.dxResult.dxScore, covid.dxScore);
        assertEquals (report.dxResult.containerVersion, covid.containerVersion);
        assertEquals (report.dxResult.classifierVersion, covid.classifierVersion);
        assertEquals (report.dxResult.pipelineVersion, covid.pipelineVersion);
        assertEquals (report.dxResult.configVersion, covid.configVersion);
        assertEquals (report.dxResult.qcFlags, covid.qcFlags);
        assertEquals (report.dxResult.posteriorProbability, covid.posteriorProbability);
        assertEquals (report.dxResult.countEnhancedSeq, covid.countEnhancedSeq);
        assertEquals (report.dxResult.uniqueProductiveTemplates, covid.uniqueProductiveTemplates);
        testLog ("found the corrct DTO structure in the " + reportData);
    }

    /**
     * @sdlc.requirements SR-7537
     */
    @Test (groups = "akita")
    public void verify_lyme_report () {
        CoraTest test = coraApi.getTDxTest (LYME_DX);
        test.tsvPath = azPipelineNorth + "/200613_NB551725_0151_AHM7N7BGXF/v3.1/20200615_1438/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev1/HM7N7BGXF_0_Hospital12deOctubre-MartinezLopez_860011348.adap.txt.results.tsv.gz";;
        test.workflowProperties = sample_860011348 ();

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
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

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

    private WorkflowProperties sample_95352_SN_2230 () {
        WorkflowProperties wProperties = new WorkflowProperties ();
        wProperties.flowcell = "H752HBGXH";
        wProperties.workspaceName = "CLINICAL-CLINICAL";
        wProperties.sampleName = "95352-SN-2230";
        return wProperties;
    }

    private WorkflowProperties sample_860011348 () {
        WorkflowProperties wProperties = new WorkflowProperties ();
        wProperties.flowcell = "HM7N7BGXF";
        wProperties.workspaceName = "Hospital12deOctubre-MartinezLopez";
        wProperties.sampleName = "860011348";
        return wProperties;
    }
}
