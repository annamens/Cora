package com.adaptivebiotech.cora.test.shipment;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Vacutainer;
import static com.adaptivebiotech.cora.dto.Orders.Assay.MRD_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Specimen.Anticoagulant.Streck;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.test.utils.DateHelper.genLocalDate;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.PageHelper.Compartment.CellFree;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Shipment;
import com.adaptivebiotech.cora.dto.Specimen;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * @author cbragg
 *         <a href="mailto:<cbragg@adaptivebiotech.com">cbragg@adaptivebiotech.com</a>
 */
@Test (groups = { "regression", "irish-wolfhound" })
public class ShipmentPriorityFlag extends CoraBaseBrowser {

    private final String[]   icdCodes         = { "Z63.1" };
    private Login            login            = new Login ();
    private OrdersList       ordersList       = new OrdersList ();
    private NewOrderClonoSeq newOrderClonoSeq = new NewOrderClonoSeq ();
    private NewShipment      shipment         = new NewShipment ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod (Method test) {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T4318
     * 
     * @sdlc.requirements SR-11342
     */
    public void noPriorityFlagBatchAndGeneralShipment () {
        shipment.selectNewGeneralShipment ();
        assertFalse (shipment.isHighPriorityFlagVisible ());
        testLog ("High Priority Flag not visible on General Shipment");
        shipment.selectNewBatchShipment ();
        assertFalse (shipment.isHighPriorityFlagVisible ());
        testLog ("High Priority Flag not visible on Batch Shipment");
    }

    /**
     * NOTE: SR-T4318
     * 
     * @sdlc.requirements SR-11342
     */
    public void priorityFlagNonStreckOrder () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.collectionDate = genLocalDate (-1);
        Assay assayTest = MRD_BCell2_CLIA;

        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);
        String ship1 = shipment.createShipment (order.orderNumber, Vacutainer);
        testLog ("Non Streck order created with shipment number: " + ship1);

        // Verify Flag visible but unselected by default in UI
        shipment.clickShipmentTab ();
        shipment.clickSave ();
        assertTrue (shipment.isHighPriorityFlagVisible ());
        assertFalse (shipment.isHighPriorityFlagSelected ());
        testLog ("High Priority Flag is visible and not selected by default");

        // Confirm False in DB
        verifyCoraShipmentProperties (ship1, false);

        // Select Flag and verify true in UI
        shipment.clickHighPriorityFlag ();
        shipment.clickSave ();
        assertTrue (shipment.isHighPriorityFlagSelected ());
        testLog ("High Priority Flag was clicked and is now selected");

        // Confirm True in DB
        verifyCoraShipmentProperties (ship1, true);
    }

    /**
     * NOTE: SR-T4318
     * 
     * @sdlc.requirements SR-11342
     */
    public void priorityFlagStreckOrder () {
        Specimen specimenDto = bloodSpecimen ();
        specimenDto.compartment = CellFree;
        specimenDto.anticoagulant = Streck;
        specimenDto.collectionDate = genLocalDate (-1);
        Assay assayTest = MRD_BCell2_CLIA;

        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            icdCodes,
                                                            assayTest,
                                                            specimenDto);

        String ship2 = shipment.createShipment (order.orderNumber, Vacutainer);
        testLog ("Streck order created with shipment number: " + ship2);

        // Verify Flag visible but selected by default in UI
        shipment.clickShipmentTab ();
        shipment.clickSave ();
        assertTrue (shipment.isHighPriorityFlagVisible ());
        assertTrue (shipment.isHighPriorityFlagSelected ());
        testLog ("High Priority Flag is visible and selected by default");

        // Confirm True in DB
        verifyCoraShipmentProperties (ship2, true);

        // Deselect Flag and verify false in UI
        shipment.clickHighPriorityFlag ();
        shipment.clickSave ();
        assertFalse (shipment.isHighPriorityFlagSelected ());
        testLog ("High Priority Flag was clicked and is now deselected");

        // Confirm False in DB
        verifyCoraShipmentProperties (ship2, false);
    }

    private void verifyCoraShipmentProperties (String shipmentNumber, boolean expectedValue) {
        Shipment shipData = coraDb.getShipmentProperties (shipmentNumber);

        if (expectedValue) {
            assertTrue (shipData.properties.HighPriority);
        } else {
            assertFalse (shipData.properties.HighPriority);
        }
    }
}
