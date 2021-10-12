package com.adaptivebiotech.cora.test.order;

import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.DeliveryType.CustomerShipment;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertEquals;
import java.time.ZoneId;
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
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.order.Billing;
import com.adaptivebiotech.cora.ui.order.Diagnostic;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.order.Specimen;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.Shipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
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

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class OrderDetailsTestSuite extends CoraBaseBrowser {

    private OrdersList     ordersList      = new OrdersList ();
    private OrderStatus    orderStatus     = new OrderStatus ();
    private Diagnostic     diagnostic      = new Diagnostic ();
    private Billing        billing         = new Billing ();
    private Specimen       specimen        = new Specimen ();
    private Shipment       shipment        = new Shipment ();
    private ShipmentDetail shipmentDetail  = new ShipmentDetail ();
    private Detail         containerDetail = new Detail ();
    private Accession      accession       = new Accession ();
    private OrderTestsList orderTestsList  = new OrderTestsList ();
    private ShipmentList   shipmentList    = new ShipmentList ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        new Login ().doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T2166
     */
    public void verifyPendingOrderLinkRedirect () {

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
//        List <String> activeHistory = specimen.getHistory (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
//        String activateDateTime = history.get (2).split ("Activated by")[0].trim ();
//        Logging.info ("Order Activated Date and Time: " + activateDateTime);

        Order activeOrder = diagnostic.parseOrder (com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        assertEquals (activeOrder.status, com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        assertEquals (activeOrder.order_number, orderNum);
        String expectedName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + orderNum;
        assertEquals (activeOrder.name, expectedName);
        assertEquals (diagnostic.getDueDate (),
                      DateUtils.getPastFutureDate (7, DateTimeFormatter.ofPattern ("MM/dd/uu"), ZoneId.of ("UTC")));
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
                      DateUtils.convertDateFormat (shipmentArrivalDate + " " + shipmentArrivalTime,
                                                   "MM/dd/yyyy hh:mm a",
                                                   "MM/dd/yyyy hh:mm a"));
        assertEquals (diagnostic.getIntakeCompleteDate (), intakeCompleteDate.split (",")[0]);
        assertEquals (diagnostic.getSpecimenApprovalDate (), specimenApprovalDate.split (",")[0]);
        assertEquals (activeOrder.specimenDto.specimenNumber, specimenId);
        assertEquals (diagnostic.getSpecimenContainerType (), containerType);

        assertEquals (activeOrder.tests.get (0).assay, orderTest);
        assertEquals (activeOrder.properties.BillingType, chargeType);

//        assertEquals (activeHistory.get (0), createdDateTime + " Created by " + CoraEnvironment.coraTestUser);
//        assertEquals (activeHistory.get (2), activateDateTime + "Activated by " + CoraEnvironment.coraTestUser);
    }

}
