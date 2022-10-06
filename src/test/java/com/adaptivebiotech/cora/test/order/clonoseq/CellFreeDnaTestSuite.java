/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.CancelOrderAction.GenerateFailureReport;
import static com.adaptivebiotech.cora.dto.Orders.CancelOrderAction.NoActionRequired;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.PendingCancellation;
import static com.adaptivebiotech.cora.dto.Patient.PatientTestStatus.MrdEnabled;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.Streck;
import static com.adaptivebiotech.cora.dto.Specimen.SpecimenActivation.FAILED;
import static com.adaptivebiotech.cora.dto.Specimen.SpecimenActivation.FAILED_ACTIVATION;
import static com.adaptivebiotech.cora.dto.Specimen.SpecimenActivation.PENDING;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Advisory;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Alarm;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Expired;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Warning;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.ContainerIntegrity;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.General;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.ShippingConditions;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenStabilityIntegrity;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenType;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.TRFHandwritten;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee.CLINICAL_TRIALS;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.DateHelper.convertDateFormat;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.DateHelper.pstZoneId;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenType.Plasma;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ClonoSEQReport;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.Finalize;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ReportDelivery;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.SecondaryAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.ShmAnalysis;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Finished;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Ready;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CLINICAL_QC;
import static com.adaptivebiotech.test.utils.PageHelper.WorkflowProperty.lastAcceptedTsvPath;
import static com.seleniumfy.test.utils.Logging.info;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.StabilityStatus;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PatientOrderHistory;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.DiscrepancyResolutions;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.utils.PageHelper.Discrepancy;
import com.adaptivebiotech.cora.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.DateHelper;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

/**
 * @author jpatel
 *
 */
@Test (groups = { "clonoSeq", "regression", "irish-wolfhound" })
public class CellFreeDnaTestSuite extends NewOrderTestBase {

    private Login                  login                   = new Login ();
    private OrdersList             ordersList              = new OrdersList ();
    private NewOrderClonoSeq       newOrderClonoSeq        = new NewOrderClonoSeq ();
    private NewShipment            shipment                = new NewShipment ();
    private Accession              accession               = new Accession ();
    private DiscrepancyResolutions discrepancyRes          = new DiscrepancyResolutions ();
    private OrderDetailClonoSeq    orderDetailClonoSeq     = new OrderDetailClonoSeq ();
    private OrderStatus            orderStatus             = new OrderStatus ();
    private ReportClonoSeq         reportClonoSeq          = new ReportClonoSeq ();
    private OrcaHistory            orcaHistory             = new OrcaHistory ();
    private PatientDetail          patientDetail           = new PatientDetail ();
    private PatientOrderHistory    patientHistory          = new PatientOrderHistory ();
    private ThreadLocal <String>   downloadDir             = new ThreadLocal <> ();

    private final String           noResultsAvailable      = "No result available";
    private final String           mrdResultDescription    = "This sample failed the quality control criteria despite multiple sequencing attempts, exceeded the sample stability time period, or there was a problem processing the test. Please contact Adaptive Biotechnologies for more information, to provide sample disposition instructions, and/or to discuss whether sending a new sample (if one is available) should be considered.";
    private final String           tsvPathOverride         = azTsvPath + "/H2YHWBGXL_0_CLINICAL-CLINICAL_77898-27PC-AJP-012.adap.txt.results.tsv.gz";

    private final String[]         icdCodes                = { "V00.218S" };
    private final String           acceptedPathOverride    = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";
    private final String           updateActivationDate    = "UPDATE cora.specimens SET activation_date = null WHERE specimen_number = '%s'";
    private final String           updateActivationStatus  = "UPDATE cora.specimen_activations SET activation_status = '%s' WHERE specimen_id = (SELECT id FROM cora.specimens WHERE specimen_number = '%s')";
    private final String           specimenActivationQuery = "SELECT * FROM cora.specimen_activations WHERE specimen_id = (SELECT id FROM cora.specimens WHERE specimen_number = '%s')";
    private final String           specimenRequiredMsg     = "Specimen is required and needs to be approved!";
    private final String           specimenActivationMsg   = "Order save failed with reason: Specimen Activation is Pending. Wait until the Specimen Activation Completes.";

    private ThreadLocal <Boolean>  cfDna                   = new ThreadLocal <> ();
    private ThreadLocal <Boolean>  specimenActivation      = new ThreadLocal <> ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        cfDna.set (featureFlags.cfDNA);
        specimenActivation.set (featureFlags.specimenActivation);
        downloadDir.set (artifacts (this.getClass ().getName (), test.getName ()));
    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R1
     */
    public void cfDnaPlasmaNoResultAvailable () {
        Specimen specimenDto = new Specimen ();
        specimenDto.sampleType = Plasma;
        specimenDto.collectionDate = genLocalDate (-3);
        Assay assayTest = MRD_BCell2_CLIA;

        createOrderAndValidateFailReport (specimenDto, assayTest);

    }

    /**
     * NOTE: SR-T4204
     * 
     * @sdlc.requirements SR-10414:R1
     */
    public void cfDnaBloodNoResultAvailable () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = MRD_BCell2_CLIA;

        createOrderAndValidateFailReport (specimenDto, assayTest);

    }

    /**
     * NOTE: SR-T4212
     * 
     * @sdlc.requirements SR-10414:R4
     */
    public void cfDnaBCellTrackingReport () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Patient patient = newSelfPayPatient ();
        patient.firstName = "SR-T4212";
        patient.lastName = "TrackingReport";
        patient.middleName = "";
        patient.dateOfBirth = "08/15/2001";
        patient.mrn = "mrnsrt2412trackingreport";

        Physician physician = coraApi.getPhysician (clonoSEQ_selfpay);
        createMrdEnabledPatient (patient, physician);

        Specimen specimenDto = new Specimen ();
        specimenDto.sampleType = Plasma;
        specimenDto.collectionDate = genLocalDate (-3);
        Assay assayTest = MRD_BCell2_CLIA;

        Order order = newOrderClonoSeq.createClonoSeqOrder (physician,
                                                            patient,
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto,
                                                            Active,
                                                            Tube);
        String sampleName = orderDetailClonoSeq.getSampleName (assayTest);
        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.setWorkflowProperty (lastAcceptedTsvPath, tsvPathOverride);
        orcaHistory.forceStatusUpdate (StageName.SecondaryAnalysis, StageStatus.Ready);
        testLog ("Order No: " + order.orderNumber + ", forced status updated to SecondaryAnalysis -> Ready");
        orcaHistory.waitFor (StageName.ClonoSEQReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);
        orcaHistory.clickOrderTest ();
        orderDetailClonoSeq.clickReportTab (assayTest);
        reportClonoSeq.releaseReport (assayTest, QC.Pass);
        testLog ("Order Number: " + order.orderNumber + ", Released Report, Tracking Report Generated");

        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.waitFor (StageName.ReportDelivery, StageStatus.Finished, StageSubstatus.ALL_SUCCEEDED);

        orcaHistory.clickOrderTest ();
        orderDetailClonoSeq.clickReportTab (assayTest);

        String pdfFileLocation = join ("/", downloadDir.get (), sampleName + ".pdf");
        coraApi.get (reportClonoSeq.getReleasedReportPdfUrl (), pdfFileLocation);

        String extractedText = getTextFromPDF (pdfFileLocation, 1);
        validatePdfContent (extractedText,
                            "Cell-free DNA (cfDNA)1 was extracted from plasma isolated from a blood sample.");
        validatePdfContent (extractedText,
                            "Circulating tumor DNA (ctDNA)2 is an indirect measure of residual disease and the mechanisms that contribute to the presence of ctDNA in the blood (and hence plasma) are complex.");
        validatePdfContent (extractedText,
                            "ctDNA levels are best assessed in the context of multiple measurements rather than at individual time points.");
        validatePdfContent (extractedText, "New dominant sequences are not assessed when evaluating ctDNA.");
        validatePdfContent (extractedText, "SAMPLE-LEVEL MRD TRACKING: CIRCULATING TUMOR DNA");
        validatePdfContent (extractedText, "SEQUENCE-LEVEL MRD TRACKING: CIRCULATING TUMOR DNA");
        validatePdfContent (extractedText,
                            "Patients with detectable disease in a primary tumor sample may not have detectable ctDNA in a plasma sample; the amount of ctDNA in a plasma sample may not correlate with the amount in a primary tumor.");
        validatePdfContent (extractedText,
                            "Any dominant sequence identified in a Clonality (ID) sample that is subsequently detected in a ctDNA Tracking (MRD) test and is above the assay's LOB is reported as a residual sequence.");
        validatePdfContent (extractedText, "1 Cell-free DNA (cfDNA)");
        validatePdfContent (extractedText,
                            "Comprises short (hundreds of base pairs) DNA fragments found in a variety of acellular biological fluids, including blood plasma.");
        validatePdfContent (extractedText, "2 Circulating tumor DNA (ctDNA)");
        validatePdfContent (extractedText, "The subset of cfDNA in plasma derived from tumor cells.");
        validatePdfContent (extractedText, "3 Sample Clonality");
        validatePdfContent (extractedText, "4 Total Volume (mL)");
        validatePdfContent (extractedText, "5 Total Sequences");
        validatePdfContent (extractedText, "6 Total Unique Sequences");
        validatePdfContent (extractedText, "7 Limit of Detection (LOD)");
        validatePdfContent (extractedText, "8 Limit of Quantitation (LOQ)");
    }

    /**
     * NOTE: SR-T4235
     * 
     * @sdlc.requirements SR-11228:R2, R3, SR-11721: R4
     */
    public void validateSpecimenActivationPresent () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Validate Specimen fields are enabled before Specimen activation");

        accession.gotoAccession (shipmentId);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification Complete");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        LocalDateTime specimenActivation = newOrderClonoSeq.waitUntilSpecimenActivated ();
        validateSpecimenSectionFields (false, false);
        assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("Specimen Pass");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (false, false);
        specimenActivation = LocalDateTime.parse (newOrderClonoSeq.getSpecimenActivationDate (), formatDt7);
        assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");

        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        specimenActivation = LocalDateTime.parse (orderDetailClonoSeq.getSpecimenActivationDate (), formatDt7);
        assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        testLog ("Specimen Activation Date is present");
    }

    /**
     * NOTE: SR-T4235, SR-T4285
     * 
     * @sdlc.requirements SR-11228:R2, R3
     */
    public void specimenActivationContainersLabelVerify () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (order.orderNumber);
        shipment.selectDiagnosticSpecimenContainerType (Vacutainer);
        shipment.clickAddContainer ();
        shipment.clickSave ();

        shipment.clickAccessionTab ();
        UUID shipmentId = accession.getShipmentId ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete (2);
        accession.clickLabelingComplete ();
        testLog ("Labeling complete of all containers");

        accession.clickLabelVerificationComplete (2);
        testLog ("Label verification complete of second container");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen Activation is empty as all containers are not label verified");

        accession.gotoAccession (shipmentId);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete of first (and all) container");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        LocalDateTime specimenActivation = newOrderClonoSeq.waitUntilSpecimenActivated ();
        validateSpecimenSectionFields (false, false);
        assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("Specimen Pass");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        specimenActivation = LocalDateTime.parse (newOrderClonoSeq.getSpecimenActivationDate (), formatDt7);
        assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");

        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        specimenActivation = LocalDateTime.parse (orderDetailClonoSeq.getSpecimenActivationDate (), formatDt7);
        assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        testLog ("Specimen Activation Date is present");
    }

    /**
     * NOTE: SR-T4287
     * 
     * @sdlc.requirements SR-11228:R5
     */
    public void validateSpecimenActivationStatus () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        accession.completeAccession ();

        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate (), PENDING.label);
        testLog ("Validate Specimen Activation Pending Label");

        newOrderClonoSeq.clickSaveAndActivate ();
        newOrderClonoSeq.confirmActivate ();
        assertEquals (newOrderClonoSeq.getToastError (), specimenActivationMsg);

        LocalDateTime specimenActivation = newOrderClonoSeq.waitUntilSpecimenActivated ();
        assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));

        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        int updateCount = coraDb.executeUpdate (format (updateActivationDate, specimenNo));
        assertEquals (updateCount, 1);

        updateCount = coraDb.executeUpdate (format (updateActivationStatus, FAILED.label, specimenNo));
        assertEquals (updateCount, 1);
        newOrderClonoSeq.refresh ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate (), FAILED.label);
        testLog ("Validate Specimen Activation Failed Label");

        newOrderClonoSeq.clickSaveAndActivate ();
        newOrderClonoSeq.confirmActivate ();
        assertEquals (newOrderClonoSeq.getToastError (), specimenActivationMsg);

        updateCount = coraDb.executeUpdate (format (updateActivationStatus, "Terminal", specimenNo));
        assertEquals (updateCount, 1);
        newOrderClonoSeq.refresh ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate (), FAILED_ACTIVATION.label);
        testLog ("Validate Specimen Activation Faield Activation Label");

        newOrderClonoSeq.clickSaveAndActivate ();
        newOrderClonoSeq.confirmActivate ();
        assertEquals (newOrderClonoSeq.getToastError (), specimenActivationMsg);

    }

    /**
     * NOTE: SR-T4235
     * 
     * @sdlc.requirements SR-11228:R3
     */
    public void validateSpecimenActivationNotPresent () {
        Specimen specimenDto = bloodSpecimen ();
        Assay assayTest = ID_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Validate Specimen fields are enabled before Specimen activation");

        accession.gotoAccession (shipmentId);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification Complete");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 0);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen Activation Date is not present and specimen fields are enabled");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("Specimen Pass");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (true, false);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen Activation Date is not present and specimen fields are enabled");

        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");

        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        assertNull (orderDetailClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen Activation Date is not present");
    }

    /**
     * NOTE: SR-T4324
     * 
     * @sdlc.requirements SR-12635:R4, SR-11721:R4
     */
    @Test (groups = "irish-wolfhound")
    public void majorDiscrepancyResolvedAfterLabelVerify () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;
        Discrepancy discrepancy = ShippingConditions;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        accession.createDiscrepancy (discrepancy, discrepancy.severity + " Discrepancy", CLINICAL_TRIALS);
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete, Major discrepancy is not resolved");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 0);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen is not sent for activation as Major discrepancy is not resolved");

        discrepancyRes.gotoDiscrepancy (shipmentId);
        discrepancyRes.resolveAllDiscrepancies ();
        testLog ("Major discrepancy is resolved, thus specimen sent for activation");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 1);
        newOrderClonoSeq.waitUntilSpecimenActivated ();
        testLog ("Specimen activated after Major discrepancy is resolved");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");
    }

    /**
     * NOTE: SR-T4324
     * 
     * @sdlc.requirements SR-12635:R4, SR-11721:R4
     */
    @Test (groups = "irish-wolfhound")
    public void minorDiscrepancyResolvedAfterLabelVerify () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;
        Discrepancy discrepancy = General;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        accession.createDiscrepancy (discrepancy, discrepancy.severity + " Discrepancy", CLINICAL_TRIALS);
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete, Minor discrepancy is not resolved");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 1);
        newOrderClonoSeq.waitUntilSpecimenActivated ();
        testLog ("Specimen activated, does not require minor discrepancy to be resolved");

        discrepancyRes.gotoDiscrepancy (shipmentId);
        discrepancyRes.resolveAllDiscrepancies ();
        testLog ("minor discrepancy resolved");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");

    }

    /**
     * NOTE: SR-T4324
     * 
     * @sdlc.requirements SR-12635:R4, SR-11721:R4
     */
    @Test (groups = "irish-wolfhound")
    public void majorDiscrepancyResolvedBeforeLabelVerify () {
        discrepancyResolvedBeforeLabelVerify (ShippingConditions);
    }

    /**
     * NOTE: SR-T4324
     * 
     * @sdlc.requirements SR-12635:R4, SR-11721:R4
     */
    @Test (groups = "irish-wolfhound")
    public void minorDiscrepancyResolvedBeforeLabelVerify () {
        discrepancyResolvedBeforeLabelVerify (General);
    }

    /**
     * NOTE: SR-T4324
     * 
     * @sdlc.requirements SR-12635:R4, SR-11721:R4
     */
    @Test (groups = "irish-wolfhound")
    public void multipleMajorDiscrepancyResolvedAfterLabelVerify () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;
        Discrepancy majorDiscrepancy1 = SpecimenStabilityIntegrity;
        Discrepancy majorDiscrepancy2 = SpecimenType;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        asList (majorDiscrepancy1, majorDiscrepancy2).forEach (d -> {
            accession.createDiscrepancy (d, "Major Discrepancy: " + d.text, CLINICAL_TRIALS);
            testLog ("Add " + d.severity.name () + " discrepancy, name: " + d.text);
        });
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete, Two Major discrepancies are not resolved");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 0);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen is not sent for activation as Major discrepancies are not resolved");

        discrepancyRes.gotoDiscrepancy (shipmentId);
        discrepancyRes.resolveDiscrepancy (majorDiscrepancy1);
        discrepancyRes.clickSave ();
        testLog ("Resolve First Major discrepancy " + majorDiscrepancy1.text);

        newOrderClonoSeq.gotoOrderEntry (order.id);
        queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 0);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen is not sent for activation as one major discrepancy is still not resolved");
        newOrderClonoSeq.clickSaveAndActivate ();
        assertEquals (newOrderClonoSeq.getRequiredFieldMsgs (), asList (specimenRequiredMsg));
        testLog ("order acctivation displays error as one major discrepancy is still not resolved");

        discrepancyRes.gotoDiscrepancy (shipmentId);
        discrepancyRes.resolveDiscrepancy (majorDiscrepancy2);
        discrepancyRes.clickSave ();
        testLog ("Resolve Second Major discrepancy " + majorDiscrepancy2.text);

        newOrderClonoSeq.gotoOrderEntry (order.id);
        queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 1);
        newOrderClonoSeq.waitUntilSpecimenActivated ();
        testLog ("Specimen activated as all major discrepancies are resolved");
        newOrderClonoSeq.clickSaveAndActivate ();
        assertEquals (newOrderClonoSeq.getRequiredFieldMsgs (), asList (specimenRequiredMsg));
        testLog ("Order activation displays error as specimen is not approved");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("specimen approved");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");
    }

    /**
     * NOTE: SR-T4324
     * 
     * @sdlc.requirements SR-12635:R4, SR-11721:R4
     */
    @Test (groups = "irish-wolfhound")
    public void majorMinorDiscrepancyResolvedAfterLabelVerify () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;
        Discrepancy majorDiscrepancy = ContainerIntegrity;
        Discrepancy minorDiscrepancy = TRFHandwritten;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        asList (majorDiscrepancy, minorDiscrepancy).forEach (d -> {
            accession.createDiscrepancy (d, "Discrepancy: " + d.text, CLINICAL_TRIALS);
            testLog ("Add " + d.severity.name () + " discrepancy, name: " + d.text);
        });
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete, One Major and one minor discrepancies are not resolved");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 0);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen is not sent for activation as Major discrepancy is not resolved");

        discrepancyRes.gotoDiscrepancy (shipmentId);
        discrepancyRes.resolveDiscrepancy (majorDiscrepancy);
        discrepancyRes.clickSave ();
        testLog ("Major discrepancy resolved");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 1);
        newOrderClonoSeq.waitUntilSpecimenActivated ();
        testLog ("Specimen activated as one major discrepancy is resolved, and minor discrepany is open");
        newOrderClonoSeq.clickSaveAndActivate ();
        List <String> errors = newOrderClonoSeq.getRequiredFieldMsgs ();
        assertEquals (errors, asList (specimenRequiredMsg));
        testLog ("Order activation error as one minor discrepancy is open and specimen is not approved");

        discrepancyRes.gotoDiscrepancy (shipmentId);
        discrepancyRes.resolveDiscrepancy (minorDiscrepancy);
        discrepancyRes.clickSave ();
        testLog ("Minor discrepancy resolved");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("specimen approved");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");
    }

    /**
     * NOTE: SR-T4286
     * 
     * @sdlc.requirements SR-11228:R7
     */
    public void specimenActivation_featureFlagOff () {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOn (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        accession.completeAccession ();

        newOrderClonoSeq.isCorrectPage ();
        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 0);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());
        testLog ("Specimen is not sent for activation as flag is off");
    }

    /**
     * NOTE: SR-T4271
     * 
     * @sdlc.requirements SR-11419
     */
    @Test (groups = "havanese")
    public void verifyStreckStabilityWindow () {
        Specimen specimenDto = bloodSpecimen ();
        Patient patient = newTrialProtocolPatient ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        specimenDto.collectionDate = genLocalDate (0);
        Assay assayTest = ID_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            patient,
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        specimenDto.specimenNumber = newOrderClonoSeq.getSpecimenId ();
        testLog ("Order No: " + order.orderNumber);
        testLog ("Specimen ID: " + specimenDto.specimenNumber);

        shipment.createShipment (order.orderNumber, Tube);
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();

        // Verify Blood Stabilization Window on Shipment ACCESSION tab
        assertEquals (accession.getStabilizationWindow ().color, Advisory.rgba);
        assertEquals (accession.getStabilizationWindow ().text, "Streck (Blood) - 7 days left");
        testLog ("Accession Tab: " + accession.getStabilizationWindow ().text + ", Styling: " + Advisory);

        // Verify Blood Stabilization Window on PATIENT ORDER HISTORY tab
        accession.clickOrderNumber ();
        newOrderClonoSeq.clickPatientCode ();
        patientDetail.clickPatientOrderHistoryTab ();
        assertEquals (patientHistory.getStabilizationWindow (order).color, Advisory.rgba);
        assertEquals (patientHistory.getStabilizationWindow (order).text, "Streck (Blood) - 7 days left");
        testLog ("Patient Order History: " + patientHistory.getStabilizationWindow (order).text + ", Styling: " + Advisory);

        // Verify Blood Stabilization Window on ORDER DETAILS tab
        newOrderClonoSeq.gotoOrderEntry (order.id);
        assertEquals (newOrderClonoSeq.getStabilizationWindow ().color, Advisory.rgba);
        assertEquals (newOrderClonoSeq.getStabilizationWindow ().text, "Streck (Blood) - 7 days left");
        testLog ("Order Details: " + newOrderClonoSeq.getStabilizationWindow ().text + ", Styling: " + Advisory);

        updateCollectionDateAndVerifyBloodStabilityWindow (-1, Advisory);
        updateCollectionDateAndVerifyBloodStabilityWindow (-2, Warning);
        updateCollectionDateAndVerifyBloodStabilityWindow (-3, Warning);
        updateCollectionDateAndVerifyBloodStabilityWindow (-4, Alarm);
        updateCollectionDateAndVerifyBloodStabilityWindow (-5, Alarm);
        updateCollectionDateAndVerifyBloodStabilityWindow (-6, Alarm);
        updateCollectionDateAndVerifyBloodStabilityWindow (-7, Expired);
        updateCollectionDateAndVerifyBloodStabilityWindow (-8, Expired);

        // Verify Plasma Stabilization Window on ORDER DETAILS tab
        newOrderClonoSeq.enterCollectionDate (genLocalDate (-1));
        updateIsolationDateAndVerifyPlasmaStabilityWindow (0, Advisory, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-39, Advisory, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-40, Warning, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-41, Warning, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-42, Alarm, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-43, Alarm, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-44, Alarm, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-45, Expired, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-46, Expired, specimenDto.specimenNumber);
        updateIsolationDateAndVerifyPlasmaStabilityWindow (-47, Expired, specimenDto.specimenNumber);
    }

    /**
     * NOTE: SR-T4290
     * 
     * @sdlc.requirements SR-11721:R1
     */
    public void verifyUpdatedOrderActivationBlood () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        specimenDto.collectionDate = genLocalDate (-6);
        Assay assayTest = MRD_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);
        shipment.createShipment (order.orderNumber, Vacutainer);
        accession.completeAccession ();
        newOrderClonoSeq.gotoOrderEntry (order.id);

        // Collection date = today - 7, activation fail
        newOrderClonoSeq.enterCollectionDate (genLocalDate (-7));
        newOrderClonoSeq.clickSaveAndActivate ();
        newOrderClonoSeq.confirmActivate ();
        assertEquals (newOrderClonoSeq.getToastError (),
                      "Order save failed with reason: Streck (Blood) was not isolated within 7 days from the collection date.");
        testLog ("Streck Blood Collection Date = Today - 7, Order Activation Blocked");

        // Collection date = today - 6, activation pass
        newOrderClonoSeq.enterCollectionDate (genLocalDate (-6));
        newOrderClonoSeq.activateOrder ();
        testLog ("Streck Blood Collection Date = Today - 6, Order Activation Successful");
    }

    /**
     * NOTE: SR-T4289
     * 
     * @sdlc.requirements SR-11721:R2
     */
    public void verifyUpdatedOrderActivationPlasma () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        specimenDto.collectionDate = genLocalDate (-6);
        Assay assayTest = MRD_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        specimenDto.specimenNumber = newOrderClonoSeq.getSpecimenId ();
        testLog ("Order No: " + order.orderNumber);
        shipment.createShipment (order.orderNumber, Vacutainer);
        accession.completeAccession ();

        // Isolation date = today - 45, activation fail
        String query = "UPDATE cora.specimens SET isolation_date = '%s' where specimen_number = '%s';";
        coraDb.executeUpdate (format (query,
                                      DateHelper.genDate (-45,
                                                          ISO_LOCAL_DATE_TIME,
                                                          pstZoneId),
                                      specimenDto.specimenNumber));
        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.clickSaveAndActivate ();
        newOrderClonoSeq.confirmActivate ();
        assertEquals (newOrderClonoSeq.getToastError (),
                      "Order save failed with reason: Streck (Plasma) was not extracted within 45 days from the isolation date.");
        testLog ("Streck Plasma Isolation Date = Today - 45, Order Activation Blocked");

        // Isolation date = today - 44, activation pass
        coraDb.executeUpdate (format (query,
                                      DateHelper.genDate (-44,
                                                          ISO_LOCAL_DATE_TIME,
                                                          pstZoneId),
                                      specimenDto.specimenNumber));
        newOrderClonoSeq.refresh ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.activateOrder ();
        testLog ("Streck Plasma Isolation Date = Today - 44, Order Activation Successful");
    }

    /**
     * NOTE: SR-T4291
     * 
     * @sdlc.requirements SR-11341
     */
    @Test (groups = "irish-wolfhound")
    public void verifyCancelStreckOrderWithFastLane () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        Assay assayTest = MRD_BCell2_CLIA;
        Patient patient = newTrialProtocolPatient ();

        // Streck sample, With Fastlane
        Specimen specimenStreck = bloodSpecimen ();
        specimenStreck.compartment = CellFree;
        specimenStreck.anticoagulant = Streck;
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            patient,
                                                            icdCodes,
                                                            assayTest,
                                                            specimenStreck);
        testLog ("Streck sample, With Fastlane: " + order.orderNumber);
        newOrderClonoSeq.clickCancelOrder ();

        // Verify New Cancel Action Default Option
        assertTrue (newOrderClonoSeq.isCancelActionDropdownVisible ());
        assertEquals (newOrderClonoSeq.getCancelActionValue (), GenerateFailureReport);
        testLog ("Cancel Action dropdown visible and set to " + GenerateFailureReport.label);
        newOrderClonoSeq.cancelStreckOrder (GenerateFailureReport);

        // Verify No Result Report can be released
        assertEquals (newOrderClonoSeq.getOrderStatus (), PendingCancellation);
        testLog ("Order cancelled and status is PendingCancellation, waiting for Awaiting Clinical QC");
        specimenStreck.sampleName = orderStatus.getWorkflowId ();
        orderStatus.waitFor (specimenStreck.sampleName, ClonoSEQReport, Awaiting, CLINICAL_QC);
        testLog ("Awaiting Clinical QC Reached");
        orderDetailClonoSeq.gotoOrderEntry (order.id);
        orderDetailClonoSeq.clickReportTab (assayTest);
        reportClonoSeq.releaseReport (assayTest, QC.Pass);
        testLog ("Released Report, waiting for delivery finished");

        // Verify Report type is No Result Available
        String pdfFileLocation = join ("/", downloadDir.get (), specimenStreck.sampleName + ".pdf");
        coraApi.get (reportClonoSeq.getReleasedReportPdfUrl (), pdfFileLocation);

        String extractedText = getTextFromPDF (pdfFileLocation, 1);
        assertTrue (extractedText.contains (noResultsAvailable));
        testLog ("Report displays No Result Available");

        // Verify Order Cancelled after Report Release
        orderDetailClonoSeq.clickOrderStatusTab ();
        orderStatus.waitFor (specimenStreck.sampleName, Finalize, StageStatus.Cancelled);
        testLog ("Report Delivery Finished");
        orderDetailClonoSeq.refresh ();
        assertEquals (newOrderClonoSeq.getOrderStatus (), Orders.OrderStatus.Cancelled);
        testLog ("Order Status is Cancelled");
    }

    /**
     * NOTE: SR-T4291
     * 
     * @sdlc.requirements SR-11341
     */
    @Test (groups = "irish-wolfhound")
    public void verifyCancelStreckOrderWithoutFastLane () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        Assay assayTest = MRD_BCell2_CLIA;
        Patient patient = newTrialProtocolPatient ();
        Specimen specimenStreck = bloodSpecimen ();
        specimenStreck.compartment = CellFree;
        specimenStreck.anticoagulant = Streck;

        // Streck sample, Without Fastlane
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            patient,
                                                            icdCodes,
                                                            assayTest,
                                                            specimenStreck);
        testLog ("Streck sample, Without Fastlane: " + order.orderNumber);
        newOrderClonoSeq.clickCancelOrder ();

        // Verify New Cancel Action Non-Default Option
        newOrderClonoSeq.cancelStreckOrder (NoActionRequired);
        testLog ("Cancel Action dropdown set to " + NoActionRequired.label);
        assertEquals (newOrderClonoSeq.getOrderStatus (), Orders.OrderStatus.Cancelled);
        testLog ("Order Status is Cancelled");
    }

    /**
     * Updates the Collection Date on the Order Details tab and verifies the stability window
     * styling based off params
     * 
     * @param dateDifference
     *            number of days away from today to test
     * @param stabilityWindow
     *            expected styling/background color of stability window component
     */
    private void updateCollectionDateAndVerifyBloodStabilityWindow (int dateDifference,
                                                                    StabilityStatus stabilityWindow) {
        newOrderClonoSeq.enterCollectionDate (genLocalDate (dateDifference));
        newOrderClonoSeq.clickSave ();
        assertEquals (newOrderClonoSeq.getStabilizationWindow ().color, stabilityWindow.rgba);

        if (dateDifference > -7) {
            assertEquals (newOrderClonoSeq.getStabilizationWindow ().text,
                          format ("Streck (Blood) - %s days left", 7 + dateDifference));
        } else {
            assertEquals (newOrderClonoSeq.getStabilizationWindow ().text,
                          format ("Streck (Blood) - Expired %s days ago", Math.abs (7 + dateDifference)));
        }

        testLog ("Order Details: " + newOrderClonoSeq.getStabilizationWindow ().text + ", Styling: " + stabilityWindow);
    }

    /**
     * Updates the Specimen isolation_date via SQL and verifies the stability window styling based
     * off params
     * 
     * @param dateDifference
     *            number of days away from today to test
     * @param stabilityWindow
     *            expected styling/background color of stability window component
     * @param asid
     *            specimen id for SQL query
     */
    private void updateIsolationDateAndVerifyPlasmaStabilityWindow (int dateDifference,
                                                                    StabilityStatus stabilityWindow,
                                                                    String asid) {
        String query = "UPDATE cora.specimens SET isolation_date = '%s' where specimen_number = '%s';";
        coraDb.executeUpdate (format (query,
                                      DateHelper.genDate (dateDifference,
                                                          ISO_LOCAL_DATE_TIME,
                                                          pstZoneId),
                                      asid));
        newOrderClonoSeq.refresh ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getStabilizationWindow ().color, stabilityWindow.rgba);

        if (dateDifference > -45) {
            assertEquals (newOrderClonoSeq.getStabilizationWindow ().text,
                          format ("Streck (Plasma) - %s days left", 45 + dateDifference));
        } else {
            assertEquals (newOrderClonoSeq.getStabilizationWindow ().text,
                          format ("Streck (Plasma) - Expired %s days ago", Math.abs (45 + dateDifference)));
        }

        testLog ("Order Details: " + newOrderClonoSeq.getStabilizationWindow ().text + ", Styling: " + stabilityWindow);
    }

    private void createOrderAndValidateFailReport (Specimen specimenDto, Assay assayTest) {
        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto,
                                                            Active,
                                                            Tube);
        String sampleName = orderDetailClonoSeq.getSampleName (assayTest);
        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.forceStatusUpdate (StageName.Clarity, StageStatus.Failed);
        testLog ("Order No: " + order.orderNumber + ", forced status updated to Clarity -> Failed");
        orcaHistory.waitFor (StageName.ClonoSEQReport, StageStatus.Awaiting, StageSubstatus.CLINICAL_QC);
        orcaHistory.clickOrderTest ();
        orderDetailClonoSeq.clickReportTab (assayTest);
        reportClonoSeq.releaseReport (assayTest, QC.Pass);
        testLog ("Order Number: " + order.orderNumber + ", Released Report, Failure Report Generated");

        orcaHistory.gotoOrderDebug (sampleName);
        orcaHistory.waitFor (StageName.ReportDelivery, StageStatus.Finished, StageSubstatus.ALL_SUCCEEDED);
        testLog ("New Clonality (ID) order needed alert should be triggered");

        orcaHistory.clickOrderTest ();
        orderDetailClonoSeq.clickReportTab (assayTest);

        String pdfFileLocation = join ("/", downloadDir.get (), sampleName + ".pdf");
        coraApi.get (reportClonoSeq.getReleasedReportPdfUrl (), pdfFileLocation);

        String extractedText = getTextFromPDF (pdfFileLocation, 1);
        validatePdfContent (extractedText, noResultsAvailable);
        validatePdfContent (extractedText, mrdResultDescription);
    }

    private void validateSpecimenSectionFields (boolean allFields, boolean specimenSource) {
        assertEquals (newOrderClonoSeq.isSpecimenDeliveryEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isSpecimenTypeEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isCompartmentEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isAnticoagulantEnabled (), allFields);
        assertTrue (newOrderClonoSeq.isCollectionDateEnabled ());
        assertTrue (newOrderClonoSeq.isUniqueSpecimenIdEnabled ());
        assertEquals (newOrderClonoSeq.isRetrievalDateEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isSpecimenSourceEnabled (), specimenSource);
    }

    private void createMrdEnabledPatient (Patient patient, Physician physician) {
        String patientDob = convertDateFormat (patient.dateOfBirth, "MM/dd/yyyy", "yyyy-MM-dd");
        List <Patient> patients = stream (coraApi.getPatients (patient.firstName)).filter (p -> patient.lastName.equals (p.lastName))
                                                                                  .filter (p -> patientDob.equals (p.dateOfBirth))
                                                                                  .collect (toList ());
        if (patients.size () > 1)
            fail (format ("found [%s] patients, for search terms: %s", patients.size (), patient));

        boolean needToCreateOrder = false;
        if (patients.size () == 0)
            needToCreateOrder = true;
        else {
            // patient exist, check if status is MrdEnabled
            boolean isPatientMrdEnabled = MrdEnabled.equals (coraApi.getPatientStatus (patients.get (0).id));
            if (!isPatientMrdEnabled) {
                Order[] orders = coraApi.getOrdersForPatient (patients.get (0).id);
                if (orders.length > 0) {
                    List <UUID> orderIds = stream (orders).map (o -> o.id).collect (toList ());
                    coraDb.deleteOrdersFromDB (orderIds);
                }
                coraDb.deletePatientFromDB (patients.get (0).id);

                needToCreateOrder = true;
            }
        }

        login.doLogin ();
        ordersList.isCorrectPage ();

        if (needToCreateOrder) {
            Assay assayTest = ID_BCell2_CLIA;
            Order order = newOrderClonoSeq.createClonoSeqOrder (physician,
                                                                patient,
                                                                icdCodes,
                                                                assayTest,
                                                                bloodSpecimen (),
                                                                Active,
                                                                Tube);
            info ("Order Number: " + order.orderNumber);

            String sampleName = orderDetailClonoSeq.getSampleName (assayTest);
            orcaHistory.gotoOrderDebug (sampleName);
            orcaHistory.setWorkflowProperty (lastAcceptedTsvPath, acceptedPathOverride);
            orcaHistory.forceStatusUpdate (SecondaryAnalysis, Ready);
            orcaHistory.waitFor (SecondaryAnalysis, Finished);
            orcaHistory.waitFor (ShmAnalysis, Finished);
            orcaHistory.waitFor (ClonoSEQReport, Awaiting, CLINICAL_QC);
            orcaHistory.clickOrderTest ();

            reportClonoSeq.clickReportTab (assayTest);
            reportClonoSeq.releaseReport (assayTest, Pass);
            orcaHistory.gotoOrderDebug (sampleName);
            orcaHistory.waitFor (ReportDelivery, Finished);
            orcaHistory.clickOrderTest ();
        }
    }

    private void discrepancyResolvedBeforeLabelVerify (Discrepancy discrepancy) {
        skipTestIfFeatureFlagOff (cfDna.get ());
        skipTestIfFeatureFlagOff (specimenActivation.get ());
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        Assay assayTest = ID_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_trial),
                                                            newTrialProtocolPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        testLog ("Order No: " + order.orderNumber);

        shipment.createShipment (order.orderNumber, Tube);
        UUID shipmentId = accession.getShipmentId ();
        accession.createDiscrepancy (discrepancy, discrepancy.severity + " Discrepancy", CLINICAL_TRIALS);
        accession.clickDiscrepancyResolutionsTab ();
        discrepancyRes.resolveAllDiscrepancies ();
        testLog ("Resolve discrepancy");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        String specimenNo = newOrderClonoSeq.getSpecimenId ();
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 0);
        assertNull (newOrderClonoSeq.getSpecimenActivationDate ());

        accession.gotoAccession (shipmentId);
        accession.clickIntakeComplete ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        queryRes = coraDb.executeSelect (format (specimenActivationQuery, specimenNo));
        assertEquals (queryRes.size (), 1);
        newOrderClonoSeq.waitUntilSpecimenActivated ();

        accession.gotoAccession (shipmentId);
        accession.clickPass ();

        newOrderClonoSeq.gotoOrderEntry (order.id);
        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");
    }
}
