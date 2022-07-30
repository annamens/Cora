/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.report.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.pipeline.utils.TestHelper.Locus.BCell;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt1;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.Cellular;
import static com.adaptivebiotech.test.utils.PageHelper.ReportType.tracking;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.picasso.dto.ClinicalReport;
import com.adaptivebiotech.pipeline.dto.mrd.ClinicalJson;
import com.adaptivebiotech.pipeline.dto.mrd.ClinicalJson.SampleInfo;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class ReportBcellLiftedTestSuite extends ReportTestBase {

    private final String   tsvPath     = azPipelineClia + "/190608_NB501743_0470_AHTJHJBGX9/v3.0/20190611_0043/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev7/HTJHJBGX9_0_CLINICAL-CLINICAL_02064-08BC.adap.txt.results.tsv.gz";
    private final String   downloadDir = artifacts (this.getClass ().getName ());
    private Login          login       = new Login ();
    private OrcaHistory    history     = new OrcaHistory ();
    private ReportClonoSeq report      = new ReportClonoSeq ();

    public void verify_tracking_report () {
        Patient patient = new Patient ();
        patient.id = fromString ("dc8a6bd2-0e68-41c2-aece-7e9d0e43f58c");
        patient.mrn = "1111111111";
        patient.insurance1 = null;
        patient.insurance2 = null;
        Diagnostic diagnostic = buildCdxOrder (patient,
                                               stage (SecondaryAnalysis, Ready),
                                               genCDxTest (MRD_BCell2_CLIA, tsvPath));
        assertEquals (coraApi.newBcellOrder (diagnostic).patientId, patient.id);

        OrderTest orderTest = diagnostic.findOrderTest (MRD_BCell2_CLIA);
        String saResultJson = join ("/", downloadDir, orderTest.sampleName, saResult);
        String saInput = format ("CLINICAL-CLINICAL.%s.json", orderTest.sampleName);
        String saInputJson = join ("/", downloadDir, orderTest.sampleName, saInput);

        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (SecondaryAnalysis, Finished);
        history.waitFor (ShmAnalysis, Finished);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (saResult), saResultJson);
        testLog ("downloaded secondaryAnalysisResult.json file");
        coraDebugApi.get (history.getFileLocation (saInput), saInputJson);
        testLog ("downloaded Analysis Config json file");

        history.clickOrderTest ();
        report.clickReportTab (MRD_BCell2_CLIA);
        report.releaseReport (MRD_BCell2_CLIA, Pass);

        ClinicalJson config = parseAnalysisConfig (saInputJson);
        assertEquals (config.reportType, tracking);
        assertEquals (config.compartment, Cellular.label.toLowerCase ());
        assertEquals (config.testLocus, BCell);
        assertEquals (config.sampleName, orderTest.sampleName);
        config.knownIds.forEach (c -> assertEquals (c.cloneSource, "ClonoSEQV1"));

        // looking for the lifted ID clones
        List <SampleInfo> samples = config.patientSamples.parallelStream ()
                                                         .filter (s -> s.sampleName.equals ("SP-79571 (202686)"))
                                                         .collect (toList ());
        assertEquals (samples.size (), 2);
        samples.forEach (s -> {
            assertTrue (s.wasClonalityTest);
            assertEquals (formatDt1.format (s.collectionDate), "12/12/2013");
            assertEquals (s.specimenType, gDNA);
        });
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
