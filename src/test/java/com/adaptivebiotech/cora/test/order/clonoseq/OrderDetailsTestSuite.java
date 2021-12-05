package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.test.utils.PageHelper.ContainerType.Tube;
import static com.adaptivebiotech.test.utils.PageHelper.ShippingCondition.Ambient;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Patient;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.test.CoraEnvironment;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.debug.OrcaHistory;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientDetail;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.utils.DateUtils;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.Logging;
import com.adaptivebiotech.test.utils.PageHelper.Anticoagulant;
import com.adaptivebiotech.test.utils.PageHelper.Assay;
import com.adaptivebiotech.test.utils.PageHelper.ChargeType;
import com.adaptivebiotech.test.utils.PageHelper.ContainerType;
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

    private Login               login               = new Login ();
    private OrdersList          ordersList          = new OrdersList ();
    private OrderStatus         orderStatus         = new OrderStatus ();
    private NewOrderClonoSeq    diagnostic          = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq clonoSeqOrderDetail = new OrderDetailClonoSeq ();
    private NewShipment         shipment            = new NewShipment ();
    private ShipmentDetail      shipmentDetail      = new ShipmentDetail ();
    private Accession           accession           = new Accession ();
    private PatientDetail       patientDetail       = new PatientDetail ();
    private OrcaHistory         historyPage         = new OrcaHistory ();

    /**
     * NOTE: SR-T2166
     */
    public void verifyOrderDetailsPage () {
        login.doLogin ();
        ordersList.isCorrectPage ();
        coraApi.login ();

        Physician physician = coraApi.getPhysician (non_CLEP_clonoseq);
        Patient patient = TestHelper.newPatient ();
        String icdCode = "C90.00";
        Assay orderTest = Assay.ID_BCell2_CLIA;
        ChargeType chargeType = ChargeType.NoCharge;
        SpecimenType specimenType = SpecimenType.Blood;
        Anticoagulant anticoagulant = Anticoagulant.EDTA;
        String collectionDate = DateUtils.getPastFutureDate (-3);
        String orderNum = diagnostic.createClonoSeqOrder (physician,
                                                          patient,
                                                          new String[] { icdCode },
                                                          Assay.ID_BCell2_CLIA,
                                                          chargeType,
                                                          SpecimenType.Blood,
                                                          null,
                                                          Anticoagulant.EDTA);

        List <String> history = diagnostic.getHistory ();
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

        String shipmentNo = shipment.getShipmentNumber ();
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
        accession.clickLabelingComplete ();
        accession.clickLabelVerificationComplete ();
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
        List <String> activeHistory = clonoSeqOrderDetail.getHistory ();
        String activateDateTime = activeHistory.get (2).split ("Activated by")[0].trim ();
        Logging.info ("Order Activated Date and Time: " + activateDateTime);

        Order activeOrder = clonoSeqOrderDetail.parseOrder ();
        assertEquals (activeOrder.status, com.adaptivebiotech.test.utils.PageHelper.OrderStatus.Active);
        assertEquals (activeOrder.order_number, orderNum);
        String expectedName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + orderNum;
        assertEquals (activeOrder.name, expectedName);
        String expectedDueDate = DateUtils.getPastFutureDate (7,
                                                              DateTimeFormatter.ofPattern ("MM/dd/uu"),
                                                              DateUtils.utcZoneId);
        assertEquals (clonoSeqOrderDetail.getDueDate (), expectedDueDate);
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

        assertEquals (clonoSeqOrderDetail.getShipmentArrivalDate (),
                      DateUtils.convertDateFormat (shipmentArrivalDate + " " + shipmentArrivalTime,
                                                   "MM/dd/yyyy h:mm a",
                                                   "MM/dd/yyyy hh:mm a"));
        assertEquals (clonoSeqOrderDetail.getIntakeCompleteDate (), intakeCompleteDate.split (",")[0]);
        assertEquals (clonoSeqOrderDetail.getSpecimenApprovalDate (), specimenApprovalDate.split (",")[0]);
        assertEquals (activeOrder.specimenDto.specimenNumber, specimenId);
        assertEquals (clonoSeqOrderDetail.getSpecimenContainerType (), containerType);

        assertEquals (activeOrder.tests.get (0).assay, orderTest);
        assertEquals (activeOrder.properties.BillingType, chargeType);

        assertEquals (activeHistory.get (0), createdDateTime + " Created by " + CoraEnvironment.coraTestUser);
        assertEquals (activeHistory.get (2), activateDateTime + "Activated by " + CoraEnvironment.coraTestUser);
        Logging.testLog ("STEP 1 - validate Order Details Page.");

        String editOrderNotes = "testing order notes";
        clonoSeqOrderDetail.editOrderNotes (editOrderNotes);
        assertEquals (clonoSeqOrderDetail.getOrderNotes (), editOrderNotes);
        Logging.testLog ("STEP 2 - Order Notes displays testing order notes.");

        clonoSeqOrderDetail.clickShipmentArrivalDate ();
        shipmentDetail.isCorrectPage ();
        Logging.testLog ("STEP 3 - Shipment details page is opened Shipment1");

        shipmentDetail.clickOrderNo ();
        orderStatus.isCorrectPage ();
        orderStatus.clickOrderDetailsTab ();
        clonoSeqOrderDetail.isCorrectPage ();
        clonoSeqOrderDetail.clickPatientCode ();
        patientDetail.isCorrectPage ();
        Logging.testLog ("STEP 4 - Patient details page is opened in a new tab for Patient1");

        clonoSeqOrderDetail.navigateToTab (0);
        clonoSeqOrderDetail.isCorrectPage ();
        clonoSeqOrderDetail.clickPatientOrderHistory ();
        clonoSeqOrderDetail.navigateToOrderDetailsPage (activeOrder.id);
        clonoSeqOrderDetail.isCorrectPage ();
        Logging.testLog ("STEP 5 - Patient order history page is opened");

        String expectedLimsUrl = CoraEnvironment.coraTestUrl.replace ("cora",
                                                                      "lims") + "/clarity/search?scope=Sample&query=" + activeOrder.specimenDto.specimenNumber;
        assertEquals (clonoSeqOrderDetail.getSpecimenIdUrlAttribute ("href"), expectedLimsUrl);
        assertEquals (clonoSeqOrderDetail.getSpecimenIdUrlAttribute ("target"), "_blank");
        Logging.testLog ("STEP 6 - Clarity LIMS link");

        ChargeType editChargeType = ChargeType.InternalPharmaBilling;
        clonoSeqOrderDetail.billing.editBilling (editChargeType);

        String coraAttachment = "test1.png";
        clonoSeqOrderDetail.uploadAttachments (coraAttachment);

        Order editOrder = clonoSeqOrderDetail.parseOrder ();
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

        orderStatus.clickOrderDetailsTab ();
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
