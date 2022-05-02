/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.Assay.ID_BCell2_CLIA;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Pending;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newNoChargePatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.patient.PatientOrderHistory;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.ui.shipment.ShipmentsList;

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class OrderLinkTestSuite extends CoraBaseBrowser {

    private final String[]      icdcodes            = new String[] { "C90.00" };
    private Login               login               = new Login ();
    private OrdersList          ordersList          = new OrdersList ();
    private OrderStatus         orderStatus         = new OrderStatus ();
    private NewOrderClonoSeq    newOrderClonoSeq    = new NewOrderClonoSeq ();
    private OrderDetailClonoSeq orderDetailClonoSeq = new OrderDetailClonoSeq ();
    private NewShipment         shipment            = new NewShipment ();
    private ShipmentDetail      shipmentDetail      = new ShipmentDetail ();
    private Detail              containerDetail     = new Detail ();
    private Accession           accession           = new Accession ();
    private OrderTestsList      orderTestsList      = new OrderTestsList ();
    private ShipmentsList       shipmentList        = new ShipmentsList ();
    private PatientOrderHistory patientOrderHistory = new PatientOrderHistory ();
    private Physician           physician;

    @BeforeClass
    public void beforeClass () {
        coraApi.addTokenAndUsername ();
        physician = coraApi.getPhysician (non_CLEP_clonoseq);
    }

    @BeforeMethod
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T3025
     */
    public void verifyPendingOrderLinkRedirect () {
        String order = newOrderClonoSeq.createClonoSeqOrder (physician,
                                                             newNoChargePatient (),
                                                             icdcodes,
                                                             ID_BCell2_CLIA,
                                                             bloodSpecimen (),
                                                             Pending,
                                                             Tube);
        testLog ("Pending Order, order1: " + order);
        ordersList.searchAndClickOrder (order);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order);
        testLog ("STEP 1 - clonoSEQ Order Form is displayed");

        ordersList.doOrderSearch (order);
        ordersList.isCorrectPage ();
        String orderName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + order;
        ordersList.clickOrderName (orderName);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order);
        testLog ("STEP 2 - clonoSEQ Order Form is displayed");

        newOrderClonoSeq.clickPatientOrderHistory ();
        patientOrderHistory.navigateToTab (1);
        patientOrderHistory.isCorrectPage ();
        newOrderClonoSeq.clickOrder (order);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order);
        testLog ("STEP 3 - clonoSEQ Order Form is displayed");

        newOrderClonoSeq.doOrderTestSearch (order);
        orderTestsList.isCorrectPage ();
        orderTestsList.clickOrderName (orderName);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order);
        testLog ("STEP 4 - clonoSEQ Order Form is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order).shipmentNumber);
        shipment.isDiagnostic ();
        assertEquals (shipment.getOrderNumber (), order);
        testLog ("STEP 5 - clonoSEQ Order Form is displayed");

        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickOrderNumber ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order);
        testLog ("STEP 6 - clonoSEQ Order Form is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order).shipmentNumber);
        shipment.isDiagnostic ();
        Containers containers = shipment.getPrimaryContainers (Tube);
        shipment.clickContainerNo (containers.list.get (0).containerNumber);
        containerDetail.isCorrectPage ();
        containerDetail.clickAccessionedOrderNo (order);
        newOrderClonoSeq.navigateToTab (2);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order);
        testLog ("STEP 7 - clonoSEQ Order Form is displayed");
    }

    /**
     * NOTE: SR-T3025
     */
    public void verifyActiveOrderLinkRedirect () {
        String order = newOrderClonoSeq.createClonoSeqOrder (physician,
                                                             newNoChargePatient (),
                                                             icdcodes,
                                                             ID_BCell2_CLIA,
                                                             bloodSpecimen (),
                                                             Active,
                                                             Tube);
        testLog ("Active Order, order2: " + order);

        ordersList.searchAndClickOrder (order);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order);
        testLog ("STEP 8 - The generic order status page is displayed");

        ordersList.doOrderSearch (order);
        ordersList.isCorrectPage ();
        String orderName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + order;
        ordersList.clickOrderName (orderName);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order);
        testLog ("STEP 9 - The generic order status page is displayed");

        orderStatus.clickOrderDetailsTab ();
        orderDetailClonoSeq.isCorrectPage ();
        orderDetailClonoSeq.clickPatientOrderHistory ();
        orderDetailClonoSeq.clickOrder (order);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order);
        testLog ("STEP 10 - The generic order status page is displayed");

        orderDetailClonoSeq.doOrderTestSearch (order);
        orderTestsList.isCorrectPage ();
        orderTestsList.clickOrderName (orderName);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order);
        testLog ("STEP 11 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order).shipmentNumber);
        shipmentDetail.isCorrectPage ();
        shipmentDetail.clickOrderNumber ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order);
        testLog ("STEP 12 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order).shipmentNumber);
        shipmentDetail.isCorrectPage ();
        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickOrderNumber ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order);
        testLog ("STEP 13 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipmentList.clickShipment (shipmentList.getShipmentForOrder (order).shipmentNumber);
        shipmentDetail.isCorrectPage ();
        Containers containers = shipmentDetail.getPrimaryContainers (Tube);
        shipmentDetail.clickContainerNo (containers.list.get (0).containerNumber);
        containerDetail.isCorrectPage ();
        containerDetail.clickAccessionedOrderNo (order);
        orderDetailClonoSeq.navigateToTab (1);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order);
        testLog ("STEP 14 - The generic order status page is displayed");
    }
}
