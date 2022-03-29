package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_TCRB;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_TCRG;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.TCRB;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.TCRG;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.Cellular;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.tracking;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
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
import static java.time.LocalDate.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.ReportRender.SampleInfo;

/**
 * Note: since we rolled back Rindja release, this test is nolonger valid
 * 
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportTcellLiftedTestSuite extends ReportTestBase {

    private final String   tsvPathTCRB        = azPipelineNorth + "/160426_NB501172_0103_AH5CJ7BGXY/v3.0/20160428_0818/packaged/rd.Human.Beta.nextseq.156x9x0.readThrough.ultralight.rev2/H5CJ7BGXY_0_CLINICAL-CLINICAL_00456-01BB.adap.txt.results.tsv.gz";
    private final String   tsvPathTCRG        = azPipelineNorth + "/160429_SN432_1257_AHJTK5BCXX/v3.0/20160501_1430/packaged/rd.Human.Gamma.hiseq.156x15x0.readThrough.ultralight.rev3/HJTK5BCXX_0_CLINICAL-CLINICAL_00456-01BG.adap.txt.results.tsv.gz";
    private final String   lastFlowcellIdTCRB = "H5CJ7BGXY";
    private final String   lastFlowcellIdTCRG = "HJTK5BCXX";
    private final String   report             = "reportData.json";
    private final String   downloadDir        = artifacts (this.getClass ().getName ());
    private Login          login              = new Login ();
    private OrcaHistory    history            = new OrcaHistory ();
    private ReportClonoSeq orderReport        = new ReportClonoSeq ();
    private Diagnostic     diagnostic;
    private Patient        patient;

    @BeforeClass
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        patient = new Patient ();
        patient.id = "6170cc74-6c83-4c22-929c-b08a6514617d";
        patient.mrn = "1111111111";
        patient.insurance1 = null;
        patient.insurance2 = null;
        diagnostic = buildDiagnosticOrder (patient,
                                           stage (NorthQC, Ready),
                                           genTcrTest (MRD_TCRG, lastFlowcellIdTCRG, tsvPathTCRG),
                                           genTcrTest (MRD_TCRB, lastFlowcellIdTCRB, tsvPathTCRB));
        diagnostic.order.postToImmunoSEQ = true;
        assertEquals (coraApi.newTcellOrder (diagnostic).patientId, patient.id);
    }

    public void verify_tcrb_tracking_report () {
        OrderTest orderTest = diagnostic.findOrderTest (MRD_TCRB);
        String reportJson = join ("/", downloadDir, orderTest.sampleName, report);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (NorthQC, Finished);
        history.waitFor (CalculateSampleSummary, Finished);
        history.waitFor (Analyzer, Finished);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        orderReport.clickReportTab (MRD_TCRB);
        orderReport.releaseReport (MRD_TCRB, Pass);
        testLog ("released the report");

        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (report), reportJson);
        testLog ("downloaded reportData.json file");

        ReportRender report = parseReportData (reportJson);
        assertEquals (report.patientInfo.reportType, tracking);
        assertEquals (report.patientInfo.reportSpecimenCompartment, Cellular.label);
        assertEquals (report.patientInfo.reportLocus, TCRB);
        assertEquals (report.patientInfo.reportSpecimenId, orderTest.specimen.specimenNumber);
        report.data.resultClones.forEach (c -> assertEquals (c.locus, TCRB));

        // looking for the lifted ID clones
        SampleInfo sample = report.specimenInfo.parallelStream ()
                                               .filter (s -> s.sampleName.equals ("SP-65425"))
                                               .findAny ().get ();
        assertTrue (sample.wasClonalityTest);
        assertEquals (sample.collectionDate, of (2016, 4, 21));
        assertEquals (sample.specimenType, Blood);
        assertNull (sample.specimenSource);
        testLog ("found a record of the sample where the lifted ID clone was found in the report");

        assertTrue (report.data.resultClones.parallelStream ()
                                            .filter (rc -> rc.history.parallelStream ()
                                                                     .filter (hc -> hc.orderTestId.equals (sample.orderTestId))
                                                                     .count () > 0)
                                            .count () > 0);
        testLog ("found a record of the sample where the lifted ID clone was found in the secondary anaylysis report");
    }

    public void verify_tcrg_tracking_report () {
        OrderTest orderTest = diagnostic.findOrderTest (MRD_TCRG);
        String reportJson = join ("/", downloadDir, orderTest.sampleName, report);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (NorthQC, Finished);
        history.waitFor (CalculateSampleSummary, Finished);
        history.waitFor (Analyzer, Finished);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        orderReport.clickReportTab (MRD_TCRG);
        orderReport.releaseReport (MRD_TCRG, Pass);
        testLog ("released the report");

        history.gotoOrderDebug (orderTest.sampleName);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (report), reportJson);
        testLog ("downloaded reportData.json file");

        ReportRender report = parseReportData (reportJson);
        assertEquals (report.patientInfo.reportType, tracking);
        assertEquals (report.patientInfo.reportSpecimenCompartment, Cellular.label);
        assertEquals (report.patientInfo.reportLocus, TCRG);
        assertEquals (report.patientInfo.reportSpecimenId, orderTest.specimen.specimenNumber);
        report.data.resultClones.forEach (c -> assertEquals (c.locus, TCRG));

        // looking for the lifted ID clones
        SampleInfo sample = report.specimenInfo.parallelStream ()
                                               .filter (s -> s.sampleName.equals ("SP-65425"))
                                               .findAny ().get ();
        assertTrue (sample.wasClonalityTest);
        assertEquals (sample.collectionDate, of (2016, 4, 21));
        assertEquals (sample.specimenType, Blood);
        assertNull (sample.specimenSource);
        testLog ("found a record of the sample where the lifted ID clone was found in the report");

        assertTrue (report.data.resultClones.parallelStream ()
                                            .filter (rc -> rc.history.parallelStream ()
                                                                     .filter (hc -> hc.orderTestId.equals (sample.orderTestId))
                                                                     .count () > 0)
                                            .count () > 0);
        testLog ("found a record of the sample where the lifted ID clone was found in the secondary anaylysis report");
    }
}
