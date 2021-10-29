package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.ui.workflow.History;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
import com.adaptivebiotech.test.utils.PageHelper.DeliveryType;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenSource;
import com.adaptivebiotech.test.utils.PageHelper.SpecimenType;
import com.adaptivebiotech.test.utils.PageHelper.StageName;
import com.adaptivebiotech.test.utils.PageHelper.StageStatus;
import com.adaptivebiotech.test.utils.PageHelper.StageSubstatus;

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class OrderDetailsTestSuite extends CoraBaseBrowser {

    private OrdersList     ordersList     = new OrdersList ();
    private OrderStatus    orderStatus    = new OrderStatus ();
    private Diagnostic     diagnostic     = new Diagnostic ();
    private Billing        billing        = new Billing ();
    private Specimen       specimen       = new Specimen ();
    private Shipment       shipment       = new Shipment ();
    private ShipmentDetail shipmentDetail = new ShipmentDetail ();
    private Accession      accession      = new Accession ();
    private PatientDetail  patientDetail  = new PatientDetail ();
    private History        historyPage    = new History ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T2166
     */
    public void verifyOrderDetailsPage () {

        // create clonoSEQ diagnostic order
        billing.selectNewClonoSEQDiagnosticOrder ();
        billing.isCorrectPage ();

        Physician physician = TestHelper.physicianTRF ();
        billing.selectPhysician (physician);
        Patient patient = TestHelper.newPatient ();
        billing.createNewPatient (patient);
        String icdCode = "C90.00";
        billing.enterPatientICD_Codes (icdCode);
        Assay orderTest = Assay.ID_BCell2_CLIA;
        billing.clickAssayTest (orderTest);
        ChargeType chargeType = ChargeType.NoCharge;
        billing.selectBilling (chargeType);
        billing.clickSave ();

        // add specimen details for order
        DeliveryType deliveryType = CustomerShipment;
        specimen.enterSpecimenDelivery (deliveryType);
        specimen.clickEnterSpecimenDetails ();
        SpecimenType specimenType = SpecimenType.Blood;
        specimen.enterSpecimenType (specimenType);
        Anticoagulant anticoagulant = Anticoagulant.EDTA;
        specimen.enterAntiCoagulant (anticoagulant);
        String collectionDate = DateUtils.getPastFutureDate (-3);
        specimen.enterCollectionDate (collectionDate);
        specimen.clickSave ();

        String orderNum = specimen.getOrderNum ();
        Logging.info ("Order Number: " + orderNum);

        List <String> history = specimen.getHistory (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending);
        String createdDateTime = history.get (0).split ("Created by")[0].trim ();
        Logging.info ("Order Created Date and Time: " + createdDateTime);

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        ContainerType containerType = Tube;
        shipment.selectDiagnosticSpecimenContainerType (containerType);
        shipment.clickSave ();

        String shipmentNo = shipment.getShipmentNum ();
        Logging.info ("Shipment No: " + shipmentNo);
        String shipmentArrivalDate = shipment.getArrivalDate ();
        String shipmentArrivalTime = shipment.getArrivalTime ();
        Logging.info ("Shipment Arrival Date: " + shipmentArrivalDate + ", Time: " + shipmentArrivalTime);

        shipment.gotoAccession ();
        accession.isCorrectPage ();

        // accession complete
        accession.clickIntakeComplete ();
        String intakeCompleteDate = accession.getIntakeCompleteDate ();
        Logging.info ("Intake complete Details: " + intakeCompleteDate);
        accession.labelingComplete ();
        accession.labelVerificationComplete ();
        accession.clickPass ();
        String specimenApprovalDate = accession.getSpecimenApprovedDate ();
        Logging.info ("Specimen Approved Details: " + specimenApprovalDate);

        accession.gotoShipment ();
        shipmentDetail.isCorrectPage ();
        String specimenId = shipmentDetail.getSpecimenId ();
        Logging.info ("Specimen ID: " + specimenId);
        shipmentDetail.clickOrderNo ();

        // activate order
        diagnostic.isCorrectPage ();
        diagnostic.activateOrder ();
        diagnostic.refresh ();
        diagnostic.isCorrectPage ();
        List <String> activeHistory = specimen.getHistory (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        String activateDateTime = activeHistory.get (2).split ("Activated by")[0].trim ();
        Logging.info ("Order Activated Date and Time: " + activateDateTime);

        Order activeOrder = diagnostic.parseOrder (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        assertEquals (activeOrder.status, com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        assertEquals (activeOrder.order_number, orderNum);
        String expectedName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + orderNum;
        assertEquals (activeOrder.name, expectedName);
        String expectedDueDate = DateUtils.getPastFutureDate (7,
                                                              DateTimeFormatter.ofPattern ("MM/dd/uu"),
                                                              DateUtils.utcZoneId);
        assertEquals (diagnostic.getDueDate (), expectedDueDate);
        assertEquals (activeOrder.data_analysis_group, "Clinical");

        assertEquals (activeOrder.physician.providerFullName, physician.firstName + " " + physician.lastName);
        assertEquals (activeOrder.physician.accountName, physician.accountName);

        assertEquals (activeOrder.patient.fullname, patient.fullname);
        assertEquals (activeOrder.patient.dateOfBirth, patient.dateOfBirth);
        assertEquals (activeOrder.patient.gender, patient.gender);
        assertEquals (activeOrder.patient.mrn, patient.mrn);

        assertEquals (activeOrder.specimenDto.sampleType, specimenType);
        assertEquals (activeOrder.specimenDto.sourceType, SpecimenSource.Blood);
        assertEquals (activeOrder.specimenDto.anticoagulant, anticoagulant);
        assertEquals (activeOrder.specimenDto.collectionDate, collectionDate);

        assertEquals (diagnostic.getShipmentArrivalDate (),
                      DateUtils.convertDateTimeFormat (shipmentArrivalDate + " " + shipmentArrivalTime,
                                                       "MM/dd/yyyy h:mm a",
                                                       "MM/dd/yyyy hh:mm a"));
        assertEquals (diagnostic.getIntakeCompleteDate (), intakeCompleteDate.split (",")[0]);
        assertEquals (diagnostic.getSpecimenApprovalDate (), specimenApprovalDate.split (",")[0]);
        assertEquals (activeOrder.specimenDto.specimenNumber, specimenId);
        assertEquals (diagnostic.getSpecimenContainerType (), containerType);

        assertEquals (activeOrder.tests.get (0).assay, orderTest);
        assertEquals (activeOrder.properties.BillingType, chargeType);

        assertEquals (activeHistory.get (0), createdDateTime + " Created by " + CoraEnvironment.coraTestUser);
        assertEquals (activeHistory.get (2), activateDateTime + "Activated by " + CoraEnvironment.coraTestUser);
        Logging.testLog ("STEP 1 - validate Order Details Page.");

        String editOrderNotes = "testing order notes";
        diagnostic.editOrderNotes (editOrderNotes);
        assertEquals (diagnostic.getOrderNotes (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active),
                      editOrderNotes);
        Logging.testLog ("STEP 2 - Order Notes displays testing order notes.");

        diagnostic.clickShipmentArrivalDate ();
        shipmentDetail.isCorrectPage ();
        Logging.testLog ("STEP 3 - Shipment details page is opened Shipment1");

        shipmentDetail.clickOrderNo ();
        orderStatus.isCorrectPage ();
        orderStatus.clickOrderDetails ();
        diagnostic.isCorrectPage ();
        diagnostic.clickPatientCode (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        patientDetail.isCorrectPage ();
        Logging.testLog ("STEP 4 - Patient details page is opened in a new tab for Patient1");

        diagnostic.navigateToTab (0);
        diagnostic.isCorrectPage ();
        diagnostic.clickPatientOrderHistory ();
        diagnostic.navigateToOrderDetailsPage (activeOrder.id);
        diagnostic.isCorrectPage ();
        Logging.testLog ("STEP 5 - Patient order history page is opened");

        String expectedLimsUrl = CoraEnvironment.coraTestUrl.replace ("cora",
                                                                      "lims") + "/clarity/search?scope=Sample&query=" + activeOrder.specimenDto.specimenNumber;
        assertEquals (diagnostic.getSpecimenIdUrlAttribute ("href"), expectedLimsUrl);
        assertEquals (diagnostic.getSpecimenIdUrlAttribute ("target"), "_blank");
        Logging.testLog ("STEP 6 - Clarity LIMS link");

        ChargeType editChargeType = ChargeType.InternalPharmaBilling;
        billing.editBilling (editChargeType);

        String coraAttachment = "test1.png";
        diagnostic.uploadAttachments (coraAttachment);

        Order editOrder = diagnostic.parseOrder (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        assertEquals (editOrder.properties.BillingType, editChargeType);
        Logging.testLog ("STEP 7 - Billing section displays Billing2");

        assertEquals (editOrder.orderAttachments.get (0), coraAttachment);
        Logging.testLog ("STEP 8 - The file is attached to the order");

        orderStatus.navigateToOrderStatusPage (editOrder.id);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getSpecimenNumber (), specimenId);
        assertEquals (orderStatus.getDueDate (), expectedDueDate);
        assertEquals (orderStatus.getTestName (), orderTest.test);
        assertEquals (orderStatus.getOrderStatusText (),
                      com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active.name ());
        assertTrue (orderStatus.getLastActivity ().contains (DateUtils.getPastFutureDate (0)));
        Logging.testLog ("STEP 9 - Order status table displays above information");

        historyPage.gotoOrderDebug (editOrder.tests.get (0).sampleName);
        historyPage.waitFor (StageName.Clarity, StageStatus.Awaiting, StageSubstatus.PROCESSING_SAMPLE);

        orderStatus.navigateToOrderStatusPage (editOrder.id);
        List <String> stageStatusUrls = orderStatus.getStageStatusUrls ();
        assertTrue (stageStatusUrls.size () == 1);
        Logging.testLog ("STEP 10 - Clarity LIMS search is opened in a new tab for ASID1");

        orderStatus.expandWorkflowHistory ();
        stageStatusUrls = orderStatus.getStageStatusUrls ();
        Logging.testLog ("STEP 11 - History for order1's order test is displayed.");

        assertTrue (stageStatusUrls.size () == 2);
        assertEquals (stageStatusUrls.get (0), stageStatusUrls.get (1));
        Logging.testLog ("STEP 12 - Clarity LIMS search is opened in a new tab for ASID1");

        diagnostic.navigateToOrderDetailsPage (editOrder.id);
        diagnostic.transferTrf (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        diagnostic.isCorrectPage ();

        Order transferTrOrder = diagnostic.parseOrder (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Pending);
        assertEquals (transferTrOrder.order_number, orderNum + "-a");

        assertEquals (transferTrOrder.physician.providerFullName, physician.firstName + " " + physician.lastName);
        assertEquals (transferTrOrder.physician.accountName, physician.accountName);

        assertEquals (transferTrOrder.patient.fullname, patient.fullname);
        assertEquals (transferTrOrder.patient.dateOfBirth, patient.dateOfBirth);
        assertEquals (transferTrOrder.patient.gender, patient.gender);
        assertEquals (transferTrOrder.patient.mrn, patient.mrn);

        assertEquals (transferTrOrder.tests.get (0).assay.type, orderTest.type);
        assertEquals (transferTrOrder.properties.BillingType, editChargeType);

        assertEquals (transferTrOrder.orderAttachments.get (0), coraAttachment);
        Logging.testLog ("STEP 13 - New diagnostic order page displays");

        orderStatus.navigateToOrderStatusPage (editOrder.id);
        orderStatus.clickOrderDetails ();
        diagnostic.isCorrectPage ();
        diagnostic.clickCancelOrder ();
        Logging.testLog ("STEP 14 - Cancel Order modal appears.");

        diagnostic.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        List <String> cancellationMsgs = orderStatus.getCancelOrderMessages ();
        assertTrue (cancellationMsgs.get (0)
                                    .contains (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Cancelled.name ()
                                                                                                              .toUpperCase ()));
        assertTrue (cancellationMsgs.get (0).contains ("Other - Internal"));
        assertTrue (cancellationMsgs.get (0).contains ("Specimen - Not Rejected"));
        assertTrue (cancellationMsgs.get (0).contains ("Other"));
        assertEquals (cancellationMsgs.get (1), "this is a test");
        Logging.testLog ("STEP 15 - Cancelled messaging displays Reason1, SStatus1, Disposition1, and Comment1");
    }

}
