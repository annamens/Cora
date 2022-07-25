/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.EDTA;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus.MUTATED;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.IGH;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt6;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.FreshBoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.FINISHED;
import static java.lang.String.join;
import static java.time.LocalDateTime.parse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import java.time.LocalDateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
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

    private final String   downloadDir = artifacts (this.getClass ().getName ());
    private final String   reportData  = "reportData.json";
    private final Patient  patient     = scenarioBuilderPatient ();
    private Login          login       = new Login ();
    private OrcaHistory    history     = new OrcaHistory ();
    private ReportClonoSeq orderReport = new ReportClonoSeq ();
    private PipelineApi    pipelineApi = new PipelineApi ();
    private Diagnostic     diagnostic;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();

        CoraTest test = genCDxTest (ID_BCell2_CLIA, azTsvPath + "/above-loq.id.tsv.gz");
        test.workflowProperties.tsvOverridePath = null;
        test.workflowProperties.ighvAnalysisEnabled = true;
        test.workflowProperties.ighvReportEnabled = true;
        test.workflowProperties.lastFinishedPipelineJobId = "8a7a94db77a26ee10179d44fe3f0410c";
        test.workflowProperties.lastAcceptedTsvPath = azPipelineClia + "/210603_NB552480_0036_AH2C2LBGXJ/v3.1/20210605_1334/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H2C2LBGXJ_0_CLINICAL-CLINICAL_105508-01MC-3902649.adap.txt.results.tsv.gz";
        test.workflowProperties.sampleName = "105508-01MC-3902649";

        diagnostic = buildCdxOrder (patient, stage (SecondaryAnalysis, Ready), test);
        diagnostic.specimen.sampleType = FreshBoneMarrow;
        diagnostic.specimen.properties.SourceType = BoneMarrow;
        diagnostic.specimen.properties.Anticoagulant = EDTA;
        diagnostic.order.properties.Icd10Codes = "C91.10"; // CLL
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
    }

    /**
     * @sdlc.requirements SR-7029, SR-7028
     */
    @Test (groups = "nutmeg")
    public void verify_clonality_report () {
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
        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        history.clickOrderTest ();
        orderReport.clickReportTab (ID_BCell2_CLIA);
        LocalDateTime releaseDt = parse (orderReport.getReportReleaseDate () + ".0000", formatDt6);

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
        testLog ("found a ReportRenderDto object with a new field called shmReportResult containing an ShmReportResult Object");

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

        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, BCell);
        clonoseq.helper.isCLIA = true;
        clonoseq.helper.isClonality = true;
        clonoseq.header.reportDt = releaseDt.toLocalDate ();
        clonoseq.pageSize = 5;
        clonoseq.appendix.sampleInfo = "0.41 122,367 IGH 9,963 5,290 IGK 7,978 5,463 IGL ≥1,661 1,661";
        clonoseq.helper.report.commentInfo.signedAt = releaseDt;
        clonoseq.helper.isSHM = true;
        clonoseq.helper.report = report;

        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (orderReport.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        prepPipelineApi (true);
        pipelineApi.addBasicAuth ();
        assertEquals (pipelineApi.findFlowcellRuns (diagnostic.order.tests.get (0).workflowProperties.sampleName).length,
                      1);
        testLog ("found 1 eos.shm analysis job spawned and completed in portal");
    }
}
