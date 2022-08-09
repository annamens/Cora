package com.adaptivebiotech.cora.test.specimen.clonoseq;

import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_selfpay;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Refrigerated;
import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.utils.PageHelper.Carrier.UPS;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newSelfPayPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertTrue;
import java.util.UUID;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Specimen;

import com.adaptivebiotech.cora.test.specimen.SpecimenTestBase;
import com.adaptivebiotech.cora.dto.Orders.DeliveryType;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author cbragg
 * 
 */
@Test (groups = { "clonoSeq", "regression", "whip" })
public class CoordinationFlagTestSuite extends SpecimenTestBase {

    private Login               login            = new Login ();
    private OrdersList          ordersList       = new OrdersList ();
    private NewOrderClonoSeq    newOrderClonoSeq = new NewOrderClonoSeq ();
    private NewShipment         shipment         = new NewShipment ();
    private Accession           accession        = new Accession ();
    private OrderDetailClonoSeq orderDetail      = new OrderDetailClonoSeq ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T3990
     * 
     * @sdlc.requirements SR-8020:R1,SR-8020:R2,SR-8020:R3
     */
    public void specimenCoordinationFlag () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genLocalDate (-1);

        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_selfpay),
                                                            newSelfPayPatient (),
                                                            icdCodes,
                                                            ID_BCell2_CLIA,
                                                            specimenDto);
        testLog ("Order created: " + order.orderNumber);

        // Default state, Test flag visible and unchecked
        assertTrue (newOrderClonoSeq.isPathologyRetrievalVisible ());
        assertFalse (newOrderClonoSeq.isPathologyRetrievalSelected ());
        testLog ("Default state: flag visible but unchecked");

        // Select the checkbox, Test flag visible and checked
        newOrderClonoSeq.clickPathologyRetrieval ();
        newOrderClonoSeq.clickSave ();
        testLog ("Clicked specimen coordination flag...");
        testFlagVisibleAndChecked ();

        // Update delivery type, Test flag visible and checked
        newOrderClonoSeq.enterSpecimenDelivery (DeliveryType.PathRequest);
        newOrderClonoSeq.clickSave ();
        testLog ("Updated delivery type...");
        testFlagVisibleAndChecked ();

        // Revert delivery type, New Shipment to Intake Complete, Test flag invisible
        newOrderClonoSeq.enterSpecimenDelivery (DeliveryType.CustomerShipment);
        newOrderClonoSeq.clickSave ();
        shipment.createShipment (Refrigerated, UPS, trackingNumber, order.orderNumber, Vacutainer, "");
        UUID shipmentId = shipment.getShipmentId ();
        accession.clickAccessionTab ();
        accession.clickIntakeComplete ();
        accession.clickOrderNumber ();
        newOrderClonoSeq.isCorrectPage ();
        testLog ("Linked shipment...");
        testFlagInvisible ();

        // Revert Intake Complete, Test flag visible and checked
        newOrderClonoSeq.clickShipmentArrivalDate ();
        accession.clickAccessionTab ();
        accession.clickRevert ();
        accession.clickOrderNumber ();
        newOrderClonoSeq.isCorrectPage ();
        testLog ("Reverted shipment link...");
        testFlagVisibleAndChecked ();

        // Bring shipment to Accession Complete, Activate order, Test Flag invisible
        newOrderClonoSeq.gotoShipmentEntry (shipmentId);
        accession.clickAccessionTab ();
        accession.completeAccession ();
        newOrderClonoSeq.isCorrectPage ();
        newOrderClonoSeq.activateOrder ();
        orderDetail.isCorrectPage ();
        testLog ("Activated order...");
        testFlagInvisible ();

    }

    private void testFlagVisibleAndChecked () {
        assertTrue (newOrderClonoSeq.isPathologyRetrievalVisible ());
        assertTrue (newOrderClonoSeq.isPathologyRetrievalSelected ());
        testLog ("Flag visible and checked");
    }

    private void testFlagInvisible () {
        assertFalse (newOrderClonoSeq.isPathologyRetrievalVisible ());
        testLog ("Flag not visible");
    }

}
