package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_TCRB;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_TCRB;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.TCRB;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt6;
import static java.lang.String.join;
import static java.time.LocalDateTime.parse;
import static org.testng.Assert.assertEquals;
import java.time.LocalDateTime;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.pipeline.dto.diagnostic.ClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportTcrbTestSuite extends ReportTestBase {

    private final String   tsvPath        = azTsvPath + "/scenarios/HKJVGBGXC_0_CLINICAL-CLINICAL_68353-01MB.adap.txt.results.tsv.gz";
    private final String   lastFlowcellId = "HKJVGBGXC";
    private final String   downloadDir    = artifacts (this.getClass ().getName ());
    private final Patient  patient        = scenarioBuilderPatient ();
    private Login          login          = new Login ();
    private OrcaHistory    history        = new OrcaHistory ();
    private ReportClonoSeq report         = new ReportClonoSeq ();
    private Diagnostic     diagnostic;

    @BeforeClass
    public void beforeClass () {
        coraApi.addCoraToken ();
        diagnostic = buildDiagnosticOrder (patient,
                                           stage (NorthQC, Ready),
                                           genTcrTest (ID_TCRB, lastFlowcellId, tsvPath),
                                           genTcrTest (MRD_TCRB, lastFlowcellId, tsvPath));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.newTcellOrder (diagnostic).patientId, patient.id);
    }

    /**
     * @sdlc.requirements SR-3445
     */
    public void verify_clonality_report () {
        OrderTest orderTest = diagnostic.findOrderTest (ID_TCRB);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (ID_TCRB);
        report.releaseReport (ID_TCRB, Pass);

        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, TCRB);
        clonoseq.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.9 61,413 TCRB 16,723 1,661";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the TCRB ClonoSEQ 2.0 clonality report matched with the baseline");
    }

    /**
     * @sdlc.requirements SR-3445
     */
    public void verify_tracking_report () {
        OrderTest orderTest = diagnostic.findOrderTest (MRD_TCRB);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (MRD_TCRB);
        report.releaseReport (MRD_TCRB, Pass);

        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, TCRB);
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = formatDt1.format (releaseDt);
        clonoseq.appendix.sampleTable = "0.9 61,413 TCRB 16,723 1,661";
        clonoseq.approval.dateTime = formatDt1.format (releaseDt);
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the TCRB ClonoSEQ 2.0 tracking report matched with the baseline");
    }
}
