package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.isIVD;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.Skin;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
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
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.pipeline.dto.diagnostic.AnalysisConfig;
import com.adaptivebiotech.pipeline.dto.diagnostic.ClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportEosTestSuite extends ReportTestBase {

    private final String   downloadDir = artifacts (this.getClass ().getName ());
    private final String   reportData  = "reportData.json";
    private final Patient  patient     = scenarioBuilderPatient ();
    private final Assay    assayID     = isIVD ? ID_BCell2_IVD : ID_BCell2_CLIA;
    private final Assay    assayMRD    = isIVD ? MRD_BCell2_IVD : MRD_BCell2_CLIA;
    private Login          login       = new Login ();
    private OrcaHistory    history     = new OrcaHistory ();
    private ReportClonoSeq report      = new ReportClonoSeq ();
    private Diagnostic     diagnostic;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        diagnostic = buildDiagnosticOrder (patient,
                                           stage (SecondaryAnalysis, Ready),
                                           genCDxTest (assayID, azTsvPath + "/scenarios/above-loq.id.tsv.gz"),
                                           genCDxTest (assayMRD, azTsvPath + "/scenarios/above-loq.mrd.tsv.gz"));
        diagnostic.specimen.sampleType = gDNA;
        diagnostic.specimen.properties.SourceType = Skin;
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
    }

    /**
     * @sdlc.requirements SR-633, SR-1017, SR-1016, SR-1014, SR-1012, SR-1011, SR-1009, SR-1007,
     *                    SR-630, SR-4103, SR-4072, SR-4073
     */
    @Test (groups = { "struay", "sanssouci" })
    public void verify_clonality_report () {
        OrderTest orderTest = diagnostic.findOrderTest (assayID);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (assayID);
        report.releaseReport (assayID, Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        history.clickOrderTest ();
        report.clickReportTab (assayID);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, BCell);
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
    public void verify_tracking_report () {
        OrderTest orderTest = diagnostic.findOrderTest (assayMRD);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);
        String reportDataJson = join ("/", downloadDir, orderTest.sampleName, reportData);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (assayMRD);
        report.releaseReport (assayMRD, Pass);
        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.get (history.getFileLocation (reportData), reportDataJson);
        testLog ("downloaded " + reportData);

        history.clickOrderTest ();
        report.clickReportTab (assayMRD);
        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, BCell);
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
