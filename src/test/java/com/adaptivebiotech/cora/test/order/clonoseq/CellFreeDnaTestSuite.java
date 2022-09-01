/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
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
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
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
import static java.util.stream.Collectors.joining;
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
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.ReportClonoSeq;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.patient.PatientOrderHistory;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.utils.PageHelper.QC;
import com.adaptivebiotech.test.utils.DateHelper;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;

/**
 * @author jpatel
 *
 */
@Test (groups = { "clonoSeq", "regression", "golden-retriever" })
public class CellFreeDnaTestSuite extends NewOrderTestBase {

    private Login                 login                  = new Login ();
    private OrdersList            ordersList             = new OrdersList ();
    private NewOrderClonoSeq      newOrderClonoSeq       = new NewOrderClonoSeq ();
    private NewShipment           shipment               = new NewShipment ();
    private Accession             accession              = new Accession ();
    private OrderDetailClonoSeq   orderDetailClonoSeq    = new OrderDetailClonoSeq ();
    private ReportClonoSeq        reportClonoSeq         = new ReportClonoSeq ();
    private OrcaHistory           orcaHistory            = new OrcaHistory ();
    private PatientDetail         patientDetail          = new PatientDetail ();
    private PatientOrderHistory   patientHistory         = new PatientOrderHistory ();
    private ThreadLocal <String>  downloadDir            = new ThreadLocal <> ();

    private final String          noResultsAvailable     = "No result available";
    private final String          mrdResultDescription   = "This sample failed the quality control criteria despite multiple sequencing attempts, exceeded the sample stability time period, or there was a problem processing the test. Please contact Adaptive Biotechnologies for more information, to provide sample disposition instructions, and/or to discuss whether sending a new sample (if one is available) should be considered.";
    private final String          tsvPathOverride        = azTsvPath + "/H2YHWBGXL_0_CLINICAL-CLINICAL_77898-27PC-AJP-012.adap.txt.results.tsv.gz";

    private final String[]        icdCodes               = { "V00.218S" };
    private final String          acceptedPathOverride   = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";
    private final String          updateActivationDate   = "UPDATE cora.specimens SET activation_date = null WHERE specimen_number = '%s'";
    private final String          updateActivationStatus = "UPDATE cora.specimen_activations SET activation_status = '%s' WHERE specimen_id = (SELECT id FROM cora.specimens WHERE specimen_number = '%s')";

    private ThreadLocal <Boolean> cfDna                  = new ThreadLocal <> ();
    private ThreadLocal <Boolean> specimenActivation     = new ThreadLocal <> ();
    private final List <String>   deleteOrders           = asList ("delete from cora.specimen_order_xref where order_id IN (%s)",
                                                                   "delete from cora.order_tests where order_id IN (%s)",
                                                                   "delete from cora.order_billing where order_id IN (%s)",
                                                                   "delete from cora.order_panel_xref where order_id IN (%s)",
                                                                   "delete from cora.order_messages where order_id IN (%s)");
    private final List <String>   deletePatient          = asList ("delete from cora.orders where patient_id = '%s'",
                                                                   "delete from cora.providers_patients where patient_id = '%s'",
                                                                   "delete from cora.patient_billing where patient_id = '%s'",
                                                                   "delete from cora.patients where id = '%s'");

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
     * @sdlc.requirements SR-10414:R2
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
        assertTrue (extractedText.contains ("Cell-free DNA was extracted from plasma isolated from a blood sample."));
        assertTrue (extractedText.contains ("Cell-free DNA (cfDNA) derived from plasma isolated from blood is an indirect measure of residual disease and the mechanisms that contribute to the presence of tumor cfDNA in the blood (and hence plasma) are complex."));

    }

    /**
     * NOTE: SR-T4235
     * 
     * @sdlc.requirements SR-11228:R2, R3
     */
    @Test (groups = "irish-wolfhound")
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

        // TODO uncomment below after SR-12693 is resolved
        // newOrderClonoSeq.activateOrder ();
        // testLog ("Activate Order");
        //
        // orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        // specimenActivation = LocalDateTime.parse (orderDetailClonoSeq.getSpecimenActivationDate
        // (), formatDt7);
        // assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        // testLog ("Specimen Activation Date is present");
    }

    /**
     * NOTE: SR-T4235, SR-T4285
     * 
     * @sdlc.requirements SR-11228:R2, R3
     */
    @Test (groups = "irish-wolfhound")
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

        // TODO uncomment below after SR-12693 is resolved
        // newOrderClonoSeq.activateOrder ();
        // testLog ("Activate Order");
        //
        // orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        // specimenActivation = LocalDateTime.parse (orderDetailClonoSeq.getSpecimenActivationDate
        // (), formatDt7);
        // assertEquals (specimenActivation.toLocalDate (), LocalDate.now (pstZoneId));
        // testLog ("Specimen Activation Date is present");
    }

    /**
     * NOTE: SR-T4287
     * 
     * @sdlc.requirements SR-11228:R5
     */
    @Test (groups = "irish-wolfhound")
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

        updateCount = coraDb.executeUpdate (format (updateActivationStatus, "Terminal", specimenNo));
        assertEquals (updateCount, 1);
        newOrderClonoSeq.refresh ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate (), FAILED_ACTIVATION.label);
        testLog ("Validate Specimen Activation Faield Activation Label");

        // TODO uncomment below after SR-12693 is resolved
        // newOrderClonoSeq.activateOrder ();
        // testLog ("Specimen Activation fail order can be activated");
    }

    /**
     * NOTE: SR-T4235
     * 
     * @sdlc.requirements SR-11228:R3
     */
    @Test (groups = "irish-wolfhound")
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
     * NOTE: SR-T4286
     * 
     * @sdlc.requirements SR-11228:R7
     */
    @Test (groups = "irish-wolfhound")
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
        List <Map <String, Object>> queryRes = coraDb.executeSelect (format ("SELECT * FROM cora.specimen_activations WHERE specimen_id = (SELECT id FROM cora.specimens WHERE specimen_number = '%s')",
                                                                             specimenNo));
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
        assertTrue (extractedText.contains (noResultsAvailable));
        assertTrue (extractedText.contains (mrdResultDescription));
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
                    String orderIds = stream (orders).map (o -> o.id.toString ()).collect (joining ("','", "'", "'"));
                    for (String deleteQuery : deleteOrders)
                        coraDb.executeUpdate (format (deleteQuery, orderIds));
                }

                for (String deleteQuery : deletePatient)
                    coraDb.executeUpdate (format (deleteQuery, patients.get (0).id));

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
}
