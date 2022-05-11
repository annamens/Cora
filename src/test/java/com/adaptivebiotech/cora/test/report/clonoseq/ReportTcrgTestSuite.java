/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_TCRG;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_TCRG;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.TCRG;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt6;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.Analyzer;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.CalculateSampleSummary;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.NorthQC;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
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
public class ReportTcrgTestSuite extends ReportTestBase {

    private final String   tsvPath        = azTsvPath + "/H7NK7BGXC_0_CLINICAL-CLINICAL_66935-01MG-C19-929_P141-19.adap.txt.results.tsv.gz";
    private final String   lastFlowcellId = "H7NK7BGXC";
    private final String   downloadDir    = artifacts (this.getClass ().getName ());
    private Login          login          = new Login ();
    private OrcaHistory    history        = new OrcaHistory ();
    private ReportClonoSeq report         = new ReportClonoSeq ();

    /**
     * @sdlc.requirements SR-3445
     */
    public void verify_tcrg_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (NorthQC, Ready),
                                               genTcrTest (ID_TCRG, lastFlowcellId, tsvPath),
                                               genTcrTest (MRD_TCRG, lastFlowcellId, tsvPath));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.newTcellOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (ID_TCRG);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (NorthQC, Finished);
        history.waitFor (CalculateSampleSummary, Finished);
        history.waitFor (Analyzer, Finished);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (ID_TCRG);
        report.releaseReport (ID_TCRG, Pass);

        LocalDateTime releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        ClonoSeq clonoseq = basicClonoSeq (patient, diagnostic, orderTest, TCRG);
        clonoseq.helper.isCLIA = true;
        clonoseq.helper.isClonality = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = releaseDt.toLocalDate ();
        clonoseq.appendix.sampleInfo = "0.95 61,795 TCRG 59,450 3,019";
        clonoseq.helper.report.commentInfo.signedAt = releaseDt;
        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the TCRG ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_TCRG);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (NorthQC, Finished);
        history.waitFor (CalculateSampleSummary, Finished);
        history.waitFor (Analyzer, Finished);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (MRD_TCRG);
        report.releaseReport (MRD_TCRG, Pass);

        releaseDt = parse (report.getReportReleaseDate () + ".0000", formatDt6);
        clonoseq = basicClonoSeq (patient, diagnostic, orderTest, TCRG);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = 3;
        clonoseq.header.reportDt = releaseDt.toLocalDate ();
        clonoseq.appendix.sampleInfo = "0.95 61,795 TCRG 59,450 3,019";
        clonoseq.helper.report.commentInfo.signedAt = releaseDt;
        actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        verifyReport (clonoseq, getReport (report.getReportUrl (), actualPdf));
        testLog ("the TCRG ClonoSEQ 2.0 tracking report matched with the baseline");
    }
}
