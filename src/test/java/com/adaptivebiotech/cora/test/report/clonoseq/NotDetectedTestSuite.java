package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.isIVD;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import java.io.File;
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
import com.seleniumfy.test.utils.HttpClientHelper;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class NotDetectedTestSuite extends ReportTestBase {

    private final String   downloadDir = artifacts (this.getClass ().getName ());
    private final Patient  patient     = scenarioBuilderPatient ();
    private final Assay    assayID     = isIVD ? ID_BCell2_IVD : ID_BCell2_CLIA;
    private final Assay    assayMRD    = isIVD ? MRD_BCell2_IVD : MRD_BCell2_CLIA;
    private Login          login       = new Login ();
    private OrcaHistory    history     = new OrcaHistory ();
    private ReportClonoSeq report      = new ReportClonoSeq ();
    private Diagnostic     diagnostic;

    @BeforeClass
    public void beforeClass () {
        coraApi.addCoraToken ();
        diagnostic = buildDiagnosticOrder (patient,
                                           stage (SecondaryAnalysis, Ready),
                                           genCDxTest (assayID, azTsvPath + "/scenarios/not-detected.id.tsv.gz"),
                                           genCDxTest (assayMRD, azTsvPath + "/scenarios/not-detected.mrd.tsv.gz"));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
    }

    /**
     * @sdlc.requirements
     */
    public void verify_clonality_report () {
        OrderTest orderTest = diagnostic.findOrderTest (assayID);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/not-detected.id.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraApi.login ();
        HttpClientHelper.get (history.getFileLocation (saResult), new File (actual));
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for not detected for clonality matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayID);
        report.releaseReport (assayID, Pass);
    }

    /**
     * @sdlc.requirements
     */
    public void verify_tracking_report () {
        OrderTest orderTest = diagnostic.findOrderTest (assayMRD);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);

        String expected = getSystemResource ("SecondaryAnalysis/not-detected.mrd.json").getPath ();
        String actual = join ("/", downloadDir, orderTest.sampleName, saResult);
        coraApi.login ();
        HttpClientHelper.get (history.getFileLocation (saResult), new File (actual));
        compareSecondaryAnalysisResults (actual, expected);
        testLog ("the secondaryAnalysisResult.json for not detected for tracking matched with the baseline");

        history.clickOrderTest ();
        report.clickReportTab (assayMRD);
        report.releaseReport (assayMRD, Pass);
    }
}
