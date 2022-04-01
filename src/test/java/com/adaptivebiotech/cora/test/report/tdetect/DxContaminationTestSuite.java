package com.adaptivebiotech.cora.test.report.tdetect;

import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_selfpay;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.TestHelper.scenarioBuilderPatient;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.buildTdetectOrder;
import static com.adaptivebiotech.cora.utils.TestScenarioBuilder.stage;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxContamination;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.DxReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.TestHelper.fileToList;
import static com.adaptivebiotech.test.utils.TestHelper.parseTsv;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.file.Files.exists;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.AssayResponse.CoraTest;
import com.adaptivebiotech.cora.dto.Diagnostic;
import com.adaptivebiotech.cora.dto.Orders.OrderTest;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.dto.Workflow.WorkflowProperties;
import com.adaptivebiotech.cora.test.report.ReportTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.OrderDetailTDetect;
import com.adaptivebiotech.cora.ui.order.ReportTDetect;
import com.adaptivebiotech.pipeline.dto.dx.SharedClones;
import com.adaptivebiotech.pipeline.dto.dx.Similarity;
import net.lingala.zip4j.ZipFile;

/**
 * @author Harry Soehalim
 *         <a href="mailto:hsoehalim@adaptivebiotech.com">hsoehalim@adaptivebiotech.com</a>
 */
@Test (groups = "regression")
public class DxContaminationTestSuite extends ReportTestBase {

    private final String       tsvPath           = azPipelineFda + "/210129_NB551550_0234_AHV5JNBGXG/v3.1/20210131_0506/packaged/rd.Human.TCRB-v4b.nextseq.156x12x0.vblocks.ultralight.rev2/HV5JNBGXG_0_CLINICAL-CLINICAL_95224-SN-2193.adap.txt.results.tsv.gz";
    private final String       contaminationFile = "contaminationResults.zip";
    private final Patient      patient           = scenarioBuilderPatient ();
    private Login              login             = new Login ();
    private OrcaHistory        history           = new OrcaHistory ();
    private OrderDetailTDetect order             = new OrderDetailTDetect ();
    private ReportTDetect      report            = new ReportTDetect ();
    private Diagnostic         diagnostic;
    private String             downloadDir;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();

        WorkflowProperties wProperties = new WorkflowProperties ();
        wProperties.flowcell = "HV5JNBGXG";
        wProperties.workspaceName = "CLINICAL-CLINICAL";
        wProperties.sampleName = "95224-SN-2193";
        wProperties.lastAcceptedTsvPath = tsvPath;
        wProperties.lastFinishedPipelineJobId = "8a7a958875d3e9d801775471b10666b2";

        CoraTest test = coraApi.getTDxTest (COVID19_DX_IVD);
        test.workflowProperties = wProperties;

        diagnostic = buildTdetectOrder (coraApi.getPhysician (TDetect_selfpay),
                                        patient,
                                        stage (DxAnalysis, Ready),
                                        test,
                                        COVID19_DX_IVD);
        assertEquals (coraApi.newTdetectOrder (diagnostic).patientId, patient.id);
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
    }

    /**
     * Note:
     * - to find workflows with contamination:
     * select * from orca.current_workflow_properties where property_name='xcContaminated'
     * 
     * @sdlc.requirements SR-5363, SR-6982:R4
     */
    public void xcContaminated_true () {
        OrderTest orderTest = diagnostic.findOrderTest (COVID19_DX_IVD);
        login.doLogin ();
        history.gotoOrderDebug (orderTest.sampleName);
        history.waitFor (DxAnalysis, Finished);
        history.waitFor (DxContamination, Ready);
        testLog ("completed DxAnalysis stage and entered the DxContamination stage with status Ready");

        String msg = "Waiting for contamination job(s) to finish.";
        history.waitFor (DxContamination, Awaiting, null, msg);
        testLog (format ("entered Awaiting status with the message '%s'", msg));

        history.waitFor (DxContamination, Finished);
        List <Stage> histories = history.parseStatusHistory ();
        List <String> messages = histories.stream ()
                                          .filter (h -> DxContamination.equals (h.stageName) && Ready.equals (h.stageStatus))
                                          .filter (h -> h.subStatusMessage != null && h.subStatusMessage.startsWith ("Found or created dx contamination job"))
                                          .map (h -> h.subStatusMessage).collect (toList ());
        assertEquals (messages.size (), 3);
        assertEquals (messages.stream ().filter (m -> m.endsWith ("for scope Flowcell")).count (), 1);
        assertEquals (messages.stream ().filter (m -> m.endsWith ("for scope Plate")).count (), 1);
        assertEquals (messages.stream ().filter (m -> m.endsWith ("for scope ExtractionBatch")).count (), 1);
        testLog ("3 statuses are displayed in the substatus message in the form: 'Found or created dx contamination job ... for scope ...'");
        testLog ("the scopes listed were Flowcell, Plate, and ExtractionBatch");

        String found = "xcContaminated";
        assertEquals (history.getWorkflowProperties ().get (found), "true");
        testLog (format ("workflow property '%s' is set to 'true'", found));

        coraDebugApi.login ();
        coraDebugApi.get (history.getFileLocation (contaminationFile), join ("/", downloadDir, contaminationFile));
        testLog ("downloaded " + contaminationFile);

        history.waitFor (DxReport, Awaiting, CLINICAL_QC);
        history.clickOrderTest ();
        order.clickReportTab (COVID19_DX_IVD);
        assertEquals (report.parseFlags ().get (0).name, "CROSS_CONTAMINATION_DETECTED");
        testLog ("Flags section contained CROSS_CONTAMINATION_DETECTED");

        report.releaseReport (COVID19_DX_IVD, Pass);
        testLog ("release the report");

        try {
            new ZipFile (join ("/", downloadDir, contaminationFile)).extractAll (downloadDir);
            String[] dirs = { "Flowcell", "Plate", "ExtractionBatch" };
            String similarity = "similarity.tsv", splots = "scatterplots", xclones = "shared_clones";
            for (String dir : dirs) {
                assertTrue (exists (Paths.get (downloadDir, dir)));
                compareTsv (join ("/", dir, similarity), Similarity.class, null);
                testLog (format ("found %s/%s", dir, similarity));

                String sFile = join ("/", downloadDir, dir, similarity);
                String[] h = {
                        "sample_a", "sample_b", "jaccard_index", "dx_score_a", "upr_a", "dx_score_b", "upr_b",
                        "fcid_a", "fcid_b", "run_date_a", "run_date_b" };
                assertEquals (asList (fileToList (sFile).get (0).split ("\t")), asList (h), sFile);
                testLog (format ("%s has the following headers: %s", sFile, join (", ", h)));

                parseTsv (sFile, Similarity.class).forEach (s -> {
                    String tsv = format ("shared_clones_%s__%s.tsv", s.sample_a, s.sample_b);
                    String png = format ("%s__%s.png", s.sample_a, s.sample_b);
                    compareTsv (join ("/", dir, xclones, tsv),
                                SharedClones.class,
                                comparing ( (SharedClones sc) -> sc.clone));
                    testLog (format ("found %s/%s/%s", dir, xclones, tsv));

                    compareImages (join ("/", dir, splots, png));
                    testLog (format ("found %s/%s/%s", dir, splots, png));
                });
            }
            testLog (format ("%s contained 3 folders labelled: %s", contaminationFile, join (", ", dirs)));
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    private <T> void compareTsv (String path, Class <T> klazz, Comparator <T> comparator) {
        String expectedFile = getSystemResource (join ("/", "contamination", path)).getPath ();
        String actualFile = join ("/", downloadDir, path);
        assertEquals (parseTsv (actualFile, klazz, comparator),
                      parseTsv (expectedFile, klazz, comparator),
                      actualFile);
    }

    private void compareImages (String path) {
        String expectedFile = getSystemResource (join ("/", "contamination", path)).getPath ();
        String actualFile = join ("/", downloadDir, path);
        assertEquals (getDifferencePercent (actualFile, expectedFile), 0.0d, path);
    }
}
