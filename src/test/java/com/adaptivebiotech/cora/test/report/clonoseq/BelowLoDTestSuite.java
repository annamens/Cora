package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt6;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.join;
import static java.time.LocalDateTime.parse;
import static org.testng.Assert.assertEquals;
import java.time.LocalDateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.report.ClonoSeq;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class BelowLoDTestSuite extends ReportTestBase {

    private final String   downloadDir  = artifacts (this.getClass ().getName ());
    private final Patient  patientClia  = scenarioBuilderPatient ();
    private final Patient  patientIvd   = scenarioBuilderPatient ();
    private final Assay    assayCliaID  = ID_BCell2_CLIA;
    private final Assay    assayCliaMRD = MRD_BCell2_CLIA;
    private final Assay    assayIvdID   = ID_BCell2_IVD;
    private final Assay    assayIvdMRD  = MRD_BCell2_IVD;
    private Login          login        = new Login ();
    private OrcaHistory    history      = new OrcaHistory ();
    private ReportClonoSeq report       = new ReportClonoSeq ();
    private Diagnostic     diagnosticClia, diagnosticIvd;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        diagnosticClia = buildDiagnosticOrder (patientClia,
                                               stage (SecondaryAnalysis, Ready),
                                               genCDxTest (assayCliaID, azTsvPath + "/below-lod.id.tsv.gz"),
                                               genCDxTest (assayCliaMRD, azTsvPath + "/below-lod.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnosticClia).patientId, patientClia.id);

        diagnosticIvd = buildDiagnosticOrder (patientIvd,
                                              stage (SecondaryAnalysis, Ready),
                                              genCDxTest (assayIvdID, azTsvPath + "/below-lod.id.tsv.gz"),
                                              genCDxTest (assayIvdMRD, azTsvPath + "/below-lod.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnosticIvd).patientId, patientIvd.id);
    }

    /**
     * @sdlc.requirements SR-5046
     */
    @Test (groups = "sinnoh")
    public void verify_clia_clonality_report () {
        OrderTest orderTest = diagnosticClia.findOrderTest (assayCliaID);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/below-lod.id.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for below LOD for clonality matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayCliaID);
        report.releaseReport (assayCliaID, Pass);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientClia, diagnosticClia, orderTest, BCell);
        clonoseq.isCLIA = true;
        clonoseq.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.87 65,603 IGH 81,603 1,976 IGK 132,814 3,187 IGL 812 565";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");
    }

    /**
     * @sdlc.requirements SR-5046
     */
    @Test (groups = "sinnoh")
    public void verify_clia_tracking_report () {
        OrderTest orderTest = diagnosticClia.findOrderTest (assayCliaMRD);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/below-lod.mrd.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for below LOD for tracking matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayCliaMRD);
        report.releaseReport (assayCliaMRD, Pass);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientClia, diagnosticClia, orderTest, BCell);
        clonoseq.isCLIA = true;
        clonoseq.pageSize = 5;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.04 322,875 IGH ≥7,550 7,550 IGK 8,328 5,829 IGL 1,968 1,503";
        clonoseq.appendix.sequenceTable = "IGH - Sequence A 6 7 IGK - Sequence B 589 740 IGK - Sequence C 589 740";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }

    /**
     * @sdlc.requirements SR-5046
     */
    @Test (groups = "sinnoh")
    public void verify_ivd_clonality_report () {
        OrderTest orderTest = diagnosticIvd.findOrderTest (assayIvdID);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/below-lod.id.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for below LOD for clonality matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayIvdID);
        report.releaseReport (assayIvdID, Pass);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientIvd, diagnosticIvd, orderTest, BCell);
        clonoseq.isIVD = true;
        clonoseq.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.87 65,603 IGH 81,603 1,976 IGK 132,814 3,187 IGL 812 565";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");
    }

    /**
     * @sdlc.requirements SR-5046
     */
    @Test (groups = "sinnoh")
    public void verify_ivd_tracking_report () {
        OrderTest orderTest = diagnosticIvd.findOrderTest (assayIvdMRD);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/below-lod.mrd.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for below LOD for tracking matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayIvdMRD);
        report.releaseReport (assayIvdMRD, Pass);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientIvd, diagnosticIvd, orderTest, BCell);
        clonoseq.isIVD = true;
        clonoseq.pageSize = 5;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.04 322,875 IGH ≥7,550 7,550 IGK 8,328 5,829 IGL 1,968 1,503";
        clonoseq.appendix.sequenceTable = "IGH - Sequence A 6 7 IGK - Sequence B 589 740 IGK - Sequence C 589 740";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 tracking report matched with the baseline");
    }
}
