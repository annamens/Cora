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
import static com.adaptivebiotech.cora.dto.Physician.PhysicianType.TDetect_client;
import static com.adaptivebiotech.cora.dto.Shipment.ShippingCondition.Ambient;
import static com.adaptivebiotech.cora.utils.TestHelper.bloodSpecimen;
import static com.adaptivebiotech.cora.utils.TestHelper.newClientPatient;
import static com.adaptivebiotech.test.utils.Logging.testLog;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.adaptivebiotech.cora.dto.Containers;
import com.adaptivebiotech.cora.dto.Containers.Container;
import com.adaptivebiotech.cora.dto.Orders.Order;
import com.adaptivebiotech.cora.test.order.NewOrderTestBase;
import com.adaptivebiotech.cora.ui.Login;
import com.adaptivebiotech.cora.ui.order.NewOrderTDetect;
import com.adaptivebiotech.cora.ui.order.OrdersList;
import com.adaptivebiotech.cora.ui.shipment.Accession;
import com.adaptivebiotech.cora.ui.shipment.NewShipment;

/**
 * Note:
 * - ICD codes and patient billing address are not required
 */
@Test (groups = "regression")
public class NewOrderTestSuite extends NewOrderTestBase {

    private Login           login           = new Login ();
    private OrdersList      ordersList      = new OrdersList ();
    private NewOrderTDetect newOrderTDetect = new NewOrderTDetect ();
    private NewShipment     shipment        = new NewShipment ();
    private Accession       accession       = new Accession ();

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
                                                          bloodSpecimen ().collectionDate.toString (),
                                                          COVID19_DX_IVD);

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

        String containerNumberPattern = "CO-\\d{7}";
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

}
