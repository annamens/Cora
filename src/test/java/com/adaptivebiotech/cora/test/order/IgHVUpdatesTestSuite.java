package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.test.CoraEnvironment.pipelinePortalTestPass;
import static com.adaptivebiotech.cora.test.CoraEnvironment.pipelinePortalTestUser;
import static com.adaptivebiotech.cora.test.CoraEnvironment.portalCliaTestUrl;
import static com.adaptivebiotech.cora.test.CoraEnvironment.portalIvdTestUrl;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.test.utils.PageHelper.Assay.ID_BCell2_IVD;
import static com.adaptivebiotech.test.utils.PageHelper.ChargeType.InternalPharmaBilling;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BCells;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.BoneMarrow;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.LymphNode;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.PBMC;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellPellet;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.CellSuspension;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.FFPEScrolls;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.gDNA;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static com.seleniumfy.test.utils.HttpClientHelper.get;
import static com.seleniumfy.test.utils.HttpClientHelper.headers;
import static com.seleniumfy.test.utils.HttpClientHelper.resetheaders;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.db.CoraDBClient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.workflow.FeatureFlags;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.cora.utils.Tunnel;
import com.adaptivebiotech.picasso.dto.ReportRender;
import com.adaptivebiotech.picasso.dto.ReportRender.ShmMutationStatus;
import com.adaptivebiotech.picasso.dto.ReportRender.ShmSequence;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.seleniumfy.test.utils.HttpClientHelper;

@Test (groups = { "regression", "nutmeg" })
public class IgHVUpdatesTestSuite extends CoraBaseBrowser {

    private Physician    IgHVPhysician;
    private Physician    NYPhysician;
    private Billing      billing                          = new Billing ();
    private Specimen     specimen                         = new Specimen ();
    private Shipment     shipment                         = new Shipment ();
    private Accession    accession                        = new Accession ();
    private Diagnostic   diagnostic                       = new Diagnostic ();
    private History      history                          = new History ();
    private FeatureFlags featureFlagsPage                 = new FeatureFlags ();
    private OrderStatus  orderStatus                      = new OrderStatus ();

    private final String c91_10                           = "C91.10";
    private final String c83_00                           = "C83.00";
    private final String c90_00                           = "C90.00";

    private final String tsvOverridePathO1O2              = "s3://pipeline-north-production-archive:us-west-2/210612_NB552467_0088_AH3CH7BGXJ/v3.1/20210614_0809/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3CH7BGXJ_0_CLINICAL-CLINICAL_96343-05BC.adap.txt.results.tsv.gz";
    private final String tsvOverridePathO3O4              = "s3://pipeline-fda-production-archive:us-west-2/210615_NB551732_0294_AH3G53BGXJ/v3.1/20210617_0828/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev24/H3G53BGXJ_0_CLINICAL-CLINICAL_96633-08MC-UA001BM.adap.txt.results.tsv.gz";
    private final String tsvOverridePathO5O6O7O8          = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";
    private final String tsvOverridePathOrcaIgHVO1O8      = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results/210605_NB552488_0035_AHFFJ2BGXJ/v3.1/20210607_1834/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/HFFJ2BGXJ_0_CLINICAL-CLINICAL_01159-11MC.adap.txt.results.tsv.gz";
    private final String tsvOverridePathOrcaIgHVO2O6O7    = "https://adaptiveivdpipeline.blob.core.windows.net/pipeline-results/210603_NB552480_0036_AH2C2LBGXJ/v3.1/20210605_1317/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev23/H2C2LBGXJ_0_CLINICAL-CLINICAL_111034-01LC.adap.txt.results.tsv.gz";
    private final String tsvOverridePathOrcaIgHVO3        = "https://adaptiveivdpipeline.blob.core.windows.net/pipeline-results/210608_NB500953_0936_AH3G3KBGXJ/v3.1/20210610_0431/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev23/H3G3KBGXJ_0_CLINICAL-CLINICAL_111730-01MC-2115301589D.adap.txt.results.tsv.gz";
    private final String tsvOverridePathOrcaIgHVO4        = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results/210602_NB552492_0027_AH3GK5BGXJ/v3.1/20210604_2027/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3GK5BGXJ_0_CLINICAL-CLINICAL_102589-01MC-B20-229.adap.txt.results.tsv.gz";
    private final String tsvOverridePathOrcaIgHVO5        = "https://adaptiveivdpipeline.blob.core.windows.net/pipeline-results/210602_NB552492_0027_AH3GK5BGXJ/v3.1/20210604_1958/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev23/H3GK5BGXJ_0_CLINICAL-CLINICAL_109306-01MC-jb20-67.adap.txt.results.tsv.gz";
    private final String tsvOverridePathOrcaIgHVO9        = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results/180122_NB501661_0323_AH3KF2BGX5/v3.0/20180124_1229/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev4/H3KF2BGX5_0_MDAnderson-Thompson_PH-5N.adap.txt.results.tsv.gz";
    private final String lastFinishedPipelineJobIdO1O2    = "8a7a94db77a26ee1017a01c874c67394";
    private final String lastFinishedPipelineJobIdO3O4    = "8a7a958877a26e74017a176ecd2b1b45";
    private final String lastFinishedPipelineOrcaIgHVO1O8 = "8a7a94db77a26ee10179dfbd004f5955";
    private final String lastFinishedPipelineOrcaIgHVO6   = "12345678901234567890";
    private final String lastFinishedPipelineOrcaIgHVO2O7 = "8a7a958877a26e740179d9e8beaf3b48";
    private final String lastFinishedPipelineOrcaIgHVO3   = "8a7a958877a26e740179f2dbc180183a";
    private final String lastFinishedPipelineOrcaIgHVO4   = "8a7a94db77a26ee10179d04ea1a739e9";
    private final String lastFinishedPipelineOrcaIgHVO5   = "8a7a958877a26e740179d5e2e51821ce";
    private final String sampleNameO1O2                   = "96343-05BC";
    private final String sampleNameO3O4                   = "96633-08MC-UA001BM";
    private final String sampleNameOrcaIgHVO1O8           = "01159-11MC";
    private final String sampleNameOrcaIgHVO2O6O7         = "111034-01LC";
    private final String sampleNameOrcaIgHVO3             = "111730-01MC-2115301589D";
    private final String sampleNameOrcaIgHVO4             = "102589-01MC-B20-229";
    private final String sampleNameOrcaIgHVO5             = "109306-01MC-jb20-67";
    private final String sampleNameOrcaIgHVO9             = "PH-5N";
    private final String shmDataSourcePathOrcaIgHVO9      = "https://adaptiveruopipeline.blob.core.windows.net/pipeline-results/180122_NB501661_0323_AH3KF2BGX5/v3.0/20180124_1229";
    private final String workSpaceNameOrcaIgHVO9          = "MDAnderson-Thompson";

    private boolean      isIgHVFlag;
    private final byte[] authBytes                        = (pipelinePortalTestUser + ":" + pipelinePortalTestPass).getBytes ();
    private final String portalTestAuth                   = "Basic " + Base64.getEncoder ().encodeToString (authBytes);

    private Tunnel       tunnel;
    private CoraDBClient coraDBClient;
    private final String orderTestQuery                   = "select * from orca.shm_results where order_test_id = 'REPLACEORDERTESTID'";
    private final String shmResultsSchema                 = "SELECT * FROM information_schema.columns WHERE table_name = 'shm_results' ORDER BY ordinal_position ASC";
    private final String noResultsAvailable               = "No result available";
    private final String beginIghvMutationStatus          = "IGHV MUTATION STATUS";
    private final String beginClonalityResult             = "CLONALITY RESULT";
    private final String endThisSampleFailed              = "This sample failed the quality control";
    private String       downloadDir;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
//        tunnel = Tunnel.getTunnel ();
//        Thread t = new Thread (tunnel);
//        t.start ();
//        tunnel.waitForConnection ();

        coraDBClient = new CoraDBClient (CoraEnvironment.coraDBUser, CoraEnvironment.coraDBPass);

        assertTrue (coraDBClient.openConnection ());

    }

    @AfterClass (alwaysRun = true)
    public void afterClass () throws Exception {
        coraDBClient.closeConnection ();
//        tunnel.close ();
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        downloadDir = artifacts (this.getClass ().getName (), test.getName ());
        // IgHVPhysician Physician
        IgHVPhysician = TestHelper.setPhysician (CoraEnvironment.physicianLastName,
                                                 CoraEnvironment.physicianFirstName,
                                                 CoraEnvironment.physicianAccountName);

        // NY Physician
        NYPhysician = TestHelper.setPhysician (CoraEnvironment.NYphysicianLastName,
                                               CoraEnvironment.NYphysicianFirstName,
                                               CoraEnvironment.physicianAccountName);

        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
        featureFlagsPage.navigateToFeatureFlagsPage ();
        Map <String, String> featureFlags = featureFlagsPage.getFeatureFlags ();
        isIgHVFlag = Boolean.valueOf (featureFlags.get ("IgHV"));
    }

    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder1CLIAFeatureFlagOn () {
        assertTrue (isIgHVFlag, "Validate IgHV flag is true before test starts");
        // order 1
        Assay assayTest = ID_BCell2_CLIA;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         CellPellet,
                                                         PBMC,
                                                         new String[] { c83_00, c91_10 },
                                                         "Order 1 Flag On");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), "true", "true");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO1O2,
                           lastFinishedPipelineJobIdO1O2,
                           sampleNameO1O2,
                           null,
                           null);
        testLog ("step 1 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 2 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 2 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest, true);
        assertTrue (isCLIAIGHVFlagPresent);
        testLog ("step 3 - CLIA-IGHV flag appears just below the Report tab ");

        ReportRender reportData = getReportDataJsonFile ();
        assertNotNull (reportData.shmReportResult);
        testLog ("step 4 - SHM analysis results are included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-6656:R1, R3, R4, R5, R6
     *                    NOTE: SR-T3689
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder2CLIAFeatureFlagOn () {
        assertTrue (isIgHVFlag, "Validate IgHV flag is true before test starts");
        // order 2
        Assay assayTest = ID_BCell2_CLIA;
        Map <String, String> orderDetails = createOrder (NYPhysician,
                                                         assayTest,
                                                         CellPellet,
                                                         PBMC,
                                                         new String[] { c83_00 },
                                                         "Order 2 Flag On");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), "false", "true");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO1O2,
                           lastFinishedPipelineJobIdO1O2,
                           sampleNameO1O2,
                           null,
                           null);
        testLog ("step 5 - ighvAnalysisEnabled is true, ighvReportEnabled is false (or absent)");
        testLog ("step 6 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 6 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest, true);
        assertFalse (isCLIAIGHVFlagPresent);
        testLog ("step 7 - CLIA-IGHV flag does not appear below the Report tab ");

        ReportRender reportData = getReportDataJsonFile ();
        assertNotNull (reportData.shmReportResult);
        testLog ("step 8 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-6656:R1, R3, R4, R5, R6
     *                    NOTE: SR-T3689
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder3IVDFeatureFlagOn () {
        assertTrue (isIgHVFlag, "Validate IgHV flag is true before test starts");
        // order 3
        Assay assayTest = ID_BCell2_IVD;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         gDNA,
                                                         BoneMarrow,
                                                         new String[] { c91_10 },
                                                         "Order 3 Flag On");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), "true", "true");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO3O4,
                           lastFinishedPipelineJobIdO3O4,
                           sampleNameO3O4,
                           null,
                           null);
        testLog ("step 9, order3 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10 - 1 - order3 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 10 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest, true);
        assertTrue (isCLIAIGHVFlagPresent);
        testLog ("step 11, order3 - CLIA-IGHV flag appears below the Report tab ");

        ReportRender reportData = getReportDataJsonFile ();
        assertNotNull (reportData.shmReportResult);
        testLog ("step 12, order3 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-6656:R1, R3, R4, R5, R6
     *                    NOTE: SR-T3689
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder4IVDFeatureFlagOn () {
        assertTrue (isIgHVFlag, "Validate IgHV flag is true before test starts");
        // order 4
        Assay assayTest = ID_BCell2_IVD;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c91_10 },
                                                         "Order 4 Flag On");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), "true", "true");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO3O4,
                           lastFinishedPipelineJobIdO3O4,
                           sampleNameO3O4,
                           null,
                           null);
        testLog ("step 9, order4 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10 - 1 - order4 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 10 - 2 - order4 - An eos.shm analysis job was spawned and Completed in portal");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest, true);
        assertTrue (isCLIAIGHVFlagPresent);
        testLog ("step 11, order3 - CLIA-IGHV flag appears below the Report tab ");

        ReportRender reportData = getReportDataJsonFile ();
        assertNotNull (reportData.shmReportResult);
        testLog ("step 12, order3 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-6656:R1, R3, R4, R5, R6
     *                    NOTE: SR-T3689
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder5CLIAFeatureFlagOn () {
        assertTrue (isIgHVFlag, "Validate IgHV flag is true before test starts");
        // order 5
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         ID_BCell2_CLIA,
                                                         FFPEScrolls,
                                                         LymphNode,
                                                         new String[] { c83_00, c91_10 },
                                                         "Order 6 Flag On");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), null, null);

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);
        testLog ("step 13, order5 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 14 - 1 - order5 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order5 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-6656:R1, R3, R4, R5, R6
     *                    NOTE: SR-T3689
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder6IVDFeatureFlagOn () {
        assertTrue (isIgHVFlag, "Validate IgHV flag is true before test starts");
        // order 6
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         ID_BCell2_CLIA,
                                                         CellSuspension,
                                                         BCells,
                                                         new String[] { c91_10 },
                                                         "Order 6 Flag On");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), null, null);

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);
        testLog ("step 13, order6 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 14 - 1 - order6 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order6 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-6656:R1, R3, R4, R5, R6
     *                    NOTE: SR-T3689
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder7IVDFeatureFlagOn () {
        assertTrue (isIgHVFlag, "Validate IgHV flag is true before test starts");
        // order 7
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         ID_BCell2_CLIA,
                                                         CellPellet,
                                                         PBMC,
                                                         new String[] { c90_00 },
                                                         "Order 7 Flag On");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), null, null);

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);
        testLog ("step 13, order7 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 14 - 1 - order7 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order7 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag OFF
     * 
     * @sdlc_requirements SR-6656:R7
     *                    NOTE: SR-T3689
     */
    @Test (groups = "featureFlagOff")
    public void verifyIgHVStageAndReportCLIAFeatureFlagOff () {
        assertFalse (isIgHVFlag, "Validate IgHV flag is false before test starts");
        // order 8
        Assay assayTest = ID_BCell2_CLIA;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         CellPellet,
                                                         PBMC,
                                                         new String[] { c83_00, c91_10 },
                                                         "Order 8 Flag Off");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), "false", "false");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);
        testLog ("step 15 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 16 - 1 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 16 - 2 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");

        boolean isCLIAIGHVFlagPresent = releaseReport (assayTest, true);
        assertFalse (isCLIAIGHVFlagPresent);
        testLog ("step 17 - CLIA-IGHV flag did not appear below the Report tab");

        ReportRender reportData = getReportDataJsonFile ();
        assertNull (reportData.shmReportResult);
        testLog ("step 18 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * @sdlc_requirements SR-7163:R1, R3, R4, SR-7029:R1
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder1 () {
        Assay assayTest = ID_BCell2_CLIA;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         CellPellet,
                                                         PBMC,
                                                         new String[] { c83_00 },
                                                         "Order 1 Orca Work");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO1O8,
                           lastFinishedPipelineOrcaIgHVO1O8,
                           sampleNameOrcaIgHVO1O8,
                           null,
                           null);
        testLog ("step 1 - order 1 - After SecondaryAnalysis stage, the workflow moved to the ShmAnalysis stage prior to ClonoSEQReport stage");

        releaseReport (assayTest, true);
        ReportRender reportData = getReportDataJsonFile ();

        assertTrue (Arrays.stream (ShmMutationStatus.values ())
                          .anyMatch ( (t) -> t.equals (reportData.shmReportResult.mutationStatus)));
        assertTrue (reportData.shmReportResult.shmSequenceList.size () >= 1);
        ShmSequence shmSequenceList = reportData.shmReportResult.shmSequenceList.get (0);
        assertNotNull (shmSequenceList.locus);
        assertNotNull (shmSequenceList.sequence);
        assertNotNull (shmSequenceList.percentMutation);
        assertNotNull (shmSequenceList.productive);
        assertNotNull (shmSequenceList.vSegment);
        testLog ("step 2 - order 1 - validate mutationStatus, shmSequenceListproperty of shmReportResult");
        testLog ("step 3 - order 1 - check test method below validateShmResultsTableSchema");

        String mutationStatus = reportData.shmReportResult.mutationStatus.toString ();
        validateShmResultReportType (orderDetails.get ("orderTestId"), mutationStatus);
        testLog ("step 4 - order 1 - validate DB report_type matches reportData.json shmReportResult.mutationStatus");

    }

    /**
     * @sdlc_requirements SR-7163:R3, SR-7028:R1
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder2 () {
        Assay assayTest = ID_BCell2_IVD;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c91_10 },
                                                         "Order 2 Orca Work");

        validateFlagsOnDebugPage (orderDetails.get ("sampleName"), null, null);

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO2O6O7,
                           lastFinishedPipelineOrcaIgHVO2O7,
                           sampleNameOrcaIgHVO2O6O7,
                           null,
                           null);

        validateShmResultReportType (orderDetails.get ("orderTestId"), ShmMutationStatus.UNMUTATED.toString ());
        testLog ("step 5 - order 2 - Value for orca.shm_results.report_type is UNMUTATED");
    }

    /**
     * @sdlc_requirements SR-7163:R3, SR-7028:R1
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder3 () {
        Assay assayTest = ID_BCell2_IVD;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c91_10 },
                                                         "Order 3 Orca Work");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO3,
                           lastFinishedPipelineOrcaIgHVO3,
                           sampleNameOrcaIgHVO3,
                           null,
                           null);

        validateShmResultReportType (orderDetails.get ("orderTestId"), ShmMutationStatus.INDETERMINATE.toString ());
        testLog ("step 6 - order 3 - Value for orca.shm_results.report_type is INDETERMINATE");
    }

    /**
     * @sdlc_requirements SR-7163:R3, SR-7028:R1
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder4 () {
        Assay assayTest = ID_BCell2_CLIA;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c83_00 },
                                                         "Order 4 Orca Work");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO4,
                           lastFinishedPipelineOrcaIgHVO4,
                           sampleNameOrcaIgHVO4,
                           null,
                           null);

        validateShmResultReportType (orderDetails.get ("orderTestId"), ShmMutationStatus.NO_CLONES.toString ());
        testLog ("step 7 - order 4 - Value for orca.shm_results.report_type is NO_CLONES");
    }

    /**
     * @sdlc_requirements SR-7163:R3, SR-7029:R1
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder5 () {
        Assay assayTest = ID_BCell2_IVD;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c91_10 },
                                                         "Order 5 Orca Work");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO5,
                           lastFinishedPipelineOrcaIgHVO5,
                           sampleNameOrcaIgHVO5,
                           null,
                           null);

        releaseReport (assayTest, false);

        String pdfUrl = diagnostic.getPreviewReportPdfUrl ();
        testLog ("PDF File URL: " + pdfUrl);
        String extractedText = getTextFromPDF (pdfUrl, 4, beginIghvMutationStatus, endThisSampleFailed);
        assertTrue (extractedText.contains (noResultsAvailable));
        testLog ("step 8 - order 5 - In SHM report of the pdf report, it is showing No Result Available for the IGHV Mutation Status");

        ReportRender reportData = getReportDataJsonFile ();
        assertEquals (reportData.shmReportResult.mutationStatus, ShmMutationStatus.QC_FAILURE);
        testLog ("step 9 - order 5 - mutationStatus property contains the value QC_FAILURE");

        validateShmResultReportType (orderDetails.get ("orderTestId"),
                                     ShmMutationStatus.QC_FAILURE.toString (),
                                     ShmMutationStatus.INDETERMINATE.toString ());
        testLog ("step 10.1 - order 5 - 1 DB record with orca.shm_results.report_type is QC_FAILURE");
        testLog ("step 10.2 - order 5 - 1 DB record with value of 'ericSampleCall' in orca.shm_results.shm_result is INDETERMINATE");
    }

    /**
     * @sdlc_requirements SR-7163:R3, SR-7029:R1
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder6 () {
        Assay assayTest = ID_BCell2_IVD;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c91_10 },
                                                         "Order 6 Orca Work");

        // debug page - get workflow properties
        history.gotoOrderDebug (orderDetails.get ("sampleName"));

        // set workflow property and force status update
        history.setWorkflowProperty (lastAcceptedTsvPath, tsvOverridePathOrcaIgHVO2O6O7);

        history.setWorkflowProperty (WorkflowProperty.lastFinishedPipelineJobId,
                                     lastFinishedPipelineOrcaIgHVO6);

        history.setWorkflowProperty (WorkflowProperty.sampleName, sampleNameOrcaIgHVO2O6O7);

        history.forceStatusUpdate (StageName.NorthQC, StageStatus.Failed);

        history.waitFor (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC);
        assertTrue (history.isStagePresent (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC));

        releaseReport (assayTest, false);
        String pdfUrl = diagnostic.getPreviewReportPdfUrl ();
        testLog ("PDF File URL: " + pdfUrl);
        String extractedText = getTextFromPDF (pdfUrl, 1, beginClonalityResult, endThisSampleFailed);
        assertTrue (extractedText.contains (noResultsAvailable));
        testLog ("step 11 - order 6 - Clonality Result for workflow with failed Primary Analysis (NorthQC) displays No Result Available");

        ReportRender reportData = getReportDataJsonFile ();
        assertEquals (reportData.shmReportResult.mutationStatus, ShmMutationStatus.QC_FAILURE);
        testLog ("step 12 - order 6 - mutationStatus property contains the value QC_FAILURE");

        validateQueryReturnsZeroRow (orderDetails.get ("orderTestId"));
        testLog ("step 13 - order 6 - Query has no rows returned");

    }

    /**
     * @sdlc_requirements SR-7163:R3, SR-7029:R1
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder7 () {
        Assay assayTest = ID_BCell2_IVD;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c91_10 },
                                                         "Order 7 Orca Work");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO2O6O7,
                           lastFinishedPipelineOrcaIgHVO2O7,
                           sampleNameOrcaIgHVO2O6O7,
                           null,
                           null);

        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        diagnostic.isOrderStatusPage ();
        diagnostic.clickReportTab (assayTest);

        diagnostic.setQCstatus (QC.Fail);

        history.gotoOrderDebug (orderDetails.get ("sampleName"));

        history.waitFor (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC);
        assertTrue (history.isStagePresent (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC));

        releaseReport (assayTest, true);
        String pdfUrl = diagnostic.getReleasedReportPdfUrl ();
        testLog ("PDF File URL: " + pdfUrl);
        String extractedText = getTextFromPDF (pdfUrl, 1, beginClonalityResult, endThisSampleFailed);
        assertTrue (extractedText.contains (noResultsAvailable));
        testLog ("step 14 - order 7 - Clonality Result for workflow with failed Clinical QC displays No Result Available");

        ReportRender reportData = getReportDataJsonFile ();
        assertEquals (reportData.shmReportResult.mutationStatus, ShmMutationStatus.QC_FAILURE);
        testLog ("step 15 - order 7 - mutationStatus property contains the value QC_FAILURE");

        validateShmResultReportType (orderDetails.get ("orderTestId"),
                                     ShmMutationStatus.UNMUTATED.toString ());
        testLog ("step 16 - order 7 - Value for orca.shm_results.report_type is UNMUTATED");
    }

    /**
     * @sdlc_requirements SR-7163:R1, R3, R4
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder8 () {
        Assay assayTest = ID_BCell2_CLIA;
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         CellPellet,
                                                         PBMC,
                                                         new String[] { c90_00 },
                                                         "Order 8 Orca Work");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO1O8,
                           lastFinishedPipelineOrcaIgHVO1O8,
                           sampleNameOrcaIgHVO1O8,
                           null,
                           null);
        validateShmAnalysisStagesDrillDownUrl (true);
        testLog ("step 17.1 - order 8 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 17.2 - order 8 - SHM Finished stage contains a message that SHM Analysis is not enabled for the workflow");

        releaseReport (assayTest, true);
        ReportRender reportData = getReportDataJsonFile ();
        assertNull (reportData.shmReportResult);
        testLog ("step 18 - order 8 - There is no shmReportResult, mutationStatus, or shmSequenceList properties in reportData.json");

        validateQueryReturnsZeroRow (orderDetails.get ("orderTestId"));
        testLog ("step 19 - order 8 - Query has no rows returned");
    }

    /**
     * @sdlc_requirements SR-7163:R1, R3, R4
     *                    NOTE: SR-T3728
     */
    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder9 () {
        Assay assayTest = ID_BCell2_CLIA;
        String passConsesusSeq = "TTCAGTAGACACGTCCATGAACCGCTTCTCCCTGCACATGACCTCTATGACTGCCGCAGACACGGCCCTGTATTATTGTGTCAGAGATGGACCCCCGGCGTTTTGGGGCCAGGGAACC";
        String lowBaseConsesusSeq = "CGGCCCAGTTTCATTGTGCGACAGACCCTTAATTTACATTGTGGTGGTGACTCCTATTGCGACTCTTGGGGCCTTGGAACC";
        Map <String, String> orderDetails = createOrder (IgHVPhysician,
                                                         assayTest,
                                                         Blood,
                                                         null,
                                                         new String[] { c83_00 },
                                                         "Order 9 Orca Work");

        forceStatusUpdate (orderDetails.get ("sampleName"),
                           tsvOverridePathOrcaIgHVO9,
                           null,
                           sampleNameOrcaIgHVO9,
                           shmDataSourcePathOrcaIgHVO9,
                           workSpaceNameOrcaIgHVO9);

        validateShmAnalysisStagesDrillDownUrl (false);
        testLog ("step 20 - order 9 - ShmAnalysis moved from Ready to Finished status, with SHM Analysis job spawned in portal");

        releaseReport (assayTest, true);
        ReportRender reportData = getReportDataJsonFile ();
        assertEquals (reportData.shmReportResult.shmSequenceList.get (0).sequence,
                      passConsesusSeq);
        assertTrue (reportData.shmReportResult.shmSequenceList.size () == 1);
        testLog ("step 21.1 - order 9 - There is only one SHM sequence in shmSequenceList for sequence " + passConsesusSeq + " clone in reportData.json");
        testLog ("step 21.2 - order 9 - There is no SHM sequence in shmSequenceList for sequence " + lowBaseConsesusSeq + " clone in reportData.json");

        List <Map <String, Object>> queryData = coraDBClient.executeSelectQuery (orderTestQuery.replace ("REPLACEORDERTESTID",
                                                                                                         orderDetails.get ("orderTestId")));
        testLog ("Query Data: " + queryData);
        assertTrue (queryData.size () == 1);
        Map <String, Object> firstRow = queryData.get (0);

        try {
            JSONObject jsonObject = new JSONObject (firstRow.get ("shm_result").toString ());
            JSONArray jsonArray = jsonObject.getJSONArray ("clones");
            assertTrue (jsonArray.length () == 2);

            Map <String, String> expectedMap = Stream.of (new String[][] {
                    { passConsesusSeq, "PASS" },
                    { lowBaseConsesusSeq, "LOW_BASE_COVERAGE" },
            }).collect (Collectors.toMap (data -> data[0], data -> data[1]));
            Map <String, String> actualMap = new HashMap <String, String> ();
            for (int i = 0; i < jsonArray.length (); i++) {
                actualMap.put (jsonArray.getJSONObject (i).getString ("consensusSequence"),
                               jsonArray.getJSONObject (i).getString ("failureFlag"));
            }
            assertEquals (actualMap, expectedMap);
            testLog ("step 22.1 - order 9 - The one with consensusSequence: " + passConsesusSeq + " had failureFlag: PASS");
            testLog ("step 22.2 - order 9 - The one with consensusSequence: " + lowBaseConsesusSeq + " had failureFlag: LOW_BASE_COVERAGE");
        } catch (JSONException e) {
            throw new RuntimeException (e);
        }

    }

    /**
     * @sdlc_requirements SR-7163:R1, R2, R3, R4, SR-7029:R1
     *                    NOTE: SR-T3728
     * 
     *                    validate shm_results table schema
     */
    @Test (groups = "orcaighv")
    private void validateShmResultsTableSchema () {
        String query = shmResultsSchema;
        testLog ("Query: " + query);

        List <Map <String, Object>> queryResult = coraDBClient.executeSelectQuery (query);
        testLog ("Query Data: " + queryResult);

        // table has 9 columns
        assertTrue (queryResult.size () == 9);

        for (int i = 0; i < queryResult.size (); i++) {
            Map <String, Object> rowMap = queryResult.get (i);
            testLog ("Column Details: " + rowMap);

            switch (Integer.parseInt (rowMap.get ("ordinal_position").toString ())) {
            case 1:
                validateColumnDetails ("id", "uuid_generate_v4()", "uuid", null, rowMap);
                break;
            case 2:
                validateColumnDetails ("order_test_id", null, "uuid", null, rowMap);
                break;
            case 3:
                validateColumnDetails ("report_type", null, "text", null, rowMap);
                break;
            case 4:
                validateColumnDetails ("shm_result", null, "jsonb", null, rowMap);
                break;
            case 5:
                validateColumnDetails ("iteration", "1", "int4", null, rowMap);
                break;
            case 6:
                validateColumnDetails ("created", "now()", "timestamp", null, rowMap);
                break;
            case 7:
                validateColumnDetails ("modified", "now()", "timestamp", null, rowMap);
                break;
            case 8:
                validateColumnDetails ("created_by", null, "varchar", "256", rowMap);
                break;
            case 9:
                validateColumnDetails ("modified_by", null, "varchar", "256", rowMap);
                break;
            }

        }
        testLog ("step 3 - order 1 - validate orca.shm_results table information schema");
    }

    /**
     * Create and activate order, return order number, sample name and order test id
     * 
     * @param physician
     *            Physician object
     * @param assayTest
     *            Assay Test
     * @param specimenType
     *            SpecimenType
     * @param specimenSource
     *            SpecimenSource
     * @param icdCodes
     *            ICD codes for the order
     * @param orderNotes
     *            order notes
     * @return
     */
    private Map <String, String> createOrder (Physician physician,
                                              Assay assayTest,
                                              SpecimenType specimenType,
                                              SpecimenSource specimenSource,
                                              String[] icdCodes,
                                              String orderNotes) {
        // create clonoSEQ diagnostic order
        billing.selectNewClonoSEQDiagnosticOrder ();
        billing.isCorrectPage ();

        billing.selectPhysician (physician);
        billing.createNewPatient (TestHelper.newInsurancePatient ());
        for (String icdCode : icdCodes)
            billing.enterPatientICD_Codes (icdCode);
        billing.selectBilling (InternalPharmaBilling);
        billing.clickSave ();

        // add specimen details for order
        specimen.enterSpecimenDelivery (CustomerShipment);
        specimen.clickEnterSpecimenDetails ();
        specimen.enterSpecimenType (specimenType);
        if (specimenSource != null)
            specimen.enterSpecimenSource (specimenSource);
        if (specimenType == SpecimenType.Blood)
            specimen.enterAntiCoagulant (Anticoagulant.EDTA);
        specimen.enterCollectionDate (DateUtils.getPastFutureDate (-3));
        specimen.enterOrderNotes (orderNotes);
        specimen.clickSave ();

        String orderNum = specimen.getOrderNum ();
        Logging.info ("Order Number: " + orderNum + ", Order Notes: " + orderNotes);

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        shipment.gotoAccession ();

        // accession complete
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();

        // activate order
        diagnostic.isCorrectPage ();
        diagnostic.clickAssayTest (assayTest);
        diagnostic.activateOrder ();

        Map <String, String> orderDetails = new HashMap <> ();
        orderDetails.put ("orderNumber", orderNum);
        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);
        orderDetails.put ("sampleName", sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();
        orderDetails.put ("orderTestId", orderTestId);

        return orderDetails;
    }

    /**
     * validate ighvReportEnabled and ighvAnalysisEnabled properties
     */
    private void validateFlagsOnDebugPage (String sampleName,
                                           String expectedIghvReportEnabled,
                                           String expectedIghvAnalysisEnabled) {
        // debug page - get workflow properties
        history.gotoOrderDebug (sampleName);
        Map <String, String> workflowProperties = history.getWorkflowProperties ();

        assertEquals (workflowProperties.get ("ighvReportEnabled"),
                      expectedIghvReportEnabled,
                      "Validate ighvReportEnabled property");
        assertEquals (workflowProperties.get ("ighvAnalysisEnabled"),
                      expectedIghvAnalysisEnabled,
                      "Validate ighvAnalysisEnabled property");

    }

    /**
     * navigate to order debug page using sampleName, override tsvOverridePath,
     * lastFinishedPipelineJobId, sampleNameOverride, shmDataSourcePathand and workspaceName. wait
     * for order to reach ClonoSEQReport stage
     * 
     * @param sampleName
     * @param tsvOverridePath
     * @param lastFinishedPipelineJobId
     * @param sampleNameOverride
     * @param shmDataSourcePath
     * @param workspaceName
     */
    private void forceStatusUpdate (String sampleName,
                                    String tsvPathOverride,
                                    String lastFinishedPipelineJobIdOverride,
                                    String sampleNameOverride,
                                    String shmDataSourcePathOverride,
                                    String workspaceNameOverride) {
        // go to debug page
        history.gotoOrderDebug (sampleName);

        // set workflow property and force status update
        history.setWorkflowProperty (lastAcceptedTsvPath, tsvPathOverride);
        if (lastFinishedPipelineJobIdOverride != null)
            history.setWorkflowProperty (WorkflowProperty.lastFinishedPipelineJobId,
                                         lastFinishedPipelineJobIdOverride);
        if (sampleNameOverride != null)
            history.setWorkflowProperty (WorkflowProperty.sampleName, sampleNameOverride);

        if (shmDataSourcePathOverride != null)
            history.setWorkflowProperty (WorkflowProperty.shmDataSourcePath,
                                         shmDataSourcePathOverride);
        if (workspaceNameOverride != null)
            history.setWorkflowProperty (WorkflowProperty.workspaceName, workspaceNameOverride);

        history.forceStatusUpdate (StageName.SecondaryAnalysis, Ready);

        history.waitFor (StageName.SecondaryAnalysis, Finished);
        assertTrue (history.isStagePresent (StageName.SecondaryAnalysis, Finished));

        history.waitFor (StageName.ShmAnalysis, Finished);
        assertTrue (history.isStagePresent (StageName.ShmAnalysis, Finished));

        history.waitFor (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC);
        assertTrue (history.isStagePresent (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC));

    }

    /**
     * validate that ShmAnalysis stage contains Ready and Finished stage status, and pipeline-portal
     * job is created or not depending on isDrilldownUrlNull (if true, drilldownUrl is not present,
     * else contains some value)
     * 
     * @param isDrilldownUrlNull
     */
    private void validateShmAnalysisStagesDrillDownUrl (boolean isDrilldownUrlNull) {
        List <Stage> stages = history.parseStatusHistory ();
        List <Stage> shmAnalysisStages = new LinkedList <> ();
        for (Stage stage : stages) {
            if (stage.stageName.equals (StageName.ShmAnalysis))
                shmAnalysisStages.add (stage);
        }

        if (isDrilldownUrlNull) {
            assertEquals (shmAnalysisStages.size (), 2);
            assertEquals (shmAnalysisStages.get (1).stageStatus, Ready);
            assertEquals (shmAnalysisStages.get (0).stageStatus, Finished);
            assertEquals (shmAnalysisStages.get (0).subStatusMessage, "SHM Analysis is not enabled for the workflow.");
            assertNull (shmAnalysisStages.get (0).drilldownUrl);
            assertNull (shmAnalysisStages.get (1).drilldownUrl);
        } else {
            assertEquals (shmAnalysisStages.size (), 3);
            assertEquals (shmAnalysisStages.get (2).stageStatus, Ready);
            assertEquals (shmAnalysisStages.get (1).stageStatus, Awaiting);
            assertEquals (shmAnalysisStages.get (0).stageStatus, Finished);
            assertNotNull (shmAnalysisStages.get (0).drilldownUrl);
            assertNotNull (shmAnalysisStages.get (1).drilldownUrl);
        }

    }

    /**
     * Validate CLIA-IGHV flag on Report if expectedCLIAIGHVFlag is not null, set QC status to pass
     * if setQCStatus is true, and return
     * 
     * @param assayTest
     * @param expectedCLIAIGHVFlag
     * @param setQCStatus
     * @return
     */
    private boolean releaseReport (Assay assayTest,
                                   boolean setQCStatus) {
        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        diagnostic.isOrderStatusPage ();
        diagnostic.clickReportTab (assayTest);
        boolean isCLIAIGHVFlagPresent = diagnostic.isCLIAIGHVBtnVisible ();

        if (setQCStatus) {
            diagnostic.setQCstatus (QC.Pass);
            diagnostic.releaseReport ();
        }
        return isCLIAIGHVFlagPresent;
    }

    /**
     * go to debug page, parse reportData.json file
     * 
     * @return ReportRender object for reportData.json file
     */
    private ReportRender getReportDataJsonFile () {
        // validate reportData.json file
        diagnostic.clickOrderDetailsTab ();
        diagnostic.isCorrectPage ();
        String sampleName = diagnostic.getSampleName ();
        testLog ("Sample Name: " + sampleName);
        history.gotoOrderDebug (sampleName);

        // get file using get request
        doCoraLogin ();
        ReportRender reportDataJson = null;
        try {
            String fileUrl = history.getFileUrl ("reportData.json");
            testLog ("File URL: " + fileUrl);
            String getResponse = get (fileUrl);
            testLog ("File URL Response: " + getResponse);
            reportDataJson = mapper.readValue (getResponse, ReportRender.class);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
        testLog ("Json File Data " + reportDataJson);
        HttpClientHelper.resetheaders ();
        return reportDataJson;
    }

    /**
     * validate pipeline portal job is completed
     * 
     * @param sampleName
     * @param assayTest
     */
    private void validatePipelineStatusToComplete (String sampleName, Assay assayTest) {
        HttpClientHelper.headers.get ().add (new BasicHeader ("Authorization", portalTestAuth));
        // pipeline portal url and end-point
        String url = assayTest.equals (ID_BCell2_IVD) ? portalIvdTestUrl : portalCliaTestUrl;
        String endpoint = "/flowcells?page=0&pageSize=1&select=id&samples.reactions.configuration.name=eos.shm&samples.name=" + sampleName + "&job.statuses.status=COMPLETED";
        testLog ("URL: " + url + endpoint);

        JSONArray response;
        try {
            response = new JSONArray (get (url + endpoint));
            testLog ("Response: " + response);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
        HttpClientHelper.resetheaders ();
        assertEquals (response.length (), 1, "Validate pipeline portal job is completed");
    }

    /**
     * validate shm_result information schema details
     * 
     * @param colName
     * @param colDefault
     * @param colDataType
     * @param charMaxLength
     * @param rowMap
     */
    private void validateColumnDetails (String colName, String colDefault, String colDataType, String charMaxLength,
                                        Map <String, Object> rowMap) {
        assertEquals (rowMap.get ("table_catalog"), "coradb", "Validate table catalog");
        assertEquals (rowMap.get ("table_schema"), "orca", "Validate table schema");
        assertEquals (rowMap.get ("table_name"), "shm_results", "Validate table name");
        assertEquals (rowMap.get ("column_name"), colName, "Validate first column is " + colName);
        assertEquals (rowMap.get ("column_default"), colDefault, "Validate column_default for " + colName);
        assertEquals (rowMap.get ("udt_name"), colDataType, "Validate udt_name for " + colName);
        assertEquals (String.valueOf (rowMap.get ("character_maximum_length")),
                      String.valueOf (charMaxLength),
                      "Validate max_length for " + colName);
    }

    /**
     * validate report_type of orderTestId in DB is the same as mutationStatus
     * 
     * @param orderTestId
     * @param mutationStatus
     */
    private void validateShmResultReportType (String orderTestId, String mutationStatus) {
        validateShmResultReportType (orderTestId, mutationStatus, null);
    }

    /**
     * validate report_type and ericSampleCall of orderTestId in DB is the same as mutationStatus
     * and ericSampleCall respectively
     * 
     * @param orderTestId
     * @param mutationStatus
     * @param ericSampleCall
     */
    private void validateShmResultReportType (String orderTestId, String mutationStatus,
                                              String ericSampleCall) {
        String query = orderTestQuery.replace ("REPLACEORDERTESTID", orderTestId);
        testLog ("Query: " + query);

        List <Map <String, Object>> queryData = coraDBClient.executeSelectQuery (query);
        testLog ("Query Data: " + queryData);

        assertTrue (queryData.size () == 1);
        Map <String, Object> firstRow = queryData.get (0);
        testLog ("Frist Row Data: " + firstRow);
        assertEquals (mutationStatus, firstRow.get ("report_type"));

        if (ericSampleCall != null) {
            String actualEricSampleCall = null;
            try {
                JSONObject jsonObject = new JSONObject (firstRow.get ("shm_result").toString ());
                actualEricSampleCall = String.valueOf (jsonObject.get ("ericSampleCall"));
            } catch (JSONException e) {
                throw new RuntimeException (e);
            }
            assertEquals (actualEricSampleCall, ericSampleCall);
        }

    }

    /**
     * Validate query returns zero row for given order test id
     * 
     * @param orderTestId
     */
    private void validateQueryReturnsZeroRow (String orderTestId) {
        String query = orderTestQuery.replace ("REPLACEORDERTESTID", orderTestId);
        testLog ("Query: " + query);

        List <Map <String, Object>> queryData = coraDBClient.executeSelectQuery (query);
        testLog ("Query Result: " + queryData);

        assertTrue (queryData.size () == 0);
    }

    /**
     * get file from URL, read pageNumber, and return extracted text from beginText and endText
     * 
     * @param url
     * @param pageNumber
     * @param beginText
     * @param endText
     * @return
     */
    private String getTextFromPDF (String url, int pageNumber, String beginText, String endText) {
        String pdfFileLocation = join ("/", downloadDir, UUID.randomUUID () + ".pdf");
        testLog ("PDF File Location: " + pdfFileLocation);

        // get file from URL and save it
        doCoraLogin ();
        headers.set (new ArrayList <> ());
        headers.get ().add (new BasicHeader ("Connection", "keep-alive"));
        get (url, new File (pdfFileLocation));
        resetheaders ();

        // read PDF and extract text
        PdfReader reader = null;
        String extractedText = null;
        try {
            reader = new PdfReader (pdfFileLocation);
            String fileContent = PdfTextExtractor.getTextFromPage (reader, pageNumber);
            testLog ("File Content: " + fileContent);

            int beginIndex = fileContent.indexOf (beginText);
            int endIndex = fileContent.indexOf (endText);
            extractedText = fileContent.substring (beginIndex, endIndex);
            testLog ("Extracted Text: " + extractedText);
        } catch (IOException e) {
            throw new RuntimeException (e);
        } finally {
            reader.close ();
        }
        return extractedText;
    }

}
