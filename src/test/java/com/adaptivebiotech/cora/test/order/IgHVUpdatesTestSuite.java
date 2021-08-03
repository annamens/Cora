package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.cora.test.CoraEnvironment.pipelinePortalTestPass;
import static com.adaptivebiotech.cora.test.CoraEnvironment.pipelinePortalTestUser;
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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.apache.http.message.BasicHeader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Workflow.Stage;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.workflow.FeatureFlags;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.pipeline.dto.Flowcells.Flowcell;
import com.adaptivebiotech.pipeline.dto.Status;
import com.adaptivebiotech.pipeline.utils.PageHelper.JobStatus;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.seleniumfy.test.utils.HttpClientHelper;
import com.seleniumfy.test.utils.Timeout;

public class IgHVUpdatesTestSuite extends CoraBaseBrowser {

    private Physician            IgHVPhysician;
    private Physician            NYPhysician;

    private final String         c91_10                        = "C91.10";
    private final String         c83_00                        = "C83.00";
    private final String         c90_00                        = "C90.00";

    private final String         tsvOverridePathO1O2           = "s3://pipeline-north-production-archive:us-west-2/210612_NB552467_0088_AH3CH7BGXJ/v3.1/20210614_0809/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev32/H3CH7BGXJ_0_CLINICAL-CLINICAL_96343-05BC.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathO3O4           = "s3://pipeline-fda-production-archive:us-west-2/210615_NB551732_0294_AH3G53BGXJ/v3.1/20210617_0828/packaged/rd.Human.BCell.nextseq.146x13x116.threeRead.ultralight.rev24/H3G53BGXJ_0_CLINICAL-CLINICAL_96633-08MC-UA001BM.adap.txt.results.tsv.gz";
    private final String         tsvOverridePathO5O6O7O8       = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";
    private final String         lastFinishedPipelineJobIdO1O2 = "8a7a94db77a26ee1017a01c874c67394";
    private final String         lastFinishedPipelineJobIdO3O4 = "8a7a958877a26e74017a176ecd2b1b45";
    private final String         sampleNameO1O2                = "96343-05BC";
    private final String         sampleNameO3O4                = "96633-08MC-UA001BM";

    private Map <String, String> featureFlags;

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
        FeatureFlags featureFlagsPage = new FeatureFlags ();
        featureFlagsPage.navigateToFeatureFlagsPage ();
        featureFlags = featureFlagsPage.getFeatureFlags ();

    }

    public void validateIgHVFeatureFlagOn () {
        assertTrue (Boolean.valueOf (featureFlags.get ("IgHV")),
                    "Validate IgHV flag is true before test starts");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder1FlagOn () {
        validateIgHVFeatureFlagOn ();
        // order 1
        createOrder (IgHVPhysician,
                     CellPellet,
                     PBMC,
                     new String[] { c83_00, c91_10 },
                     "Order 1 Flag On");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO1O2,
                                                 lastFinishedPipelineJobIdO1O2,
                                                 sampleNameO1O2,
                                                 "true",
                                                 "true");
        testLog ("step 1 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 2 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        waitForPipelineStatusToComplete (stages);
        testLog ("step 2 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (true, true);
        testLog ("step 3 - CLIA-IGHV flag appears just below the Report tab ");
        testLog ("step 4 - SHM analysis results are included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder2FlagOn () {
        validateIgHVFeatureFlagOn ();
        // order 2
        createOrder (NYPhysician,
                     CellPellet,
                     PBMC,
                     new String[] { c83_00, c91_10 },
                     "Order 2 Flag On");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO1O2,
                                                 lastFinishedPipelineJobIdO1O2,
                                                 sampleNameO1O2,
                                                 "false",
                                                 "true");
        testLog ("step 5 - ighvAnalysisEnabled is true, ighvReportEnabled is false (or absent)");
        testLog ("step 6 - 1 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        waitForPipelineStatusToComplete (stages);
        testLog ("step 6 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (false, false);
        testLog ("step 7 - CLIA-IGHV flag does not appear below the Report tab ");
        testLog ("step 8 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder3FlagOn () {
        validateIgHVFeatureFlagOn ();
        // order 3
        createOrder (IgHVPhysician,
                     gDNA,
                     BoneMarrow,
                     new String[] { c83_00, c91_10 },
                     "Order 3 Flag On");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO3O4,
                                                 lastFinishedPipelineJobIdO3O4,
                                                 sampleNameO3O4,
                                                 "true",
                                                 "true");
        testLog ("step 9, order3 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10 - 1 - order3 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        waitForPipelineStatusToComplete (stages);
        testLog ("step 10 - 2 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (true, true);
        testLog ("step 11, order3 - CLIA-IGHV flag does not appear below the Report tab ");
        testLog ("step 12, order3 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder4FlagOn () {
        validateIgHVFeatureFlagOn ();
        // order 4
        createOrder (IgHVPhysician,
                     Blood,
                     null,
                     new String[] { c83_00, c91_10 },
                     "Order 4 Flag On");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO3O4,
                                                 lastFinishedPipelineJobIdO3O4,
                                                 sampleNameO3O4,
                                                 "true",
                                                 "true");
        testLog ("step 9, order4 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10 - 1 - order4 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        waitForPipelineStatusToComplete (stages);
        testLog ("step 10 - 2 - order4 - An eos.shm analysis job was spawned and Completed in portal");

        releaseReport (true, true);
        testLog ("step 11, order3 - CLIA-IGHV flag does not appear below the Report tab ");
        testLog ("step 12, order3 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder5FlagOn () {
        validateIgHVFeatureFlagOn ();
        // order 5
        createOrder (IgHVPhysician,
                     FFPEScrolls,
                     LymphNode,
                     new String[] { c83_00, c91_10 },
                     "Order 6 Flag On");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO5O6O7O8,
                                                 null,
                                                 null,
                                                 null,
                                                 null);
        testLog ("step 13, order5 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled (stages);
        testLog ("step 14 - 1 - order5 - ​ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order5 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder6FlagOn () {
        validateIgHVFeatureFlagOn ();
        // order 6
        createOrder (IgHVPhysician,
                     CellSuspension,
                     BCells,
                     new String[] { c83_00, c91_10 },
                     "Order 6 Flag On");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO5O6O7O8,
                                                 null,
                                                 null,
                                                 null,
                                                 null);
        testLog ("step 13, order6 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled (stages);
        testLog ("step 14 - 1 - order6 - ​ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order6 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");

    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureOrder7FlagOn () {
        validateIgHVFeatureFlagOn ();
        // order 7
        createOrder (IgHVPhysician,
                     CellPellet,
                     PBMC,
                     new String[] { c90_00 },
                     "Order 7 Flag On");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO5O6O7O8,
                                                 null,
                                                 null,
                                                 null,
                                                 null);
        testLog ("step 13, order7 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled (stages);
        testLog ("step 14 - 1 - order7 - ​ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 14 - 2 - order7 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");
    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag OFF
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R7
     */
    @Test (groups = "featureFlagOff")
    public void verifyIgHVStageAndReportFeatureFlagOff () {
        assertFalse (Boolean.valueOf (featureFlags.get ("IgHV")),
                     "Validate IgHV flag is true before test starts");
        // order 8
        createOrder (IgHVPhysician,
                     CellPellet,
                     PBMC,
                     new String[] { c83_00, c91_10 },
                     "Order 8 Flag Off");
        List <Stage> stages = forceStatusUpdate (tsvOverridePathO5O6O7O8,
                                                 null,
                                                 null,
                                                 "false",
                                                 "false");
        testLog ("step 15 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");

        validateShmAnalysisNotEnabled (stages);
        testLog ("step 16 - 1 - ​ShmAnalysis moved from Ready to Finished status, with no SHM Analysis job spawned in portal");
        testLog ("step 16 - 2 - SHM Finished stage contains message that SHM Analysis is not enabled for the workflow");

        releaseReport (false, false);
        testLog ("step 17 - CLIA-IGHV flag did not appear below the Report tab");
        testLog ("step 18 - SHM analysis results are not included in reportData.json within shmReportResult property");
    }

    /**
     * Create and activate order
     * 
     * @param physician
     *            Physician object
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
                              SpecimenType specimenType,
                              SpecimenSource specimenSource,
                              String[] icdCodes,
                              String orderNotes) {
        // create clonoSEQ diagnostic order
        Billing billing = new Billing ();
        billing.selectNewClonoSEQDiagnosticOrder ();
        billing.isCorrectPage ();

        billing.selectPhysician (physician);
        billing.createNewPatient (TestHelper.newInsurancePatient ());
        for (String icdCode : icdCodes)
            billing.enterPatientICD_Codes (icdCode);

        billing.clickAssayTest (ID_BCell2_IVD);
        billing.selectBilling (InternalPharmaBilling);
        billing.clickSave ();

        // add specimen details for order
        Specimen specimen = new Specimen ();
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
        Logging.info ("Order Number: " + orderNum);

        // add diagnostic shipment
        Shipment shipment = new Shipment ();
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        shipment.selectDiagnosticSpecimenContainerType (Tube);
        shipment.clickSave ();
        shipment.gotoAccession ();

        // accession complete
        Accession accession = new Accession ();
        accession.isCorrectPage ();
        accession.clickIntakeComplete ();
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        accession.gotoOrderDetail ();

        // activate order
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isCorrectPage ();
        doWait (2000);
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
     * @return
     */
    private List <Stage> forceStatusUpdate (String tsvOverridePath,
                                            String lastFinishedPipelineJobId,
                                            String sampleName,
                                            String expectedIghvReportEnabled,
                                            String expectedIghvAnalysisEnabled) {
        // debug page - get workflow properties
        History history = new History ();
        history.gotoOrderDebug (new Diagnostic ().getSampleName ());
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

        return history.parseStatusHistory ();

    }

    /**
     * validate that ShmAnalysis stage contains Ready and Finished stage status, and pipeline-portal
     * job is not created
     * 
     * @param stages
     */
    private void validateShmAnalysisNotEnabled (List <Stage> stages) {
        List <Stage> shmAnalysisStages = new ArrayList <> ();
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
     * @param expectedCLIAIGHVFlag
     * @param expectedShmReportKey
     */
    private void releaseReport (boolean expectedCLIAIGHVFlag, boolean expectedShmReportKey) {
        History history = new History ();
        history.isCorrectPage ();
        history.clickOrderTest ();

        // navigate to order status page
        Diagnostic diagnostic = new Diagnostic ();
        diagnostic.isOrderStatusPage ();
        diagnostic.clickReportTab (ID_BCell2_CLIA);
        assertEquals (diagnostic.isCLIAIGHVBtnPresent (),
                      expectedCLIAIGHVFlag,
                      "Validate CLIA-IGHV flag");

        diagnostic.setQCstatus (QC.Pass);
        diagnostic.releaseReport ();

        // validate reportData.json file
        history.gotoOrderDebug (diagnostic.getSampleName ());

        Map <String, Object> reportData = null;
        try {
            reportData = mapper.readValue (new FileInputStream (history.downloadFile ("reportData.json")),
                                           new TypeReference <Map <String, Object>> () {});
        } catch (Exception e) {
            e.printStackTrace ();
        }
        testLog ("Json File Data " + reportData.toString ());
        assertEquals (reportData.containsKey ("shmReportResult"), expectedShmReportKey);
    }

    /**
     * Wait for pipeline portal job to complete and validate job is completed
     * 
     * @param stages
     */
    private void waitForPipelineStatusToComplete (List <Stage> stages) {
        // get flowCellId from drillDown link, next to shmAnalysis stage
        String drillDownUrl = null;
        for (Stage stage : stages) {
            testLog ("Checking Stage: " + stage.toString ());
            if (stage.drilldownUrl != null && stage.drilldownUrl.contains ("qcsummary")) {
                testLog ("ShmAnalysis stage found: " + stage.toString ());
                drillDownUrl = stage.drilldownUrl;
            }
        }

        String flowCellId = drillDownUrl.replace ("/qcsummary", "").split ("flowcells/", 2)[1];
        testLog ("flow cell id: " + flowCellId);

        // check every 30 seconds for max 30 minutes
        Timeout timer = new Timeout (1800000, 30000);

        // pipeline portal header and end-point
        byte[] authBytes = (pipelinePortalTestUser + ":" + pipelinePortalTestPass).getBytes ();
        String portalTestAuth = "Basic " + Base64.getEncoder ().encodeToString (authBytes);

        HttpClientHelper.headers.get ().add (new BasicHeader ("Authorization", portalTestAuth));

        String jobId;
        try {
            jobId = mapper.readValue (get (CoraEnvironment.pipelinePortalTestUrl + "/flowcells?id=" + flowCellId),
                                      Flowcell[].class)[0].jobId;
            testLog ("Job Id: " + jobId);
        } catch (Exception e) {
            throw new RuntimeException (e);
        }

        String jobIdEndpoint = "/statuses?sort=-serialNum&select=status%2Cstarted%2CcreatedBy%2CserialNum&pageSize=2&job.id=" + jobId;
        Status status;
        do {
            try {
                status = mapper.readValue (HttpClientHelper.get (CoraEnvironment.pipelinePortalTestUrl + jobIdEndpoint),
                                           Status[].class)[0];
            } catch (Exception e) {
                throw new RuntimeException (e);
            }
            timer.Wait ();
        } while (!timer.Timedout () && status.status != JobStatus.COMPLETED);

        HttpClientHelper.headers.get ().remove (new BasicHeader ("Authorization", portalTestAuth));
        testLog ("Last status: " + status.status + " for job ID: " + jobId);

        assertEquals (status.status, JobStatus.COMPLETED, "Validate pipeline portal job is completed");
    }
}
