package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.InternalPharmaBilling;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static com.adaptivebiotech.cora.test.CoraEnvironment.limsTestUrl;
import static com.adaptivebiotech.cora.utils.DateUtils.convertDateFormat;
import static com.adaptivebiotech.cora.utils.DateUtils.getPastFutureDate;
import static com.adaptivebiotech.cora.utils.DateUtils.pstZoneId;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newNoChargePatient;
import static com.adaptivebiotech.test.BaseEnvironment.coraTestUser;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.SpecimenSource.Blood;
import static com.adaptivebiotech.test.utils.PageHelper.StageName.Clarity;
import static com.adaptivebiotech.test.utils.PageHelper.StageStatus.Awaiting;
import static com.adaptivebiotech.test.utils.PageHelper.StageSubstatus.CANCELLED;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.ChargeType;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class OrderDetailsTestSuite extends CoraBaseBrowser {

    private Login               login               = new Login ();
    private OrdersList          ordersList          = new OrdersList ();
    private OrderStatus         orderStatus         = new OrderStatus ();
    private NewOrderClonoSeq    diagnostic          = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq clonoSeqOrderDetail = new OrderDetailClonoSeq ();
    private NewShipment         shipment            = new NewShipment ();
    private ShipmentDetail      shipmentDetail      = new ShipmentDetail ();
    private Accession           accession           = new Accession ();
    private PatientDetail       patientDetail       = new PatientDetail ();

    /**
     * Note: SR-T2166
     * - we nolonger seeing Clarity/Awaiting/PROCESSING_SAMPLE
     * - now, we're getting Clarity/Awaiting/SAMPLE_NOT_FOUND, we can't check for Clarity link
     */
    public void verifyOrderDetailsPage () {
        login.doLogin ();
        ordersList.isCorrectPage ();

        Physician physician = coraApi.getPhysician (non_CLEP_clonoseq);
        Patient patient = newNoChargePatient ();
        String[] icdCode = new String[] { "C90.00" };
        Assay orderTest = ID_BCell2_CLIA;
        Specimen specimen = bloodSpecimen ();
        String orderNum = diagnostic.createClonoSeqOrder (physician, patient, icdCode, orderTest, specimen);
        List <String> history = diagnostic.getHistory ();
        String createdDateTime = history.get (0).split ("Created by")[0].trim ();

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (orderNum);
        ContainerType containerType = Tube;
        shipment.selectDiagnosticSpecimenContainerType (containerType);
        shipment.clickSave ();

        String shipmentArrivalDate = shipment.getArrivalDate ();
        String shipmentArrivalTime = shipment.getArrivalTime ().toUpperCase ();
        shipment.clickAccessionTab ();
        accession.isCorrectPage ();

        // accession complete
        accession.clickIntakeComplete ();
        String intakeCompleteDate = accession.getIntakeCompleteDate ();
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
        accession.clickPass ();
        String specimenApprovalDate = accession.getSpecimenApprovedDate ();

        accession.clickShipmentTab ();
        shipmentDetail.isCorrectPage ();
        String specimenId = shipmentDetail.getSpecimenId ();
        shipmentDetail.clickOrderNumber ();

        // activate order
        diagnostic.isCorrectPage ();
        diagnostic.activateOrder ();
        diagnostic.refresh ();
        diagnostic.isCorrectPage ();
        List <String> activeHistory = clonoSeqOrderDetail.getHistory ();
        String activateDateTime = activeHistory.get (2).split ("Activated by")[0].trim ();
        Order activeOrder = clonoSeqOrderDetail.parseOrder ();
        assertEquals (activeOrder.status, Active);
        assertEquals (activeOrder.order_number, orderNum);
        String expectedName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + orderNum;
        assertEquals (activeOrder.name, expectedName);
        String expectedDueDate = getPastFutureDate (7, DateTimeFormatter.ofPattern ("M/d/uu"), pstZoneId);
        assertEquals (clonoSeqOrderDetail.getHeaderDueDate (), expectedDueDate);
        assertEquals (activeOrder.data_analysis_group, "Clinical");

        assertEquals (activeOrder.physician.providerFullName, physician.firstName + " " + physician.lastName);
        assertEquals (activeOrder.physician.accountName, physician.accountName);

        assertEquals (activeOrder.patient.fullname, patient.fullname);
        assertEquals (activeOrder.patient.dateOfBirth, patient.dateOfBirth);
        assertEquals (activeOrder.patient.gender, patient.gender);
        assertEquals (activeOrder.patient.mrn, patient.mrn);

        // for Blood and Fresh Bone Marrow, specimen source is predefined
        assertEquals (activeOrder.specimenDto.sampleType, specimen.sampleType);
        assertEquals (activeOrder.specimenDto.sampleSource, Blood);
        assertEquals (activeOrder.specimenDto.anticoagulant, specimen.anticoagulant);
        assertEquals (activeOrder.specimenDto.collectionDate, specimen.collectionDate);

        assertEquals (clonoSeqOrderDetail.getShipmentArrivalDate (),
                      convertDateFormat (shipmentArrivalDate + " " + shipmentArrivalTime,
                                         "MM/dd/uuuu h:mm a",
                                         "MM/dd/uuuu hh:mm a"));
        assertEquals (clonoSeqOrderDetail.getIntakeCompleteDate (), intakeCompleteDate.split (",")[0]);
        assertEquals (clonoSeqOrderDetail.getSpecimenApprovalDate (), specimenApprovalDate.split (",")[0]);
        assertEquals (activeOrder.specimenDto.specimenNumber, specimenId);
        assertEquals (clonoSeqOrderDetail.getSpecimenContainerType (), containerType);
        assertEquals (activeOrder.tests.get (0).assay, orderTest);
        assertEquals (activeOrder.properties.BillingType, patient.billingType);
        assertEquals (activeHistory.get (0), createdDateTime + " Created by " + coraTestUser);
        assertEquals (activeHistory.get (2), activateDateTime + "Activated by " + coraTestUser);
        testLog ("STEP 1 - validate Order Details Page.");

        String editOrderNotes = "testing order notes";
        clonoSeqOrderDetail.editOrderNotes (editOrderNotes);
        assertEquals (clonoSeqOrderDetail.getOrderNotes (), editOrderNotes);
        testLog ("STEP 2 - Order Notes displays testing order notes.");

        clonoSeqOrderDetail.clickShipmentArrivalDate ();
        shipmentDetail.isCorrectPage ();
        testLog ("STEP 3 - Shipment details page is opened Shipment1");

        shipmentDetail.clickOrderNumber ();
        orderStatus.isCorrectPage ();
        orderStatus.clickOrderDetailsTab ();
        clonoSeqOrderDetail.isCorrectPage ();
        clonoSeqOrderDetail.clickPatientCode ();
        patientDetail.isCorrectPage ();
        testLog ("STEP 4 - Patient details page is opened in a new tab for Patient1");

        clonoSeqOrderDetail.navigateToTab (0);
        clonoSeqOrderDetail.isCorrectPage ();
        clonoSeqOrderDetail.clickPatientOrderHistory ();
        clonoSeqOrderDetail.gotoOrderDetailsPage (activeOrder.id);
        clonoSeqOrderDetail.isCorrectPage ();
        testLog ("STEP 5 - Patient order history page is opened");

        String expectedLimsUrl = limsTestUrl + "/clarity/search?scope=Sample&query=" + activeOrder.specimenDto.specimenNumber;
        assertEquals (clonoSeqOrderDetail.getSpecimenIdUrlAttribute ("href"), expectedLimsUrl);
        assertEquals (clonoSeqOrderDetail.getSpecimenIdUrlAttribute ("target"), "_blank");
        testLog ("STEP 6 - Clarity LIMS link");

        ChargeType editChargeType = InternalPharmaBilling;
        clonoSeqOrderDetail.billing.editBilling (editChargeType);

        String coraAttachment = "test1.png";
        clonoSeqOrderDetail.uploadAttachments (coraAttachment);

        Order editOrder = clonoSeqOrderDetail.parseOrder ();
        assertEquals (editOrder.properties.BillingType, editChargeType);
        testLog ("STEP 7 - Billing section displays Billing2");

        assertEquals (editOrder.orderAttachments.get (0), coraAttachment);
        testLog ("STEP 8 - The file is attached to the order");

        clonoSeqOrderDetail.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getSpecimenNumber (), specimenId);
        assertEquals (orderStatus.getHeaderDueDate (), expectedDueDate);
        assertEquals (orderStatus.getTestName (), orderTest.test);
        assertEquals (orderStatus.getOrderStatus (), Active);
        assertTrue (orderStatus.getLastActivity ().contains (getPastFutureDate (0)));
        testLog ("STEP 9 - Order status table displays above information");

        orderStatus.waitFor (editOrder.tests.get (0).sampleName, Clarity, Awaiting);
        // unable to cancel until we refresh the page
        orderStatus.gotoOrderDetailsPage (activeOrder.id);
        clonoSeqOrderDetail.isCorrectPage ();
        clonoSeqOrderDetail.clickCancelOrder ();
        testLog ("STEP 14 - Cancel Order modal appears.");

        clonoSeqOrderDetail.clickOrderStatusTab ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getCancelOrderMessages (),
                      asList (CANCELLED + " - Other - Internal. Specimen - Not Rejected. Other.", "this is a test"));
        testLog ("STEP 15 - Cancelled messaging displays Reason1, SStatus1, Disposition1, and Comment1");
    }
}
