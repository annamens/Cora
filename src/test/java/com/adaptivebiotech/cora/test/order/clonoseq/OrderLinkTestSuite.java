package com.adaptivebiotech.cora.test.order.clonoseq;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.Tube;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Active;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Pending;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.non_CLEP_clonoseq;
import static org.testng.Assert.assertEquals;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.ContainerType;
import com.adaptivebiotech.cora.dto.Orders.Assay;
import com.adaptivebiotech.cora.dto.Physician;
import com.adaptivebiotech.cora.test.CoraBaseBrowser;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.container.Detail;
import com.adaptivebiotech.cora.ui.order.NewOrderClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderDetailClonoSeq;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrderTestsList;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;
import com.adaptivebiotech.cora.ui.shipment.ShipmentDetail;
import com.adaptivebiotech.cora.ui.shipment.ShipmentList;
import com.adaptivebiotech.cora.utils.TestHelper;
import com.adaptivebiotech.test.utils.Logging;

/**
 * @author jpatel
 *
 */
@Test (groups = "regression")
public class OrderLinkTestSuite extends CoraBaseBrowser {

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
    private ShipmentList        shipmentList        = new ShipmentList ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * NOTE: SR-T3025
     */
    public void verifyPendingOrderLinkRedirect () {
        String order1 = createClonoSeqOrder (Pending);
        Logging.testLog ("Pending Order, order1: " + order1);
        ordersList.searchAndClickOrder (order1);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order1);
        Logging.testLog ("STEP 1 - clonoSEQ Order Form is displayed");

        ordersList.doOrderSearch (order1);
        ordersList.isCorrectPage ();
        Physician physician = coraApi.getPhysician (non_CLEP_clonoseq);
        String orderName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + order1;
        ordersList.clickOrderName (orderName);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order1);
        Logging.testLog ("STEP 2 - clonoSEQ Order Form is displayed");

        newOrderClonoSeq.clickPatientOrderHistory ();
        newOrderClonoSeq.clickOrder (order1);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order1);
        Logging.testLog ("STEP 3 - clonoSEQ Order Form is displayed");

        newOrderClonoSeq.doOrderTestSearch (order1);
        orderTestsList.isCorrectPage ();
        orderTestsList.clickOrderName (orderName);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order1);
        Logging.testLog ("STEP 4 - clonoSEQ Order Form is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        List <com.adaptivebiotech.cora.dto.Shipment> shipments = shipmentList.getAllShipments ();
        com.adaptivebiotech.cora.dto.Shipment orderShipment = shipments.stream ().filter (sh -> sh.link.equals (order1))
                                                                       .findFirst ().get ();
        shipmentList.clickShipment (orderShipment.shipmentNumber);
        shipment.isDiagnostic ();
        assertEquals (shipment.getOrderNumber (), order1);
        Logging.testLog ("STEP 5 - clonoSEQ Order Form is displayed");

        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickOrderNumber ();
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order1);
        Logging.testLog ("STEP 6 - clonoSEQ Order Form is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipments = shipmentList.getAllShipments ();
        orderShipment = shipments.stream ().filter (sh -> sh.link.equals (order1))
                                 .findFirst ().get ();
        shipmentList.clickShipment (orderShipment.shipmentNumber);
        shipment.isDiagnostic ();
        Containers containers = shipment.getPrimaryContainers (Tube);
        shipment.clickContainerNo (containers.list.get (0).containerNumber);
        containerDetail.isCorrectPage ();
        containerDetail.clickAccessionedOrderNo (order1);
        newOrderClonoSeq.navigateToTab (1);
        newOrderClonoSeq.isCorrectPage ();
        assertEquals (newOrderClonoSeq.getOrderNumber (), order1);
        Logging.testLog ("STEP 7 - clonoSEQ Order Form is displayed");

    }

    /**
     * NOTE: SR-T3025
     */
    public void verifyActiveOrderLinkRedirect () {
        String order2 = createClonoSeqOrder (Active);
        Logging.testLog ("Active Order, order2: " + order2);

        ordersList.searchAndClickOrder (order2);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order2);
        Logging.testLog ("STEP 8 - The generic order status page is displayed");

        ordersList.doOrderSearch (order2);
        ordersList.isCorrectPage ();
        Physician physician = coraApi.getPhysician (non_CLEP_clonoseq);
        String orderName = "Clinical-" + physician.firstName.charAt (0) + physician.lastName + "-" + order2;
        ordersList.clickOrderName (orderName);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order2);
        Logging.testLog ("STEP 9 - The generic order status page is displayed");

        orderStatus.clickOrderDetailsTab ();
        orderDetailClonoSeq.isCorrectPage ();
        orderDetailClonoSeq.clickPatientOrderHistory ();
        orderDetailClonoSeq.clickOrder (order2);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order2);
        Logging.testLog ("STEP 10 - The generic order status page is displayed");

        orderDetailClonoSeq.doOrderTestSearch (order2);
        orderTestsList.isCorrectPage ();
        orderTestsList.clickOrderName (orderName);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order2);
        Logging.testLog ("STEP 11 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        List <com.adaptivebiotech.cora.dto.Shipment> shipments = shipmentList.getAllShipments ();
        com.adaptivebiotech.cora.dto.Shipment orderShipment = shipments.stream ().filter (sh -> sh.link.equals (order2))
                                                                       .findFirst ().get ();
        shipmentList.clickShipment (orderShipment.shipmentNumber);
        shipmentDetail.isCorrectPage ();
        shipmentDetail.clickOrderNo ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order2);
        Logging.testLog ("STEP 12 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipments = shipmentList.getAllShipments ();
        orderShipment = shipments.stream ().filter (sh -> sh.link.equals (order2))
                                 .findFirst ().get ();
        shipmentList.clickShipment (orderShipment.shipmentNumber);
        shipmentDetail.isCorrectPage ();
        shipment.clickAccessionTab ();
        accession.isCorrectPage ();
        accession.clickOrderNumber ();
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order2);
        Logging.testLog ("STEP 13 - The generic order status page is displayed");

        shipmentList.goToShipments ();
        shipmentList.isCorrectPage ();
        shipments = shipmentList.getAllShipments ();
        orderShipment = shipments.stream ().filter (sh -> sh.link.equals (order2))
                                 .findFirst ().get ();
        shipmentList.clickShipment (orderShipment.shipmentNumber);
        shipmentDetail.isCorrectPage ();
        Containers containers = shipmentDetail.getPrimaryContainers (Tube);
        shipmentDetail.clickContainerNo (containers.list.get (0).containerNumber);
        containerDetail.isCorrectPage ();
        containerDetail.clickAccessionedOrderNo (order2);
        orderDetailClonoSeq.navigateToTab (1);
        orderStatus.isCorrectPage ();
        assertEquals (orderStatus.getheaderOrderNumber (), order2);
        Logging.testLog ("STEP 14 - The generic order status page is displayed");

    }

    private String createClonoSeqOrder (com.adaptivebiotech.cora.dto.Orders.OrderStatus orderStatus) {
        // create clonoSEQ diagnostic order
        return newOrderClonoSeq.createClonoSeqOrder (coraApi.getPhysician (non_CLEP_clonoseq),
                                                     TestHelper.newNoChargePatient (),
                                                     new String[] { "C90.00" },
                                                     Assay.ID_BCell2_CLIA,
                                                     TestHelper.bloodSpecimen (),
                                                     orderStatus,
                                                     ContainerType.Tube);
    }

}
