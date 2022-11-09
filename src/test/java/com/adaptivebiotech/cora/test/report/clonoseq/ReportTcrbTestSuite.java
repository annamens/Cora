/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_TCRB;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_TCRB;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.PdfUtil.getPageCount;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
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
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.verify.ClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportTcrbTestSuite extends ReportTestBase {

    private final String   tsvPath        = azTsvPath + "/HKJVGBGXC_0_CLINICAL-CLINICAL_68353-01MB.adap.txt.results.tsv.gz";
    private final String   lastFlowcellId = "HKJVGBGXC";
    private final String   downloadDir    = artifacts (this.getClass ().getName ());
    private Login          login          = new Login ();
    private OrcaHistory    history        = new OrcaHistory ();
    private ReportClonoSeq report         = new ReportClonoSeq ();

    /**
     * @sdlc.requirements SR-3445
     */
    public void verify_tcrb_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (NorthQC, Ready),
                                               genTcrTest (ID_TCRB, lastFlowcellId, tsvPath),
                                               genTcrTest (MRD_TCRB, lastFlowcellId, tsvPath));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.newTcellOrder (diagnostic).patientId, patient.id);
        testLog ("[TCRB] submitted clonality and tracking orders");

        OrderTest orderTest = diagnostic.findOrderTest (ID_TCRB);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (NorthQC, Finished);
        history.waitFor (CalculateSampleSummary, Finished);
        history.waitFor (Analyzer, Finished);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (ID_TCRB);
        report.releaseReport (ID_TCRB, Pass);

        String actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        String actual = join ("/", downloadDir, orderTest.sampleName, reportData);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        ReportRender reportRender = parseReportData (actual);
        testLog ("[TCRB] downloaded " + reportData);

        ClonoSeq clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[TCRB] the ClonoSEQ 2.0 clonality report matched with the baseline");

        orderTest = diagnostic.findOrderTest (MRD_TCRB);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (NorthQC, Finished);
        history.waitFor (CalculateSampleSummary, Finished);
        history.waitFor (Analyzer, Finished);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        report.clickReportTab (MRD_TCRB);
        report.releaseReport (MRD_TCRB, Pass);

        actualPdf = join ("/", downloadDir, orderTest.sampleName + ".pdf");
        coraApi.get (report.getReportUrl (), actualPdf);
        history.gotoOrderDebug (orderTest.sampleName);

        actual = join ("/", downloadDir, orderTest.sampleName, reportData);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (reportData), actual);
        reportRender = parseReportData (actual);
        testLog ("[TCRB] downloaded " + reportData);

        clonoseq = basicClonoSeq (reportRender, patient, diagnostic, orderTest);
        clonoseq.helper.isCLIA = true;
        clonoseq.pageSize = getPageCount (actualPdf);
        verifyReport (clonoseq, getTextFromPDF (actualPdf));
        testLog ("[TCRB] the ClonoSEQ 2.0 tracking report matched with the baseline");
    }
}
