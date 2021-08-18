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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.seleniumfy.test.utils.HttpClientHelper;

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

    @BeforeMethod
    public void beforeMethod () {

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
        createOrder (IgHVPhysician,
                     assayTest,
                     CellPellet,
                     PBMC,
                     new String[] { c83_00, c91_10 },
                     "Order 1 Flag On");
        forceStatusUpdate (tsvOverridePathO1O2,
                           lastFinishedPipelineJobIdO1O2,
                           sampleNameO1O2,
                           "true",
                           "true");
        testLog ("step 1 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 2 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 2 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (assayTest, true, true);
        testLog ("step 3 - CLIA-IGHV flag appears just below the Report tab ");
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
        createOrder (NYPhysician,
                     assayTest,
                     CellPellet,
                     PBMC,
                     new String[] { c83_00 },
                     "Order 2 Flag On");
        forceStatusUpdate (tsvOverridePathO1O2,
                           lastFinishedPipelineJobIdO1O2,
                           sampleNameO1O2,
                           "false",
                           "true");
        testLog ("step 5 - ighvAnalysisEnabled is true, ighvReportEnabled is false (or absent)");
        testLog ("step 6 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 6 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (assayTest, false, false);
        testLog ("step 7 - CLIA-IGHV flag does not appear below the Report tab ");
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
        createOrder (IgHVPhysician,
                     assayTest,
                     gDNA,
                     BoneMarrow,
                     new String[] { c91_10 },
                     "Order 3 Flag On");
        forceStatusUpdate (tsvOverridePathO3O4,
                           lastFinishedPipelineJobIdO3O4,
                           sampleNameO3O4,
                           "true",
                           "true");
        testLog ("step 9, order3 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10 - 1 - order3 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 10 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (assayTest, true, true);
        testLog ("step 11, order3 - CLIA-IGHV flag does not appear below the Report tab ");
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
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c91_10 },
                     "Order 4 Flag On");
        forceStatusUpdate (tsvOverridePathO3O4,
                           lastFinishedPipelineJobIdO3O4,
                           sampleNameO3O4,
                           "true",
                           "true");
        testLog ("step 9, order4 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10 - 1 - order4 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        validatePipelineStatusToComplete (history.getWorkflowProperties ().get ("sampleName"), assayTest);
        testLog ("step 10 - 2 - order4 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (assayTest, true, true);
        testLog ("step 11, order3 - CLIA-IGHV flag does not appear below the Report tab ");
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
        createOrder (IgHVPhysician,
                     ID_BCell2_CLIA,
                     FFPEScrolls,
                     LymphNode,
                     new String[] { c83_00, c91_10 },
                     "Order 6 Flag On");
        forceStatusUpdate (tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);
        testLog ("step 13, order5 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled ();
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
        createOrder (IgHVPhysician,
                     ID_BCell2_CLIA,
                     CellSuspension,
                     BCells,
                     new String[] { c91_10 },
                     "Order 6 Flag On");
        forceStatusUpdate (tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);
        testLog ("step 13, order6 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled ();
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
        createOrder (IgHVPhysician,
                     ID_BCell2_CLIA,
                     CellPellet,
                     PBMC,
                     new String[] { c90_00 },
                     "Order 7 Flag On");
        forceStatusUpdate (tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           null,
                           null);
        testLog ("step 13, order7 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled ();
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
        createOrder (IgHVPhysician,
                     assayTest,
                     CellPellet,
                     PBMC,
                     new String[] { c83_00, c91_10 },
                     "Order 8 Flag Off");
        forceStatusUpdate (tsvOverridePathO5O6O7O8,
                           null,
                           null,
                           "false",
                           "false");
        testLog ("step 15 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled ();
        testLog ("step 16 - 1 - ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 16 - 2 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");

        releaseReport (assayTest, false, false);
        testLog ("step 17 - CLIA-IGHV flag did not appear below the Report tab");
        testLog ("step 18 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder1 () {
        Assay assayTest = ID_BCell2_CLIA;
        createOrder (IgHVPhysician,
                     assayTest,
                     CellPellet,
                     PBMC,
                     new String[] { c83_00 },
                     "Order 1 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO1O8,
                                   lastFinishedPipelineOrcaIgHVO1O8,
                                   sampleNameOrcaIgHVO1O8,
                                   null,
                                   null);

        releaseReport (assayTest, true);

        // TODO skip step 3
        // TODO skip step 4

    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder2 () {
        Assay assayTest = ID_BCell2_IVD;
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c91_10 },
                     "Order 2 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO2O6O7,
                                   lastFinishedPipelineOrcaIgHVO2O7,
                                   sampleNameOrcaIgHVO2O6O7,
                                   null,
                                   null);

        // TODO skip step 5.7

    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder3 () {
        Assay assayTest = ID_BCell2_IVD;
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c91_10 },
                     "Order 3 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO3,
                                   lastFinishedPipelineOrcaIgHVO3,
                                   sampleNameOrcaIgHVO3,
                                   null,
                                   null);

        // TODO skip step 6.7

    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder4 () {
        Assay assayTest = ID_BCell2_CLIA;
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c83_00 },
                     "Order 4 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO4,
                                   lastFinishedPipelineOrcaIgHVO4,
                                   sampleNameOrcaIgHVO4,
                                   null,
                                   null);

        // TODO skip step 4.7

    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder5 () {
        Assay assayTest = ID_BCell2_IVD;
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c91_10 },
                     "Order 5 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO5,
                                   lastFinishedPipelineOrcaIgHVO5,
                                   sampleNameOrcaIgHVO5,
                                   null,
                                   null);

        releaseReport (assayTest, false);

        // TODO skip step 10

    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder6 () {
        Assay assayTest = ID_BCell2_IVD;
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c91_10 },
                     "Order 6 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO2O6O7,
                                   lastFinishedPipelineOrcaIgHVO6,
                                   sampleNameOrcaIgHVO2O6O7,
                                   null,
                                   null);

        releaseReport (assayTest, false);

        // TODO skip step 9, 10

    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder7 () {
        Assay assayTest = ID_BCell2_IVD;
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c91_10 },
                     "Order 7 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
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

        history.gotoOrderDebug (sampleName);

        history.waitFor (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC);
        assertTrue (history.isStagePresent (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC));

        releaseReport (assayTest, true);

        // TODO skip step 15, 16
    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder8 () {
        Assay assayTest = ID_BCell2_CLIA;
        createOrder (IgHVPhysician,
                     assayTest,
                     CellPellet,
                     PBMC,
                     new String[] { c90_00 },
                     "Order 8 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO1O8,
                                   lastFinishedPipelineOrcaIgHVO1O8,
                                   sampleNameOrcaIgHVO1O8,
                                   null,
                                   null);

        releaseReport (assayTest, true);

        // TODO skip step 18, 19
    }

    @Test (groups = "orcaighv")
    public void verifyOrcaForIgHVOrder9 () {
        Assay assayTest = ID_BCell2_CLIA;
        createOrder (IgHVPhysician,
                     assayTest,
                     Blood,
                     null,
                     new String[] { c83_00 },
                     "Order 9 Orca Work");

        String sampleName = diagnostic.getSampleName ();
        Logging.info (sampleName);

        orderStatus.clickOrderStatusTab ();
        orderStatus.isOrderStatusPage ();
        orderStatus.expandWorkflowHistory ();
        String orderTestId = orderStatus.getOrderTestIdFromUrl ();

        forceStatusUpdateOrcaIgHV (sampleName,
                                   tsvOverridePathOrcaIgHVO9,
                                   null,
                                   sampleNameOrcaIgHVO9,
                                   shmDataSourcePathOrcaIgHVO9,
                                   workSpaceNameOrcaIgHVO9);

        releaseReport (assayTest, true);

        // TODO skip step 18, 19
    }

    /**
     * Create and activate order
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
     */
    private void createOrder (Physician physician,
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
    }

    /**
     * navigate to order debug page and force order status update
     * validate ighvReportEnabled and ighvAnalysisEnabled properties
     * 
     * @param tsvOverridePath
     * @param lastFinishedPipelineJobId
     * @param sampleName
     * @param expectedIghvReportEnabled
     * @param expectedIghvAnalysisEnabled
     */
    private void forceStatusUpdate (String tsvOverridePath,
                                    String lastFinishedPipelineJobId,
                                    String sampleName,
                                    String expectedIghvReportEnabled,
                                    String expectedIghvAnalysisEnabled) {
        // debug page - get workflow properties
        history.gotoOrderDebug (diagnostic.getSampleName ());
        Map <String, String> workflowProperties = history.getWorkflowProperties ();

        assertEquals (workflowProperties.getOrDefault ("ighvReportEnabled", null),
                      expectedIghvReportEnabled,
                      "Validate ighvReportEnabled property");
        assertEquals (workflowProperties.getOrDefault ("ighvAnalysisEnabled", null),
                      expectedIghvAnalysisEnabled,
                      "Validate ighvAnalysisEnabled property");

        // set workflow property and force status update
        history.setWorkflowProperty (lastAcceptedTsvPath, tsvOverridePath);
        if (lastFinishedPipelineJobId != null)
            history.setWorkflowProperty (WorkflowProperty.lastFinishedPipelineJobId,
                                         lastFinishedPipelineJobId);
        if (sampleName != null)
            history.setWorkflowProperty (WorkflowProperty.sampleName, sampleName);

        history.forceStatusUpdate (StageName.SecondaryAnalysis, Ready);

        history.waitFor (StageName.SecondaryAnalysis, Finished);
        assertTrue (history.isStagePresent (StageName.SecondaryAnalysis, Finished));

        history.waitFor (StageName.ShmAnalysis, Finished);
        assertTrue (history.isStagePresent (StageName.ShmAnalysis, Finished));

        history.waitFor (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC);
        assertTrue (history.isStagePresent (StageName.ClonoSEQReport, Awaiting, CLINICAL_QC));

    }

    private void forceStatusUpdateOrcaIgHV (String sampleName,
                                            String tsvOverridePath,
                                            String lastFinishedPipelineJobId,
                                            String sampleNameOverride,
                                            String shmDataSourcePath,
                                            String workspaceName) {
        // debug page - get workflow properties
        history.gotoOrderDebug (sampleName);

        // set workflow property and force status update
        history.setWorkflowProperty (lastAcceptedTsvPath, tsvOverridePath);
        if (lastFinishedPipelineJobId != null)
            history.setWorkflowProperty (WorkflowProperty.lastFinishedPipelineJobId,
                                         lastFinishedPipelineJobId);
        if (sampleNameOverride != null)
            history.setWorkflowProperty (WorkflowProperty.sampleName, sampleNameOverride);

        if (shmDataSourcePath != null)
            history.setWorkflowProperty (WorkflowProperty.shmDataSourcePath,
                                         shmDataSourcePath);
        if (workspaceName != null)
            history.setWorkflowProperty (WorkflowProperty.workspaceName, workspaceName);

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
     * job is not created
     * 
     */
    private void validateShmAnalysisNotEnabled () {
        List <Stage> stages = history.parseStatusHistory ();
        List <Stage> shmAnalysisStages = new LinkedList <> ();
        for (Stage stage : stages) {
            if (stage.stageName.equals (StageName.ShmAnalysis))
                shmAnalysisStages.add (stage);
        }
        assertEquals (shmAnalysisStages.size (), 2);
        assertEquals (shmAnalysisStages.get (1).stageStatus, Ready);
        assertEquals (shmAnalysisStages.get (0).stageStatus, Finished);
        assertEquals (shmAnalysisStages.get (0).subStatusMessage, "SHM Analysis is not enabled for the workflow.");
        assertNull (shmAnalysisStages.get (0).drilldownUrl);
        assertNull (shmAnalysisStages.get (1).drilldownUrl);
    }

    /**
     * Validate CLIA-IGHV flag on Report page and shmReportResult property on reportData.json file
     * 
     * @param assayTest
     * @param expectedCLIAIGHVFlag
     * @param expectedShmReportKey
     */
    private void releaseReport (Assay assayTest,
                                boolean expectedCLIAIGHVFlag,
                                boolean expectedShmReportKey) {
        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        diagnostic.isOrderStatusPage ();
        diagnostic.clickReportTab (assayTest);
        assertEquals (diagnostic.isCLIAIGHVBtnVisible (),
                      expectedCLIAIGHVFlag,
                      "Validate CLIA-IGHV flag");

        diagnostic.setQCstatus (QC.Pass);
        diagnostic.releaseReport ();

        // validate reportData.json file
        diagnostic.clickOrderDetailsTab ();
        diagnostic.isCorrectPage ();
        String sampleName = diagnostic.getSampleName ();
        testLog ("Sample Name: " + sampleName);
        history.gotoOrderDebug (sampleName);

        // get file using get request
        doCoraLogin ();
        Map <String, Object> reportData = null;
        try {
            String fileUrl = history.getFileUrl ("reportData.json");
            testLog ("File URL: " + fileUrl);
            String getResponse = get (fileUrl);
            testLog ("File URL Response: " + getResponse);
            reportData = mapper.readValue (getResponse,
                                           new TypeReference <Map <String, Object>> () {});
        } catch (Exception e) {
            e.printStackTrace ();
        }
        testLog ("Json File Data " + reportData);
        HttpClientHelper.resetheaders ();
        assertEquals (reportData.containsKey ("shmReportResult"), expectedShmReportKey);
    }

    public void releaseReport (Assay assayTest, boolean isQCStatus) {
        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        diagnostic.isOrderStatusPage ();
        diagnostic.clickReportTab (assayTest);

        if (isQCStatus) {
            diagnostic.setQCstatus (QC.Pass);
            diagnostic.releaseReport ();
        }

        // validate reportData.json file
        diagnostic.clickOrderDetailsTab ();
        diagnostic.isCorrectPage ();
        String sampleName = diagnostic.getSampleName ();
        testLog ("Sample Name: " + sampleName);
        history.gotoOrderDebug (sampleName);

        // get file using get request
        doCoraLogin ();
        Map <String, Object> reportData = null;
        try {
            String fileUrl = history.getFileUrl ("reportData.json");
            testLog ("File URL: " + fileUrl);
            String getResponse = get (fileUrl);
            testLog ("File URL Response: " + getResponse);
            reportData = mapper.readValue (getResponse,
                                           new TypeReference <Map <String, Object>> () {});
        } catch (Exception e) {
            e.printStackTrace ();
        }
        testLog ("Json File Data " + reportData);
        HttpClientHelper.resetheaders ();
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

}
