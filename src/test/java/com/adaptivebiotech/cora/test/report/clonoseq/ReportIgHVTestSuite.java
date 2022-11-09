/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.EDTA;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.PdfUtil.getPageCount;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;
import static com.adaptivebiotech.cora.utils.TestHelper.newInternalPharmaPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.MUTATED;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.NO_CLONES;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.IGH;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.PBMC;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellPellet;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.FreshBoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.FINISHED;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSave;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.disableHiFreqSharing;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastFinishedPipelineJobId;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.sampleName;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.Boolean.TRUE;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.ReportRender.ShmReportResult;
import com.adaptivebiotech.picasso.dto.ReportRender.ShmSequence;
import com.adaptivebiotech.picasso.dto.verify.ClonoSeq;
import com.adaptivebiotech.pipeline.api.PipelineApi;

/**
 * Note:
 * - instead of lastFinishedPipelineJobId, we can use: shmDataSourcePath (SR-T3728 - 20) and this
 * will force a shm analysis job
 * - for example,
 * shmDataSourcePath=https://adaptiveruopipeline.blob.core.windows.net/pipeline-results/180122_NB501661_0323_AH3KF2BGX5/v3.0/20180124_1229
 * 
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportIgHVTestSuite extends ReportTestBase {

    private final String        downloadDir         = artifacts (this.getClass ().getName ());
    private final String        reportData          = "reportData.json";
    private final Patient       patient             = scenarioBuilderPatient ();
    private Login               login               = new Login ();
    private NewOrderClonoSeq    diagnostic          = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq orderDetailClonoSeq = new OrderDetailClonoSeq ();
    private OrderStatus         orderStatus         = new OrderStatus ();
    private OrcaHistory         history             = new OrcaHistory ();
    private ReportClonoSeq      orderReport         = new ReportClonoSeq ();
    private PipelineApi         pipelineApi         = new PipelineApi ();

    /**
     * @sdlc.requirements SR-7029, SR-7028
     */
    @Test (groups = "nutmeg")
    public void verify_clonality_report () {
        CoraTest test = genCDxTest (ID_BCell2_CLIA, azTsvPath + "/above-loq.id.tsv.gz");
        test.workflowProperties.tsvOverridePath = null;
        test.workflowProperties.ighvAnalysisEnabled = true;
        test.workflowProperties.ighvReportEnabled = true;
        test.workflowProperties.lastFinishedPipelineJobId = "8a7a94db77a26ee10179d44fe3f0410c";
        test.workflowProperties.lastAcceptedTsvPath = azPipelineClia + "/210603_NB552480_0036_AH2C2LBGXJ/v3.1/20210605_1334/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H2C2LBGXJ_0_CLINICAL-CLINICAL_105508-01MC-3902649.adap.txt.results.tsv.gz";
        test.workflowProperties.sampleName = "105508-01MC-3902649";

        Diagnostic diagnostic = buildCdxOrder (patient, stage (SecondaryAnalysis, Ready), test);
        diagnostic.specimen.sampleType = FreshBoneMarrow;
        diagnostic.specimen.properties.SourceType = BoneMarrow;
        diagnostic.specimen.properties.Anticoagulant = EDTA;
        diagnostic.order.properties.Icd10Codes = "C91.10"; // CLL
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished, FINISHED);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        orderReport.clickReportTab (ID_BCell2_CLIA);
        assertTrue (orderReport.isCLIAIGHVBtnVisible ());
        testLog ("CLIA-IGHV flag appeared just below the Report tab");

        orderReport.releaseReport (ID_BCell2_CLIA, Pass);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        coraApi.get (orderReport.getReportUrl (), actualPdf);

        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        ReportRender report = parseReportData (reportDataJson);
        assertEquals (report.patientInfo.reportSpecimenType, FreshBoneMarrow);
        assertEquals (report.patientInfo.reportSpecimenSource, BoneMarrow);
        report.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, FreshBoneMarrow);
            assertEquals (s.specimenSource, BoneMarrow);
        });
        testLog ("found specimenSource in specimenInfo objects in " + reportData);

        ShmReportResult result = report.shmReportResult;
        assertNotNull (result);
        testLog ("found a ReportRenderDto object with a new field called shmReportResult containing n ShmReportResult Object");

        assertEquals (result.mutationStatus, MUTATED);
        assertEquals (result.shmSequenceList.size (), 1);
        for (ShmSequence s : result.shmSequenceList) {
            assertEquals (s.locus, IGH);
            assertEquals (s.sequence,
                          "CAGAGACAATTCCAAGAACACTCTGTATTTGCAAATGAACAACCTGCGAACTGAGGACACGGCTATGTACTACTGTGCGAAAACGAATCGATTACTTTGGTTCGGGGAACCATCCCCCTGGGGCCAGGGAACC");
            assertEquals (s.percentMutation, 9.9415205419d, 0);
            assertTrue (s.productive);
            assertEquals (s.vSegment, "IGHV3-30*18");
        }
        testLog (join ("\n\t",
                       "found shmReportResult object with following fields:",
                       "mutationStatus (enum)",
                       "shmSequenceList (List<ShmSequence>)"));

        ClonoSeq clonoseq = basicClonoSeq (report, patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        prepPipelineApi (true);
        pipelineApi.addBasicAuth ();
        assertEquals (pipelineApi.findFlowcellRuns (diagnostic.order.tests.get (0).workflowProperties.sampleName).length,
                      1);
        testLog ("found 1 eos.shm analysis job spawned and completed in portal");
    }

    /**
     * Note:
     * - SR-T3689
     * - non-CLEP physician
     * - specimen type / source: Cell pellet / PBMC
     * - icd codes: C83.00
     */
    @Test (groups = "nutmeg")
    public void verifyIgHVStageAndReportFeatureOrder1CLIA () {
        Assay assayTest = ID_BCell2_CLIA;

        Specimen specimen = new Specimen ();
        specimen.sampleType = CellPellet;
        specimen.sampleSource = PBMC;
        specimen.anticoagulant = Blood.equals (specimen.sampleType) ? EDTA : null;
        specimen.collectionDate = genLocalDate (-3);

        login.doLogin ();
        Order order = diagnostic.createClonoSeqOrder (coraApi.getPhysician (non_CLEP_clonoseq),
                                                      newInternalPharmaPatient (),
                                                      new String[] { "C83.00", "C91.10" },
                                                      assayTest,
                                                      specimen,
                                                      Active,
                                                      Tube);
        info ("Order Number: " + order.orderNumber + ", Order Notes: Order 1 Flag On");

        String sample = orderDetailClonoSeq.getSampleName (assayTest);
        String sampleOverride = "96343-05BC";
        String reportDataJson = join ("/", downloadDir, sample, reportData);
        orderStatus.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        orderStatus.expandWorkflowHistory ();

        history.gotoOrderDebug (sample);
        WorkflowProperties workflowProperties = history.getWorkflowProperties ();

        assertTrue (workflowProperties.ighvReportEnabled, "Validate ighvReportEnabled property");
        assertTrue (workflowProperties.ighvAnalysisEnabled, "Validate ighvAnalysisEnabled property");
        testLog ("step 1 - ighvAnalysisEnabled and ighvReportEnabled are true");

        history.setWorkflowProperty (lastAcceptedTsvPath,
                                     azPipelineClia + "/210612_NB552467_0088_AH3CH7BGXJ/v3.1/20210614_0809/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3CH7BGXJ_0_CLINICAL-CLINICAL_96343-05BC.adap.txt.results.tsv.gz");
        history.setWorkflowProperty (lastFinishedPipelineJobId, "8a7a94db77a26ee1017a01c874c67394");
        history.setWorkflowProperty (sampleName, sampleOverride);
        history.setWorkflowProperty (disableHiFreqSave, TRUE.toString ());
        history.setWorkflowProperty (disableHiFreqSharing, TRUE.toString ());
        history.forceStatusUpdate (SecondaryAnalysis, Ready);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        testLog ("step 2 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        prepPipelineApi (true);
        pipelineApi.addBasicAuth ();
        assertEquals (pipelineApi.findFlowcellRuns (sampleOverride).length,
                      1,
                      "Validate pipeline portal job is completed");
        testLog ("step 2 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        history.clickOrderTest ();

        // navigate to order status page
        orderStatus.isCorrectPage ();
        orderStatus.clickReportTab (assayTest);
        orderReport.isCorrectPage ();
        assertTrue (orderReport.isCLIAIGHVBtnVisible ());
        testLog ("step 3 - CLIA-IGHV flag appears just below the Report tab ");
        orderReport.releaseReport (assayTest, Pass);

        history.gotoOrderDebug (sample);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);

        ReportRender report = parseReportData (reportDataJson);
        assertEquals (report.patientInfo.reportSpecimenType, CellPellet);
        assertEquals (report.patientInfo.reportSpecimenSource, PBMC);
        report.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, CellPellet);
            assertEquals (s.specimenSource, PBMC);
        });
        testLog ("found specimenSource in specimenInfo objects in " + reportData);

        ShmReportResult result = report.shmReportResult;
        assertNotNull (result);
        testLog ("found a ReportRenderDto object with a new field called shmReportResult containing n ShmReportResult Object");

        assertEquals (result.mutationStatus, NO_CLONES);
        assertEquals (result.shmSequenceList.size (), 0);
        testLog ("step 4 - SHM analysis results are included in reportData.json within shmReportResult property");
    }
}
