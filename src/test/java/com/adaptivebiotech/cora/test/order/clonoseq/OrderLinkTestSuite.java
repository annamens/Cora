/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Pending;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.clonoSEQ_client;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.cora.utils.PageHelper.Discrepancy.SpecimenType;
import static com.adaptivebiotech.cora.utils.PageHelper.DiscrepancyAssignee.CLINICAL_TRIALS;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.cora.utils.TestHelper.newNoChargePatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientOrderHistory;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.DiscrepancyResolutions;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.ui.shipment.ShipmentsList;

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class OrderLinkTestSuite extends NewOrderTestBase {

    private final String[]         icdcodes               = new String[] { "C90.00" };
    private Login                  login                  = new Login ();
    private OrdersList             ordersList             = new OrdersList ();
    private OrderStatus            orderStatus            = new OrderStatus ();
    private NewOrderClonoSeq       newOrderClonoSeq       = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq    orderDetailClonoSeq    = new OrderDetailClonoSeq ();
    private NewShipment            shipment               = new NewShipment ();
    private ShipmentDetail         shipmentDetail         = new ShipmentDetail ();
    private Detail                 containerDetail        = new Detail ();
    private Accession              accession              = new Accession ();
    private OrderTestsList         orderTestsList         = new OrderTestsList ();
    private ShipmentsList          shipmentList           = new ShipmentsList ();
    private PatientOrderHistory    patientOrderHistory    = new PatientOrderHistory ();
    private DiscrepancyResolutions discrepancyResolutions = new DiscrepancyResolutions ();
    private Physician              physician;

    @BeforeClass (alwaysRun = true)
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        physician = coraApi.getPhysician (non_CLEP_clonoseq);
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T3025
     */
    public void verifyPendingOrderLinkRedirect () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (physician,
                                                            newNoChargePatient (),
                                                            icdcodes,
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen (),
                                                            Pending,
                                                            Tube);
        testLog ("Pending Order, order1: " + order.orderNumber);
        ordersList.searchAndClickOrder (order.orderNumber);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order.orderNumber);
        testLog ("STEP 1 - clonoSEQ Order Form is displayed");

        ordersList.doOrderSearch (order.orderNumber);
        ordersList.isCorrectPage ();
        String orderName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + order.orderNumber;
        ordersList.clickOrderName (orderName);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order.orderNumber);
        testLog ("STEP 2 - clonoSEQ Order Form is displayed");

        newOrderClonoSeq.clickPatientOrderHistory ();
        patientOrderHistory.navigateToTab (1);
        patientOrderHistory.isCorrectPage ();
        newOrderClonoSeq.clickOrder (order.orderNumber);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order.orderNumber);
        testLog ("STEP 3 - clonoSEQ Order Form is displayed");

        newOrderClonoSeq.doOrderTestSearch (order.orderNumber);
        orderTestsList.isCorrectPage ();
        orderTestsList.clickOrderName (orderName);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order.orderNumber);
        testLog ("STEP 4 - clonoSEQ Order Form is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order.orderNumber).shipmentNumber);
        shipment.isDiagnostic ();
        assertEquals (shipment.getOrderNumber (), order.orderNumber);
        testLog ("STEP 5 - clonoSEQ Order Form is displayed");

        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickOrderNumber ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order.orderNumber);
        testLog ("STEP 6 - clonoSEQ Order Form is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order.orderNumber).shipmentNumber);
        shipment.isDiagnostic ();
        Containers containers = shipment.getPrimaryContainers (Tube);
        shipment.clickContainerNo (containers.list.get (0).containerNumber);
        containerDetail.isCorrectPage ();
        containerDetail.clickAccessionedOrderNo (order.orderNumber);
        newOrderClonoSeq.navigateToTab (2);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order.orderNumber);
        testLog ("STEP 7 - clonoSEQ Order Form is displayed");
    }

    /**
     * NOTE: SR-T3025
     */
    public void verifyActiveOrderLinkRedirect () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (physician,
                                                            newNoChargePatient (),
                                                            icdcodes,
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen (),
                                                            Active,
                                                            Tube);
        testLog ("Active Order, order2: " + order.orderNumber);

        ordersList.searchAndClickOrder (order.orderNumber);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order.orderNumber);
        testLog ("STEP 8 - The generic order status page is displayed");

        ordersList.doOrderSearch (order.orderNumber);
        ordersList.isCorrectPage ();
        String orderName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + order.orderNumber;
        ordersList.clickOrderName (orderName);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order.orderNumber);
        testLog ("STEP 9 - The generic order status page is displayed");

        orderStatus.clickOrderDetailsTab ();
        orderDetailClonoSeq.isCorrectPage ();
        orderDetailClonoSeq.clickPatientOrderHistory ();
        orderDetailClonoSeq.clickOrder (order.orderNumber);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order.orderNumber);
        testLog ("STEP 10 - The generic order status page is displayed");

        orderDetailClonoSeq.doOrderTestSearch (order.orderNumber);
        orderTestsList.isCorrectPage ();
        orderTestsList.clickOrderName (orderName);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order.orderNumber);
        testLog ("STEP 11 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order.orderNumber).shipmentNumber);
        shipmentDetail.isCorrectPage ();
        shipmentDetail.clickOrderNumber ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order.orderNumber);
        testLog ("STEP 12 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order.orderNumber).shipmentNumber);
        shipmentDetail.isCorrectPage ();
        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickOrderNumber ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order.orderNumber);
        testLog ("STEP 13 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order.orderNumber).shipmentNumber);
        shipmentDetail.isCorrectPage ();
        Containers containers = shipmentDetail.getPrimaryContainers (Tube);
        shipmentDetail.clickContainerNo (containers.list.get (0).containerNumber);
        containerDetail.isCorrectPage ();
        containerDetail.clickAccessionedOrderNo (order.orderNumber);
        orderDetailClonoSeq.navigateToTab (1);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order.orderNumber);
        testLog ("STEP 14 - The generic order status page is displayed");
    }

    /**
     * NOTE: SR-T4182
     * 
     * @sdlc.requirements SR-10524:R1
     */
    @Test (groups = "fox-terrier")
    public void validateOrderTabsWithDiscrepancy () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            new String[] { "C90.00" },
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen ());
        validateTabsOrderPage (order, asList (orderDetailsTab));

        shipment.createShipment (order.orderNumber, Tube);
        testLog ("Shipment Created");

        String shipmentId = accession.getShipmentId ();
        validateTabsShipmentPage (shipmentId, accessionTabList);

        validateTabsOrderPage (order, asList (orderDetailsTab));

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickAddContainerSpecimenDiscrepancy ();
        accession.addDiscrepancy (SpecimenType, "This is a specimen/container discrepancy", CLINICAL_TRIALS);
        accession.clickDiscrepancySave ();
        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        testLog ("Discrepancy created");

        validateTabsOrderPage (order, asList (orderDetailsTab));

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickIntakeComplete ();
        testLog ("Accession - Intake complete");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickLabelingComplete ();
        testLog ("Labelling complete");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        assertFalse (accession.isApproveSpecimenEnabled ());
        testLog ("Specimen approval is disabled");

        accession.gotoDiscrepancyResolutions ();
        discrepancyResolutions.resolveAllDiscrepancies ();
        discrepancyResolutions.clickSave ();
        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        testLog ("Resolve discrepancy");

        validateTabsOrderPage (order, orderDiscrepTabList);

        validateTabsShipmentPage (shipmentId, discrepancyTabList);
        accession.clickPass ();
        testLog ("Specimen approval pass");

        validateTabsOrderPage (order, orderDiscrepTabList);

        newOrderClonoSeq.activateOrder ();
        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        assertEquals (orderDetailClonoSeq.getTabList (), asList (orderStatusTab, orderDetailsTab));
        testLog ("activate Order");

    }

    /**
     * NOTE: SR-T4182
     * 
     * @sdlc.requirements SR-10524:R1
     */
    @Test (groups = "fox-terrier")
    public void validateOrderTabsWithoutDiscrepancy () {
        Order order = newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (clonoSEQ_client),
                                                            newClientPatient (),
                                                            new String[] { "C90.00" },
                                                            ID_BCell2_CLIA,
                                                            bloodSpecimen ());
        validateTabsOrderPage (order, asList (orderDetailsTab));

        shipment.createShipment (order.orderNumber, Tube);
        testLog ("Shipment Created");

        String shipmentId = accession.getShipmentId ();
        validateTabsShipmentPage (shipmentId, accessionTabList);

        validateTabsOrderPage (order, asList (orderDetailsTab));

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickIntakeComplete ();
        testLog ("Accession - Intake complete");

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickLabelingComplete ();
        testLog ("Labelling complete");

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickLabelVerificationComplete ();
        testLog ("Label verification complete");

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        assertTrue (accession.isApproveSpecimenEnabled ());
        testLog ("Specimen approval is enabled");

        validateTabsShipmentPage (shipmentId, accessionTabList);

        validateTabsOrderPage (order, orderDetailsTabList);

        validateTabsShipmentPage (shipmentId, accessionTabList);
        accession.clickPass ();
        testLog ("Specimen approval pass");

        validateTabsOrderPage (order, orderDetailsTabList);

        newOrderClonoSeq.activateOrder ();
        orderDetailClonoSeq.gotoOrderDetailsPage (order.id);
        assertEquals (orderDetailClonoSeq.getTabList (), asList (orderStatusTab, orderDetailsTab));
        testLog ("activate Order");

    }

    private void validateTabsShipmentPage (String shipmentId, List <String> expTabs) {
        accession.gotoAccession (shipmentId);
        assertEquals (accession.getTabList (), expTabs);
        testLog ("Validate Tabs on Shipment Page");
    }

    private void validateTabsOrderPage (Order order, List <String> expTabs) {
        newOrderClonoSeq.gotoOrderEntry (order.id);
        assertEquals (newOrderClonoSeq.getTabList (), expTabs);
        testLog ("Validate Tabs on Orders Page");
    }
}
