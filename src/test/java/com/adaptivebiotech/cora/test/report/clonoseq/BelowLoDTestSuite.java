/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt6;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.join;
import static java.time.LocalDateTime.parse;
import static org.testng.Assert.assertEquals;
import java.time.LocalDateTime;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.verify.ClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class BelowLoDTestSuite extends ReportTestBase {

    private final String   downloadDir = artifacts (this.getClass ().getName ());
    private final String   analysisID  = getSystemResource ("SecondaryAnalysis/below-lod.id.json").getPath ();
    private final String   analysisMRD = getSystemResource ("SecondaryAnalysis/below-lod.mrd.json").getPath ();
    private Login          login       = new Login ();
    private OrcaHistory    history     = new OrcaHistory ();
    private ReportClonoSeq report      = new ReportClonoSeq ();

    /**
     * @sdlc.requirements SR-5046
     */
    @Test (groups = "sinnoh")
    public void verify_clia_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (SecondaryAnalysis, Ready),
                                               genCDxTest (ID_BCell2_CLIA, azTsvPath + "/below-lod.id.tsv.gz"),
                                               genCDxTest (MRD_BCell2_CLIA, azTsvPath + "/below-lod.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_CLIA);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, analysisID);
        testLog ("the secondaryAnalysisResult.json for below LOD for clonality matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (ID_BCell2_CLIA);
        report.releaseReport (ID_BCell2_CLIA, Pass);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, BCell);
        clonoseq.helper.isCLIA = true;
        clonoseq.helper.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = releaseDt.toLocalDate ();
        clonoseq.appendix.sampleInfo = "0.87 65,603 IGH 81,603 1,976 IGK 132,814 3,187 IGL 812 565";
        clonoseq.helper.report.commentInfo.signedAt = releaseDt;
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, analysisMRD);
        testLog ("the secondaryAnalysisResult.json for below LOD for tracking matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (MRD_BCell2_CLIA);
        report.releaseReport (MRD_BCell2_CLIA, Pass);
        releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        clonoseq = basicClonoSeq (patient, diagnostic, orderTest, BCell);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = 5;
        clonoseq.header.reportDt = releaseDt.toLocalDate ();
        clonoseq.appendix.sampleInfo = "0.04 322,875 IGH ≥7,550 7,550 IGK 8,328 5,829 IGL 1,968 1,503";
        clonoseq.appendix.sequenceInfo = "IGH - Sequence A 6 7 IGK - Sequence B 589 740 IGK - Sequence C 589 740";
        clonoseq.helper.report.commentInfo.signedAt = releaseDt;
        actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }

    /**
     * @sdlc.requirements SR-5046
     */
    @Test (groups = "sinnoh")
    public void verify_ivd_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (SecondaryAnalysis, Ready),
                                               genCDxTest (ID_BCell2_IVD, azTsvPath + "/below-lod.id.tsv.gz"),
                                               genCDxTest (MRD_BCell2_IVD, azTsvPath + "/below-lod.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (ID_BCell2_IVD);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, analysisID);
        testLog ("the secondaryAnalysisResult.json for below LOD for clonality matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (ID_BCell2_IVD);
        report.releaseReport (ID_BCell2_IVD, Pass);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, BCell);
        clonoseq.helper.isIVD = true;
        clonoseq.helper.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = releaseDt.toLocalDate ();
        clonoseq.appendix.sampleInfo = "0.87 65,603 IGH 81,603 1,976 IGK 132,814 3,187 IGL 812 565";
        clonoseq.helper.report.commentInfo.signedAt = releaseDt;
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_BCell2_IVD);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, analysisMRD);
        testLog ("the secondaryAnalysisResult.json for below LOD for tracking matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (MRD_BCell2_IVD);
        report.releaseReport (MRD_BCell2_IVD, Pass);
        releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        clonoseq = basicClonoSeq (patient, diagnostic, orderTest, BCell);
        clonoseq.helper.isIVD = true;
        clonoseq.pageSize = 5;
        clonoseq.header.reportDt = releaseDt.toLocalDate ();
        clonoseq.appendix.sampleInfo = "0.04 322,875 IGH ≥7,550 7,550 IGK 8,328 5,829 IGL 1,968 1,503";
        clonoseq.appendix.sequenceInfo = "IGH - Sequence A 6 7 IGK - Sequence B 589 740 IGK - Sequence C 589 740";
        clonoseq.helper.report.commentInfo.signedAt = releaseDt;
        actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }
}
