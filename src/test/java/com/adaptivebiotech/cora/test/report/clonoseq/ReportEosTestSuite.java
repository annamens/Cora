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
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.Skin;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt6;
import static java.lang.String.format;
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
import com.adaptivebiotech.cora.dto.report.AnalysisConfig;
import com.adaptivebiotech.cora.dto.report.ClonoSeq;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ReportRender;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportEosTestSuite extends ReportTestBase {

    private final String   downloadDir  = artifacts (this.getClass ().getName ());
    private final String   reportData   = "reportData.json";
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
                                               genCDxTest (assayCliaID, azTsvPath + "/above-loq.id.tsv.gz"),
                                               genCDxTest (assayCliaMRD, azTsvPath + "/above-loq.mrd.tsv.gz"));
        diagnosticClia.specimen.sampleType = gDNA;
        diagnosticClia.specimen.properties.SourceType = Skin;
        assertEquals (coraApi.newBcellOrder (diagnosticClia).patientId, patientClia.id);

        diagnosticIvd = buildDiagnosticOrder (patientIvd,
                                              stage (SecondaryAnalysis, Ready),
                                              genCDxTest (assayIvdID, azTsvPath + "/above-loq.id.tsv.gz"),
                                              genCDxTest (assayIvdMRD, azTsvPath + "/above-loq.mrd.tsv.gz"));
        diagnosticIvd.specimen.sampleType = gDNA;
        diagnosticIvd.specimen.properties.SourceType = Skin;
        assertEquals (coraApi.newBcellOrder (diagnosticIvd).patientId, patientIvd.id);
    }

    /**
     * @sdlc.requirements SR-633, SR-1017, SR-1016, SR-1014, SR-1012, SR-1011, SR-1009, SR-1007,
     *                    SR-630, SR-4103, SR-4072, SR-4073
     */
    @Test (groups = { "struay", "sanssouci" })
    public void verify_clia_clonality_report () {
        OrderTest orderTest = diagnosticClia.findOrderTest (assayCliaID);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (assayCliaID);
        report.releaseReport (assayCliaID, Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        history.clickOrderTest ();
        report.clickReportTab (assayCliaID);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientClia, diagnosticClia, orderTest, BCell);
        clonoseq.isCLIA = true;
        clonoseq.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.81 70,977 IGH 60,049 2,671 IGK 64,371 4,749 IGL 2,108 1,700";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        AnalysisConfig config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("found specimenSource in patientSamples objects in the Analysis Config json file");

        ReportRender report = parseReportData (reportDataJson);
        assertEquals (report.patientInfo.reportSpecimenType, gDNA);
        assertEquals (report.patientInfo.reportSpecimenSource, Skin);
        report.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("found specimenSource in specimenInfo objects in " + reportData);
    }

    /**
     * @sdlc.requirements SR-633, SR-1017, SR-1016, SR-1014, SR-1012, SR-1011, SR-1009, SR-1007,
     *                    SR-630, SR-4103, SR-4072, SR-4073
     */
    @Test (groups = { "struay", "sanssouci" })
    public void verify_clia_tracking_report () {
        OrderTest orderTest = diagnosticClia.findOrderTest (assayCliaMRD);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (assayCliaMRD);
        report.releaseReport (assayCliaMRD, Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        history.clickOrderTest ();
        report.clickReportTab (assayCliaMRD);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientClia, diagnosticClia, orderTest, BCell);
        clonoseq.isCLIA = true;
        clonoseq.pageSize = 4;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.09 3,261,145 IGH 73,326 71,309 IGK 98,699 37,457 IGL 28,278 11,994";
        clonoseq.appendix.sequenceTable = "IGH - Sequence A <1 <1 IGK - Sequence B <1 <1";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 tracking report matched with the baseline");

        AnalysisConfig config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("found specimenSource in patientSamples objects in the Analysis Config json file");

        ReportRender report = parseReportData (reportDataJson);
        assertEquals (report.patientInfo.reportSpecimenType, gDNA);
        assertEquals (report.patientInfo.reportSpecimenSource, Skin);
        report.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("found specimenSource in specimenInfo objects in " + reportData);
    }

    /**
     * @sdlc.requirements SR-633, SR-1017, SR-1016, SR-1014, SR-1012, SR-1011, SR-1009, SR-1007,
     *                    SR-630, SR-4103, SR-4072, SR-4073
     */
    @Test (groups = { "struay", "sanssouci" })
    public void verify_ivd_clonality_report () {
        OrderTest orderTest = diagnosticIvd.findOrderTest (assayIvdID);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (assayIvdID);
        report.releaseReport (assayIvdID, Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        history.clickOrderTest ();
        report.clickReportTab (assayIvdID);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientIvd, diagnosticIvd, orderTest, BCell);
        clonoseq.isIVD = true;
        clonoseq.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.81 70,977 IGH 60,049 2,671 IGK 64,371 4,749 IGL 2,108 1,700";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 clonality report matched with the baseline");

        AnalysisConfig config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("found specimenSource in patientSamples objects in the Analysis Config json file");

        ReportRender report = parseReportData (reportDataJson);
        assertEquals (report.patientInfo.reportSpecimenType, gDNA);
        assertEquals (report.patientInfo.reportSpecimenSource, Skin);
        report.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("found specimenSource in specimenInfo objects in " + reportData);
    }

    /**
     * @sdlc.requirements SR-633, SR-1017, SR-1016, SR-1014, SR-1012, SR-1011, SR-1009, SR-1007,
     *                    SR-630, SR-4103, SR-4072, SR-4073
     */
    @Test (groups = { "struay", "sanssouci" })
    public void verify_ivd_tracking_report () {
        OrderTest orderTest = diagnosticIvd.findOrderTest (assayIvdMRD);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (assayIvdMRD);
        report.releaseReport (assayIvdMRD, Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        history.clickOrderTest ();
        report.clickReportTab (assayIvdMRD);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patientIvd, diagnosticIvd, orderTest, BCell);
        clonoseq.isIVD = true;
        clonoseq.pageSize = 4;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.09 3,261,145 IGH 73,326 71,309 IGK 98,699 37,457 IGL 28,278 11,994";
        clonoseq.appendix.sequenceTable = "IGH - Sequence A <1 <1 IGK - Sequence B <1 <1";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the EOS ClonoSEQ 2.0 tracking report matched with the baseline");

        AnalysisConfig config = parseAnalysisConfig (saInputJson);
        config.patientSamples.forEach (p -> {
            assertEquals (p.specimenType, gDNA);
            assertEquals (p.specimenSource, Skin);
        });
        testLog ("found specimenSource in patientSamples objects in the Analysis Config json file");

        ReportRender report = parseReportData (reportDataJson);
        assertEquals (report.patientInfo.reportSpecimenType, gDNA);
        assertEquals (report.patientInfo.reportSpecimenSource, Skin);
        report.specimenInfo.forEach (s -> {
            assertEquals (s.specimenType, gDNA);
            assertEquals (s.specimenSource, Skin);
        });
        testLog ("found specimenSource in specimenInfo objects in " + reportData);
    }
}
