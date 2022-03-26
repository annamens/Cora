package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_IVD;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.test.PipelineEnvironment.isIVD;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.Cellular;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.tracking;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.BoneMarrowAspirateSlide;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.util.List;
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
import com.adaptivebiotech.picasso.dto.ClinicalReport;
import com.adaptivebiotech.picasso.dto.ReportRender.SampleInfo;
import com.adaptivebiotech.pipeline.dto.diagnostic.AnalysisConfig;
import com.seleniumfy.test.utils.HttpClientHelper;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportBcellLiftedTestSuite extends ReportTestBase {

    private final String   tsvPath     = azPipelineNorth + "/190608_NB501743_0470_AHTJHJBGX9/v3.0/20190611_0043/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev7/HTJHJBGX9_0_CLINICAL-CLINICAL_02064-08BC.adap.txt.results.tsv.gz";
    private final String   downloadDir = artifacts (this.getClass ().getName ());
    private final Assay    assayMRD    = isIVD ? MRD_BCell2_IVD : MRD_BCell2_CLIA;
    private Login          login       = new Login ();
    private OrcaHistory    history     = new OrcaHistory ();
    private ReportClonoSeq report      = new ReportClonoSeq ();
    private Diagnostic     diagnostic;
    private Patient        patient;

    @BeforeClass
    public void beforeClass () {
        coraApi.addCoraToken ();
        patient = new Patient ();
        patient.id = isIVD ? "7648a779-465e-411e-b3a6-7935aeb27628" : "dc8a6bd2-0e68-41c2-aece-7e9d0e43f58c";
        patient.mrn = "1111111111";
        patient.insurance1 = null;
        patient.insurance2 = null;
        diagnostic = buildDiagnosticOrder (patient, stage (SecondaryAnalysis, Ready), genCDxTest (assayMRD, tsvPath));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);
    }

    /**
     * @sdlc.requirements
     */
    public void verify_tracking_report () {
        OrderTest orderTest = diagnostic.findOrderTest (assayMRD);
        String saResultJson = join ("/", downloadDir, orderTest.sampleName, saResult);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraApi.login ();
        HttpClientHelper.get (history.getFileLocation (saResult), new File (saResultJson));
        testLog ("downloaded secondaryAnalysisResult.json file");
        HttpClientHelper.get (history.getFileLocation (saInput), new File (saInputJson));
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (assayMRD);
        report.releaseReport (assayMRD, Pass);

        AnalysisConfig config = parseAnalysisConfig (saInputJson);
        assertEquals (config.reportType, tracking);
        assertEquals (config.compartment, Cellular.label.toLowerCase ());
        assertEquals (config.testLocus, BCell);
        assertEquals (config.sampleName, orderTest.sampleName);
        config.knownIds.forEach (c -> assertEquals (c.cloneSource, "ClonoSEQV1"));

        // looking for the lifted ID clones
        List <SampleInfo> samples;
        if (isIVD) {
            samples = config.patientSamples.parallelStream ()
                                           .filter (s -> s.sampleName.equals ("SP-87659 (17S-040SO0025)"))
                                           .collect (toList ());
            assertEquals (samples.size (), 2);
            samples.forEach (s -> {
                assertTrue (s.wasClonalityTest);
                assertEquals (formatDt1.format (s.collectionDate), "08/09/2016");
                assertEquals (s.specimenType, BoneMarrowAspirateSlide);
            });
        } else {
            samples = config.patientSamples.parallelStream ()
                                           .filter (s -> s.sampleName.equals ("SP-79571 (202686)"))
                                           .collect (toList ());
            assertEquals (samples.size (), 2);
            samples.forEach (s -> {
                assertTrue (s.wasClonalityTest);
                assertEquals (formatDt1.format (s.collectionDate), "12/12/2013");
                assertEquals (s.specimenType, gDNA);
            });
        }
        testLog ("found a record of the sample where the lifted ID clone was found in the report");

        ClinicalReport report = parseAnalysisResult (saResultJson);
        assertTrue (report.resultClones.parallelStream ()
                                       .filter (rc -> rc.history.parallelStream ()
                                                                .filter (hc -> hc.orderTestId.equals (samples.get (0).orderTestId) || hc.orderTestId.equals (samples.get (1).orderTestId))
                                                                .count () > 0)
                                       .count () > 0);
        testLog ("found a record of the sample where the lifted ID clone was found in the secondary anaylysis report");
    }
}
