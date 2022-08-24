/**
* Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
*/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Patient.PatientTestStatus.MrdEnabled;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_trial;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.Streck;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Advisory;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Alarm;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Expired;
import static com.adaptivebiotech.cora.dto.Specimen.StabilityStatus.Warning;
import static com.adaptivebiotech.cora.utils.PageHelper.QC.Pass;
import static com.adaptivebiotech.cora.utils.PdfUtil.getTextFromPDF;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newTrialProtocolPatient;
import static com.adaptivebiotech.test.utils.DateHelper.convertDateFormat;
import static com.adaptivebiotech.test.utils.DateHelper.formatDt7;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.DateHelper.pstZoneId;
import static com.adaptivebiotech.test.utils.DateHelper.utcZoneId;
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
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Patient.PatientTestStatus;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.dto.Specimen.StabilityStatus;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.CoraPage;
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
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

/**
 * @author jpatel
 *
 */
@Test (groups = { "clonoSeq", "regression", "golden-retriever" })
public class CellFreeDnaTestSuite extends NewOrderTestBase {

    private Login                   login                = new Login ();
    private OrdersList              ordersList           = new OrdersList ();
    private NewOrderClonoSeq        newOrderClonoSeq     = new NewOrderClonoSeq ();
    private NewShipment             shipment             = new NewShipment ();
    private Accession               accession            = new Accession ();
    private OrderDetailClonoSeq     orderDetailClonoSeq  = new OrderDetailClonoSeq ();
    private ReportClonoSeq          reportClonoSeq       = new ReportClonoSeq ();
    private OrcaHistory             orcaHistory          = new OrcaHistory ();
    private PatientDetail           patientDetail        = new PatientDetail ();
    private PatientOrderHistory     patientHistory       = new PatientOrderHistory ();
    private ThreadLocal <String>    downloadDir          = new ThreadLocal <> ();

    private final String            noResultsAvailable   = "No result available";
    private final String            mrdResultDescription = "This sample failed the quality control criteria despite multiple sequencing attempts, exceeded the sample stability time period, or there was a problem processing the test. Please contact Adaptive Biotechnologies for more information, to provide sample disposition instructions, and/or to discuss whether sending a new sample (if one is available) should be considered.";
    private final String            updateQuery          = "UPDATE cora.specimens SET properties = jsonb_set(properties, '{ActivationDate}', '\"%s\"', true) WHERE id = (SELECT specimen_id FROM cora.specimen_order_xref WHERE order_id = '%s')";
    private final String            tsvPathOverride      = azTsvPath + "/H2YHWBGXL_0_CLINICAL-CLINICAL_77898-27PC-AJP-012.adap.txt.results.tsv.gz";

    private final DateTimeFormatter formatDt8            = ofPattern ("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final String[]          icdCodes             = { "C90.00" };
    private final String            acceptedPathOverride = "https://adaptivetestcasedata.blob.core.windows.net/selenium/tsv/postman-collection/HHTMTBGX5_0_EOS-VALIDATION_CPB_C4_L3_E11.adap.txt.results.tsv.gz";
    private final String            patientQuery         = "SELECT id FROM cora.patients WHERE firstname = '%s' and lastname = '%s' AND dateofbirth = '%s'";
    private final String            patientOrderQuery    = "select * from cora.orders where patient_id IN (SELECT id FROM cora.patients WHERE firstname = '%s' and lastname = '%s')";

    private final List <String>     deleteOrders         = asList ("delete from cora.specimen_order_xref where order_id IN (%s)",
                                                                   "delete from cora.order_tests where order_id IN (%s)",
                                                                   "delete from cora.order_billing where order_id IN (%s)",
                                                                   "delete from cora.order_panel_xref where order_id IN (%s)",
                                                                   "delete from cora.order_messages where order_id IN (%s)");
    private final List <String>     deletePatient        = asList ("delete from cora.orders where patient_id IN (%s)",
                                                                   "delete from cora.providers_patients where patient_id IN (%s)",
                                                                   "delete from cora.patient_billing where patient_id IN (%s)",
                                                                   "delete from cora.patients where id IN (%s)");

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
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
        Patient patient = setPatient ("SR-T4212", "TrackingReport", "08/15/2001", "mrnsrt2412trackingreport");
        Physician physician = coraApi.getPhysician (clonoSEQ_selfpay);
        createMrdEnabledPatient (patient, physician);

        Specimen specimenDto = new Specimen ();
        specimenDto.sampleType = Plasma;
        specimenDto.collectionDate = genLocalDate (-3);
        Assay assayTest = MRD_BCell2_CLIA;

        login.doLogin ();
        ordersList.isCorrectPage ();
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
     * @sdlc.requirements SR-11228:R3
     */
    @Test (groups = "havanese")
    public void validateSpecimenActivationPresent () {
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

        Instant instant = Instant.ofEpochMilli (System.currentTimeMillis ());
        String utcDateTime = LocalDateTime.ofInstant (instant, utcZoneId).format (formatDt8);
        String pstDateTime = LocalDateTime.ofInstant (instant, pstZoneId).format (formatDt7);
        int updateCount = coraDb.executeUpdate (format (updateQuery, utcDateTime, order.id));
        assertEquals (updateCount, 1);
        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (false, false);
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate ().format (formatDt7), pstDateTime);
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        accession.gotoAccession (shipmentId);
        accession.clickPass ();
        testLog ("Specimen Pass");

        newOrderClonoSeq.gotoOrderEntry (order.id);
        validateSpecimenSectionFields (false, false);
        assertEquals (newOrderClonoSeq.getSpecimenActivationDate ().format (formatDt7), pstDateTime);
        testLog ("Specimen Activation Date is present and specimen fields are disabled");

        newOrderClonoSeq.activateOrder ();
        testLog ("Activate Order");

        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        assertEquals (orderDetailClonoSeq.getSpecimenActivationDate ().format (formatDt7), pstDateTime);
        testLog ("Specimen Activation Date is present");
    }

    /**
     * NOTE: SR-T4235
     * 
     * @sdlc.requirements SR-11228:R3
     */
    @Test (groups = "havanese")
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
        coraDb.executeUpdate (format (query, getLocalDateTimeStamp (dateDifference), asid));
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

    private String getLocalDateTimeStamp (int daysDifference) {
        return LocalDateTime.now ().plusDays (daysDifference).format (DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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
        assertEquals (newOrderClonoSeq.isCollectionDateEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isUniqueSpecimenIdEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isRetrievalDateEnabled (), allFields);
        assertEquals (newOrderClonoSeq.isSpecimenSourceEnabled (), specimenSource);
    }

    private Patient setPatient (String firstName, String lastName, String dob, String mrn) {
        Patient patient = newSelfPayPatient ();
        patient.firstName = firstName;
        patient.lastName = lastName;
        patient.dateOfBirth = dob;
        patient.mrn = mrn;
        patient.middleName = "";
        return patient;
    }

    private void createMrdEnabledPatient (Patient patient, Physician physician) {
        boolean isPatientExist = isPatientOrderExist (patient);
        String patientDob = convertDateFormat (patient.dateOfBirth, "MM/dd/yyyy", "yyyy-MM-dd");

        boolean isPatientMrdEnabled = false;
        if (isPatientExist) {
            String query = "SELECT id FROM cora.patients WHERE firstname = '%s' and lastname = '%s' AND dateofbirth = '%s'";
            query = format (query, patient.firstName, patient.lastName, patientDob);
            List <Map <String, Object>> queryRes = coraDb.executeSelect (query);
            if (queryRes.size () > 0) {
                String patinetId = queryRes.get (0).get ("id").toString ();
                PatientTestStatus patinetStatus = coraApi.getPatientStatus (UUID.fromString (patinetId));
                testLog ("Patient Id: " + patinetId + ", Status: " + patinetStatus);
                isPatientMrdEnabled = patinetStatus.equals (MrdEnabled);
            }
            if (!isPatientMrdEnabled) {
                deletePatients (patient);
            }
        }

        if (!isPatientExist || !isPatientMrdEnabled) {
            Assay assayTest = ID_BCell2_CLIA;

            login.doLogin ();
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

            new CoraPage ().clickSignOut ();
        }
    }

    private boolean isPatientOrderExist (Patient patient) {
        String query = format (patientOrderQuery,
                               patient.firstName,
                               patient.lastName);

        List <Map <String, Object>> queryRes = coraDb.executeSelect (query);
        boolean isPatientProviderExist = true;
        if (queryRes.size () == 0) {
            deletePatients (patient);
            isPatientProviderExist = false;
        }
        return isPatientProviderExist;
    }

    private void deletePatients (Patient patient) {
        String patientDob = convertDateFormat (patient.dateOfBirth, "MM/dd/yyyy", "yyyy-MM-dd");
        List <Map <String, Object>> patientQueryRes = coraDb.executeSelect (format (patientQuery,
                                                                                    patient.firstName,
                                                                                    patient.lastName,
                                                                                    patientDob));

        List <Map <String, Object>> orderQueryRes = coraDb.executeSelect (format (patientOrderQuery,
                                                                                  patient.firstName,
                                                                                  patient.lastName,
                                                                                  patientDob));

        if (orderQueryRes.size () > 0) {
            String orderIds = orderQueryRes.stream ().map (e -> e.get ("id").toString ())
                                           .collect (Collectors.joining ("','", "'", "'"));
            for (String deleteQuery : deleteOrders) {
                coraDb.executeUpdate (format (deleteQuery, orderIds));
            }
        }

        if (patientQueryRes.size () > 0) {
            String patientIds = patientQueryRes.stream ().map (e -> e.get ("id").toString ())
                                               .collect (Collectors.joining ("','", "'", "'"));

            for (String deleteQuery : deletePatient) {
                coraDb.executeUpdate (format (deleteQuery, patientIds));
            }
        }
    }

}
