/*******************************************************************************
 * Copyright (c) 2022 by Adaptive Biotechnologies, Co. All rights reserved
 *******************************************************************************/
package com.adaptivebiotech.cora.test.order.tdetect;

import static com.adaptivebiotech.cora.dto.Containers.ContainerType.SlideBox5CS;
import static com.adaptivebiotech.cora.dto.Orders.Assay.COVID19_DX_IVD;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Client;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.CommercialInsurance;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.Medicare;
import static com.adaptivebiotech.cora.dto.Orders.ChargeType.PatientSelfPay;
import static com.adaptivebiotech.cora.dto.Orders.OrderStatus.Pending;
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static com.adaptivebiotech.test.utils.TestHelper.mapper;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.dto.Orders.OrderProperties;
import com.adaptivebiotech.cora.test.order.OrderTestBase;
//git@gitlab.com/adaptivebiotech/cora/test/cora.git
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrderDetail;
import com.adaptivebiotech.cora.ui.order.OrderStatus;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * Note:
 * - ICD codes and patient billing address are not required
 */
@Test (groups = "regression")
public class NewOrderTestSuite extends OrderTestBase {

    private Login           login           = new Login ();
    private OrdersList      ordersList      = new OrdersList ();
    private NewOrderTDetect newOrderTDetect = new NewOrderTDetect ();
    private NewShipment     shipment        = new NewShipment ();
    private Accession       accession       = new Accession ();
    private OrderStatus     orderStatus     = new OrderStatus ();
    private OrderDetail     orderDetail     = new OrderDetail ();

    @BeforeMethod (alwaysRun = true)
    public void beforeMethod () {
        login.doLogin ();
        ordersList.isCorrectPage ();
    }

    /**
     * @sdlc.requirements SR-7907:R12
     */
    @Test (groups = "corgi")
    public void sections_order () {
        newOrderTDetect.selectNewTDetectDiagnosticOrder ();
        newOrderTDetect.isCorrectPage ();
        assertEquals (newOrderTDetect.getSectionHeaders (), headers);
        testLog ("found the Order Authorization section below the Billing section of the T-Detect order form");
    }

    /**
     * @sdlc.requirements SR-7907:R3
     */
    @Test (groups = "corgi")
    public void billing_questions () {
        String log = "the qualifying questionnaires input fields are hidden for '%s'";

        newOrderTDetect.selectNewTDetectDiagnosticOrder ();
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.billing.selectBilling (CommercialInsurance);
        assertFalse (newOrderTDetect.billing.isBillingQuestionsVisible ());
        testLog (format (log, CommercialInsurance.label));

        newOrderTDetect.billing.selectBilling (Medicare);
        assertFalse (newOrderTDetect.billing.isBillingQuestionsVisible ());
        testLog (format (log, Medicare.label));

        newOrderTDetect.billing.selectBilling (Client);
        assertFalse (newOrderTDetect.billing.isBillingQuestionsVisible ());
        testLog (format (log, Client.label));

        newOrderTDetect.billing.selectBilling (PatientSelfPay);
        assertFalse (newOrderTDetect.billing.isBillingQuestionsVisible ());
        testLog (format (log, PatientSelfPay.label));
    }

    /**
     * NOTE: SR-T3243
     */
    public void order_activation () {
        // create T-Detect diagnostic order
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                                          newClientPatient (),
                                                          null,
                                                          COVID19_DX_IVD,
                                                          bloodSpecimen ());

        // add diagnostic shipment
        shipment.selectNewDiagnosticShipment ();
        shipment.isDiagnostic ();
        shipment.enterShippingCondition (Ambient);
        shipment.enterOrderNumber (order.orderNumber);
        shipment.selectDiagnosticSpecimenContainerType (SlideBox5CS);
        shipment.clickAddSlide ();
        shipment.clickAddSlide ();
        shipment.clickSave ();
        testLog ("STEP 1.1 - Shipment saves successfully");

        Containers containers = shipment.getPrimaryContainers (SlideBox5CS);
        assertEquals (containers.list.size (), 1);

        Container container = containers.list.get (0);
        assertTrue (container.containerNumber.matches (containerNumberPattern), container.containerNumber);
        assertEquals (container.children.size (), 3);
        assertTrue (container.children.get (0).containerNumber.matches (containerNumberPattern));
        assertTrue (container.children.get (1).containerNumber.matches (containerNumberPattern));
        assertTrue (container.children.get (2).containerNumber.matches (containerNumberPattern));
        testLog ("STEP 1.2 - Shipment bx and slides have CO-#");

        shipment.clickAccessionTab ();
        accession.completeAccession ();

        // activate order
        newOrderTDetect.isCorrectPage ();
        newOrderTDetect.activateOrder ();
        testLog ("STEP 2 - Order is Active.");
    }

    /**
     * @sdlc.requirements SR-4383:R1
     */
    public void verifyUndoCancellationMultipleTimes () {
        int repetition = 3;
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                                          newClientPatient (),
                                                          null,
                                                          null,
                                                          bloodSpecimen ());
        testLog (format ("T-Detect Order number without Order Tests is: %s", order.orderNumber));
        for (int i = 1; i <= repetition; i++) {
            cancelAndRestartOrder (order.orderNumber);
        }
        testLog ("T-Detect order cancelled and restarted to pending status " + repetition + " times");
    }

    /**
     * @sdlc.requirements SR-4383:R1
     */
    public void verifyUndoCancellationAbsentForOrdersWithOrderTests () {
        Order order = newOrderTDetect.createTDetectOrder (coraApi.getPhysician (TDetect_client),
                                                          newClientPatient (),
                                                          null,
                                                          COVID19_DX_IVD,
                                                          bloodSpecimen ());
        testLog (format ("T-Detect Order Number with Order Tests is: %s", order.orderNumber));
        newOrderTDetect.clickAndCancelOrder ();
        orderStatus.isCorrectPage ();
        assertFalse (orderStatus.isOrderActionDotsPresent ());
        testLog ("Restart order button not visible for T-Detect order with order tests");
    }

    private void cancelAndRestartOrder (String orderNumber) {
        newOrderTDetect.clickAndCancelOrder ();
        // orderDetail.isCorrectPage ();
        newOrderTDetect.clickOrderStatusTab ();
        orderStatus.clickRestartOrder ();
        newOrderTDetect.isCorrectPage ();
        assertEquals (orderDetail.getOrderStatus (), Pending);
        List <Map <String, Object>> properties = coraDb.executeSelect (format ("select properties from cora.orders where order_number= '%s';",
                                                                               orderNumber));
        OrderProperties cancellationProperties = mapper.readValue (properties.get (0).get ("properties").toString (),
                                                                   OrderProperties.class);
        asList (cancellationProperties).forEach (r -> {
            assertEquals (r.CancellationNotes, "");
            assertEquals (r.CancellationReason, "");
            assertEquals (r.CancellationReason2, "");
            assertEquals (r.CancellationReason3, "");
            assertNull (r.CancellationDateTime);
        });
    }
}
