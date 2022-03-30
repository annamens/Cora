package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
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
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class AboveLoQTestSuite extends ReportTestBase {

    private final String   downloadDir  = artifacts (this.getClass ().getName ());
    private final Assay    assayCliaID  = ID_BCell2_CLIA;
    private final Assay    assayCliaMRD = MRD_BCell2_CLIA;
    private final Assay    assayIvdID   = ID_BCell2_IVD;
    private final Assay    assayIvdMRD  = MRD_BCell2_IVD;
    private Login          login        = new Login ();
    private OrcaHistory    history      = new OrcaHistory ();
    private ReportClonoSeq report       = new ReportClonoSeq ();

    public void verify_clia_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (SecondaryAnalysis, Ready),
                                               genCDxTest (assayCliaID, azTsvPath + "/above-loq.id.tsv.gz"),
                                               genCDxTest (assayCliaMRD, azTsvPath + "/above-loq.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (assayCliaID);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/above-loq.id.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for above LOQ for clonality matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayCliaID);
        report.releaseReport (assayCliaID, Pass);

        orderTest = diagnostic.findOrderTest (assayCliaMRD);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        expected = getSystemResource ("SecondaryAnalysis/above-loq.mrd.json").getPath ();
        actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for above LOQ for tracking matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayCliaMRD);
        report.releaseReport (assayCliaMRD, Pass);
    }

    public void verify_ivd_report () {
        Patient patient = scenarioBuilderPatient ();
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (SecondaryAnalysis, Ready),
                                               genCDxTest (assayIvdID, azTsvPath + "/above-loq.id.tsv.gz"),
                                               genCDxTest (assayIvdMRD, azTsvPath + "/above-loq.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (assayIvdID);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/above-loq.id.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for above LOQ for clonality matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayIvdID);
        report.releaseReport (assayIvdID, Pass);

        orderTest = diagnostic.findOrderTest (assayIvdMRD);
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        expected = getSystemResource ("SecondaryAnalysis/above-loq.mrd.json").getPath ();
        actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraDebugApi.get (history.getFileLocation (saResult), actual);
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for above LOQ for tracking matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayIvdMRD);
        report.releaseReport (assayIvdMRD, Pass);
    }
}
