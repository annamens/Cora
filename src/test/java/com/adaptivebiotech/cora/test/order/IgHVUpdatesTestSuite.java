package com.adaptivebiotech.cora.test.order;

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
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static com.adaptivebiotech.test.utils.TestHelper.formatDt1;
import static com.adaptivebiotech.test.utils.TestHelper.setDate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.time.ZoneId;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Physician;
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
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;

public class IgHVUpdatesTestSuite extends CoraBaseBrowser {

    private Physician            IgHVPhysician;
    private Physician            NYPhysician;

    private final String         c91_10          = "C91.10";
    private final String         c83_00          = "C83.00";

    private final String         tsvOverridePath = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";

    private Map <String, String> featureFlags;

    @BeforeMethod
    public void beforeMethod () {

        // IgHVPhysician Physician
        IgHVPhysician = TestHelper.createPhysician (CoraEnvironment.physicianLastName,
                                                    CoraEnvironment.physicianFirstName,
                                                    CoraEnvironment.physicianAccountName);

        // NY Physician
        NYPhysician = TestHelper.createPhysician (CoraEnvironment.NYphysicianLastName,
                                                  CoraEnvironment.NYphysicianFirstName,
                                                  CoraEnvironment.physicianAccountName);

        new Login ().doLogin ();
        new OrdersList ().isCorrectPage ();
        FeatureFlags featureFlagsPage = new FeatureFlags ();
        featureFlagsPage.navigateToFeatureFlagsPage ();
        featureFlags = featureFlagsPage.getFeatureFlags ();

    }

    /**
     * Ask the Cora dev team to turn the IgHV feature flag ON
     * 
     * @sdlc_requirements SR-T3689, SR-6656:R1, SR-6656:R3, SR-6656:R4, SR-6656:R5, SR-6656:R6
     */
    @Test (groups = "featureFlagOn")
    public void verifyIgHVStageAndReportFeatureFlagOn () {

        assertTrue (Boolean.valueOf (featureFlags.get ("IgHV")),
                    "Validate IgHV flag is true before test starts");
        // order 1
        createOrderAndReleaseReport (IgHVPhysician,
                                     CellPellet,
                                     PBMC,
                                     true,
                                     true,
                                     true);
        testLog ("step 1 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 2 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");
        testLog ("step 3 - CLIA-IGHV flag appears just below the Report tab ");
        testLog ("step 4 - SHM analysis results are included in reportData.json");

        // order 2
        createOrderAndReleaseReport (NYPhysician,
                                     CellPellet,
                                     PBMC,
                                     false,
                                     true,
                                     false);
        testLog ("step 5 - ighvAnalysisEnabled is true, ighvReportEnabled is false (or absent)");
        testLog ("step 6 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");
        testLog ("step 7 - CLIA-IGHV flag does not appear below the Report tab ");
        testLog ("step 8 - SHM analysis results are not included in reportData.json");

        // order 3
        createOrderAndReleaseReport (IgHVPhysician,
                                     gDNA,
                                     BoneMarrow,
                                     true,
                                     true,
                                     true);
        testLog ("step 9, order3 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10, order3 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        // order 4
        createOrderAndReleaseReport (IgHVPhysician,
                                     Blood,
                                     null,
                                     true,
                                     true,
                                     true);
        testLog ("step 9, order3 - ighvAnalysisEnabled and ighvReportEnabled are true");
        testLog ("step 10, order3 - Workflow moved from SecondaryAnalysis -> SHM Analysis -> ClonoSEQReport");

        // order 5
        createOrderAndReleaseReport (IgHVPhysician,
                                     FFPEScrolls,
                                     LymphNode,
                                     false,
                                     false,
                                     false);
        testLog ("step 11, order5 - ighvAnalysisEnabled and ighvReportEnabled are false (or absent)");
        testLog ("step 12, order5 - SHM Analysis stage is skipped");

        // order 6
        createOrderAndReleaseReport (IgHVPhysician,
                                     CellSuspension,
                                     BCells,
                                     false,
                                     false,
                                     false);
        testLog ("step 11, order6 - ighvAnalysisEnabled and ighvReportEnabled are false (or absent)");
        testLog ("step 12, order6 - SHM Analysis stage is skipped");

        // order 7
        createOrderAndReleaseReport (IgHVPhysician,
                                     CellPellet,
                                     PBMC,
                                     false,
                                     false,
                                     false);
        testLog ("step 11, order7 - ighvAnalysisEnabled and ighvReportEnabled are false (or absent)");
        testLog ("step 12, order7 - SHM Analysis stage is skipped");
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

        // order 1
        createOrderAndReleaseReport (IgHVPhysician,
                                     CellPellet,
                                     PBMC,
                                     false,
                                     false,
                                     false);
        testLog ("step 13 - ighvAnalysisEnabled and ighvReportEnabled are not displayed");
        testLog ("step 14 - SHM Analysis stage is skipped");
        testLog ("step 15 - CLIA-IGHV flag did not appear below the Report tab");
        testLog ("step 16 - SHM analysis results are not included in reportData.json");
    }

    /**
     * Create Order and Release Report, Validate ighvReportEnabled and ighvAnalysisEnabled fields on
     * debug page, CLIA-IGHV flag on Report Page
     * 
     * @param physician
     * @param specimenType
     * @param specimenSource
     * @param expectedIghvReportEnabled
     * @param expectedIghvAnalysisEnabled
     * @param expectedCLIAIGHVFlag
     */
    private void createOrderAndReleaseReport (Physician physician,
                                              SpecimenType specimenType,
                                              SpecimenSource specimenSource,
                                              boolean expectedIghvReportEnabled,
                                              boolean expectedIghvAnalysisEnabled,
                                              boolean expectedCLIAIGHVFlag) {
        // create clonoSEQ diagnostic order
        Billing billing = new Billing ();
        billing.selectNewDiagnosticOrder ();
        billing.isCorrectPage ();

        billing.selectPhysician (physician);
        billing.createNewPatient (TestHelper.newInsurancePatient ());
        billing.enterPatientICD_Codes (c83_00);
        billing.enterPatientICD_Codes (c91_10);
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
        String collectionDate = formatDt1.format (setDate (-1).getTime ().toInstant ()
                                                              .atZone (ZoneId.systemDefault ()));
        specimen.enterCollectionDate (collectionDate);
        specimen.clickSave ();

        String orderNum = specimen.getOrderNum ();
        Logging.testLog ("Order Number: " + orderNum);

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

        // debug page - get workflow properties
        History history = new History ();
        history.gotoOrderDebug (diagnostic.getSampleName ());
        Map <String, String> workflowProperties = history.getWorkflowProperties ();

        assertEquals (workflowProperties.get ("ighvReportEnabled"),
                      String.valueOf (expectedIghvReportEnabled),
                      "Validate ighvReportEnabled property");
        assertEquals (workflowProperties.get ("ighvAnalysisEnabled"),
                      String.valueOf (expectedIghvAnalysisEnabled),
                      "Validate ighvAnalysisEnabled property");

        // set workflow property and force status update
        history.setWorkflowProperty (lastAcceptedTsvPath, tsvOverridePath);
        history.forceStatusUpdate (SecondaryAnalysis, Ready);
        history.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
        assertTrue (history.isStagePresent (SecondaryAnalysis, Finished));
        // TODO uncomment validate SHM Analysis stage
        // assertTrue (history.isStagePresent (StageName.SHMAnalysis, Finished));
        assertTrue (history.isStagePresent (ClonoSEQReport, Awaiting));

        // go to report
        history.clickOrderTest ();
        diagnostic.isOrderStatusPage ();
        diagnostic.clickReportTab (ID_BCell2_CLIA);
        assertEquals (diagnostic.isCLIAIGHVBtnPresent (),
                      expectedCLIAIGHVFlag,
                      "Validate CLIA-IGHV flag");

        // TODO validate SHMAnalysis results in reports.json
        diagnostic.setQCstatus (QC.Pass);
        diagnostic.releaseReport ();
    }
}
